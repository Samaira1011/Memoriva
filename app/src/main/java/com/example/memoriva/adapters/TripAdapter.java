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
import com.example.memoriva.models.Trip;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    public interface OnTripLongClickListener {
        void onTripEdit(Trip trip);
        void onTripDelete(Trip trip);
    }

    private final Context context;
    private List<Trip> trips;
    private OnTripClickListener clickListener;
    private OnTripLongClickListener longClickListener;

    public TripAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = trips;
    }

    public void setOnTripClickListener(OnTripClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnTripLongClickListener(OnTripLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateTrips(List<Trip> newTrips) {
        this.trips = newTrips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.tvName.setText(trip.getName());
        holder.tvDestination.setText(trip.getDestination() != null ? trip.getDestination() : "");

        String dateRange = "";
        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            dateRange = trip.getStartDate() + " – " + trip.getEndDate();
        }
        holder.tvDateRange.setText(dateRange);

        // Load cover photo using Glide — handles content:// URIs and file paths
        String coverPath = trip.getCoverPhotoPath();
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
            if (clickListener != null) clickListener.onTripClick(trip);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenu().add(0, 0, 0, "Edit");
                popup.getMenu().add(0, 1, 1, "Delete");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        longClickListener.onTripEdit(trip);
                        return true;
                    } else if (item.getItemId() == 1) {
                        longClickListener.onTripDelete(trip);
                        return true;
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
        return trips != null ? trips.size() : 0;
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

    static class TripViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName, tvDestination, tvDateRange;

        TripViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvName = itemView.findViewById(R.id.tvName);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
        }
    }
}
