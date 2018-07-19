package com.example.mahe.sos;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyService extends Service {
//    static {
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//    }
   DatabaseReference myRef,myUserRef;

    ChildEventListener childEventListener;
    NotificationManager nm;
    Notification n;
    private static final String TAG="Service Activity";
    Notification.Builder nb;
    String CHANNEL_ID = "my_sos_channel";
    int notification_request_code= 200;

    public MyService() {
    }

    @Override
    public void onCreate() {
        nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        CharSequence name = "SOS ALERT";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    name,
                    importance);
            nm.createNotificationChannel(channel);
        }

        myRef = FirebaseDatabase.getInstance().getReference("Locations");
        myUserRef = FirebaseDatabase.getInstance().getReference("Users");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildChanged: from service");
                final FirebaseLocationData fld = dataSnapshot.getValue(FirebaseLocationData.class);

                myUserRef.child(fld.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String sos_name=dataSnapshot.getValue(String.class);

                        nb= new Notification.Builder(MyService.this);
                        nb.setContentTitle("Emergency");
                        nb.setContentText("SOS broadcasted from "+sos_name);
                        nb.setSmallIcon(android.R.drawable.ic_dialog_alert);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            nb.setChannelId(CHANNEL_ID);
                        }
                        nb.setDefaults(Notification.DEFAULT_ALL);
                        Intent i =new Intent(MyService.this,MapsActivity.class);
                        Bundle b = new Bundle();
                        b.putDouble("lat", fld.getLatitude());
                        b.putDouble("long", fld.getLongitude());
                        b.putString("name", sos_name);
                        b.putString("time",fld.getSos_time());
                        i.putExtras(b);;
                        nb.setAutoCancel(false);
                        PendingIntent pi =PendingIntent.getActivity(MyService.this,notification_request_code,i,PendingIntent.FLAG_UPDATE_CURRENT);
                        nb.setContentIntent(pi);
                        n=nb.build();
                        nm.notify(notification_request_code,n);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MyService.this, "SOS APP service: database fetch cancelled", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };



        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (childEventListener != null)
            myRef.addChildEventListener(childEventListener);
//        Toast.makeText(this, "SOS background service started", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (childEventListener != null)
            myRef.removeEventListener(childEventListener);
//        Toast.makeText(this, "SOS backgroung service stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
