package com.example.pantago;

public class Pant {
    private String description, image;
    private int quantity;
    private double latitude,longitude;

    public Pant(String description, int quantity, double latitude, double longitude){
        this.description = description;
        //this.image = image;
        this.quantity = quantity;
        this.longitude = longitude;
        this.latitude = latitude;

    }
    public Pant(){

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

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }


}
