package com.pdx.ece558finalproject_ad.smarthome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText mUserId;
    private EditText mPassword;
    private  Button mSignin;
    private Button mRegister;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsers;
    private FirebaseHelper mFirebaseHelper = new FirebaseHelper();
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabase = mFirebaseDatabase.getInstance();
        mUsers = FirebaseDatabase.getInstance().getReference();

        mUserId = findViewById(R.id.etUserId);
        mPassword = findViewById(R.id.etPassword);
        mSignin = findViewById(R.id.btnSignin);
        mRegister = findViewById(R.id.btnRegister);

        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignIn(mUserId.getText().toString(),
                        mPassword.getText().toString()
                        );
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRegistration(mUserId.getText().toString(),
                               mPassword.getText().toString()
                               );
            }
        });

    }

    private void doSignIn(final String userId, final String password) {
        if(userId == null || password == null){
            displayError();
        }else{
            mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(userId).exists()){
                        if(!userId.isEmpty()){
                            User login = dataSnapshot.child(userId).getValue(User.class);
                            if(login.getPassWord().equals(password)){
                                Toast.makeText(MainActivity.this,"Login Success", Toast.LENGTH_SHORT).show();

                                NetworkInformation apInfo = new NetworkInformation();
                                apInfo = getAPInfo(getApplicationContext().getApplicationContext());
                                login.setNetworkInfo(apInfo);

                                mUsers.child(login.getUserId()).setValue(login);

                                Intent intent = new Intent(getApplicationContext(), SmartHomeActivity.class);
                                intent.putExtra("userId", userId);
                                startActivity(intent);

                            }else{
                                Toast.makeText(MainActivity.this, "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(MainActivity.this,"No such user exisits", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void displayError() {

    }

    private void doRegistration(String userId, String passWord) {

        int result = 0;
        Intent intent = new Intent(getApplicationContext().getApplicationContext(), RegistrationActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("password", passWord);
        startActivityForResult(intent, result);
    }

    @Override
    protected void onStart(){
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiStateReceiver);
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            switch (wifiStateExtra){
                case WifiManager.WIFI_STATE_ENABLED :
                    //TODO
                    Log.d(TAG, "WiFi is enabled");
                    updateAP();
                    break;
                case WifiManager.WIFI_STATE_DISABLED :
                    //TODO
                    Log.d(TAG,"WiFi is off");
                    break;
            }
        }
    };

    private void updateAP() {
        NetworkInformation apInfo = new NetworkInformation();
        apInfo = getAPInfo(getApplicationContext().getApplicationContext());
        Log.d(TAG, "Currently connected to " + apInfo.getSSID());
        Log.d(TAG, "Currently connected to MAC " + apInfo.getMAC());
        Log.d(TAG, "Currently signal strength of Wifi is " + apInfo.getRSSI());

    }

    public static NetworkInformation getAPInfo(Context context) {
        String ssid = null;
        String macAddr = null;
        int RSSI = 0;
        NetworkInformation apInfo = new NetworkInformation();
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                apInfo.setSSID(connectionInfo.getSSID());
                apInfo.setMAC(connectionInfo.getBSSID());
                apInfo.setRSSI(connectionInfo.getRssi());
            }
        }
        return apInfo;
    }

    //This is the reentry point for this activity when the called activity finishes
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String userID = data.getStringExtra("userId");
    }
}
