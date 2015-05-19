package com.aware.plugin.collapse_detector;



import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.ESM;
import com.aware.utils.Aware_Plugin;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;


public class Plugin extends Aware_Plugin implements SensorEventListener {

    private static SensorManager mSensorManager = null;
    private static Sensor mAccelerometer = null;

    public static Intent intent2;

    Sender sender = null;
    Receiver receiver = null;

    public boolean monitoring = true;
    boolean run = true;

    //default threshold value
    double threshold = 0.3;



    @Override
    public void onCreate() {

        super.onCreate();
        TAG = "collapse_detector";

        //getting preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String selected_frequency = prefs.getString("accelerometer_delay_plugin_collapse_detector","0");
        int selection = Integer.parseInt(selected_frequency);

        String selected_threshold = prefs.getString("threshold","0.3");

        try {
            this.threshold = Double.parseDouble(selected_threshold);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error getting threshold", e);
            e.printStackTrace();
            this.threshold = 0.3;
            Toast.makeText(getApplicationContext(),"Failed to get threshold from settings. Using: "+this.threshold , Toast.LENGTH_LONG).show();
        }


        Toast.makeText(getApplicationContext(),"Monitoring Started \n"+"Frequency level: " +selected_frequency+ ", Threshold: "+this.threshold , Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Selected frequency: " +selected_frequency);

        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");
        if( DEBUG ) Log.d(TAG, "collapse_detector plugin running");

        //accelerometer on
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, selection);

        //aware gps on
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, true);
        Intent activate_gps = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(activate_gps);

        // ESM plugin for pop-up question after a fall is detected
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ESM, true);
        IntentFilter esm_filter = new IntentFilter();
        esm_filter.addAction(ESM.ACTION_AWARE_ESM_ANSWERED);
        esm_filter.addAction(ESM.ACTION_AWARE_ESM_DISMISSED);
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));


        //connecting and starting threads
        int UDP_SERVER_PORT = 80;
        String UDP_SERVER_IP = "85.23.168.159";

        try {
            // Retrieve the ServerName
            InetAddress serverAddr = InetAddress.getByName(UDP_SERVER_IP);
            Log.d("UDP", "C: Connecting...");
            //Create new UDP-Socket
            DatagramSocket socket = new DatagramSocket();

            sender = new Sender(this, socket, serverAddr,UDP_SERVER_PORT );
            new Thread(sender).start();
            registerReceiver(sender, esm_filter);

            receiver = new Receiver(this, socket, serverAddr, UDP_SERVER_PORT);
            new Thread (receiver).start();
            receiver.setRun(true);

        } catch(Exception e){
            Log.e("Client", "Error connecting", e);
            e.printStackTrace();
        }

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

        //the acceleration is around 0.3 as its lowest when in free fall. Threshold may depend on the phone used.
        if (vector_sum < threshold ){
            ArrayList<Double> sums = new ArrayList<>();
            sums.add(vector_sum);

            Log.d(TAG, "Vector sum: " + vector_sum);
            notifyUser();

            sender.setAcc(vector_sum);
            sender.setFall(true);

        }
    }

    // notify user that a fall was detected
    void notifyUser() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("Phone fall detected");
        mBuilder.setContentText("Please elaborate");

        int mNotificationId = 1;
        Intent intent1 = new Intent(this, PopUp.class);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 999, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent).build();
        mBuilder.setAutoCancel(true);
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(sender);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ESM, false);
        if( DEBUG ) Log.d(TAG, "collapse_detector plugin terminated");
        mSensorManager.unregisterListener(this, mAccelerometer);

        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, false);
        Intent activate_gps = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(activate_gps);

        monitoring = false;
        run=false;
        sender.setRun(false);
        receiver.setRun(false);

    }
}
