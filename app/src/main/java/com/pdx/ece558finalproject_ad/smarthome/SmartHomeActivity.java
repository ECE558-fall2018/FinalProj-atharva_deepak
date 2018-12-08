/**
 * NOT USED
 */

package com.pdx.ece558finalproject_ad.smarthome;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SmartHomeActivity extends FragmentActivity {

    public static FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_home);

        mFragmentManager = getSupportFragmentManager();

        if(findViewById(R.id.BaseFragment)!= null){

            if(savedInstanceState != null){
                return;
            }

            mFragmentManager.beginTransaction().add(R.id.BaseFragment, new HomeView(),null).commit();
        }

    }


}
