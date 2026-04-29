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
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String path = photoPaths.get(position);

        // Load image using BitmapFactory with inSampleSize to avoid OOM
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, 200, 200);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        if (bitmap != null) {
            holder.ivPhoto.setImageBitmap(bitmap);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_photo);
        }

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
