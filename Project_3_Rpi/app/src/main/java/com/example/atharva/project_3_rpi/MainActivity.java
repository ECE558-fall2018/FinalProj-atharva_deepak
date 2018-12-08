/**
 * Building an IoT Home Automation system
 *
 * @author Atharva Mahindrakar
 *
 * This is an Android things application to controls RGB LEDs, DAC, Temperature
 * sensor, ADCs and a motor interfaced to PIC16F1532 microcontroller via I2C protocol.
 * The user sets sends the data of RGB LED's brightness and 5 bit DAC data to
 * this application via Firebase database and in return this application sends
 * Temperature and ADCs data back to Firebase which is given to user's mobile
 * application. The PWM of motor is set by the temperature given by the sensor.
 * Also one RGB LED blinks Green untill any exceptions are thrown by the system,
 * and if thrown it gets changed to blue.
 * The FirebaseHelper class is developed to access Firebase database for this application.
 */


package com.example.atharva.project_3_rpi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.PrivateKey;
import java.text.DecimalFormat;
import java.util.Iterator;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseReference databaseRef;
    private DatabaseReference childRef;
    private FirebaseHelper fbHelper = new FirebaseHelper();
    private Handler mHandler = new Handler();

    private static final String I2C_DEVICE_NAME = "I2C1";
    // I2C Slave Address
    private static final int I2C_ADDRESS = 0x08;
    private I2cDevice mDevice;
    private Gpio mGpio_blue, mGpio_green;
    private boolean doNotUpdate = false;
    private boolean doNotUpdateDAC = false;
    private boolean I2Cexception = false;
    private short val = 0;
    private byte MOTOR_PWM = 0x40;
    private static final int delay = 1000;
    private static final String pin_name = "BCM6";
    private static final String pin_name2 = "BCM5";

    private static DecimalFormat tempval = new DecimalFormat(".##");

    /**
     * In onCreat lifecycle call of this application, the I2C and the GPIO
     * ports are set. Also 2 different handlers are set poll temperature values
     * after 5 seconds and to blink RGB LED at delay of 1 second.
     * An event listener for the firebase database is set to get triggered whenever
     * there is change in data from user.
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseRef = fbHelper.getDatabaseRef();        // Firebase database reference is set

        PeripheralManager manager = PeripheralManager.getInstance();    // an object to control all peripherals of Raspberry Pi 3B is declared and set
        try {
            mDevice = manager.openI2cDevice(I2C_DEVICE_NAME, I2C_ADDRESS);  // I2C bus is set to slave 0x08
            mGpio_blue = manager.openGpio(pin_name);                        // GPIO pin BCM 6 is set to blue LED
            mGpio_green = manager.openGpio(pin_name2);                      // GPIO pin BCM 5 is set to blue GREEN
            mGpio_blue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);     // GPIO BCM 6 set as output
            mGpio_green.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);    // GPIO BCM 5 set as output
            mHandler.post(mPollTemperature);                                // Handler for polling temperature is set
            mHandler.post(mBlinking);                                       // Handler forbliking LED is set

        }catch(IOException e){
            e.printStackTrace();
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

    /**
     * In this function, for each node id there is particular function
     * call to perform an given operation / update data to peripherals.
     * @param nodId
     * @param value
     */

    private void processDBData(String nodId, String value) {

        doNotUpdate = true;
        try {
            byte ledbrightness;

            // nose id is assigned to a switch case
            switch (nodId) {
                // for node id PWM1
                case "PWM1":
                    Log.d(TAG, "Update Motor PWM to  " + value);
                    updateMotorSpeed(Byte.valueOf(value));             // function call to update motor speed
                    break;
                // for node id PWM2
                case "PWM2":
                    // brightness level is converted to byte
                    ledbrightness = Byte.valueOf(value);
                    Log.d(TAG, "Update RED LED brightness " + ledbrightness);
                    updateLED("RED", ledbrightness);            // function call to update RED LED brightness
                    break;
                // for node id PWM3
                case "PWM3":
                    // brightness level is converted to byte
                    ledbrightness = Byte.valueOf(value);
                    Log.d(TAG, "Update GREEN LED brightness " + ledbrightness);
                    updateLED("GREEN", ledbrightness);            // function call to update Green LED brightness
                    break;
                // for node id PWM3
                case "PWM4":
                    ledbrightness = Byte.valueOf(value);
                    Log.d(TAG, "Update BLUE LED brightness " + ledbrightness);
                    updateLED("BLUE", ledbrightness);             // function call to update Blue LED brightness
                    break;
                // for node id DAC
                case "DAC1OUT":
                    Log.d("TAG", "Update DAC value to :" + Integer.valueOf(value));
                    updateDAC(Byte.valueOf(value));                     // function call to update DAC
                    break;
                default:
                    fbHelper.updateTimeStamp();                         // update timestamp
            }
            doNotUpdate = false;
        }catch (NullPointerException e){
            Log.d(TAG, "Exception occurred while processing the database values");
            doNotUpdate = false;
        }
    }

    /**
     * In this function the DAC value given by user is sent to register
     * address 0x04 of PIC microcontroller via I2C.
     * Also the ADC data from register addresses 0x07 to 0x0C is read
     * using I2C protocol and these values are updated to subsequent
     * firebase database's child / node.
     * @param dac
     */

    private void updateDAC(Byte dac) {
        //TODO - write code to update DAC

            try {
                mDevice.writeRegByte(0x04, dac);        // DAC data is sent via I2C to address 0x04

                int lsb_adc3 = 0x00 << 24 | (mDevice.readRegByte(0x07)) & 0xff;     // LSB of ADC1 is read and converted to appropriate int value
                int msb_adc3 = 0x00 << 24 | (mDevice.readRegByte(0x08)) & 0xff;     // MSB of ADC1 is read and converted to appropriate int value
                msb_adc3 = msb_adc3 * 256;                                             // MSB is shifted by 8 bits
                int regdata_adc3 = msb_adc3 + lsb_adc3;                                 // 10 bit output of ADC1

                childRef = fbHelper.getDatabaseChildRef("ADC1INPUT");           // child reference of ADC1INPUT received
                //fbHelper.writeToDataBase("ADC1INPUT", String.valueOf(regdata_adc3));
                childRef.setValue(regdata_adc3);                                        // ADC1 value set to database child ADC1INPUT

                Log.d(TAG, "Writing ADC1INPUT: " + regdata_adc3);


                int lsb_adc4 = 0x00 << 24 | (mDevice.readRegByte(0x09)) & 0xff;     // LSB of ADC2 is read and converted to appropriate int value
                int msb_adc4 = 0x00 << 24 | (mDevice.readRegByte(0x0a)) & 0xff;     // MSB of ADC2 is read and converted to appropriate int value

                msb_adc4 = msb_adc4 * 256;                                              // MSB is shifted by 8 bits

                int regdata_adc4 = msb_adc4 + lsb_adc4;                                 // 10 bit output of ADC2

                childRef = fbHelper.getDatabaseChildRef("ADC2INPUT");           // child reference of ADC2INPUT received
                childRef.setValue(regdata_adc4);                                        // ADC1 value set to database child ADC2INPUT

                Log.d(TAG, "Writing ADC2INPUT: " + regdata_adc4);


                int lsb_adc5 = 0x00 << 24 | (mDevice.readRegByte(0x0b)) & 0xff;     // LSB of ADC3 is read and converted to appropriate int value
                int msb_adc5 = 0x00 << 24 | (mDevice.readRegByte(0x0c)) & 0xff;     // MSB of ADC3 is read and converted to appropriate int value
                msb_adc5 = msb_adc5 * 256;                                              // MSB is shifted by 8 bits
                int regdata_adc5 = msb_adc5 + lsb_adc5;                                 // 10 bit output of ADC3

                childRef = fbHelper.getDatabaseChildRef("ADC3INPUT");           // child reference of ADC3INPUT received
                childRef.setValue(regdata_adc5);                                        // ADC1 value set to database child ADC3INPUT
                Log.d(TAG, "Writing ADC3INPUT: " + regdata_adc5);
                I2Cexception = false;                                                   // I2C IO exception set to false
            } catch (IOException e) {
                Log.d(TAG, "Exception occurred while writing DAC value");
                I2Cexception = true;                                                    // I2C IO exception set to true
            }
            doNotUpdateDAC = false;


    }

    /**
     * Only PWM value for motor is received.
     * The PWM value for the motor is not sent to microcontroller in this function.
     * It is updated in handler mPollTemperature as motor should rotate at given RPM
     * for at least 5 seconds for us to notice change.
     * @param data
     */

    private void updateMotorSpeed(Byte data) {
        //TODO - write code to update Motor speed

        MOTOR_PWM = data;

    }

    /**
     * Each color is assigned to a switch case and subsequent PWM for
     * the LED is updated.
     * @param color
     * @param data
     */


    private void updateLED(String color, short data) {
        try {
            switch(color) {
                case "RED":
                    Log.d("TAG", "Writing to 0x00 the value :" + data);
                    mDevice.writeRegByte(0x00, (byte)data);                         // PWM data is sent via I2C to address 0x00
                    break;
                case "GREEN":
                    Log.d("TAG", "Writing to 0x01 the value :" + data);
                    mDevice.writeRegByte(0x01,(byte) data);                         // PWM data is sent via I2C to address 0x01
                    break;
                case "BLUE":
                    Log.d("TAG", "Writing to 0x03 the value :" + data);
                    mDevice.writeRegByte(0x02, (byte)data);                         // PWM data is sent via I2C to address 0x02
                    break;
            }
            I2Cexception = false;
        } catch (IOException e) {
            Log.d(TAG, "Exception occurred while updating LEDs");
            e.printStackTrace();
            I2Cexception = true;
        }
    }

    /**
     * Removes callbacks to both the handlers
     */
    @Override
    protected void onDestroy() {

        mHandler.removeCallbacks(mPollTemperature);
        mHandler.removeCallbacks(mBlinking);
        super.onDestroy();

    }

    /**
     * This is the handler created to poll the Temperature values each 5
     * seconds to firebase.
     *
     */

    private Runnable mPollTemperature = new Runnable() {
        @Override
        public void run() {

            try {
                int lsb = 0x00 << 24 | (mDevice.readRegByte(0x05)) & 0xff;      // LSD of ADC is read and converted to appropriate int value
                int msb = 0x00 << 24 | (mDevice.readRegByte(0x06)) & 0xff;      // MSB of ADC is read and converted to appropriate int value
                msb=msb*256;                                                       // MSB shiftedlest by 8 bits
                int regdata = msb+lsb;                                             // 10 bits ADC value from MSB & LSB bits
                double temp = ((((double)regdata/1023)*5)-1.4856)*100;             // 10 bit register value converted to degree celsius with Vref of 1.4856

                Log.d(TAG, "Temperature data : " + tempval.format(temp));
                childRef = fbHelper.getDatabaseChildRef("TEMPERATURE");     // Temperature value set to database child TEMPERATURE
                childRef.setValue(tempval.format(temp));                            // Temperature value sent to firebase
                mDevice.writeRegByte(0x03, MOTOR_PWM);                           // PWM data is sent via I2C to address 0x03
                mHandler.postDelayed(mPollTemperature, 5000);               // delay of 5 seconds
                I2Cexception = false;
            } catch (IOException e) {
                I2Cexception = true;
                e.printStackTrace();
            }

        }
    };


    /**
     * This handler is created to blink LED to show I2C bus is
     * working properly or it has thrown an exception.
     */

    private Runnable mBlinking = new Runnable() {
        @Override
        public void run() {

            if (mGpio_blue == null && mGpio_green == null) {
                return;
            }
            if (I2Cexception){
                try{
                    mGpio_blue.setValue(false);                     // blue led is turned off
                    mGpio_green.setValue(!mGpio_green.getValue());  // status of green led toggled
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

            else {
                try{
                    mGpio_green.setValue(false);                    // green led turned off
                    mGpio_blue.setValue(!mGpio_blue.getValue());    // status of blue led toggled
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

            mHandler.postDelayed(mBlinking,1000);           // delay of 1 second
        }
    };


}
