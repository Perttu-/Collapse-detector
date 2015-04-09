package com.aware.plugin.collapse_detector;

import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.aware.providers.Locations_Provider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.text.SimpleDateFormat;

import java.util.List;



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

        DatabaseHandler db = new DatabaseHandler(this);

//        Criteria criteria = new Criteria();
//
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        String bestProvider = locationManager.getBestProvider(criteria, true);
//        Location location = locationManager.getLastKnownLocation(bestProvider);
//        if (location != null) {
//            onLocationChanged(location);
//        }
//        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);




//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();

        String [] selections = new String[2];
        selections[0] = "double_latitude";
        selections[1] = "double_longitude";
        Cursor gps_data = this.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, selections, null, null, "timestamp DESC");
        gps_data.moveToFirst();
        String sLatitude = gps_data.getString(gps_data.getColumnIndex("double_latitude"));
        String sLongitude = gps_data.getString(gps_data.getColumnIndex("double_longitude"));
        Log.d("test", "LOCATION: lat: "+sLatitude+" long: "+sLongitude);
        double latitude = Double.parseDouble(sLatitude);
        double longitude = Double.parseDouble(sLongitude);
        gps_data.close();
        LatLng myLocation = new LatLng(latitude, longitude);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,10));


        List<CollapseInfo> collapses = db.getAllCollapses();
        for (CollapseInfo ci : collapses) {
            String info = ci.getInfo();
            Long timestamp = ci.getTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy HH:mm ");
            String date = sdf.format(timestamp);

            //this if statement is just a workaround to ignore encrypted data
            if (info.charAt(0) == '[') {

                String collapseDecrypted = info;
                //LatLng collapseLocation = parseInfo(collapseDecrypted);
                InfoParser ip = new InfoParser(collapseDecrypted);
                LatLng collapseLocation = ip.getLatLng();
                String id = ip.getId();
                int count = ip.getCount();
                marker = googleMap.addMarker(new MarkerOptions()
                        .position(collapseLocation)
                        .title("Building collapse")
                        .snippet("Date: " + date + "\n Id: " + id + "\n Fall count: "+count ));//not tested
            }

        }
    }



//    public byte[] decrypt(String encString){
//        Log.d("DECRYPTING", "Encrypted: '"+ encString + "'");
//        String key = "0123456789abcdef";
//        byte[] encBytes = encString.getBytes();
//        byte[] b_EncString =encString.getBytes();
//        try {
//
//            // Get a cipher object.
//            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
//            cipher.init(Cipher.DECRYPT_MODE, key);
//            byte[] decryptedBytes = cipher.doFinal(cipherBytes);
//            String decryptedText = new String(decryptedBytes, "UTF8");
//
//        }catch (Exception e) {
//            Log.e("DECRYPTING", "Error decrypting data", e);
//            e.printStackTrace();
//            return null;
//        }
//    }

        //not tested
    public LatLng parseInfo(String receivedString){
        //extracting all the info from the string
        String receivedId = receivedString.split(",")[0].replace("[","");
        double receivedLatitude = Double.parseDouble(receivedString.split(",")[1].replace(" ",""));
        double receivedLongitude = Double.parseDouble(receivedString.split(",")[2].replace(" ",""));
        int receivedCount = Integer.parseInt(receivedString.split(",")[3].replace("]","").replace(" ",""));
        return new LatLng(receivedLatitude, receivedLongitude);
    }

    private class InfoParser{
        String receivedString;

        InfoParser(String pString){
            this.receivedString=pString;
        }

        String getId(){
            return receivedString.split(",")[0].replace("[","");
        }

        LatLng getLatLng(){
            double receivedLatitude = Double.parseDouble(receivedString.split(",")[1].replace(" ","").replace("\"",""));
            double receivedLongitude = Double.parseDouble(receivedString.split(",")[2].replace(" ","").replace("\"",""));
            return new LatLng(receivedLatitude, receivedLongitude);
        }
        int getCount(){
            return Integer.parseInt(receivedString.split(",")[3].replace("]","").replace(" ","").replace("\"",""));
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
