package com.example.pantago;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class PantAdapter extends RecyclerView.Adapter<PantAdapter.ViewHolder> {


    private final List<PantListObject> mValues;
    private Context mContext;

    public PantAdapter(ArrayList<PantListObject> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mThumbnail.setImageBitmap(mValues.get(position).getBitmap());
        holder.mAntalText.setText(String.format("Antal: %s", holder.mItem.getPant().getQuantity()));
        holder.mAfstandNumb.setText(String.format("%.1f km",holder.mItem.getDistance()/1000));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(MapsActivity.TAG, String.valueOf(holder.mItem.getPant().getQuantity()));
                MapsActivity activity =(MapsActivity) mContext;
                activity.zoomToPant(holder.mItem.getPant());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mAntalText;
        public final TextView mAfstandNumb;
        public final ImageView mThumbnail;
        public PantListObject mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAntalText= (TextView) view.findViewById(R.id.antaltext);
            mAfstandNumb= (TextView) view.findViewById(R.id.AfstandNumb);
            mThumbnail= (ImageView) view.findViewById(R.id.thumbnail);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAfstandNumb.getText() + "'";
        }
    }
    public void addContext(Context context){
        mContext = context;
    }
}