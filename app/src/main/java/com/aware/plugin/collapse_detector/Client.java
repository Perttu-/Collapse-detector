package com.aware.plugin.collapse_detector;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.aware.ESM;
import com.aware.providers.ESM_Provider;
import com.aware.providers.Locations_Provider;
import org.json.JSONObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;




// receiving and sending data with server
public class Client extends BroadcastReceiver implements Runnable {

    int UDP_SERVER_PORT = 80;
    String UDP_SERVER_IP = "85.23.168.159";

    LocationManager locationManager;
    TelephonyManager telephonyManager;
    DatabaseHandler db;

    boolean run = true;
    boolean monitoring = true;
    boolean fall = false;
    Context context;

    String TAG ="Client";

    DatagramSocket socket;
    InetAddress serverAddr;
    Double acceleration;


    public Client(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        db = new DatabaseHandler(context);

        try {
            // Retrieve the ServerName
            this.serverAddr = InetAddress.getByName(UDP_SERVER_IP);
            Log.d("UDP", "C: Connecting...");
            //Create new UDP-Socket
            this.socket = new DatagramSocket();

        } catch(Exception e){
            Log.e("Client", "Error connecting", e);
            e.printStackTrace();
        }
    }

    public void setFall(boolean pFall) {
        this.fall = pFall;
    }

    public void setAcc(Double acc){
        this.acceleration = acc;
    }

    public void setRun(Boolean pRun) {
        this.run = pRun;
        this.monitoring = pRun;
    }



    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ESM.ACTION_AWARE_ESM_DISMISSED))
            Log.d(TAG, "Pop Up was dismissed");

        if (intent.getAction().equals(ESM.ACTION_AWARE_ESM_ANSWERED)) {

            Log.d(TAG, "Pop Up was answered");
            Cursor esm_answers = context.getContentResolver().query(ESM_Provider.ESM_Data.CONTENT_URI, null, null, null, null);
            if (esm_answers != null && esm_answers.moveToLast()) {
                String ans = esm_answers.getString(esm_answers.getColumnIndex(ESM_Provider.ESM_Data.ANSWER));

                Log.d(TAG, "User answer: '" + ans + "'");

                try {
                    final JSONObject msg_json = new JSONObject();
                    msg_json.put("tag", 2);
                    msg_json.put("message", ans);
                    Log.d("Client", "Sending message");

                    send(socket, msg_json, serverAddr);

                } catch (Exception e) {
                    Log.e("JSON", "Error in id", e);
                    e.printStackTrace();
                }



            }
            esm_answers.close();
        }

    }


    @Override
    public void run() {
        try {
            Log.d("Client", "client start");


            //get device id
            final String device_id=telephonyManager.getDeviceId();

            // Creating JSONObjects
            final JSONObject fall_json = new JSONObject();
            final JSONObject id_json = new JSONObject();


            //expecting to receive string of size x
            byte[] buf = new byte[88];
            final DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, UDP_SERVER_PORT);


            //using timer to check the if packet was received
            TimerTask receivingTask = new TimerTask() {
                @Override
                public void run() {
                    if (run && !socket.isClosed()) {

                        try {
                            socket.receive(packet);
                            String receivedString = new String(packet.getData());
                            Log.d("UDP", "C: Received: '" + receivedString + "'");
                            //Toast.makeText(context, "Received: "+receivedString, Toast.LENGTH_SHORT).show();

                            final Long timestamp = System.currentTimeMillis();
                            //saves the received data and its time of arrival to database
                            db.addCollapse(new CollapseInfo(timestamp, receivedString));

                        } catch (Exception e) {
                            Log.e("UDP", "Error receiving data", e);
                            e.printStackTrace();
                        }
                    }
                }
            };

            Timer rTimer = new Timer();
            long rDelay = 0;
            long rIntervalPeriod = 500;
            rTimer.scheduleAtFixedRate(receivingTask, rDelay, rIntervalPeriod);


            //using timer to send device id once in a minute
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    if (run) {
                        try {
                            id_json.put("tag", 0);
                            id_json.put("device id", device_id);
                            Log.d("Client", "Sending id");
                           send(socket, id_json, serverAddr);

                        } catch (Exception e) {
                            Log.e("JSON", "Error in id", e);
                            e.printStackTrace();
                        }
                    }
                }
            };

            Timer timer = new Timer();
            long delay = 0;
            long intervalPeriod = 60000;
            timer.scheduleAtFixedRate(task, delay, intervalPeriod);

            while (monitoring) {
                if (fall) {
                    //getting the user location now with AWARE

                    String [] selections = new String[2];
                    selections[0] = "double_latitude";
                    selections[1] = "double_longitude";
                    Cursor gps_data = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, selections, null, null, "timestamp DESC");
                    gps_data.moveToFirst();
                    String sLatitude = gps_data.getString(gps_data.getColumnIndex("double_latitude"));
                    String sLongitude = gps_data.getString(gps_data.getColumnIndex("double_longitude"));
                    Log.d("Client", "LOCATION: lat: " + sLatitude + " long: " + sLongitude);
                    double latitude = Double.parseDouble(sLatitude);
                    double longitude = Double.parseDouble(sLongitude);
                    gps_data.close();

                    final Long timestamp = System.currentTimeMillis();
                    fall_json.put("tag", 1);
                    fall_json.put("timestamp", timestamp);
                    fall_json.put("latitude", latitude);
                    fall_json.put("longitude", longitude);
                    fall_json.put("device id", device_id);
                    fall_json.put("acceleration", acceleration);

                    Log.d("Client", "Sending fall info");
                    send(socket, fall_json, serverAddr);

                    //waiting for one second before allowing to fall event to be sent again
                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

                    fall = false;
                }
            }

        } catch (Exception e) {
            Log.e("JSON", "Error", e);
            e.printStackTrace();
        }


    }
    public void send(DatagramSocket socket, JSONObject jsonObj, InetAddress serverAddr) {

        try {
            //Encrypt and prepare data to be sent
            String stringJson = jsonObj.toString();
            String encJson = AES.encrypt(stringJson);
            byte[] buf = encJson.getBytes();



            //Create UDP-packet with data & destination(url+port)
            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, UDP_SERVER_PORT);
            Log.d("UDP", "C: Sending: '" + new String(buf) + "'");

            //Send out the packet */
            socket.send(packet);
            Log.d("UDP", "C: Sent.");


        } catch (Exception e) {
            Log.e("UDP", "Error in sending", e);
            e.printStackTrace();
        }
    }
}
