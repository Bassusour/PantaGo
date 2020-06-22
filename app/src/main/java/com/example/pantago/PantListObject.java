package com.example.pantago;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;



import java.io.ByteArrayOutputStream;

public class PantListObject implements Comparable<PantListObject>, Parcelable {
    Pant pant;
    Bitmap bitmap;
    Double distance;

    public PantListObject(Pant pant, Bitmap bitmap, double distance){
        this.pant = pant;
        this.bitmap = bitmap;
        this.distance = distance;
    }

    public PantListObject(Parcel in){
        Pant parcelPant = new Pant(in.readString(),in.readInt(),in.readDouble(),in.readDouble());
        pant.setPantKey(in.readString());
        pant.setClaimerUID(in.readString());
        pant.setOwnerUID(in.readString());
        pant.setClaimerUID(in.readString());
        this.pant = parcelPant;
        this.distance = in.readDouble();

        byte[] data = new byte[in.readInt()];
        in.readByteArray(data);
        this.bitmap = BitmapFactory.decodeByteArray(data, -1, data.length );
    }

    public Pant getPant() {
        return pant;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Double getDistance() {
        return distance;

    }

    @Override
    public int compareTo(PantListObject pantListObject) {
        if(this.getDistance() < pantListObject.getDistance()) return -1;
        else if(this.getDistance() > pantListObject.getDistance()) return 1;
        else return 0;
    }

    public static final Creator<PantListObject> CREATOR = new Creator<PantListObject>() {


        @Override
        public PantListObject createFromParcel(Parcel in) {
            return new PantListObject(in);
        }

        @Override
        public PantListObject[] newArray(int i) {
            return new PantListObject[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(pant.getDescription());
        parcel.writeInt(pant.getQuantity());
        parcel.writeDouble(pant.getLatitude());
        parcel.writeDouble(pant.getLongitude());
        parcel.writeString(pant.getPantKey());
        parcel.writeString(pant.getOwnerUID());
        parcel.writeString(pant.getClaimerUID());
        parcel.writeDouble(distance);
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        bitmap.compress(Bitmap. CompressFormat. PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        parcel.writeInt(byteArray.length);
        parcel.writeByteArray(byteArray);
    }


}
