package com.example.pantago;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;

public class PantFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<PantListObject> pantlist = new ArrayList<PantListObject>();
    PantAdapter mAdapter = new PantAdapter(pantlist);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            pantlist = savedInstanceState.getParcelableArrayList("list");
            mAdapter.notifyDataSetChanged();
            Log.i(MapsActivity.TAG, String.valueOf(pantlist.get(0).getPant().getQuantity()));
            mAdapter = new PantAdapter(pantlist);
        }
        View rootView = inflater.inflate(R.layout.fragment_item_list, container, false);
        mAdapter.addContext(getActivity());

        rootView.setBackgroundColor(getResources().getColor(R.color.darkgray));
        recyclerView = (RecyclerView)rootView.findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
       return  rootView;
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", pantlist);
    }

    public void addPant(Pant pant, double myLan, double myLon){
        Log.i(MapsActivity.TAG, "Added pant to list");
        Boolean isThere = false;
        for(PantListObject pantListObject : pantlist){
            if(pantListObject.getPant().getPantKey().equals(pant.getPantKey())){
                isThere = true;

            }
        }
        if(!isThere) {
            FirebaseStorage.getInstance().getReference().child("Pictures/" + pant.getPantKey() + ".jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap map = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    pantlist.add(new PantListObject(pant, map, MapsActivity.getDistance(pant.getLatitude(), pant.getLongitude(), myLan, myLon)));
                    sort();
                    refreshDistance(myLan, myLon);
                }
            });
        }
    }

    public void removePant(Pant pant){
        for(PantListObject pantListObject : pantlist){
            if(pantListObject.getPant().getPantKey().equals(pant.getPantKey())){
                pantlist.remove(pantListObject);
                mAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    public void sort(){
        Collections.sort(pantlist);
        mAdapter.notifyDataSetChanged();
    }
    public void refreshDistance(Double myLan, Double myLon){
        for(PantListObject plo : pantlist){
            Pant pant = plo.getPant();
           plo.distance = MapsActivity.getDistance(pant.getLatitude(), pant.getLongitude(), myLan, myLon);
        }
        mAdapter.notifyDataSetChanged();
    }
}