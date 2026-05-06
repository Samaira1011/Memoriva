package com.example.memoriva.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memoriva.R;

import java.io.File;
import java.util.List;

public class GridPhotoAdapter extends RecyclerView.Adapter<GridPhotoAdapter.ViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(int position);
    }

    private final Context context;
    private final List<String> photoPaths;
    private OnPhotoClickListener clickListener;

    public GridPhotoAdapter(Context context, List<String> photoPaths) {
        this.context = context;
        this.photoPaths = photoPaths;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String path = photoPaths.get(position);
        Object source = path.startsWith("content://") ? Uri.parse(path) : new File(path);
        Glide.with(context)
                .load(source)
                .centerCrop()
                .placeholder(R.drawable.ic_photo)
                .error(R.drawable.ic_photo)
                .into(holder.ivPhoto);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onPhotoClick(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return photoPaths != null ? photoPaths.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivGridPhoto);
        }
    }
}
