package com.aware.plugin.collapse_detector;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class Homescreen extends FragmentActivity implements LocationListener {

    GoogleMap googleMap;
    String TAG = "collapse_detector";
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);



        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

    }

    @Override
    public void onLocationChanged(Location location) {

        if (marker != null)
            marker.remove();

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.d(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);
        LatLng latLng = new LatLng(latitude, longitude);
        marker = googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        // to get the address
        LocationAddress locationAddress = new LocationAddress();
        locationAddress.getAddressFromLocation(latitude, longitude,
        getApplicationContext(), new GeocoderHandler());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }



    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            TextView locationTv = (TextView) findViewById(R.id.latlongLocation);
            locationTv.setText(locationAddress);

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
