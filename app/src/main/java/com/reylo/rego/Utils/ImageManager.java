package com.reylo.rego.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageManager {

    private static final String TAG = "ImageManager";

    // use url to return bitmap
    public static Bitmap getBitmapFromUrl(String src) {

        try {

            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            connection.disconnect();
            return bitmap;

        } catch (IOException e) {

            Log.e(TAG, "getBitmapFromUrl: FileNotFoundException: " + e.getMessage());
            return null;

        }

    }

    // get byes from bitmap
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        bitmap.recycle();

        return bytes;

    }

}
