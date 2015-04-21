package com.aware.plugin.collapse_detector.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aware.plugin.collapse_detector.R;

public class SafetyFragment extends Fragment {

    public SafetyFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_safety, container, false);
        System.out.println("fragment added");


        return rootView;
    }
}