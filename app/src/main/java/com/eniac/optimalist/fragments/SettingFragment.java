package com.eniac.optimalist.fragments;

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
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

import com.eniac.optimalist.MainActivity;
import com.eniac.optimalist.R;
import com.eniac.optimalist.database.DBHelper;

public class SettingFragment extends Fragment {
    private Switch backgroundSwitch;
    private int state1;
    private int state2;
    private Context context;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context=container.getContext();
        return inflater.inflate(R.layout.fragment_settings, container, false);

    }
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        backgroundSwitch=view.findViewById(R.id.backgroundSwitch);
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        boolean a=settings.getBoolean("backgroundswitch",true);
        backgroundSwitch.setChecked(a);
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

    }
}
