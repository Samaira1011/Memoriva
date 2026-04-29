package com.example.memoriva.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for photo import, thumbnail loading, and file management.
 */
public class PhotoImporter {

    /**
     * Queries MediaStore for all images on the device and returns a list of file paths.
     */
    public static List<String> getGalleryPhotos(Context context) {
        List<String> paths = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder)) {
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                while (cursor.moveToNext()) {
                    String path = cursor.getString(columnIndex);
                    if (path != null && new File(path).exists()) {
                        paths.add(path);
                    }
                }
            }
        } catch (Exception e) {
            // Return empty list on failure
        }
        return paths;
    }

    /**
     * Loads and scales a bitmap from the given file path to fit within maxWidth.
     */
    public static Bitmap loadThumbnail(Context context, String path, int maxWidth) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int inSampleSize = 1;
        if (options.outWidth > maxWidth) {
            inSampleSize = Math.round((float) options.outWidth / (float) maxWidth);
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Saves a bitmap to the app's Pictures directory and returns the file path.
     * Returns null on failure.
     */
    public static String savePhotoToAppDir(Context context, Bitmap bitmap) {
        File picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) return null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File imageFile = new File(picturesDir, "MEMORIVA_" + timeStamp + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Creates an empty image file in the app's Pictures directory for camera capture.
     * Returns the absolute file path, or null on failure.
     */
    public static String createImageFile(Context context) {
        File picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) return null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File imageFile = new File(picturesDir, "MEMORIVA_" + timeStamp + ".jpg");

        try {
            if (imageFile.createNewFile()) {
                return imageFile.getAbsolutePath();
            }
        } catch (IOException e) {
            return null;
        }
        return imageFile.getAbsolutePath();
    }

    /**
     * Builds a JSON array string from a list of photo file paths.
     */
    public static String buildPhotoPathsJson(List<String> paths) {
        JSONArray array = new JSONArray();
        for (String path : paths) {
            array.put(path);
        }
        return array.toString();
    }
}
