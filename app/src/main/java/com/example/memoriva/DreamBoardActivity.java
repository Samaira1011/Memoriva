package com.example.memoriva;

import androidx.activity.EdgeToEdge;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.memoriva.adapters.DreamDestinationAdapter;
import com.example.memoriva.database.DreamDestinationDao;
import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.models.DreamDestination;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DreamBoardActivity extends BaseActivity {

    private RecyclerView rvDreams;
    private DreamDestinationAdapter dreamAdapter;
    private List<DreamDestination> dreams = new ArrayList<>();
    private MemorivaDbHelper dbHelper;
    private DreamDestinationDao dreamDao;

    private String currentFilter = "ALL"; // ALL, PLANNED, WISHLIST

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dream_board);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0), (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                boolean changed = v.getPaddingLeft() != systemBars.left || v.getPaddingTop() != systemBars.top || v.getPaddingRight() != systemBars.right || v.getPaddingBottom() != systemBars.bottom;
                if (changed) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });

        dbHelper = new MemorivaDbHelper(this);
        dreamDao = new DreamDestinationDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvDreams = findViewById(R.id.rvDreams);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvDreams.setLayoutManager(layoutManager);

        dreamAdapter = new DreamDestinationAdapter(this, dreams);
        dreamAdapter.setOnDreamClickListener(dream -> {
            Intent intent = new Intent(this, DreamDestinationDetailActivity.class);
            intent.putExtra("dream_id", dream.getDreamId());
            startActivity(intent);
        });
        dreamAdapter.setOnDreamLongClickListener(new DreamDestinationAdapter.OnDreamLongClickListener() {
            @Override
            public void onDreamEdit(DreamDestination dream) {
                Intent intent = new Intent(DreamBoardActivity.this, DreamDestinationCreateEditActivity.class);
                intent.putExtra("dream_id", dream.getDreamId());
                startActivity(intent);
            }

            @Override
            public void onDreamDelete(DreamDestination dream) {
                new androidx.appcompat.app.AlertDialog.Builder(DreamBoardActivity.this)
                        .setTitle("Delete Destination")
                        .setMessage("Delete \"" + dream.getPlaceName() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                                dreamDao.deleteDreamDestination(db, dream.getDreamId());
                            }
                            loadDreams();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onDreamMarkVisited(DreamDestination dream) {
                markAsVisited(dream);
            }
        });
        rvDreams.setAdapter(dreamAdapter);

        // Filter chips
        TextView chipAll = findViewById(R.id.chipAll);
        TextView chipPlanned = findViewById(R.id.chipPlanned);
        TextView chipWishlist = findViewById(R.id.chipWishlist);

        chipAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateChipSelection(chipAll, chipPlanned, chipWishlist);
            loadDreams();
        });
        chipPlanned.setOnClickListener(v -> {
            currentFilter = DreamDestination.STATUS_PLANNED;
            updateChipSelection(chipPlanned, chipAll, chipWishlist);
            loadDreams();
        });
        chipWishlist.setOnClickListener(v -> {
            currentFilter = DreamDestination.STATUS_WISHLIST;
            updateChipSelection(chipWishlist, chipAll, chipPlanned);
            loadDreams();
        });

        FloatingActionButton fabAddDream = findViewById(R.id.fabAddDream);
        fabAddDream.setOnClickListener(v -> {
            Intent intent = new Intent(this, DreamDestinationCreateEditActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation(bottomNav, R.id.nav_home);

        loadDreams();
    }

    private void updateChipSelection(TextView selected, TextView... others) {
        selected.setBackgroundResource(R.drawable.chip_selected);
        selected.setTextColor(getResources().getColor(R.color.white, null));
        for (TextView chip : others) {
            chip.setBackgroundResource(R.drawable.chip_unselected);
            chip.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        }
    }

    private void loadDreams() {
        dreams.clear();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<DreamDestination> loaded;
            if ("ALL".equals(currentFilter)) {
                loaded = dreamDao.getDreamDestinationsByUserId(db, 1); // placeholder userId
            } else {
                loaded = dreamDao.getDreamDestinationsByStatus(db, 1, currentFilter);
            }
            dreams.addAll(loaded);
        }
        dreamAdapter.updateDreams(dreams);
    }

    private void markAsVisited(DreamDestination dream) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Mark as Visited")
                .setMessage("Mark \"" + dream.getPlaceName() + "\" as visited? This will open a new trip creation.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(this, TripCreateEditActivity.class);
                    intent.putExtra("destination", dream.getPlaceName());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDreams();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
