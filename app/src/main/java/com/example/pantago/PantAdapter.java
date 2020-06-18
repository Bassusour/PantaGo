package com.example.pantago;

import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class PantAdapter extends RecyclerView.Adapter<PantAdapter.ViewHolder> {

    private final List<PantListObject> mValues;

    public PantAdapter(List<PantListObject> items) {
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
        holder.mAfstandNumb.setText("ff");
        holder.mAntalNumb.setText(String.valueOf(mValues.get(position).getPant().getQuantity()));
        holder.mThumbnail.setImageBitmap(mValues.get(position).getBitmap());
        holder.mAfstandNumb.setText(String.valueOf(mValues.get(position).getDistance()));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mAntalText;
        public final TextView mAntalNumb;
        public final TextView mAfstandText;
        public final TextView mAfstandNumb;
        public final ImageView mThumbnail;
        public PantListObject mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAntalText= (TextView) view.findViewById(R.id.antaltext);
            mAntalNumb= (TextView) view.findViewById(R.id.antalNumb);
            mAfstandText= (TextView) view.findViewById(R.id.afstandText);
            mAfstandNumb= (TextView) view.findViewById(R.id.AfstandNumb);
            mThumbnail= (ImageView) view.findViewById(R.id.thumbnail);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAfstandNumb.getText() + "'";
        }
    }
}