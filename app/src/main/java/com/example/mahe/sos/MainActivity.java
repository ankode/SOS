package com.example.mahe.sos;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.flags.impl.DataUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String ANONYMOUS = "anonymous";
    GoogleApiClient gac;
    LocationRequest locationRequest;
    private static final int UPDATE_INTERVAL = 15 * 1000;
    private static final int FASTEST_UPDATE_INTERVAL = 2 * 1000;
    private static final int notification_request_code = 100;
    FusedLocationProviderClient mFusedLocationProviderClient;
    Location lastLocation,lastLocationGpsProvider;
    TextView tv;


    //Firebase Variables
    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseuser;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private String mUsername;
    private String mPhotoUrl;
    private ChildEventListener childEventListener;
    private String mEmail;
    private String mUid;

    //Notification
    NotificationManager nm;
    Notification n;
    Notification.Builder nb;

    private static final String TAG = "MainActivity";

    @Override
    protected void onStart() {
        gac.connect();
        if (childEventListener != null) {
            myRef.addChildEventListener(childEventListener);
            Log.d(TAG, "onStart: ChildEventListener Attached");
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        gac.disconnect();
        if (childEventListener != null) {
            myRef.removeEventListener(childEventListener);
            Log.d(TAG, "onStop: ChildEventListener Removed");
        }
        super.onStop();
    }







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=findViewById(R.id.textView);
        nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        /* firebase initialization */
        mUsername = ANONYMOUS;
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mFirebaseAuth=FirebaseAuth.getInstance();
        mFirebaseuser=mFirebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Locations");
        myRef.keepSynced(true);

        if(mFirebaseuser==null)
        {
            startActivity(new Intent(this,LoginActivity.class));
            finish();
            return;
        }
        else {

            mEmail=mFirebaseuser.getEmail();
            if(!TextUtils.isEmpty(mFirebaseuser.getDisplayName()))
                mUsername=mFirebaseuser.getDisplayName();
            else
                mUsername=mEmail.split("@")[0];
            mUid=mFirebaseuser.getUid();
            if(mFirebaseuser.getPhotoUrl()!=null)
                mPhotoUrl=mFirebaseuser.getPhotoUrl().toString();
            Toast.makeText(this, "Welcome "+mUsername+"\n"+mUid, Toast.LENGTH_SHORT).show();

        }

         childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                FirebaseLocationData fld = dataSnapshot.getValue(FirebaseLocationData.class);
                Toast.makeText(MainActivity.this, fld.getEmail()+" added", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                FirebaseLocationData fld = dataSnapshot.getValue(FirebaseLocationData.class);
                String locKey = dataSnapshot.getKey();
                nb= new Notification.Builder(MainActivity.this);
                nb.setContentTitle("Emergency");
                nb.setContentText("SOS broadcasted from "+fld.email.split("@")[0]);
                nb.setSmallIcon(android.R.drawable.ic_dialog_alert);
                nb.setDefaults(Notification.DEFAULT_ALL);
                Intent i =new Intent(MainActivity.this,MainActivity.class);
                nb.setAutoCancel(false);
                PendingIntent pi =PendingIntent.getActivity(MainActivity.this,notification_request_code,i,PendingIntent.FLAG_UPDATE_CURRENT);
                nb.setContentIntent(pi);
                n=nb.build();
                nm.notify(notification_request_code,n);
//                Toast.makeText(MainActivity.this,fld.getEmail()+" changed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                String locKey = dataSnapshot.getKey();


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                FirebaseLocationData fld = dataSnapshot.getValue(FirebaseLocationData.class);
                String locKey = dataSnapshot.getKey();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, ":onCancelled", databaseError.toException());
            }
        };
        if(childEventListener!=null)
            Log.d(TAG, "onCreate: childEventListenerCreated");



        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);



        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        gac = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.firebase_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.profile:
                startActivity(new Intent(MainActivity.this,ProfileActivity.class));

                return true;
            case R.id.sign_out:
                mFirebaseAuth.signOut();
                mUsername=ANONYMOUS;
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
                return  true;
            case R.id.view_location:
                startActivity(new Intent(MainActivity.this,LocationListActivity.class));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void startSos(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
//        mFusedLocationProviderClient.getLastLocation()
//                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            lastLocation = task.getResult();
//
////                            txtLatitude.setText(String.valueOf(lastLocation.getLatitude()));
////                            txtLongitude.setText(String.valueOf(lastLocation.getLongitude()));
//
//                        } else {
//                            Log.w(TAG, "getLastLocation:exception", task.getException());
////                            showSnackbar(getString(R.string.no_location_detected));
//                        }
//                    }
//                });

        LocationManager lm= (LocationManager) getSystemService(LOCATION_SERVICE);
        lastLocationGpsProvider=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //write data to firebase
        updateUserLocationToFirebase(lastLocationGpsProvider);

        Toast.makeText(this, "GPS location without google client\n"+lm.getLastKnownLocation(LocationManager.GPS_PROVIDER).toString(), Toast.LENGTH_SHORT).show();



    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        else{
//            Toast.makeText(this, "Permission is given", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Permission is already given");
        }

        /* mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations, this can be null.

                        if (location != null) {
                            // Logic to handle location object
                            lastLocation=location;
                        }
                    }
                });
        */
        Location ll = LocationServices.FusedLocationApi.getLastLocation(gac);
        // TODO: use fusedlocaionproviderclient

        Log.d(TAG, "LastLocation from Deprecated: " + (ll == null ? "NO LastLocation" : ll.toString()));
        tv.setText("LastLocation from Deprecated: " + (ll == null ? "NO LastLocation" : ll.toString()));
//        Log.d(TAG, "LastLocation: " + (ll == null ? "NO LastLocation" : lastLocation.toString()));

        LocationServices.FusedLocationApi.requestLocationUpdates(gac, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this, "onConnectionFailed: \n" + connectionResult.toString(),
                Toast.LENGTH_LONG).show();
        Log.d(TAG, connectionResult.toString());

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            updateUI(location);
        }

    }
    private void updateUI(Location loc) {
        Log.d(TAG, "updateUI");
        tv.setText(Double.toString(loc.getLatitude()) + '\n' + Double.toString(loc.getLongitude()) + '\n' + DateFormat.getTimeInstance().format(loc.getTime()));
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }



    private void updateUserLocationToFirebase( Location location) {
        FirebaseLocationData fld= new FirebaseLocationData(mEmail,location.getLatitude() ,location.getLongitude(),DateFormat.getTimeInstance().format(location.getTime()),DateFormat.getTimeInstance().format(new Date()));
        myRef.child(mUid).setValue(fld);
    }


    public void firebaseSignOut(View view) {
        mFirebaseAuth.signOut();
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        finish();
    }
}
