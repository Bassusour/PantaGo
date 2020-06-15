package com.example.pantago;

public class Pant {
    private String description, image;
    private int quantity;

    public Pant(String description, String image, int quantity){
        this.description = description;
        this.image = image;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }
}
