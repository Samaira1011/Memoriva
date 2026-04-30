package com.example.memoriva.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.R;
import com.example.memoriva.models.Memory;

import java.util.List;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder> {

    public interface OnMemoryClickListener {
        void onMemoryClick(Memory memory);
    }

    public interface OnMemoryLongClickListener {
        void onMemoryEdit(Memory memory);
        void onMemoryDelete(Memory memory);
        void onMemoryShare(Memory memory);
    }

    private final Context context;
    private List<Memory> memories;
    private OnMemoryClickListener clickListener;
    private OnMemoryLongClickListener longClickListener;

    public MemoryAdapter(Context context, List<Memory> memories) {
        this.context = context;
        this.memories = memories;
    }

    public void setOnMemoryClickListener(OnMemoryClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnMemoryLongClickListener(OnMemoryLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateMemories(List<Memory> newMemories) {
        this.memories = newMemories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_memory, parent, false);
        return new MemoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {
        Memory memory = memories.get(position);

        holder.tvTitle.setText(memory.getTitle());
        holder.tvDate.setText(memory.getDate() != null ? memory.getDate() : "");
        holder.tvLocation.setText(""); // Location name would require Place lookup

        // Load first photo thumbnail using Glide
        List<String> paths = memory.getPhotoPathList();
        if (!paths.isEmpty()) {
            String path = paths.get(0);
            com.bumptech.glide.Glide.with(context)
                    .load(path.startsWith("content://") ? android.net.Uri.parse(path) : new java.io.File(path))
                    .centerCrop()
                    .placeholder(R.drawable.ic_photo)
                    .error(R.drawable.ic_photo)
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.ic_photo);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onMemoryClick(memory);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenu().add(0, 0, 0, "Edit");
                popup.getMenu().add(0, 1, 1, "Delete");
                popup.getMenu().add(0, 2, 2, "Share");
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 0: longClickListener.onMemoryEdit(memory); return true;
                        case 1: longClickListener.onMemoryDelete(memory); return true;
                        case 2: longClickListener.onMemoryShare(memory); return true;
                    }
                    return false;
                });
                popup.show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return memories != null ? memories.size() : 0;
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

    static class MemoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvDate, tvLocation;

        MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}
