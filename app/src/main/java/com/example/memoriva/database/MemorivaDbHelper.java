package com.example.memoriva.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MemorivaDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "memoriva.db";
    public static final int DATABASE_VERSION = 1;

    // -------------------------------------------------------------------------
    // Table: users
    // -------------------------------------------------------------------------
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE users (" +
            "  user_id     INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  firebase_uid TEXT UNIQUE NOT NULL," +
            "  email        TEXT UNIQUE NOT NULL," +
            "  full_name    TEXT NOT NULL," +
            "  username     TEXT UNIQUE," +
            "  bio          TEXT," +
            "  avatar_url   TEXT," +
            "  created_at   INTEGER" +
            ");";

    // -------------------------------------------------------------------------
    // Table: places
    // -------------------------------------------------------------------------
    private static final String CREATE_TABLE_PLACES =
            "CREATE TABLE places (" +
            "  place_id   INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name       TEXT NOT NULL," +
            "  latitude   REAL NOT NULL," +
            "  longitude  REAL NOT NULL," +
            "  address    TEXT," +
            "  city       TEXT," +
            "  country    TEXT," +
            "  created_at INTEGER" +
            ");";

    // -------------------------------------------------------------------------
    // Table: memories
    // -------------------------------------------------------------------------
    private static final String CREATE_TABLE_MEMORIES =
            "CREATE TABLE memories (" +
            "  memory_id   INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id     INTEGER NOT NULL REFERENCES users(user_id)," +
            "  title       TEXT NOT NULL," +
            "  date        TEXT NOT NULL," +
            "  place_id    INTEGER REFERENCES places(place_id)," +
            "  notes       TEXT," +
            "  photo_paths TEXT," +
            "  is_shared   INTEGER DEFAULT 0," +
            "  is_locked   INTEGER DEFAULT 0," +
            "  created_at  INTEGER," +
            "  updated_at  INTEGER" +
            ");";

    // -------------------------------------------------------------------------
    // Table: trips
    // -------------------------------------------------------------------------
    private static final String CREATE_TABLE_TRIPS =
            "CREATE TABLE trips (" +
            "  trip_id          INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id          INTEGER NOT NULL REFERENCES users(user_id)," +
            "  name             TEXT NOT NULL," +
            "  start_date       TEXT NOT NULL," +
            "  end_date         TEXT NOT NULL," +
            "  destination      TEXT," +
            "  description      TEXT," +
            "  cover_photo_path TEXT," +
            "  created_at       INTEGER" +
            ");";

    // -------------------------------------------------------------------------
    // Table: memory_trip_mapping
    // -------------------------------------------------------------------------
    private static final String CREATE_TABLE_MEMORY_TRIP_MAPPING =
            "CREATE TABLE memory_trip_mapping (" +
            "  mapping_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  memory_id  INTEGER NOT NULL REFERENCES memories(memory_id)," +
            "  trip_id    INTEGER NOT NULL REFERENCES trips(trip_id)," +
            "  UNIQUE(memory_id, trip_id)" +
            ");";

    // -------------------------------------------------------------------------
    // Table: reviews
    // -------------------------------------------------------------------------
    private static final String CREATE_TABLE_REVIEWS =
            "CREATE TABLE reviews (" +
            "  review_id   INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id     INTEGER NOT NULL REFERENCES users(user_id)," +
            "  place_id    INTEGER NOT NULL REFERENCES places(place_id)," +
            "  rating      INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5)," +
            "  review_text TEXT," +
            "  created_at  INTEGER," +
            "  updated_at  INTEGER" +
            ");";

    // -------------------------------------------------------------------------
    // Table: dream_destinations
    // -------------------------------------------------------------------------
    private static final String CREATE_TABLE_DREAM_DESTINATIONS =
            "CREATE TABLE dream_destinations (" +
            "  dream_id         INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id          INTEGER NOT NULL REFERENCES users(user_id)," +
            "  place_name       TEXT NOT NULL," +
            "  status           TEXT NOT NULL DEFAULT 'WISHLIST'," +
            "  expected_date    TEXT," +
            "  cover_image_path TEXT," +
            "  notes            TEXT," +
            "  budget_estimate  REAL," +
            "  sort_order       INTEGER DEFAULT 0," +
            "  created_at       INTEGER" +
            ");";

    // -------------------------------------------------------------------------
    // Table: time_capsules
    // -------------------------------------------------------------------------
    private static final String CREATE_TABLE_TIME_CAPSULES =
            "CREATE TABLE time_capsules (" +
            "  capsule_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  memory_id  INTEGER NOT NULL REFERENCES memories(memory_id)," +
            "  user_id    INTEGER NOT NULL REFERENCES users(user_id)," +
            "  open_date  TEXT NOT NULL," +
            "  message    TEXT," +
            "  is_opened  INTEGER DEFAULT 0," +
            "  created_at INTEGER," +
            "  opened_at  INTEGER" +
            ");";

    // -------------------------------------------------------------------------
    // Table: friends
    // -------------------------------------------------------------------------
    private static final String CREATE_TABLE_FRIENDS =
            "CREATE TABLE friends (" +
            "  friendship_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id       INTEGER NOT NULL REFERENCES users(user_id)," +
            "  friend_id     INTEGER NOT NULL REFERENCES users(user_id)," +
            "  status        TEXT NOT NULL DEFAULT 'PENDING'," +
            "  created_at    INTEGER," +
            "  UNIQUE(user_id, friend_id)" +
            ");";

    // -------------------------------------------------------------------------
    // Indexes
    // -------------------------------------------------------------------------
    private static final String[] CREATE_INDEXES = {
            "CREATE INDEX idx_memories_user_date ON memories(user_id, date);",
            "CREATE INDEX idx_memories_place     ON memories(place_id);",
            "CREATE INDEX idx_trips_user         ON trips(user_id, start_date);",
            "CREATE INDEX idx_reviews_place      ON reviews(place_id, created_at);",
            "CREATE INDEX idx_dream_user         ON dream_destinations(user_id, status);",
            "CREATE INDEX idx_capsules_user_date ON time_capsules(user_id, open_date);",
            "CREATE INDEX idx_friends_user       ON friends(user_id, status);"
    };

    // -------------------------------------------------------------------------
    // Drop statements (used in onUpgrade)
    // -------------------------------------------------------------------------
    private static final String[] DROP_TABLES = {
            "DROP TABLE IF EXISTS friends;",
            "DROP TABLE IF EXISTS time_capsules;",
            "DROP TABLE IF EXISTS dream_destinations;",
            "DROP TABLE IF EXISTS reviews;",
            "DROP TABLE IF EXISTS memory_trip_mapping;",
            "DROP TABLE IF EXISTS trips;",
            "DROP TABLE IF EXISTS memories;",
            "DROP TABLE IF EXISTS places;",
            "DROP TABLE IF EXISTS users;"
    };

    public MemorivaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PLACES);
        db.execSQL(CREATE_TABLE_MEMORIES);
        db.execSQL(CREATE_TABLE_TRIPS);
        db.execSQL(CREATE_TABLE_MEMORY_TRIP_MAPPING);
        db.execSQL(CREATE_TABLE_REVIEWS);
        db.execSQL(CREATE_TABLE_DREAM_DESTINATIONS);
        db.execSQL(CREATE_TABLE_TIME_CAPSULES);
        db.execSQL(CREATE_TABLE_FRIENDS);

        for (String indexSql : CREATE_INDEXES) {
            db.execSQL(indexSql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables in reverse dependency order, then recreate
        for (String dropSql : DROP_TABLES) {
            db.execSQL(dropSql);
        }
        onCreate(db);
    }
}
