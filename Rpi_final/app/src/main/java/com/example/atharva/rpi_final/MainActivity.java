/**
 * @author - Atharva Mahindrakar
 * This is an Android things application to controls peripherals like
 * LEDs and motors. There are two modes defined in this application. First when user has
 * kept Override switch off in android application, Raspberry Pi would be in automation mode,
 * controlling all the parameters based on LDR, Temperature and PIR sensors output.
 * When override is given, custom input given by user is given to these peripherals.
 * The FirebaseHelper class is developed to access Firebase database for this application.
 */


package com.example.atharva.rpi_final;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Iterator;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseReference databaseRef;
    private DatabaseReference childRef;
    private FirebaseHelper fbHelper = new FirebaseHelper();

    private static final String I2C_DEVICE_NAME = "I2C1";
    // I2C Slave Address
    private static final int I2C_ADDRESS = 0x08;

    private I2cDevice mDevice;
    private Gpio PIR1, PIR2;
    private boolean room1_status = false;
    private boolean room2_status = false;
    private byte LED1_brightness, LED2_brightness;
    private byte LED1_override, LED2_override;
    private byte motor1_speed, motor2_speed;
    private byte motor1_override, motor2_override;
    private Handler mHandler = new Handler();
    private int LDR1 = 0, LDR2 = 0;
    private int override = 0;
    private boolean geolocation = true;

    private boolean intrusion_detection = false;

    private double homelat =0, homelon = 0, lat = 0, lon = 0;
    private double Temperature;

    private boolean doNotUpdate = false;
    private boolean doNotUpdateDAC = false;


    /**
     * In onCreat lifecycle call of this application, the I2C and the GPIO
     * ports are set. Also 3 different handlers are set to poll temperature and
     * for registering PIR sensor's triggers.
     * An event listener for the firebase database is set to get triggered whenever
     * there is change in data from user.
     * @param savedInstanceState
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseRef  = fbHelper.getDatabaseRef();        // Firebase database reference is set


        try {
            // an object to control all peripherals of Raspberry Pi 3B is declared and set
            PeripheralManager manager = PeripheralManager.getInstance();
            // I2C bus is set to slave 0x08
            mDevice = manager.openI2cDevice(I2C_DEVICE_NAME, I2C_ADDRESS);


            // GPIO pin BCM 5 is set to PIR sensor1
            PIR1 = manager.openGpio("BCM5");
            // GPIO pin BCM 6 is set to PIR sensor 2
            PIR2 = manager.openGpio("BCM6");
            // direction for GPIOs set as inputs
            PIR1.setDirection(Gpio.DIRECTION_IN);
            PIR2.setDirection(Gpio.DIRECTION_IN);

            PIR1.setActiveType(Gpio.ACTIVE_HIGH);

            // Register for all state changes
            PIR1.setEdgeTriggerType(Gpio.EDGE_BOTH);
            // handler for PIR 1
            PIR1.registerGpioCallback(mPIR1callback);

            PIR2.setActiveType(Gpio.ACTIVE_HIGH);

            // Register for all state changes
            PIR2.setEdgeTriggerType(Gpio.EDGE_BOTH);
            // handler for PIR 1
            PIR2.registerGpioCallback(mPIR2callback);

            mHandler.post(mtemp);


        } catch (IOException e) {
            Log.w(TAG, "Unable to access I2C device", e);
        }

        /**
         * This is a Value Event Listener for the database. This gets called
         * whenever there is change in one of child value is noticed. Functions
         * are assigned to each child reference's event.
         */

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TAG", "Database Data change detected");
                if(dataSnapshot.hasChildren() && !doNotUpdate){
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();    // The iterator is assigned with the all the Node IDs and values where data was changed
                    try{
                        for(int counter = 0; iter.hasNext(); counter++) {
                            DataSnapshot snap = iter.next();
                            String nodId = snap.getKey();               // Node ID is read
                            String value = snap.getValue().toString();  // Data of particular Node ID
                            processDBData(nodId, value);                // Function call to Process data
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



    private void processDBData(String nodId, String value) {

        doNotUpdate = true;
        try {

            // nose id is assigned to a switch case
            switch (nodId) {
                // for node id PWM1
                case "PWM1":
                    if(value.length()!=0)
                        LED1_override = Byte.valueOf(value);
                    Log.d(TAG, "LED 1 brightness from firebase " + LED1_override);

                    break;
                // for node id PWM2
                case "PWM2":
                    motor1_override = Byte.valueOf(value);
                    Log.d(TAG, "Motor 1 PWM from firebase "+motor1_override);

                    break;
                // for node id PWM3
                case "PWM3":
                    // brightness level is converted to byte
                    LED2_override = Byte.valueOf(value);
                    Log.d(TAG, "LED 2 brightness from firebase " + value);

                    break;
                // for node id PWM3
                case "PWM4":
                    motor2_override = Byte.valueOf(value);
                    Log.d(TAG, "Motor 2 PWM from firebase "+motor2_override);

                    break;
                // for node id HOMELAT
                case "HOMELAT":
                    homelat = Double.valueOf(value);
                    Log.d(TAG, "Home Latitude "+homelat);
                    break;
                // for node id HOMELNG
                case "HOMELNG":
                    homelon = Double.valueOf(value);
                    Log.d(TAG, "Home Longititude "+homelon);
                    break;

                // for node id LATITUDE
                case "LATITUDE":
                    lat = Double.valueOf(value);
                    Log.d(TAG, "Current Latitude "+lat);
                    break;
                // // for node id LONGITUDE
                case "LONGITUDE":
                    lon = Double.valueOf(value);
                    Log.d(TAG, "Current Longitude "+lon);
                    break;
                // for node id OVERRIDE
                case "OVERRIDE":
                    override = Integer.valueOf(value);
                    Log.d(TAG, "Override status "+override);
                    break;

                /*case "geolocation":
                    if(value.equalsIgnoreCase("TRUE")){
                        geolocation = true;
                        Log.d(TAG,"USER IS AT HOME");
                    }
                    else if(value.equalsIgnoreCase("FALSE")){
                        geolocation = false;
                        Log.d(TAG,"USER IS NOT AT HOME");
                    }*/

                default:
                    fbHelper.updateTimeStamp();                         // update timestamp
            }
            doNotUpdate = false;
        }catch (NullPointerException e){
            Log.d(TAG, "Exception occurred while processing the database values");
            doNotUpdate = false;
        }

        /**here we are manually setting up the delta.
         * our findings on google map has showed that
         * delta of 0.00003 for both latitude and longitude
         * would give us locations within 30 feet radius
         * of our homelocation.
         */

        double latdiff = homelat - lat;
        double londiff = homelon - lon;

        Log.d(TAG,"Differeces - "+latdiff+ " "+londiff);

        if(latdiff > -0.00003 && latdiff < 0.00003){
            if (londiff > -0.00003 && londiff < 0.00003){

                geolocation = true;
                Log.d(TAG, "USER IS AT HOME. DETERMINED BY GEO LOCATION SERVICES " + geolocation);
                //geolocation = true;
            }
        }
        else {
            geolocation = false;
            fbHelper.writeToDataBase("geolocation", "FALSE");
            Log.d(TAG, "USER IS NOT AT HOME. DETERMINED BY GEO LOCATION SERVICES " + geolocation);
            //false
        }

        // user is at home then intrusin os set to false
        if (geolocation) {
            intrusion_detection = false;

            fbHelper.writeToDataBase("INTRUSION", "0");
        }


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDevice != null) {
            try {
                mDevice.close();
                mDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }
        mHandler.removeCallbacks(mtemp);

    }

    private Runnable mtemp = new Runnable() {
        @Override
        public void run() {
            try {
                int lsb = 0x00 << 24 | (mDevice.readRegByte(0x05)) & 0xff;      // LSD of ADC is read and converted to appropriate int value
                int msb = 0x00 << 24 | (mDevice.readRegByte(0x06)) & 0xff;      // MSB of ADC is read and converted to appropriate int value
                msb=msb*256;                                                       // MSB shiftedlest by 8 bits
                int regdata = msb+lsb;                                             // 10 bits ADC value from MSB & LSB bits
                Temperature = (double)regdata/10;                                  // 10 bit register value converted to degree celsius
                Temperature = Temperature +2;
                Log.d(TAG,"Temperature is - "+Temperature);
                fbHelper.writeToDataBase("TEMPERATURE", String.valueOf(Temperature));

                // if temperature is less than 15 degrees
                // PWM is set to 30%
                if(Temperature < 15) {
                    motor1_speed = 30;
                    motor2_speed = 30;
                }
                // if temperature is less than 17.5 and greater than 15 degrees
                // PWM is set to 50%
                else if(Temperature >= 15 && Temperature <= 17.5) {
                    motor1_speed = 50;
                    motor2_speed = 50;
                }
                // if temperature is greater than 17.5 degrees
                // PWM is set to 80%
                else {
                    motor1_speed = 80;
                    motor2_speed = 80;
                }

                /*****************************************************************************/

                // takes ADCinput from LDR 1
                int lsb_adc3 = 0x00 << 24 | (mDevice.readRegByte(0x07)) & 0xff;
                int msb_adc3 = 0x00 << 24 | (mDevice.readRegByte(0x08)) & 0xff;
                msb_adc3=msb_adc3*256;
                LDR1 = msb_adc3 + lsb_adc3;
                System.out.println("LDR 1 VALUE - "+LDR1);

                // sets LED brightness upon
                // LDR sensors output
                double res =(double) LDR1/1023;
                res = 1-res;
                res = res*100;
                int temp = (int)res;

                LED1_brightness = (byte)( temp & 0xff);


                System.out.println("LED1 brightness = "+LED1_brightness);

                // takes ADC input from LDR 2
                int lsb_adc4 = 0x00 << 24 | (mDevice.readRegByte(0x09)) & 0xff;
                int msb_adc4 = 0x00 << 24 | (mDevice.readRegByte(0x0a)) & 0xff;
                msb_adc4 = msb_adc4*256;
                LDR2 = msb_adc4 + lsb_adc4;
                System.out.println("LDR 2 VALUE - "+LDR2);

                // sets LED brightness upon
                // LDR sensors output
                res =(double) LDR2/1023;
                res = 1-res;
                res = res*100;
                temp = (int)res;

                LED2_brightness = (byte)( temp & 0xff);

                System.out.println("LED2 brightness = "+LED2_brightness);

                /*****************************************************************/
                /**
                 * this is automation & control part.
                 */

                /**
                 * If PIR sensor output is 1 and override is 0 and
                 * there is no intrusion detection then
                 */
                if(room1_status && override == 0 && !intrusion_detection) {
                    mDevice.writeRegByte(0x00, LED1_brightness);
                    mDevice.writeRegByte(0x02, motor1_speed);
                    fbHelper.writeToDataBase("PWM1", String.valueOf(LED1_brightness));
                    fbHelper.writeToDataBase("PWM2", String.valueOf(motor1_speed));

                    System.out.println("room 1 executed ");

                }
                /**
                 * If PIR sensor output is 2 and override is 0 and
                 * there is no intrusion detection then
                 */
                if (room2_status && override ==0 && !intrusion_detection) {
                    mDevice.writeRegByte(0x01, LED2_brightness);
                    mDevice.writeRegByte(0x03, motor2_speed);
                    fbHelper.writeToDataBase("PWM3", String.valueOf(LED2_brightness));
                    fbHelper.writeToDataBase("PWM4", String.valueOf(motor2_speed));
                    System.out.println("room 2 executed ");
                }

                // if intrusion detected
                else if(intrusion_detection)
                    Log.d(TAG,"IMPLEMENT INTRUSION DETECTION");

                /**
                 * if override is 1 the custon inputs from user is
                 * given to peripherals.
                 */
                else if(override == 1){
                    mDevice.writeRegByte(0x00, LED1_override);
                    mDevice.writeRegByte(0x01, LED2_override);
                    mDevice.writeRegByte(0x02, motor2_override);
                    mDevice.writeRegByte(0x03, motor2_override);
                }

                mHandler.postDelayed(mtemp, 2000);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * this is GpioCallback for PIR 1 sensor.
     * whenever there is change in output of
     * this sensor, this handler gets called
     */


    private GpioCallback mPIR1callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {

            try{
                if(gpio.getValue()) {
                    room1_status = true;
                    // if PIR sensor output is 1 and
                    // user is not home
                    if(!geolocation && room1_status) {
                        // intrusion detection is implemented
                        intrusion_detection = true;
                        fbHelper.writeToDataBase("INTRUSION", "1");
                    }
                    System.out.println("PIR1 status - " + room1_status+" Intrusion status - "+intrusion_detection);
                }
                else {
                    room1_status = false;
                    intrusion_detection = false;
                    fbHelper.writeToDataBase("INTRUSION", "0");
                    System.out.println("PIR1 status - "+room1_status);
                }
            }catch (IOException e){
                System.out.println("Error in PIR1 GPIO callback");
            }

            return true;
        }
    };

    /**
     * this is GpioCallback for PIR 2 sensor.
     * whenever there is change in output of
     * this sensor, this handler gets called
     */

    private GpioCallback mPIR2callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {

            try{
                if(gpio.getValue()) {
                    room2_status = true;
                    // if PIR sensor output is 1 and
                    // user is not home
                    if(!geolocation && room2_status) {
                        // intrusion detection is implemented
                        intrusion_detection = true;
                        fbHelper.writeToDataBase("INTRUSION", "1");
                    }
                    System.out.println("******PIR2 status - "+room2_status+" Intrusion status - "+intrusion_detection);
                }
                else {
                    room2_status = false;
                    intrusion_detection = false;
                    fbHelper.writeToDataBase("INTRUSION", "0");
                    System.out.println("**********PIR2 status - "+room2_status);
                }
            }catch (IOException e){
                System.out.println("Error in PIR1 GPIO callback");
            }

            return true;
        }
    };


}
