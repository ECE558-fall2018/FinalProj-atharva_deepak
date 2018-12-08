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

        fragmentA = new FragmentA();
        fragmentB = new FragmentB();

        override_switch = (Switch) findViewById(R.id.override);
        Temperature = (TextView) findViewById(R.id.tempview);
        IntruderStatus = (TextView) findViewById(R.id.intruderStatus);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_a, fragmentA)
                .replace(R.id.container_b, fragmentB)
                .commit();

        override_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fragmentA.updateOverride(isChecked);
                fragmentB.updateOverride(isChecked);

                if(isChecked)
                    fbHelper.writeToDataBase("OVERRIDE", "1");
                else
                    fbHelper.writeToDataBase("OVERRIDE", "0");
            }
        });

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

    @Override
    public void onInputASent(String progress, String childname) {
        Log.d(TAG,"Room 1 Data - "+childname+"  "+"Status - "+progress );

        fbHelper.writeToDataBase(childname, progress);
        //fragmentB.updateEditText(input);
    }

    @Override
    public void onInputBSent(String progress, String childname) {
        Log.d(TAG,"Room 2 Data - "+childname+"  "+"Status - "+progress );
        fbHelper.writeToDataBase(childname, progress);
        //fragmentA.updateEditText(input);
    }

    private void updateUI(String nodId, String data) {
        doNotUpdate = true;
        try {
            switch (nodId) {
                case "PWM1":
                    fragmentA.updateStatus("PWM1", data);
                    break;
                case "PWM2":
                    fragmentA.updateStatus("PWM2", data);
                    break;
                case "PWM3":
                    fragmentB.updateStatus("PWM3", data);
                    break;
                case "PWM4":
                    fragmentB.updateStatus("PWM4", data);
                    break;

                case "TEMPERATURE":
                    Temperature.setText(data + "C");
                    break;

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
                    break;

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