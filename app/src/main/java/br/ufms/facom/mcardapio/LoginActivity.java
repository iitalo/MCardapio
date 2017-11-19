package br.ufms.facom.mcardapio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{/*implements LoaderCallbacks<Cursor> {*/

    private static final String TAG_E = "LoginEmail";
    private static final String TAG_G = "LoginGoogle";
    private static final String TAG_F = "LoginFacebook";


    private static final int RC_SIGN_IN = 9001;
    private static final int RC_SIGN_IN_FB = 64206;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // UI references.
    private TextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CallbackManager callbackManager;
    private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupFormLogin();
        configLoginGoogle();
        configLoginFacebook();
    }

    public void setupFormLogin(){
        // Set up the login form.
        mEmailView = (TextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);
        SignInButton signInButton = findViewById(R.id.google_sign_in_button);
        TextView textView = (TextView) signInButton.getChildAt(0);
        textView.setText("Entrar com Google");

        loginButton = (LoginButton) findViewById(R.id.login_button);


    }

    public void configLoginGoogle(){
        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void configLoginFacebook(){

        loginButton.setReadPermissions(Arrays.asList("email","public_profile"));
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Toast.makeText(LoginActivity.this, "Successfuly logged in ", Toast.LENGTH_SHORT).show();
                Log.e(TAG_F, loginResult.toString());
                handlerFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(LoginActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(LoginActivity.this, "Login error ", Toast.LENGTH_SHORT).show();
                exception.printStackTrace();
            }
        });
    }


    @Override
    public void onStart(){
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();
        if (user!=null){
            logado();
        }
    }

    public void logado(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("nome", user.getDisplayName());
        i.putExtra("email", user.getEmail());
        i.putExtra("photoUrl", user.getPhotoUrl().toString());
        startActivity(i);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i){
            case R.id.email_sign_in_button:
                attemptLogin();
                break;
            case R.id.google_sign_in_button:
                signInGoogle();
                break;

        }
    }


    // Valida formulário para login com email e senha
    private void attemptLogin() {
        if (mAuth == null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)){
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            signInEmail(email, password);
            /*mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);*/
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }



    // Login com email e senha
    public void signInEmail(final String email, final String password){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            user = mAuth.getCurrentUser();
                            Log.d(TAG_E, "signInWithEmail:success: " + user.getEmail());
                            logado();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG_E, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            user = null;

                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
                                mPasswordView.setError("TESTE AuthWeakPassword");
                                mPasswordView.requestFocus();

                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                //Senha incorreta
                                mPasswordView.setError("Senha incorreta");
                                mPasswordView.requestFocus();

                            } catch(FirebaseAuthUserCollisionException e) {
                                mEmailView.setError("TESTE AuthUserCollision");
                                mEmailView.requestFocus();

                            } catch(Exception e) {
                                //Caso o email não exista
                                mEmailView.setError("Email não cadastrado");
                                mEmailView.requestFocus();
                                Log.e(TAG_E, e.getMessage());
                            }
                        }
                        showProgress(false);
                    }
                });
    }


    //Cria nova conta com email e senha
    public void createUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            user = mAuth.getCurrentUser();
                            Log.d("TAG>>>>>>","createUserWithEmail:success: " + user.getEmail());
                            logado();
                        } else {
                            Log.w("TAG>>>>>>", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this,"Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // Login com conta do google
    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG_G, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgress(true);
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            user = mAuth.getCurrentUser();
                            Log.d(TAG_G, "signInWithCredential:success: " + user.getEmail());
                            logado();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG_G, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            user = null;
                        }

                        // [START_EXCLUDE]
                        showProgress(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    // Login com conta do facebook
    private  void handlerFacebookAccessToken(AccessToken accessToken)   {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                showProgress(true);

                if (!task.isSuccessful()) {
                    Log.w(TAG_F, "signInWithCredential", task.getException());
                    Toast.makeText(LoginActivity.this, "Erro de Login", Toast.LENGTH_SHORT).show();
                    user = null;
                } else {
                   user = mAuth.getCurrentUser();
                   logado();

                }
            }
        });
    }


    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG_G, "Google sign in failed", e);
                user = null;
            }
        } else if(requestCode==RC_SIGN_IN_FB){
            callbackManager.onActivityResult(requestCode, resultCode, data);

        }
    }


    // Mostra o progresso (loading).
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    //LogOut
    public void signOut() {
        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user = null;
                    }
                });
        LoginManager.getInstance().logOut();

    }

    @Override
    protected void onStop() {
        super.onStop();
        signOut();
    }

}

