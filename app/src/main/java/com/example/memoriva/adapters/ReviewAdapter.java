package com.example.memoriva.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.R;
import com.example.memoriva.models.Review;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public static final int ACTION_EDIT = 0;
    public static final int ACTION_DELETE = 1;

    public interface OnReviewActionListener {
        void onAction(Review review, int action);
    }

    private final Context context;
    private final List<Review> reviews;
    private final int currentUserId;
    private final OnReviewActionListener actionListener;

    public ReviewAdapter(Context context, List<Review> reviews, int currentUserId,
                         OnReviewActionListener actionListener) {
        this.context = context;
        this.reviews = reviews;
        this.currentUserId = currentUserId;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.tvAuthor.setText("User #" + review.getUserId());
        holder.tvRating.setText(buildStarString(review.getRating()));
        holder.tvReviewText.setText(review.getReviewText() != null ? review.getReviewText() : "");

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(review.getCreatedAt())));

        boolean isOwner = review.getUserId() == currentUserId;
        holder.btnEdit.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onAction(review, ACTION_EDIT);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Review")
                    .setMessage("Are you sure you want to delete this review?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (actionListener != null) actionListener.onAction(review, ACTION_DELETE);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    private String buildStarString(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= rating ? "★" : "☆");
        }
        return sb.toString();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvRating, tvReviewText, tvDate;
        TextView btnEdit, btnDelete;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
