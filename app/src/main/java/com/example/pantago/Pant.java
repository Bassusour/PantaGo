package com.example.pantago;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.ParcelableSpan;

import com.google.android.gms.maps.model.Marker;

public class Pant {
    private String description, image;
    private int quantity;
    private double latitude,longitude;
    private String key;
    private String markerID;
    Marker marker;
    private String ownerUID;
    private String claimerUID;
    private boolean claimed;

    public Pant(String description, int quantity, double latitude, double longitude){
        this.description = description;
        //this.image = image;
        this.quantity = quantity;
        this.longitude = longitude;
        this.latitude = latitude;

    }

    public Pant(){

    }

    public Pant(Parcel in){
        this.description = in.readString();
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


    public String getOwnerUID() { return ownerUID; }

    public void setOwnerUID(String UID) { this.ownerUID = UID; }

    public String getClaimerUID() { return claimerUID; }

    public void setClaimerUID(String UID) { this.claimerUID = UID; }

    public boolean getClaimed() { return claimed; }

    public void setClaimed(boolean claimed) { this.claimed = claimed; }
}
