package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.adapters.ReviewAdapter;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.PlaceDao;
import com.example.memoriva.database.ReviewDao;
import com.example.memoriva.models.Place;
import com.example.memoriva.models.Review;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PlaceDetailActivity extends BaseActivity {

    public static final String EXTRA_PLACE_ID = "place_id";

    private int placeId;
    private MemorivaDbHelper dbHelper;
    private PlaceDao placeDao;
    private ReviewDao reviewDao;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviews = new ArrayList<>();

    private TextView tvPlaceName;
    private TextView tvAddress;
    private TextView tvRatingStars;
    private TextView tvAverageRating;
    private TextView tvReviewCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_place_detail);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

        placeId = getIntent().getIntExtra(EXTRA_PLACE_ID, -1);

        dbHelper = new MemorivaDbHelper(this);
        placeDao = new PlaceDao();
        reviewDao = new ReviewDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvPlaceName = findViewById(R.id.tvPlaceName);
        tvAddress = findViewById(R.id.tvAddress);
        tvRatingStars = findViewById(R.id.tvRatingStars);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);

        RecyclerView rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        reviewAdapter = new ReviewAdapter(this, reviews, 1, (review, action) -> {
            if (action == ReviewAdapter.ACTION_EDIT) {
                Intent intent = new Intent(this, ReviewCreateEditActivity.class);
                intent.putExtra(ReviewCreateEditActivity.EXTRA_PLACE_ID, placeId);
                intent.putExtra(ReviewCreateEditActivity.EXTRA_REVIEW_ID, review.getReviewId());
                startActivity(intent);
            } else if (action == ReviewAdapter.ACTION_DELETE) {
                deleteReview(review);
            }
        });
        rvReviews.setAdapter(reviewAdapter);

        FloatingActionButton fabAddReview = findViewById(R.id.fabAddReview);
        fabAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReviewCreateEditActivity.class);
            intent.putExtra(ReviewCreateEditActivity.EXTRA_PLACE_ID, placeId);
            startActivity(intent);
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (placeId < 0) return;
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Place place = placeDao.getPlaceById(db, placeId);
            if (place != null) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(place.getName());
                }
                tvPlaceName.setText(place.getName());
                String address = place.getAddress() != null ? place.getAddress() : "";
                if (place.getCity() != null && !place.getCity().isEmpty()) {
                    address = address.isEmpty() ? place.getCity() : address + ", " + place.getCity();
                }
                tvAddress.setText(address);
            }

            reviews.clear();
            reviews.addAll(reviewDao.getReviewsByPlaceId(db, placeId));
            float avg = reviewDao.getAverageRating(db, placeId);
            reviewAdapter.notifyDataSetChanged();

            tvRatingStars.setText(buildStarString((int) Math.round(avg)));
            tvAverageRating.setText(String.format(java.util.Locale.getDefault(), "%.1f", avg));
            tvReviewCount.setText("(" + reviews.size() + ")");
        }
    }

    private void deleteReview(Review review) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Review")
                .setMessage("Are you sure you want to delete this review?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                        reviewDao.deleteReview(db, review.getReviewId());
                    }
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static String buildStarString(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= rating ? "★" : "☆");
        }
        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_place_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_sort) {
            // Sort reviews - toggle between date and rating
            reviews.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            reviewAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
