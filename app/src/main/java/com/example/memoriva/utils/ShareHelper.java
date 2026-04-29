package com.example.memoriva.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * ShareHelper provides simple static methods for sharing memory content
 * via the system share sheet, email, or SMS.
 */
public class ShareHelper {

    /**
     * Share a memory as plain text using the system share sheet.
     */
    public static void shareMemory(Context context, String title, String date, String notes) {
        String text = "📸 " + title
                + "\n📅 " + date
                + (notes != null && !notes.isEmpty() ? "\n" + notes : "")
                + "\n\nShared from Memoriva";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "Share Memory via"));
    }

    /**
     * Share a memory via the device's email app.
     */
    public static void shareViaEmail(Context context, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share a memory via the device's SMS app.
     */
    public static void shareViaSMS(Context context, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:"));
        intent.putExtra("sms_body", message);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No SMS app found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Generic text share using the system share sheet.
     */
    public static void shareText(Context context, String chooserTitle, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, chooserTitle));
    }
}
