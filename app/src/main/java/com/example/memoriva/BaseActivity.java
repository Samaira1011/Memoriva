package com.example.memoriva;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Base activity that sets up BottomNavigationView tab switching
 * to avoid code duplication across main activities.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Call this after setContentView to wire up the bottom navigation.
     * @param bottomNav the BottomNavigationView in the layout
     * @param selectedItemId the menu item id that should be selected for this activity
     */
    protected void setupBottomNavigation(BottomNavigationView bottomNav, int selectedItemId) {
        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                if (!(this instanceof MapActivity)) {
                    Intent intent = new Intent(this, MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                } else if (this instanceof MapActivity) {
                    ((MapActivity) this).showListView();
                }
                return true;
            } else if (id == R.id.nav_map) {
                if (!(this instanceof WorldMapActivity)) {
                    Intent intent = new Intent(this, WorldMapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                return true;
            } else if (id == R.id.nav_memories) {
                if (!(this instanceof CalendarActivity)) {
                    Intent intent = new Intent(this, CalendarActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                return true;
            } else if (id == R.id.nav_profile) {
                if (!(this instanceof ProfileActivity)) {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });
    }
}
