package br.ufms.facom.mcardapio.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.ufms.facom.mcardapio.R;
import br.ufms.facom.mcardapio.model.Drink;

/**
 * Created by italo on 10/11/17.
 */

public class AdapterList extends RecyclerView.Adapter<AdapterList.MyViewHolder> {

    private List<Drink> lista;
    public AdapterList(List<Drink> lista) {
        this.lista=lista;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titulo;
        TextView descicao;
        TextView preco;
        ImageView imagem;

        public MyViewHolder(View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.txt_adapter_title);
            descicao = itemView.findViewById(R.id.txt_adapter_description);
            preco = itemView.findViewById(R.id.txt_adapter_pricce);
            imagem = itemView.findViewById(R.id.img);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_list, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Drink drink = lista.get(position);
        holder.titulo.setText(drink.getTitle());
        holder.descicao.setText(drink.getDescription());
        holder.preco.setText(drink.getPrice());
        holder.imagem.setImageResource(R.drawable.ic_menu_camera);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }




}
