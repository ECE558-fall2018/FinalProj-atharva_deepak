/**
 * @author Atharva Mahindrakar
 *
 * this is a fragment for Room 1
 */



package com.pdx.ece558finalproject_ad.smarthome;


import android.support.v4.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.security.PrivateKey;


public class FragmentA extends Fragment {
    private FragmentAListener listener;
    private SeekBar seekbar_led,seekbar_motor;
    private TextView LED, Motor;


    // interface is declared to send seekbar data to HomeActivity
    public interface FragmentAListener {
        void onInputASent(String progress, String childname);
    }

    /**
     * This is a view inflater for this fragment
     * 2 seekbars and 2 text views are set in this
     * fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_a, container, false);

        // setting up UI for this fragment
        seekbar_led = v.findViewById(R.id.seekbar_LED);
        seekbar_motor = v.findViewById(R.id.seekbar_Motor);
        LED = v.findViewById(R.id.ledstatus);
        Motor = v.findViewById(R.id.motorstatus);


        SeekBar.OnSeekBarChangeListener mseekbarlistener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSeekbar(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                updateSeekbar(seekBar);
            }
        };

        seekbar_led.setOnSeekBarChangeListener(mseekbarlistener);
        seekbar_motor.setOnSeekBarChangeListener(mseekbarlistener);

        return v;
    }

    /**
     * this function gets called when there is change in
     * status of seekbar
     * @param seekBar
     */
    public void updateSeekbar(SeekBar seekBar){
        // seekbar ID are stored
        final int PWM1 = seekbar_led.getId();
        final int PWM2 = seekbar_motor.getId();
        int seekBarID = seekBar.getId();

        // if its a seekbar for PWM1 then
        if(seekBarID == PWM1){
            //data is sent to activity using this functioncall
            listener.onInputASent(String.valueOf(seekbar_led.getProgress()), "PWM1");
        }
        else if(seekBarID == PWM2){
            //data is sent to activity using this functioncall
            listener.onInputASent(String.valueOf(seekbar_motor.getProgress()), "PWM2");
        }

    }

    /**
     * if our system is in automation mode then
     * data of current statuses of LEDs and MOtor
     * is sent form HomeActivity to this fragment
     * @param childname
     * @param data
     */
    public void updateStatus(String childname, String data){
        if(childname == "PWM1")
            LED.setText(data);
        if(childname == "PWM2")
            Motor.setText(data);
    }

    /**
     * if override switch is on then,
     * Text Views are made invisible
     * and seekbars are made visible
     * and vice versa
     * @param status
     */
    public void updateOverride(boolean status) {
        if(!status) {
            seekbar_led.setVisibility(getView().GONE);
            seekbar_motor.setVisibility(getView().GONE);
            LED.setVisibility(getView().VISIBLE);
            Motor.setVisibility(getView().VISIBLE);
        }
        else {
            seekbar_led.setVisibility(getView().VISIBLE);
            seekbar_motor.setVisibility(getView().VISIBLE);
            LED.setVisibility(getView().GONE);
            Motor.setVisibility(getView().GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentAListener) {
            listener = (FragmentAListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentAListener");
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}