package com.example.pid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImagensAdapter extends RecyclerView.Adapter<ImagensAdapter.ImagensViewHolder> {

    private static final int IMAGE = 0;
    private ArrayList<ImageItem> imageItems;

    public ImagensAdapter() {
        this.imageItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ImagensViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == IMAGE) {
            ImagensViewHolder holder = new ImagensViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_1, parent, false));
            return holder;
        }
        throw new IllegalStateException();
    }

    @Override
    public void onBindViewHolder(@NonNull ImagensViewHolder holder, int position) {
        holder.imageView.setImageBitmap(imageItems.get(position).getBitmap());
    }

    @Override
    public int getItemViewType(int position) {
        return IMAGE;
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    class ImagensViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;

        public ImagensViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
