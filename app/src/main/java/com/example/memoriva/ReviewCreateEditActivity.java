package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.ReviewDao;
import com.example.memoriva.models.Review;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ReviewCreateEditActivity extends BaseActivity {

    public static final String EXTRA_PLACE_ID = "place_id";
    public static final String EXTRA_REVIEW_ID = "review_id";

    private int placeId = -1;
    private int reviewId = -1;
    private int selectedRating = 0;

    private ImageView[] starViews = new ImageView[5];
    private TextInputEditText etReviewText;

    private MemorivaDbHelper dbHelper;
    private ReviewDao reviewDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_create_edit);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

        placeId = getIntent().getIntExtra(EXTRA_PLACE_ID, -1);
        reviewId = getIntent().getIntExtra(EXTRA_REVIEW_ID, -1);

        dbHelper = new MemorivaDbHelper(this);
        reviewDao = new ReviewDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Write a Review");
        }

        starViews[0] = findViewById(R.id.ivStar1);
        starViews[1] = findViewById(R.id.ivStar2);
        starViews[2] = findViewById(R.id.ivStar3);
        starViews[3] = findViewById(R.id.ivStar4);
        starViews[4] = findViewById(R.id.ivStar5);

        etReviewText = findViewById(R.id.etReviewText);

        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            starViews[i].setOnClickListener(v -> {
                selectedRating = starIndex;
                updateStarDisplay();
            });
        }

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitReview);
        btnSubmit.setOnClickListener(v -> submitReview());

        // Load existing review if editing
        if (reviewId > 0) {
            loadExistingReview();
        }
    }

    private void loadExistingReview() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Review review = reviewDao.getReviewById(db, reviewId);
            if (review != null) {
                selectedRating = review.getRating();
                updateStarDisplay();
                if (review.getReviewText() != null) {
                    etReviewText.setText(review.getReviewText());
                }
            }
        }
    }

    private void updateStarDisplay() {
        for (int i = 0; i < 5; i++) {
            if (i < selectedRating) {
                starViews[i].setImageResource(R.drawable.ic_star);
                starViews[i].setColorFilter(getResources().getColor(R.color.colorPrimary, getTheme()));
            } else {
                starViews[i].setImageResource(R.drawable.ic_star_outline);
                starViews[i].setColorFilter(getResources().getColor(R.color.colorTextSecondary, getTheme()));
            }
        }
    }

    private void submitReview() {
        if (selectedRating < 1 || selectedRating > 5) {
            Toast.makeText(this, "Please select a rating (1-5 stars)", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = etReviewText.getText() != null ? etReviewText.getText().toString().trim() : "";

        long now = System.currentTimeMillis();

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            if (reviewId > 0) {
                // Update existing
                Review review = reviewDao.getReviewById(db, reviewId);
                if (review != null) {
                    review.setRating(selectedRating);
                    review.setReviewText(reviewText);
                    review.setUpdatedAt(now);
                    reviewDao.updateReview(db, review);
                    Toast.makeText(this, "Review updated!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Insert new
                Review review = new Review();
                review.setUserId(com.example.memoriva.utils.UserManager.getLocalUserId(
                        this, com.example.memoriva.auth.AuthManager.getInstance(this).getCurrentUser()));
                review.setPlaceId(placeId);
                review.setRating(selectedRating);
                review.setReviewText(reviewText);
                review.setCreatedAt(now);
                review.setUpdatedAt(now);
                long result = reviewDao.insertReview(db, review);
                if (result < 0) {
                    Toast.makeText(this, "Failed to save review", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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
