package com.tash_had.angelseye;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by tash-had on 2017-10-19.
 */

public class GeofenceTransitionsIntentService extends IntentService {
    static HashMap<String, Long> timeStampHashMap = new HashMap<>();

    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }
    public GeofenceTransitionsIntentService(){
        super("intentService");
    }
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e("ERROR_TAG", errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition title
            String[] geofenceTransitionInfo = getGeofenceTransitionInfo(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );


            // Send notification and log the transition details.
            makeNotification(geofenceTransitionInfo[0], geofenceTransitionInfo[1]);
        } else {
            // Log the error.
            Log.e("TRANSITION_ERROR_TAG", "geofence_transition_invalid_type");
        }
    }

    public void makeNotification(String title, String desc) {
        Bundle b = new Bundle();
        b.putString("Test", "test bundle");
        PugNotification.with(this)
                .load()
                .title(title)
                .message(desc)
                .bigTextStyle(desc)
                .color(R.color.red)
                .smallIcon(R.mipmap.ic_warning_black_36dp)
                .largeIcon(R.mipmap.ic_warning_black_36dp)
                .flags(Notification.DEFAULT_ALL)
                .click(GeofencingMain.class, b)
                .simple()
                .build();
    }

    private String[] getGeofenceTransitionInfo(GeofenceTransitionsIntentService geofenceTransitionsIntentService, int geofenceTransition, List triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

//            // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Object geofence : triggeringGeofences) {
            Geofence gf = (Geofence) geofence;
            triggeringGeofencesIdsList.add(gf.getRequestId());
        }
//            String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        ArrayList<Long> timestamps = new ArrayList<>();
        for (int i = 0; i < triggeringGeofences.size(); i++) {
            Geofence gf = (Geofence) triggeringGeofences.get(i);
            timestamps.add(timeStampHashMap.get(gf.getRequestId()));
        }
        Long mostRecentTimestamp = Collections.max(timestamps);
        String latestAlert = getDateFromTimeStamp(mostRecentTimestamp);

        String alert = geofenceTransitionString.substring(0, 1).toUpperCase() +
                geofenceTransitionString.substring(1, geofenceTransitionString.length()) + " Alert";

        // Get transition details
        String geofenceTransitionDesc = "Stay Safe - You've entered a Danger Zone. Alert Detected at " + latestAlert;
        if (alert.equals("Exit Alert")){
            geofenceTransitionDesc = "You have exited a Danger Zone";
        }
        String[] notificationInfo = {alert, geofenceTransitionDesc};
        return notificationInfo;

    }
    private String getDateFromTimeStamp(long timeStamp){
        java.sql.Timestamp ts = new java.sql.Timestamp(timeStamp);
        DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return df.format(ts);
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "enter";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exit";
            default:
                return "dwelling";
        }
    }
}
