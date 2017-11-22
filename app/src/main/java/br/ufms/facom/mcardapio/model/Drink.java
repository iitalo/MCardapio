package br.ufms.facom.mcardapio.model;

/**
 * Created by italo on 10/11/17.
 */

public class Drink {

    private String title;
    private String description;
    private String price;
    private String urlImg;

    public Drink(String title, String description, String price, String urlImg) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.urlImg = urlImg;
    }

    public Drink(){

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUrlImg() {
        return urlImg;
    }

    public void setUrlImg(String urlImg) {
        this.urlImg = urlImg;
    }
}
