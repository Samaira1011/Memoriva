package com.example.memoriva;

import androidx.activity.EdgeToEdge;

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
import androidx.annotation.Nullable;
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
    private static final int REQUEST_PROFILE_PHOTO = 99;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

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

        // Tap avatar to change photo
        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PROFILE_PHOTO);
        });

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
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());

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

    private void showEditProfileDialog() {
        // Create a properly styled dialog layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);
        layout.setBackgroundColor(android.graphics.Color.WHITE);

        // Name field
        android.widget.TextView labelName = new android.widget.TextView(this);
        labelName.setText("Display Name");
        labelName.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
        labelName.setTextSize(12);
        layout.addView(labelName);

        android.widget.EditText etName = new android.widget.EditText(this);
        etName.setText(tvName.getText());
        etName.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        etName.setHintTextColor(getResources().getColor(R.color.colorTextSecondary, null));
        etName.setHint("Your name");
        etName.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etName.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.colorPrimary, null)));
        android.widget.LinearLayout.LayoutParams nameParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.setMargins(0, 8, 0, 24);
        etName.setLayoutParams(nameParams);
        layout.addView(etName);

        // Bio field
        android.widget.TextView labelBio = new android.widget.TextView(this);
        labelBio.setText("Bio");
        labelBio.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
        labelBio.setTextSize(12);
        layout.addView(labelBio);

        android.widget.EditText etBio = new android.widget.EditText(this);
        etBio.setText(tvBio.getText());
        etBio.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        etBio.setHintTextColor(getResources().getColor(R.color.colorTextSecondary, null));
        etBio.setHint("Tell us about yourself");
        etBio.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etBio.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.colorPrimary, null)));
        android.widget.LinearLayout.LayoutParams bioParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        bioParams.setMargins(0, 8, 0, 8);
        etBio.setLayoutParams(bioParams);
        layout.addView(etBio);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(layout)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(R.color.colorPrimary, null));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String newName = etName.getText().toString().trim();
                        String newBio = etBio.getText().toString().trim();

                        if (!newName.isEmpty()) {
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                    .edit().putString("display_name", newName).apply();
                            tvName.setText(newName);

                            FirebaseUser user = AuthManager.getInstance(this).getCurrentUser();
                            if (user != null) {
                                com.google.firebase.auth.UserProfileChangeRequest req =
                                        new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                .setDisplayName(newName).build();
                                user.updateProfile(req);
                            }
                        }
                        if (!newBio.isEmpty()) {
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                    .edit().putString("bio", newBio).apply();
                            tvBio.setText(newBio);
                        }
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_PROFILE_PHOTO
                && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}

            // Save photo URI
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit().putString("profile_photo", uri.toString()).apply();

            // Load into avatar
            Glide.with(this).load(uri).circleCrop()
                    .placeholder(R.drawable.avatar_placeholder).into(ivAvatar);

            // Update Firebase photo
            FirebaseUser user = AuthManager.getInstance(this).getCurrentUser();
            if (user != null) {
                com.google.firebase.auth.UserProfileChangeRequest req =
                        new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri).build();
                user.updateProfile(req);
            }
        }
    }

    private void loadProfile() {
        FirebaseUser firebaseUser = AuthManager.getInstance(this).getCurrentUser();

        if (firebaseUser != null) {
            String displayName = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();
            Uri photoUrl = firebaseUser.getPhotoUrl();

            // Prefer locally saved name (user may have edited it)
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String savedName = prefs.getString("display_name", null);
            String savedBio = prefs.getString("bio", "Exploring the world, one memory at a time.");

            String name = (savedName != null && !savedName.isEmpty()) ? savedName :
                    (displayName != null && !displayName.isEmpty() ? displayName : "Traveler");

            tvName.setText(name);
            tvUsername.setText("@" + (email != null && email.contains("@") ?
                    email.substring(0, email.indexOf('@')) : "traveler"));
            tvBio.setText(savedBio);

            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.avatar_placeholder)
                        .into(ivAvatar);
            } else {
                // Check locally saved photo
                String savedPhoto = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .getString("profile_photo", null);
                if (savedPhoto != null) {
                    Glide.with(this).load(Uri.parse(savedPhoto)).circleCrop()
                            .placeholder(R.drawable.avatar_placeholder).into(ivAvatar);
                }
            }
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String displayName = prefs.getString("display_name", "Traveler");
            String email = prefs.getString("email", "");
            String bio = prefs.getString("bio", "Exploring the world, one memory at a time.");
            tvName.setText(displayName != null ? displayName : "Traveler");
            tvUsername.setText("@" + (email != null && email.contains("@") ?
                    email.substring(0, email.indexOf('@')) : "traveler"));
            tvBio.setText(bio);
        }
    }

    private void loadStats() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            int userId = com.example.memoriva.utils.UserManager.getLocalUserId(
                    this, AuthManager.getInstance(this).getCurrentUser());
            int trips = tripDao.getTripsByUserId(db, userId).size();
            int friends = friendDao.getFriendsCount(db, userId);
            int memories = memoryDao.getMemoriesByUserId(db, userId).size();

            tvTripsCount.setText(String.valueOf(trips));
            tvFriendsCount.setText(String.valueOf(friends));
            tvMemoriesCount.setText(String.valueOf(memories));
        }
    }

    private void signOut() {
        AuthManager.getInstance(this).signOut();
        AuthManager.getInstance(this).clearUserFromPrefs(this);
        com.example.memoriva.utils.UserManager.clearCache();
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
