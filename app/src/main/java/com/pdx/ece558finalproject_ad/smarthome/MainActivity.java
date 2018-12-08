package com.pdx.ece558finalproject_ad.smarthome;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText mUserId;
    private EditText mPassword;
    private  Button mSignin;
    private Button mRegister;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsers;
    private FirebaseHelper mFirebaseHelper = new FirebaseHelper();
    private static final String TAG = MainActivity.class.getSimpleName();
    private NetworkInformation apInfo = new NetworkInformation();
    private LocationManager mLocationManager;
    private android.location.LocationListener mLocationListener;
    private double mLatitude = 0;
    private double mLongitude = 0;
    private boolean loggedIn = false;
    private double homeLatitude = 0;
    private double homeLongitude = 0;
    private String homeAPSSID;
    private String homeAPMAC;
    private double homeAPRSSI;
    private DatabaseReference ref;
    private DatabaseReference occupiedRef;
    private DatabaseReference latitudeRef;
    private DatabaseReference longitudeRef;
    DatabaseReference userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabase = mFirebaseDatabase.getInstance();
        mUsers = FirebaseDatabase.getInstance().getReference();
        ref = FirebaseDatabase.getInstance().getReference();
        occupiedRef = ref.child("OCCUPIED");

        mUserId = findViewById(R.id.etUserId);
        mPassword = findViewById(R.id.etPassword);
        mSignin = findViewById(R.id.btnSignin);
        mRegister = findViewById(R.id.btnRegister);

        //Setting up Onclick event handler for the Signin Button in UI
        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Checking to see that the user input is free of illeagal characters
                boolean allOK = checkUserId(mUserId.getText().toString());

                if(allOK) {
                    //Invoke signin and pass along the string values from the corresponding textviews
                    doSignIn(mUserId.getText().toString(),
                            mPassword.getText().toString()
                    );
                }else{
                    Toast.makeText(MainActivity.this,"Illegal character.. please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Setting up Onclcik event handler for the Register Button in the UI
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRegistration(mUserId.getText().toString(),
                        mPassword.getText().toString()
                );
            }
        });
        //Subscribing to system notifications relating to LOCATION
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        //setting up a listener to catch the changes in location
        mLocationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, location.toString());
                //Checking to see if the user is logged in. We are saving the location info for each user and
                //if the user is not logged in, we need worry about updating the location
                if(loggedIn) {
                    //Checking to see if the users location has changed in a significant enough manner, if not, we will discard the change
                    if((Math.abs(mLatitude - location.getLatitude()) > 0.00002) || (Math.abs(mLongitude - location.getLongitude()) > 0.00002)){
                        Log.d(TAG, "Location Change Detected..Delta is:"
                                + (mLongitude - location.getLongitude()) + "and "
                                + (mLatitude - location.getLatitude()) +
                                "writing back to DB");
                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();

                        latitudeRef.setValue(mLatitude);
                        longitudeRef.setValue(mLongitude);

                    }else
                    {
                        Log.d(TAG, "Delta is:" + (mLongitude - location.getLongitude()) + "and " + (mLatitude - location.getLatitude()) + "Discarding Location Data.. no significant change in postion");
                    }

                    isOccupied();

                }else {
                    Log.d(TAG, "Not logged in.. discarding the write to DB");
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //if the application doesnt have sufficient permission to access location, it will then request permission from the User to approve its use
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]  {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        //Location manager is set to listen to GPS location updates and raise events in Location listener
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) mLocationListener);

    }

    /**
     * checkUserId - Method to check if the UserId contains illegal characters that
     * may break the firebase database
     * @param userId
     * @return
     */
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

    /**
     * doSignIn - Method to perform signin operation
     * @param userId
     * @param password
     */

    private void doSignIn(@NonNull final String userId,@NonNull final String password) {

        mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(userId).exists()){

                    Log.d(TAG, "Child exists for the user " + dataSnapshot.child(userId));

                    User login = (User) dataSnapshot.child(userId).getValue(User.class);

                    //Retreive Home's location and Network information from Firebase
                    homeLatitude = Double.valueOf(dataSnapshot.child("HOMELAT").getValue(String.class));
                    homeLongitude = Double.valueOf(dataSnapshot.child("HOMELNG").getValue(String.class));
                    homeAPMAC = dataSnapshot.child("MAC").getValue(String.class);
                    homeAPRSSI = Double.valueOf(dataSnapshot.child("RSSI").getValue(String.class));
                    homeAPSSID = dataSnapshot.child("SSID").getValue(String.class);

                    Log.d(TAG, "Read back info from DB; Userid " + login.getUserId() + " " + homeLatitude + " " + homeLongitude);

                    //check to see the user entered the correct password
                    if(login.getPassword().equals(password)){
                        Log.d(TAG, "Passwords match.. loging you in");

                        Toast.makeText(MainActivity.this,"Login Success", Toast.LENGTH_SHORT).show();

                        apInfo = getAPInfo(getApplicationContext().getApplicationContext());
                        //Update the User class with the relevant location information
                        login.setMAC(apInfo.getMAC());
                        login.setRSSI(apInfo.getRSSI());
                        login.setSSID(apInfo.getSSID());
                        login.setLatitude(mLatitude);
                        login.setLongitude(mLongitude);

                        //Write back the data into the Firebase database
                        mUsers.child(login.getUserId()).setValue(login);
                        //Database references used to write data (location/wifi) into User class
                        userDB = mFirebaseHelper.getDatabaseChildRef(mUserId.getText().toString());
                        latitudeRef = ref.child(mUserId.getText().toString()).child("latitude");
                        longitudeRef = ref.child(mUserId.getText().toString()).child("longitude");


                        //Globally track login
                        loggedIn = true;
                        //Call the UI for controlling the devices
                        Intent intent = new Intent(getApplicationContext().getApplicationContext(), HomeActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);

                    }else{
                        Toast.makeText(MainActivity.this, "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this,"No such user exisits", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Checking to see if the user is at home based on his current location and Home location
        isOccupied();

    }

    /**
     * IsOccupied - Method to write the Status "OCCUPIED" back into the firedb
     */
    private void isOccupied() {

        apInfo = getAPInfo(getApplicationContext().getApplicationContext());
        double latitudeDelta = Math.abs(homeLatitude - mLatitude);
        double longitudeDelta = Math.abs(homeLongitude - mLongitude);

        if((latitudeDelta < 0.00002) && (longitudeDelta < 0.00002) && (homeAPMAC.equals(apInfo.getMAC()))){
            occupiedRef.setValue("TRUE");
        }else{
            occupiedRef.setValue("FALSE");
        }
    }

    /**
     * doRegistration - Kicks off the Registration activity
     * @param userId
     * @param passWord
     */
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

    /**
     * Broadcast receiver to capture and raise events for WIFI state change notifications
     */
    public BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
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

    /**
     * updateAP - method to read information of the currently connected AP
     * and update the firebase entries which track them
     */
    private void updateAP() {
        NetworkInformation apInfo = new NetworkInformation();
        apInfo = getAPInfo(getApplicationContext().getApplicationContext());
        Log.d(TAG, "Currently connected to " + apInfo.getSSID());
        Log.d(TAG, "Currently connected to MAC " + apInfo.getMAC());
        Log.d(TAG, "Currently signal strength of Wifi is " + apInfo.getRSSI());
        if(loggedIn) {

            userDB.child("SSID").setValue(apInfo.getSSID());
            userDB.child("MAC").setValue(apInfo.getMAC());
            userDB.child("RSSI").setValue(apInfo.getRSSI());
        }

    }

    /**
     * getAPinfo - helper function to get the relevant Access point information
     * @param context
     * @return
     */
    public static NetworkInformation getAPInfo(Context context) {

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
        //String userID = data.getStringExtra("userId");
    }

}
