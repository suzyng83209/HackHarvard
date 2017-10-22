package com.tash_had.android.surveillanceShotCamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by tash-had on 2017-10-21.
 */

class ProcessImageDetails {
    private HashMap<String, String> imageDetailsMap = new HashMap<>();
    private Context context;

    String timeStamp;
    Bitmap bitmap;
    private String lon;
    private String lat;

    public ProcessImageDetails(){
        super();
        context = GlobalVariables.cameraActivity.getApplicationContext();
        setLatLon();
    }

    void setImageData(){
        imageDetailsMap.put("timestamp", timeStamp);
        imageDetailsMap.put("lat", lat);
        imageDetailsMap.put("lon", lon);

        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, 360, 480, false);
        try{
            new EncodeImage().execute(this.bitmap);
        }catch (OutOfMemoryError e){
            e.printStackTrace();
        }
    }

    static void renderImage(Image mImage){
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {

                output = new FileOutputStream(new File("/storage/emulated/0/Android/data/com.tash_had.android.surveillanceShotCamera/files/zzzz.jpg"));
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }
    private void setLatLon() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        this.lat = Double.toString(location.getLatitude());
        this.lon = Double.toString(location.getLongitude());
    }

    class EncodeImage extends AsyncTask<Bitmap, Integer, String>{
        String encodedImage;
        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap btmap = bitmaps[0];
            btmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            btmap.recycle();

            byte[] imageAsByteArray = baos.toByteArray();
            encodedImage = Base64.encodeToString(imageAsByteArray, Base64.NO_WRAP);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            imageDetailsMap.put("encoded_image", encodedImage);

            if (bitmap != null){
                bitmap.recycle();
                bitmap = null;
            }
            try {
                SendPhotoToServer.sendPhoto(imageDetailsMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
