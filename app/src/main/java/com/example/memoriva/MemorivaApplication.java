package com.example.memoriva;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class MemorivaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
