package com.example.memoriva.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.R;
import com.example.memoriva.models.Memory;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    public interface OnMemoryClickListener {
        void onMemoryClick(Memory memory);
    }

    private final Context context;
    private List<Memory> memories;
    private OnMemoryClickListener clickListener;

    public TimelineAdapter(Context context, List<Memory> memories) {
        this.context = context;
        this.memories = memories;
    }

    public void setOnMemoryClickListener(OnMemoryClickListener listener) {
        this.clickListener = listener;
    }

    public void updateMemories(List<Memory> newMemories) {
        this.memories = newMemories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline_entry, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        Memory memory = memories.get(position);

        // Format date for display
        String date = memory.getDate() != null ? memory.getDate() : "";
        if (date.length() >= 10) {
            // Convert YYYY-MM-DD to MMM\nDD
            String[] parts = date.split("-");
            if (parts.length == 3) {
                String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
                try {
                    int monthIdx = Integer.parseInt(parts[1]) - 1;
                    String monthStr = (monthIdx >= 0 && monthIdx < 12) ? months[monthIdx] : parts[1];
                    holder.tvDate.setText(monthStr + "\n" + parts[2]);
                } catch (NumberFormatException e) {
                    holder.tvDate.setText(date);
                }
            } else {
                holder.tvDate.setText(date);
            }
        } else {
            holder.tvDate.setText(date);
        }

        holder.tvTitle.setText(memory.getTitle());
        holder.tvLocation.setText(""); // Would require Place lookup

        // Load first photo thumbnail
        List<String> paths = memory.getPhotoPathList();
        if (!paths.isEmpty()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(paths.get(0), options);
            options.inSampleSize = 4;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(paths.get(0), options);
            if (bitmap != null) {
                holder.ivThumbnail.setImageBitmap(bitmap);
            } else {
                holder.ivThumbnail.setImageResource(R.drawable.ic_photo);
            }
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.ic_photo);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onMemoryClick(memory);
        });
    }

    @Override
    public int getItemCount() {
        return memories != null ? memories.size() : 0;
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvLocation;
        ImageView ivThumbnail;

        TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}
