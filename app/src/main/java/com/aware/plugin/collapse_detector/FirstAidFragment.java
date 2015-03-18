package com.aware.plugin.collapse_detector;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FirstAidFragment extends Fragment {

    public FirstAidFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_firstaid, container, false);
        System.out.println("fragment added");


        return rootView;
    }
}