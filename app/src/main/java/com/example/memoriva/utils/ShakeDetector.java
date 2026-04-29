package com.example.memoriva.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * ShakeDetector listens to the accelerometer and fires onShake()
 * when the phone is shaken hard enough. Uses a 1-second debounce
 * so rapid shakes don't trigger multiple events.
 */
public class ShakeDetector implements SensorEventListener {

    public interface OnShakeListener {
        void onShake();
    }

    private static final float SHAKE_THRESHOLD = 15.0f; // m/s²
    private static final long DEBOUNCE_MS = 1000;       // 1 second between shakes

    private OnShakeListener listener;
    private long lastShakeTime = 0;

    /** No-arg constructor — call setOnShakeListener() before registering. */
    public ShakeDetector() {
    }

    /** Constructor that accepts the listener directly. */
    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Total acceleration magnitude
        double acceleration = Math.sqrt(x * x + y * y + z * z);

        if (acceleration > SHAKE_THRESHOLD) {
            long now = System.currentTimeMillis();
            if (now - lastShakeTime > DEBOUNCE_MS) {
                lastShakeTime = now;
                if (listener != null) {
                    listener.onShake();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
