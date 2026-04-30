package com.example.memoriva.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.R;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    public interface OnPhotoRemoveListener {
        void onPhotoRemove(int position);
    }

    private final Context context;
    private final List<String> photoPaths;
    private OnPhotoRemoveListener removeListener;

    public PhotoAdapter(Context context, List<String> photoPaths) {
        this.context = context;
        this.photoPaths = photoPaths;
    }

    public void setOnPhotoRemoveListener(OnPhotoRemoveListener listener) {
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo_thumbnail, parent, false);
        // Make each photo fill the full width of the RecyclerView
        view.getLayoutParams().width = parent.getWidth() > 0 ?
                parent.getWidth() : android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        view.getLayoutParams().height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        // Hide the remove button in detail view (only show in edit mode)
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String path = photoPaths.get(position);

        // Use Glide to load image — handles both file paths and content URIs
        com.bumptech.glide.Glide.with(context)
                .load(path.startsWith("content://") ? android.net.Uri.parse(path) : new java.io.File(path))
                .centerCrop()
                .placeholder(R.drawable.ic_photo)
                .error(R.drawable.ic_photo)
                .into(holder.ivPhoto);

        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onPhotoRemove(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoPaths != null ? photoPaths.size() : 0;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton btnRemove;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnRemove = itemView.findViewById(R.id.btnRemovePhoto);
        }
    }
}
