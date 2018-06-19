package com.example.mahe.sos;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class LocationListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "LocationListActivity";
    ListView lv;
    CustomLocationListAdapter locationListAdapter;
    ArrayList<String> firebaseLocationData_ids=new ArrayList<>();

    //firebase variable
    ChildEventListener childEventListener;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<FirebaseLocationData> locationDataArrayList= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        lv=findViewById(R.id.location_list_view);
        lv.setOnItemClickListener(this);

        locationListAdapter=new CustomLocationListAdapter(this,locationDataArrayList);
        lv.setAdapter(locationListAdapter);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Locations");

        childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.child("time").exists())
                {

                    FirebaseLocationData fld = dataSnapshot.getValue(FirebaseLocationData.class);
                    if(fld.isPrivacy())
                        return;
                    locationDataArrayList.add(fld);
                    firebaseLocationData_ids.add(dataSnapshot.getKey());
                    locationListAdapter.notifyDataSetChanged();
//                    Toast.makeText(LocationListActivity.this, dataSnapshot.child("email").getValue(String.class)+"data added", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(LocationListActivity.this, fld.getEmail()+" data added", Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                FirebaseLocationData fld = dataSnapshot.getValue(FirebaseLocationData.class);
                int pos= firebaseLocationData_ids.indexOf(dataSnapshot.getKey());
                locationDataArrayList.remove(pos);
                locationDataArrayList.add(pos,fld);
                locationListAdapter.notifyDataSetChanged();
//                Toast.makeText(LocationListActivity.this, fld.getEmail()+" data changed", Toast.LENGTH_SHORT).show();




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

        myRef.addChildEventListener(childEventListener);



    }


    @Override
    protected void onDestroy() {
        if (childEventListener != null) {
            myRef.removeEventListener(childEventListener);
            Log.d(TAG, "onDestroy: ChildEventListener Removed");
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FirebaseLocationData mfld= (FirebaseLocationData) locationListAdapter.getItem(position);
        Intent i =new Intent(this,MapsActivity.class);
        Bundle b = new Bundle();
        b.putDouble("lat", mfld.getLatitude());
        b.putDouble("long", mfld.getLongitude());
        b.putString("name", mfld.getEmail().split("@")[0]);
        b.putString("time",mfld.getSos_time());
        i.putExtras(b);
        startActivity(i);




    }
}
