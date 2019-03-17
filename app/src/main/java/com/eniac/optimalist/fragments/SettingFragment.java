package com.eniac.optimalist.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TimePicker;

import com.eniac.optimalist.MainActivity;
import com.eniac.optimalist.R;
import com.eniac.optimalist.database.DBHelper;

public class SettingFragment extends Fragment {
    private Switch backgroundSwitch;
    private int state1=0;
    private int state2=0;
    private Context context;
    private Switch recommendSwitch;
    private TimePicker timePicker=null;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context=container.getContext();
        return inflater.inflate(R.layout.fragment_settings, container, false);

    }
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        backgroundSwitch=view.findViewById(R.id.backgroundSwitch);
        recommendSwitch=view.findViewById(R.id.recommendEveryday);
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        SharedPreferences rec_settings = context.getSharedPreferences("rec_settings", 0);
        SharedPreferences time_settings = context.getSharedPreferences("time_settings", 0);
        SharedPreferences time_settings1 = context.getSharedPreferences("time_settings1", 0);

        boolean a=settings.getBoolean("backgroundswitch",true);
        boolean b=rec_settings.getBoolean("recommendSwitch",false);
        state1=time_settings.getInt("time_settings",0);
        state2=time_settings1.getInt("time_settings1",0);
        backgroundSwitch.setChecked(a);
        recommendSwitch.setChecked(b);
        backgroundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((MainActivity)getActivity()).changeLocationServiceStatus();
                SharedPreferences settings = context.getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("backgroundswitch",isChecked);
                editor.commit();
            }
        });
        recommendSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = context.getSharedPreferences("rec_settings", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("recommendSwitch",isChecked);
                editor.commit();
                if (isChecked){
                    Log.d("MyLocation","kontrol");
                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity().getApplicationContext());
                    View view = layoutInflaterAndroid.inflate(R.layout.pick_timer, null);
                    AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getActivity());
                    alertDialogBuilderUserInput.setView(view);
                    final AlertDialog alertDialog = alertDialogBuilderUserInput.create();

                    Button save_picker = (Button) view.findViewById(R.id.save_picker);
                    timePicker=view.findViewById(R.id.simpleTimePicker);
                    timePicker.setMinute(state2);
                    timePicker.setHour(state1);
                    save_picker.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            Context context=getContext();
                            int hour = timePicker.getHour();
                            int minute = timePicker.getMinute();
                            SharedPreferences settings = context.getSharedPreferences("time_settings", 0);
                            SharedPreferences settings1 = context.getSharedPreferences("time_settings1", 0);

                            SharedPreferences.Editor editor = settings.edit();
                            SharedPreferences.Editor editor1 = settings1.edit();

                            editor.putInt("time_settings",hour);
                            editor1.putInt("time_settings1",minute);

                            editor.commit();
                            editor1.commit();
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();

                }
            }
        });
    }
}
