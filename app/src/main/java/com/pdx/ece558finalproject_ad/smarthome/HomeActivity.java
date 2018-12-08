/**
 * @author - Atharva MAhindrakar
 */

package com.pdx.ece558finalproject_ad.smarthome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;


public class HomeActivity extends AppCompatActivity implements FragmentA.FragmentAListener, FragmentB.FragmentBListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private DatabaseReference databaseRef;
    private FirebaseHelper fbHelper = new FirebaseHelper();
    private boolean doNotUpdate = false;

    private FragmentA fragmentA;
    private FragmentB fragmentB;
    private Switch override_switch;

    private TextView Temperature, IntruderStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        databaseRef  = fbHelper.getDatabaseRef();        // Firebase database reference is set

        // defining instances of fragments to send
        // data to them
        fragmentA = new FragmentA();
        fragmentB = new FragmentB();

        override_switch = (Switch) findViewById(R.id.override);
        Temperature = (TextView) findViewById(R.id.tempview);

        // fragment UI is set in this activities layout.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_a, fragmentA)
                .replace(R.id.container_b, fragmentB)
                .commit();

        /**
         * onclick listener for Override switch
         * this function sends status of this switch back to
         * each fragment to change UI elements
         */

        override_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fragmentA.updateOverride(isChecked);
                fragmentB.updateOverride(isChecked);
                // override data is updated to firebase database
                if(isChecked)
                    fbHelper.writeToDataBase("OVERRIDE", "1");
                else
                    fbHelper.writeToDataBase("OVERRIDE", "0");
            }
        });


        /**
         * This is a Value Event Listener for the database. This gets called
         * whenever there is change in one of child value is noticed. Functions
         * are assigned to each child reference's event.
         */

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren() && !doNotUpdate){
                    Log.d(TAG, "Reading updated DB values");
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    try{
                        for(int counter = 0; iter.hasNext(); counter++) {
                            DataSnapshot snap = iter.next();
                            String nodId = snap.getKey();
                            String value = snap.getValue().toString();
                            // function call to update UI
                            updateUI(nodId, value);
                        }
                    }catch(NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    /**
     * implementation of onInputASent method from
     * FragmentA's Fragment listener
     * @param progress
     * @param childname
     */

    @Override
    public void onInputASent(String progress, String childname) {
        Log.d(TAG,"Room 1 Data - "+childname+"  "+"Status - "+progress );

        fbHelper.writeToDataBase(childname, progress);
        //fragmentB.updateEditText(input);
    }

    /**
     * implementation of onInputBSent method from
     * FragmentB's Fragment listener
     * @param progress
     * @param childname
     */

    @Override
    public void onInputBSent(String progress, String childname) {
        Log.d(TAG,"Room 2 Data - "+childname+"  "+"Status - "+progress );
        fbHelper.writeToDataBase(childname, progress);
        //fragmentA.updateEditText(input);
    }


    /**
     * when our system is in automation mode
     * we are displaying the current statuses of the
     * peripherals. this function helps in getting
     * data at each node when there is change noticed.
     * @param nodId
     * @param data
     */

    private void updateUI(String nodId, String data) {
        doNotUpdate = true;
        try {
            switch (nodId) {
                case "PWM1":
                    // sending PWM1 status i.e. LED1 status back to
                    // fragment 1
                    fragmentA.updateStatus("PWM1", data);
                    break;
                case "PWM2":
                    // sending PWM1 status i.e. LED1 status back to
                    // fragment 1
                    fragmentA.updateStatus("PWM2", data);
                    break;
                case "PWM3":
                    // sending PWM1 status i.e. LED1 status back to
                    // fragment 1
                    fragmentB.updateStatus("PWM3", data);
                    break;
                case "PWM4":
                    // sending PWM1 status i.e. LED1 status back to
                    // fragment 1
                    fragmentB.updateStatus("PWM4", data);
                    break;
                // displaying temperature in UI of this activity
                case "TEMPERATURE":
                    Temperature.setText(data + "C");
                    break;

                /*
                if intrision is detected, the massage is displyed on the screen

                 */
                case "INTRUSION":
                    if(data.equalsIgnoreCase("1")) {
                        Log.d(TAG, "intrusion detected");
                        IntruderStatus.setTextColor(getResources().getColor(R.color.colorAccent));
                        IntruderStatus.setText("INTRUDER DETECTED");
                    }
                    else{
                        Log.d(TAG, "no intrusion " + data);
                        IntruderStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                        IntruderStatus.setText("SAFE");

                    }

                default:
                    fbHelper.updateTimeStamp();
            }
            doNotUpdate = false;
        }catch (NullPointerException e){
            Log.d(TAG,"Error updating UI");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(HomeActivity.this,"Logging out", Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent();
        setResult(RESULT_OK, myIntent);
        finish();
    }
}