package com.aware.plugin.collapse_detector;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class GMapFragment extends SupportMapFragment {

    //public GMapFragment(){}

    MapView mapView;
    GoogleMap map;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);


        //MapsInitializer.initialize(this.getActivity());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(65, 25), 10));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));

        return rootView;


    }

}

