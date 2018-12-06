package com.pdx.ece558finalproject_ad.smarthome;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private TextView mUserId;
    private TextView mPassword;
    private TextView mEmail;
    private Button mRegister;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsers;
    private static final String TAG = RegistrationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mFirebaseDatabase = mFirebaseDatabase.getInstance();
        mUsers = FirebaseDatabase.getInstance().getReference();

        mUserId = findViewById(R.id.etUserId);
        mPassword = findViewById(R.id.etPassword);
        mEmail = findViewById(R.id.etEmail);
        mRegister = findViewById(R.id.btnRegister);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean allOK = checkUserId(mUserId.getText().toString());

                if(allOK) {
                    doRegistration(mUserId.getText().toString(),
                            mPassword.getText().toString(),
                            mEmail.getText().toString());
                }
                else
                {
                    Toast.makeText(RegistrationActivity.this,"Illegal character.. please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean checkUserId(String userId) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(userId);
        boolean b = m.find();

        if(b) {
            Log.d(TAG, "There is a special character in userId");
            return false;
        }

        return true;

    }

    private void doRegistration(final String userId, final String password, final String email) {

        if(userId == null || password == null || email == null){
            displayError();
        }

        NetworkInformation apInfo = new NetworkInformation();
        apInfo = MainActivity.getAPInfo(getApplicationContext().getApplicationContext());
        final User user = new User(userId, password);
        user.setEmail(email);
        user.setSSID(apInfo.getSSID());
        user.setRSSI(apInfo.getRSSI());
        user.setMAC(apInfo.getMAC());

        mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(user.getUserId()).exists()) {
                    Toast.makeText(RegistrationActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                }else{
                    mUsers.child(userId).setValue(user);
                    Toast.makeText(RegistrationActivity.this, "User Registered successfully", Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent();
                    myIntent.putExtra("userId", userId);
                    setResult(RESULT_OK, myIntent);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayError() {
        Toast.makeText(RegistrationActivity.this, "All fields required. Please retry", Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent();
        myIntent.putExtra("userId", "Not Registered");
        setResult(RESULT_CANCELED, myIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent();
        setResult(RESULT_OK, myIntent);
        finish();
    }
}
