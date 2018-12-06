package com.pdx.ece558finalproject_ad.smarthome;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;


public class FragmentB extends Fragment {
    private FragmentBListener listener;
    private SeekBar seekbar_led,seekbar_motor;
    private TextView LED, Motor;

    public interface FragmentBListener {
        void onInputBSent(String progress, String childname);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_b, container, false);

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

    public void updateSeekbar(SeekBar seekBar){
        final int PWM1 = seekbar_led.getId();
        final int PWM2 = seekbar_motor.getId();
        int seekBarID = seekBar.getId();

        if(seekBarID == PWM1){
            listener.onInputBSent(String.valueOf(seekbar_led.getProgress()), "PWM3");
        }
        else if(seekBarID == PWM2){
            listener.onInputBSent(String.valueOf(seekbar_motor.getProgress()), "PWM4");
        }

    }

    public void updateStatus(String childname, String data){
        if(childname == "PWM3")
            LED.setText(data);
        if(childname == "PWM4")
            Motor.setText(data);
    }

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
        if (context instanceof FragmentBListener) {
            listener = (FragmentBListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentBListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}