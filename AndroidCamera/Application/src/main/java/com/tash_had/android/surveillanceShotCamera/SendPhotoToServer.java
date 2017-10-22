package com.tash_had.android.surveillanceShotCamera;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by tash-had on 2017-10-21.
 */

public class SendPhotoToServer {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static OkHttpClient client;

    static void sendPhoto(HashMap<String, String> photoDetailsMap) throws IOException{
        client = new OkHttpClient();
        String URL = "https://ia8s1k2mhd.execute-api.us-west-2.amazonaws.com/dev/detect";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", GlobalVariables.uuid);
            for (String key : photoDetailsMap.keySet()){
                jsonObject.put(key, photoDetailsMap.get(key));
                Log.w("JSON_OBJECT_TAG", key + " " + photoDetailsMap.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        new makePostRequest().execute(URL, jsonObject.toString());
        handleServerResponse(true);
        Log.w("JSON_TAG", jsonObject.toString());
    }

    static void handleServerResponse(boolean dangerDetected){
        if (dangerDetected){
            View view = GlobalVariables.bottomBarView;
            view.setBackgroundColor(Color.RED);
        }else {
            GlobalVariables.bottomBarView.setBackgroundColor(Color.BLACK);
        }
//        Toast.makeText(GlobalVariables.cameraActivity, Boolean.toString(dangerDetected), Toast.LENGTH_SHORT).show();

    }
    private static class makePostRequest extends AsyncTask<String, Integer, Response>{

        @Override
        protected Response doInBackground(String... strings) {
            // Create request body
            okhttp3.RequestBody  body = RequestBody.create(JSON, strings[1]);
            // Prepare request
            okhttp3.Request request = new Request.Builder()
                    .url(strings[0])
                    .post(body)
                    .build();
            // Send request
            okhttp3.Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            if (response != null){
                try {
                    String responseString = response.body().string();
                    if (responseString.equals("true")){
                        handleServerResponse(true);
                    }else if (responseString.equals("false")){
                        handleServerResponse(false);
                    }else{
                        Toast.makeText(GlobalVariables.cameraActivity, "Network Error.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else{
                Toast.makeText(GlobalVariables.cameraActivity, "Network Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
