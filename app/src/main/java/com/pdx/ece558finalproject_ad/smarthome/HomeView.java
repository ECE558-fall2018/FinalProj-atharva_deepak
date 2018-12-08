/**
 * not used
 */
package com.pdx.ece558finalproject_ad.smarthome;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeView extends Fragment {

    private static final String TAG = HomeView.class.getSimpleName();

    public HomeView() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_view, container, false);
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        this.getContext().getApplicationContext().registerReceiver(wifiStateReceiver, intentFilter);

        return view;
    }

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

    private void updateAP() {
        NetworkInformation apInfo = new NetworkInformation();
        apInfo = getAPInfo(this.getContext().getApplicationContext());
        Log.d(TAG, "Currently connected to " + apInfo.getSSID());
        Log.d(TAG, "Currently connected to MAC " + apInfo.getMAC());
        Log.d(TAG, "Currently signal strength of Wifi is " + apInfo.getRSSI());

        //DatabaseReference userDB = mFirebaseHelper.getDatabaseChildRef(mUserId.toString());

        //userDB.child("SSID").setValue(apInfo.getSSID());
        //userDB.child("MAC").setValue(apInfo.getMAC());
        //userDB.child("RSSI").setValue(apInfo.getRSSI());
    }

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

}
