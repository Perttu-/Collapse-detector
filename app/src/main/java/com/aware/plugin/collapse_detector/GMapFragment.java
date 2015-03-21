package com.aware.plugin.collapse_detector;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.maps.SupportMapFragment;

public class GMapFragment extends SupportMapFragment {

    public GMapFragment(){}



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        //if possible add map fragment/view here


        return rootView;


    }

}

