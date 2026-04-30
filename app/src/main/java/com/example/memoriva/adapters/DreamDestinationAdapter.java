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
import com.example.memoriva.models.DreamDestination;

import java.util.List;

public class DreamDestinationAdapter extends RecyclerView.Adapter<DreamDestinationAdapter.DreamViewHolder> {

    public interface OnDreamClickListener {
        void onDreamClick(DreamDestination dream);
    }

    public interface OnDreamLongClickListener {
        void onDreamEdit(DreamDestination dream);
        void onDreamDelete(DreamDestination dream);
        void onDreamMarkVisited(DreamDestination dream);
    }

    private final Context context;
    private List<DreamDestination> dreams;
    private OnDreamClickListener clickListener;
    private OnDreamLongClickListener longClickListener;

    public DreamDestinationAdapter(Context context, List<DreamDestination> dreams) {
        this.context = context;
        this.dreams = dreams;
    }

    public void setOnDreamClickListener(OnDreamClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnDreamLongClickListener(OnDreamLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateDreams(List<DreamDestination> newDreams) {
        this.dreams = newDreams;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DreamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dream_destination, parent, false);
        return new DreamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DreamViewHolder holder, int position) {
        DreamDestination dream = dreams.get(position);

        holder.tvPlaceName.setText(dream.getPlaceName());
        holder.tvExpectedDate.setText(dream.getExpectedDate() != null
                ? "Expected: " + dream.getExpectedDate() : "");
        holder.tvStatus.setText(dream.getStatus());

        // Status badge color
        if (DreamDestination.STATUS_PLANNED.equals(dream.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.chip_selected);
            // colorPlanned = blue - set programmatically
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            context.getResources().getColor(R.color.colorPlanned, null)));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.chip_selected);
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            context.getResources().getColor(R.color.colorWishlist, null)));
        }

        // Load cover image using Glide
        String coverPath = dream.getCoverImagePath();
        if (coverPath != null && !coverPath.isEmpty()) {
            Object source = coverPath.startsWith("content://")
                    ? android.net.Uri.parse(coverPath) : new java.io.File(coverPath);
            com.bumptech.glide.Glide.with(context)
                    .load(source)
                    .centerCrop()
                    .placeholder(R.drawable.ic_photo)
                    .error(R.drawable.ic_photo)
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_photo);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onDreamClick(dream);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenu().add(0, 0, 0, "Edit");
                popup.getMenu().add(0, 1, 1, "Delete");
                popup.getMenu().add(0, 2, 2, "Mark as Visited");
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 0: longClickListener.onDreamEdit(dream); return true;
                        case 1: longClickListener.onDreamDelete(dream); return true;
                        case 2: longClickListener.onDreamMarkVisited(dream); return true;
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
        return dreams != null ? dreams.size() : 0;
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

    static class DreamViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvPlaceName, tvExpectedDate, tvStatus;

        DreamViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvExpectedDate = itemView.findViewById(R.id.tvExpectedDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
