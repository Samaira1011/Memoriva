package com.example.memoriva.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for geocoding and reverse geocoding using Android's built-in Geocoder.
 */
public class LocationService {

    /**
     * Forward geocoding: searches for places matching the query string.
     * Returns a list of Address objects (up to 5 results).
     * Returns an empty list if network is unavailable or geocoding fails.
     */
    public static List<Address> searchPlaces(Context context, String query) {
        List<Address> results = new ArrayList<>();
        if (!isNetworkAvailable(context)) return results;
        if (query == null || query.trim().isEmpty()) return results;

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(query.trim(), 5);
            if (addresses != null) {
                results.addAll(addresses);
            }
        } catch (IOException e) {
            // Network or geocoding error - return empty list
        }
        return results;
    }

    /**
     * Reverse geocoding: converts coordinates to a human-readable place name.
     * Returns the formatted address string, or an empty string on failure.
     */
    public static String getPlaceName(Context context, double lat, double lng) {
        if (!isNetworkAvailable(context)) return "";

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return formatAddress(addresses.get(0));
            }
        } catch (IOException e) {
            // Network or geocoding error
        }
        return "";
    }

    /**
     * Formats an Address object into a readable string.
     * Includes locality (city), admin area (state/province), and country name.
     */
    public static String formatAddress(Address address) {
        if (address == null) return "";

        StringBuilder sb = new StringBuilder();

        // Try to build: "City, State, Country" or fallback to address lines
        String locality = address.getLocality();
        String adminArea = address.getAdminArea();
        String country = address.getCountryName();

        if (locality != null && !locality.isEmpty()) {
            sb.append(locality);
        }
        if (adminArea != null && !adminArea.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(adminArea);
        }
        if (country != null && !country.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }

        // Fallback to address lines if nothing was built
        if (sb.length() == 0) {
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                String line = address.getAddressLine(i);
                if (line != null && !line.isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(line);
                }
            }
        }

        return sb.toString();
    }

    /**
     * Checks whether a network connection is currently available.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
