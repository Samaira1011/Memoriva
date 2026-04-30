package com.example.memoriva;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memoriva.auth.AuthManager;
import com.example.memoriva.database.FriendDao;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.database.TripDao;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends BaseActivity {

    private static final String PREFS_NAME = "memoriva_prefs";
    private static final int USER_ID = 1; // placeholder

    private MemorivaDbHelper dbHelper;
    private MemoryDao memoryDao;
    private TripDao tripDao;
    private FriendDao friendDao;

    private TextView tvName;
    private TextView tvUsername;
    private TextView tvBio;
    private TextView tvTripsCount;
    private TextView tvFriendsCount;
    private TextView tvMemoriesCount;
    private ImageView ivAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new MemorivaDbHelper(this);
        memoryDao = new MemoryDao();
        tripDao = new TripDao();
        friendDao = new FriendDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
        }

        tvName = findViewById(R.id.tvName);
        tvUsername = findViewById(R.id.tvUsername);
        tvBio = findViewById(R.id.tvBio);
        tvTripsCount = findViewById(R.id.tvTripsCount);
        tvFriendsCount = findViewById(R.id.tvFriendsCount);
        tvMemoriesCount = findViewById(R.id.tvMemoriesCount);
        ivAvatar = findViewById(R.id.ivAvatar);

        // Quick-access cards — set titles and subtitles
        setupCard(R.id.cardAddFriends, R.drawable.ic_friends,
                "Add Friends", "Discover people to follow",
                () -> startActivity(new Intent(this, FriendsDiscoverActivity.class)));

        setupCard(R.id.cardMyTrips, R.drawable.ic_trips,
                "My Trips", "View all your travel trips",
                () -> startActivity(new Intent(this, MyTripsActivity.class)));

        setupCard(R.id.cardFutureTrips, R.drawable.ic_dream,
                "Dream Board", "Places you want to visit",
                () -> startActivity(new Intent(this, DreamBoardActivity.class)));

        setupCard(R.id.cardTripTimeline, R.drawable.ic_calendar,
                "Trip Timeline", "Select a trip to view its timeline",
                () -> startActivity(new Intent(this, MyTripsActivity.class)));

        // Edit profile
        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                Toast.makeText(this, "Profile editing coming soon", Toast.LENGTH_SHORT).show());

        // Sign out
        MaterialButton btnSignOut = findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(v -> signOut());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_profile);

        loadProfile();
        loadStats();
    }

    private void setupCard(int cardId, int iconRes, String title, String subtitle, Runnable onClick) {
        View card = findViewById(cardId);
        if (card == null) return;
        TextView tvTitle = card.findViewById(R.id.tvCardTitle);
        TextView tvSubtitle = card.findViewById(R.id.tvCardSubtitle);
        android.widget.ImageView ivIcon = card.findViewById(R.id.ivCardIcon);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvSubtitle != null) tvSubtitle.setText(subtitle);
        if (ivIcon != null) ivIcon.setImageResource(iconRes);
        card.setOnClickListener(v -> onClick.run());
    }

    private void loadProfile() {
        // Try Firebase user first, fall back to SharedPreferences
        FirebaseUser firebaseUser = AuthManager.getInstance(this).getCurrentUser();

        if (firebaseUser != null) {
            String displayName = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();
            Uri photoUrl = firebaseUser.getPhotoUrl();

            tvName.setText(displayName != null && !displayName.isEmpty() ? displayName : "Traveler");
            tvUsername.setText("@" + (email != null && email.contains("@") ?
                    email.substring(0, email.indexOf('@')) : "traveler"));
            tvBio.setText("Exploring the world, one memory at a time.");

            // Load profile photo from Google account
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.avatar_placeholder)
                        .into(ivAvatar);
            }
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String displayName = prefs.getString("display_name", "Traveler");
            String email = prefs.getString("email", "");
            tvName.setText(displayName != null ? displayName : "Traveler");
            tvUsername.setText("@" + (email != null && email.contains("@") ?
                    email.substring(0, email.indexOf('@')) : "traveler"));
            tvBio.setText("Exploring the world, one memory at a time.");
        }
    }

    private void loadStats() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            int trips = tripDao.getTripsByUserId(db, USER_ID).size();
            int friends = friendDao.getFriendsCount(db, USER_ID);
            int memories = memoryDao.getMemoriesByUserId(db, USER_ID).size();

            tvTripsCount.setText(String.valueOf(trips));
            tvFriendsCount.setText(String.valueOf(friends));
            tvMemoriesCount.setText(String.valueOf(memories));
        }
    }

    private void signOut() {
        AuthManager.getInstance(this).signOut();
        AuthManager.getInstance(this).clearUserFromPrefs(this);
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
