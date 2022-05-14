package com.emdad.travalerts.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;
import android.widget.Toast;

import com.emdad.travalerts.models.Notification;
import com.emdad.travalerts.models.Place;
import com.emdad.travalerts.utils.NotificationHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_NOTIFICATION;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_PLACE;

public class GeoFenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeoFenceBroadcastReceiv";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: GeoFence is triggered");
//        Toast.makeText(context, "GeoFence is triggered!", Toast.LENGTH_SHORT).show();


        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<String> placeIds = new ArrayList<>();
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : triggeringGeofences) {
            Log.i(TAG, "onReceive: triggeringGeofences: " + geofence.getRequestId());
            placeIds.add(geofence.getRequestId());
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        getPlaceInfoAndSendNotification(context, notificationHelper, transitionType, placeIds);

    }

    private void getPlaceInfoAndSendNotification(Context context, NotificationHelper notificationHelper, int transitionType, List<String> placeIds) {
        Log.d(TAG, "getPlaceInfo: placeIds: " + placeIds.toString());
        List<Place> placeList = new ArrayList<>();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(DB_PATH_PLACE)
                .whereIn(DB_FIELD_PLACE_ID, placeIds)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot d : list) {
                    Place p = d.toObject(Place.class);
                    p.setId(d.getId());
                    placeList.add(p);
                    Log.d(TAG, "getPlaceInfo: found geoFenced place info: " + p.toString());
                }

                for (Place place : placeList) {
                    sendNotification(context, notificationHelper, transitionType, place);
                }

            } else {
                Log.d(TAG, "getPlaceInfo: no place found in db with geoFence ids (place ids)");
            }
        });
        Log.d(TAG, "getPlaceInfo: found placeList: " + placeList.size());
    }

    private void sendNotification(Context context, NotificationHelper notificationHelper, int transitionType, Place place) {
        String body = "";
        String title = "";
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                title = "You entered into " + place.getName();
                body = String.format("You just entered the '%s'. How do you feel.", place.getName());
                Toast.makeText(context, body, Toast.LENGTH_LONG).show();
                notificationHelper.sendHighPriorityNotification(title, body, place);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                title = "You're in " + place.getName();
                body = String.format("You dwell in the '%s'. How do you feel.", place.getName());
                Toast.makeText(context, body, Toast.LENGTH_LONG).show();
                notificationHelper.sendHighPriorityNotification(title, body, place);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                title = "You've leave the place.";
                body = String.format("You just left the '%s'. How do you feel.", place.getName());
                Toast.makeText(context, body, Toast.LENGTH_LONG).show();
                notificationHelper.sendHighPriorityNotification(title, body, place);
                break;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        Notification notification = new Notification(UUID.randomUUID().toString(), place.getId(), mAuth.getUid(), title, body, place.getImage(), String.valueOf(Calendar.getInstance().getTimeInMillis()));
        saveNotificationToFirebase(notification);
    }

    private void saveNotificationToFirebase(Notification notification) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(DB_PATH_NOTIFICATION).add(notification).addOnSuccessListener(documentReference -> Log.d(TAG, "saveNotification: success. " + documentReference.getId()));
    }
}