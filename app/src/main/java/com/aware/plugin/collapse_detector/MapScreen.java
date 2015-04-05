package com.aware.plugin.collapse_detector;

import android.database.sqlite.SQLiteDatabase;
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

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MapScreen extends FragmentActivity implements LocationListener {

    GoogleMap googleMap;
    String TAG = "collapse_detector";
    Marker marker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapscreen);

        SupportMapFragment supportMapFragment =(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);

        DatabaseHandler db = new DatabaseHandler(this);

//        db.addCollapse(new CollapseInfo(0L,"[65.00,25.00]"));
//        db.addCollapse(new CollapseInfo(1L,"[65.03,25.10]"));
//        db.addCollapse(new CollapseInfo(2L,"[65.05,25.20]"));
//        db.addCollapse(new CollapseInfo(3L,"[65.10,25.30]"));
//        db.addCollapse(new CollapseInfo(4L,"[65.02,25.40]"));
//        Log.d("Reading: ", "Reading all contacts..");
//        List<CollapseInfo> collapses = db.getAllCollapses();
//        int i=0;
//        for (CollapseInfo cn : collapses) {
//            String receivedString = db.getCollapse(i).getCoordinates();
//            String receivedLatitudeString = receivedString.split(",")[0].replace("[","");
//            String receivedLongitudeString = receivedString.split(",")[1].replace("]","").replace(" ","");
//            double receivedLatitude = Double.parseDouble(receivedLatitudeString);
//            double receivedLongitude = Double.parseDouble(receivedLongitudeString);
//            LatLng latlng = new LatLng(receivedLatitude, receivedLongitude);
//            Log.d("--", "LAT: "+receivedLatitude+ " LONG: "+receivedLongitude);
//
//
//            i++;
//        }
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng myLocation = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,10));

        List<CollapseInfo> collapses = db.getAllCollapses();
        for (CollapseInfo ci : collapses) {
            String coordinates = ci.getCoordinates();
            Long timestamp =ci.getTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy HH:mm ");
            String date = sdf.format(timestamp);
            //this if statement ignores encrypted data
            if (coordinates.charAt(0) == '[') {
                LatLng collapseLocation = decrypt(ci.getCoordinates());
                marker = googleMap.addMarker(new MarkerOptions()
                        .position(collapseLocation)
                        .title("Building collapse")
                        .snippet(date));
            }
        }
    }



    public LatLng decrypt(String encString){
        String key = "0123456789abcdef";
        byte[] encBytes = encString.getBytes();
        //SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(), "AES");

        try {
              //decrypting
//            byte[] raw = key.getBytes(Charset.forName("UTF-8"));
//            if (raw.length != 16) {
//                throw new IllegalArgumentException("Invalid key size.");
//            }
//            SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, new IvParameterSpec(new byte[16]));
//            byte[] original = cipher.doFinal(encBytes);
//
//            String receivedString = new String(original, Charset.forName("UTF-8"));
//
            //parsing
            String receivedString = encString;
            double receivedLatitude = Double.parseDouble(receivedString.split(",")[0].replace("[",""));
            double receivedLongitude = Double.parseDouble(receivedString.split(",")[1].replace("]","").replace(" ",""));
            LatLng coordinates = new LatLng(receivedLatitude, receivedLongitude);
            return coordinates;

        }catch (Exception e) {
            Log.e("DECRYPTING", "Error decrypting data", e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

//        if (marker != null)
//            marker.remove();
//
//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();
//        Log.d(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);
//        LatLng latLng = new LatLng(latitude, longitude);
//        marker = googleMap.addMarker(new MarkerOptions().position(latLng));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
//
//        // to get the address
//        LocationAddress locationAddress = new LocationAddress();
//        locationAddress.getAddressFromLocation(latitude, longitude, getApplicationContext(), new GeocoderHandler());
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
