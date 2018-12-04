package com.pdx.ece558finalproject_ad.smarthome;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FirebaseHelper {

    private DatabaseReference databaseRef;
    private DatabaseReference timeStampRef;
    private DatabaseReference dac1Ref;
    private DatabaseReference adc1Ref;
    private DatabaseReference adc2Ref;
    private DatabaseReference adc3Ref;
    private DatabaseReference adc4Ref;
    private DatabaseReference pwm1Ref;
    private DatabaseReference pwm2Ref;
    private DatabaseReference pwm3Ref;
    private DatabaseReference pwm4Ref;
    private DatabaseReference defaultRef;
    private boolean doNotUpdate = false;

    /**
     * This function returns the reference to the database
     * created on firebase.
     * @return databaseRef
     */

    public DatabaseReference getDatabaseRef(){
        databaseRef = FirebaseDatabase.getInstance().getReference();
        return databaseRef;
    }

    public FirebaseHelper() {
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseReference getDatabaseChildRef(DatabaseReference db, String childKey){
        return db;
    }

    /**
     * This function returns reference to the child / node
     * of the created database.
     * @param childKey
     * @return
     */

    public DatabaseReference getDatabaseChildRef(String childKey){

        defaultRef = databaseRef.child(childKey);
        return defaultRef;

    }

    /**
     * This function could be called to reset all values
     * nodes to 0.
     * @param Keys
     */

    public void setupDefaultDatabase(String[] Keys){
        doNotUpdate = true;
        databaseRef = FirebaseDatabase.getInstance().getReference();        // database reference to all child nodes gets saved into database Ref
        for(int counter = 0;counter < Keys.length; counter ++ ){
            databaseRef.setValue(Keys[counter]);
            DatabaseReference childRef = databaseRef.child(Keys[counter]);  // traversing to particular node
            childRef.setValue("0");                                         // reset value to 0
        }
        doNotUpdate = false;
    }

    /**
     * this function call updates the timestamp for
     * the latest time when our firebase databasegot updated
     */

    public void updateTimeStamp() {
        Date date = new Date();                                                     // date and time is received from system
        timeStampRef = databaseRef.child("TIMESTAMP");                              // getting node value for TIMESTAMP
        timeStampRef.setValue(date.toString());                                     // timestamp is updated to firebase
    }

    /**
     * This function is used to write the data back to firebase database.
     * @param selectedKey
     * @param data
     * This function gets child key and the data and that data is updated to
     * subsequent child.
     */

    public void writeToDataBase(String selectedKey, String data) {
        switch(selectedKey){
            case "ADC1INPUT":
                adc1Ref = databaseRef.child("ADC1INPUT");       // gets child reference to ADC1INPUT
                adc1Ref.setValue(data);                         // data is sent to child node ADC1INPUT
                break;
            case "ADC2INPUT":
                adc2Ref = databaseRef.child("ADC2INPUT");       // gets child reference to ADC2INPUT
                adc2Ref.setValue(data);                         // data is sent to child node ADC2INPUT
                break;
            case "ADC3INPUT":
                adc3Ref = databaseRef.child("ADC3INPUT");       // gets child reference to ADC3INPUT
                adc3Ref.setValue(data);                         // data is sent to child node ADC3INPUT
                break;
            case "ADC4INPUT":
                adc4Ref = databaseRef.child("TEMPERATURE");     // gets child reference to TEMPERATURE
                adc4Ref.setValue(data);                         // data is sent to child node TEMPERATURE
                break;
            case "PWM1":
                pwm1Ref = databaseRef.child("PWM1");            // gets child reference to PWM1
                pwm1Ref.setValue(data);                         // data is sent to child node PWM1
                break;
            case "PWM2":
                pwm2Ref = databaseRef.child("PWM2");            // gets child reference to PWM2
                pwm2Ref.setValue(data);                         // data is sent to child node PWM2
                break;
            case "PWM3":
                pwm3Ref = databaseRef.child("PWM3");            // gets child reference to PWM3
                pwm3Ref.setValue(data);                         // data is sent to child node PWM3
                break;
            case "PWM4":
                pwm4Ref = databaseRef.child("PWM4");            // gets child reference to PWM4
                pwm4Ref.setValue(data);                         // data is sent to child node PWM4
                break;
            case "TIMESTAMP":
                timeStampRef = databaseRef.child("TIMESTAMP");  // gets child reference to TIMESTAMP
                timeStampRef.setValue(data);                    // data is sent to child node TIMESTAMP
                break;
            case "DAC1OUT":
                dac1Ref = databaseRef.child("DAC1OUT");         // gets child reference to DAC1OUT
                dac1Ref.setValue(data);                         // data is sent to child node DAC1OUT
                break;
            default:
                defaultRef = databaseRef.child(selectedKey);
                defaultRef.setValue(data);
                break;
        }
    }
}
