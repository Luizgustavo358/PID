package com.example.pid;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImagensAdapter extends RecyclerView.Adapter<ImagensAdapter.ImagensViewHolder> {

    private ArrayList<ImageItem> imageItems;

    @NonNull
    @Override
    public ImagensViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ImagensViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ImagensViewHolder extends RecyclerView.ViewHolder{

        public ImagensViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
