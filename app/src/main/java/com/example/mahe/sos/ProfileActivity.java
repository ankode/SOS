package com.example.mahe.sos;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {
    EditText email,name,phone;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseuser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        email=findViewById(R.id.email_et);
        name=findViewById(R.id.name_et);
        phone=findViewById(R.id.phone_et);

        mFirebaseAuth= FirebaseAuth.getInstance();
        mFirebaseuser=mFirebaseAuth.getCurrentUser();
        email.setText(mFirebaseuser.getEmail());
        email.setEnabled(false);

        if(mFirebaseuser.getPhoneNumber()!=null)
            phone.setText(mFirebaseuser.getPhoneNumber());
        else
            phone.setText("91XXXXXXXX");
        phone.setEnabled(false);

        if(mFirebaseuser.getDisplayName()!=null)
            name.setText(mFirebaseuser.getDisplayName());
        else
            name.setText(mFirebaseuser.getEmail().split("@")[0]);




    }

    public void submitData(View view) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(String.valueOf(name.getText()))
                .build();

        mFirebaseuser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseDatabase.getInstance().getReference("Locations").child(mFirebaseuser.getUid()).child("name").setValue(name.getText().toString());
                            Toast.makeText(ProfileActivity.this, "Name Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }
}
