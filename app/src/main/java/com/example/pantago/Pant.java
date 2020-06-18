package com.example.pantago;

import com.google.android.gms.maps.model.Marker;

public class Pant {
    private String description, image;
    private int quantity;
    private double latitude,longitude;
    private String key;
    private String markerID;
    Marker marker;

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

    public void setPantKey(String key){ this.key = key; }

    public String getPantKey(){ return key; }

    public void setPantMarkerID(String markerID){ this.markerID = markerID; }

    public String getPantMarkerID(){ return markerID; }

}
