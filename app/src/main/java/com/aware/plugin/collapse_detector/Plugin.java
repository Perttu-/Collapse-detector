package com.aware.plugin.collapse_detector;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.*;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.ESM;
import com.aware.providers.ESM_Provider;
import com.aware.utils.Aware_Plugin;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Plugin extends Aware_Plugin implements SensorEventListener {

    private static SensorManager mSensorManager = null;
    private static Sensor mAccelerometer = null;

    private  final ESMStatusListener esm_statuses = new ESMStatusListener();
    public static Intent intent2;


    //server details
    int UDP_SERVER_PORT = 80;
    String UDP_SERVER_IP = "85.23.168.159";




    @Override
    public void onCreate() {
        super.onCreate();
        //uncomment to test activity without fall event
//        intent2 = new Intent(getApplicationContext(),MainActivity.class);
//        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent2);
        Toast.makeText(getApplicationContext(),"Monitoring Started", Toast.LENGTH_SHORT).show();

        TAG = "collapse_detector";
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");
        if( DEBUG ) Log.d(TAG, "collapse_detector plugin running");



        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);


        // ESM plugin for pop-up question after a fall is detected
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ESM, true);
        IntentFilter esm_filter = new IntentFilter();
        esm_filter.addAction(ESM.ACTION_AWARE_ESM_ANSWERED);
        esm_filter.addAction(ESM.ACTION_AWARE_ESM_DISMISSED);
        registerReceiver(esm_statuses, esm_filter);
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
        new Thread(new InfoSenderClient()).start();
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        double vector_sum = Math.sqrt(x*x + y*y + z*z);

        //the acceleration is around 0.3 as its lowest when in free fall. This may depend on the phone used.
        if (vector_sum < 0.3){
            //Toast.makeText(getApplicationContext(),"I fell down!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Vector sum: " + vector_sum);
            notifyUser();

            // send data to server
            new Thread(new Client()).start();


        }
    }

    // notify user that a fall was detected
    void notifyUser() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("Phone fall detected");
        mBuilder.setContentText("Please confirm");


        int mNotificationId = 001;
        Intent intent1 = new Intent(this, PopUp.class);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 999, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent).build();
        mBuilder.setAutoCancel(true);
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    public class ESMStatusListener extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ESM.ACTION_AWARE_ESM_DISMISSED))
                Log.d(TAG, "Pop Up was dismissed");

            if (intent.getAction().equals(ESM.ACTION_AWARE_ESM_ANSWERED)) {

                Log.d(TAG, "Pop Up was answered");
                Cursor esm_answers = context.getContentResolver().query(ESM_Provider.ESM_Data.CONTENT_URI, null, null, null, null);
                if (esm_answers != null && esm_answers.moveToLast()) {
                    String ans = esm_answers.getString(esm_answers.getColumnIndex(ESM_Provider.ESM_Data.ANSWER));

                    Log.d(TAG, "User answer ----- " + ans);

                    if (ans.equalsIgnoreCase("Yes")) {
                        Log.d(TAG, "answer is yes, homescreen shows up");
                        //shows map UI
//                        intent2 = new Intent(getApplicationContext(), Homescreen.class);
//                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent2);

                        //shows ui for map and other information
                        intent2 = new Intent(getApplicationContext(),InfoPanel.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent2);


                    }
                    if (esm_answers != null && !esm_answers.isClosed()) esm_answers.close();
                }
            }
        }
    }
    public class InfoSenderClient implements Runnable {

        //sending the socket info to server once in a minute
        @Override
        public void run() {
            try {
                final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                String device_id = tm.getDeviceId();
                JSONObject sJsonObj = new JSONObject();

                DatagramSocket socket = new DatagramSocket();

                //get device WIFI ip-address
                //make sure you have your wifi on otherwise ip will be 0.0.0.0.
                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInf = wm.getConnectionInfo();
                int ipAddress = wifiInf.getIpAddress();
                String PHONE_IP = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
                InetAddress phoneAddr = InetAddress.getByName(PHONE_IP);
                DatagramSocket sendableSocket = new DatagramSocket(0, phoneAddr);

                int PHONE_PORT= sendableSocket.getLocalPort();


                while (true) {
                    sJsonObj.put("message", "hello");
                    sJsonObj.put("device id", device_id);
                    sJsonObj.put("phone ip", PHONE_IP);
                    sJsonObj.put("phone port", PHONE_PORT);

                    //sending socket info to server
                    byte[] buf = sJsonObj.toString().getBytes();
                    InetAddress serverAddr = InetAddress.getByName(UDP_SERVER_IP);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, UDP_SERVER_PORT);
                    Log.d("UDP", "C: Sending: '" + new String(buf) + "'");
                    socket.send(packet);
                    Thread.sleep(60 * 1000);
                }
            } catch (Exception e) {
                Log.e("UDP", "C: Error", e);
                e.printStackTrace();
            }
        }
    }
    // sending data to server
    public class Client implements Runnable {

        @Override
        public void run() {
            try {
                //current timestamp
                Long timestamp = System.currentTimeMillis();

                //get longitude and latitude
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String bestProvider = locationManager.getBestProvider(criteria, true);
                android.location.Location location = locationManager.getLastKnownLocation(bestProvider);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                //get device id
                final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                String device_id = tm.getDeviceId();

                // Here we convert Java Object to JSON
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("timestamp", timestamp);
                jsonObj.put("latitude", latitude);
                jsonObj.put("longitude", longitude);
                jsonObj.put("device id", device_id);

                // Retrieve the ServerName
                InetAddress serverAddr = InetAddress.getByName(UDP_SERVER_IP);
                Log.d("UDP", "C: Connecting...");
                //Create new UDP-Socket
                DatagramSocket socket = new DatagramSocket();

                //Prepare some data to be sent
                byte[] buf = jsonObj.toString().getBytes();
                //Create UDP-packet with data & destination(url+port)
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, UDP_SERVER_PORT);
                Log.d("UDP", "C: Sending: '" + new String(buf) + "'");

                //Send out the packet */
                socket.send(packet);
                Log.d("UDP", "C: Sent.");
                Log.d("UDP", "C: Done.");


                //sending the socket info to server every n seconds


                //socket.receive(packet);
                //Log.d("UDP", "C: Received: '" + new String(packet.getData()) + "'");

            } catch (Exception e) {
                Log.e("UDP", "C: Error", e);
                e.printStackTrace();
            }

        }
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(esm_statuses);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ESM, false);
        if( DEBUG ) Log.d(TAG, "collapse_detector plugin terminated");
        mSensorManager.unregisterListener(this, mAccelerometer);
    }
}
