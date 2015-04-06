package com.aware.plugin.collapse_detector;


import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.aware.providers.Locations_Provider;
import org.json.JSONObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;




// receiving and sending data with server
public class Client implements Runnable {
    int UDP_SERVER_PORT = 80;
    String UDP_SERVER_IP = "85.23.168.159";
    LocationManager locationManager;
    TelephonyManager telephonyManager;
    DatabaseHandler db;
    boolean run=true;
    boolean monitoring=true;
    boolean fall = false;
    Context context;



    public Client(Context context){
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        db = new DatabaseHandler(context);
    }

    public void setFall(boolean pFall){
        this.fall=pFall;
    }

    public void setRun(Boolean pRun){
        this.run = pRun;
        this.monitoring = pRun;
    }


    @Override
    public void run() {
        try {
            Log.d("test", "client start");


            //get device id
            final String device_id=telephonyManager.getDeviceId();

            // Retrieve the ServerName
            final InetAddress serverAddr = InetAddress.getByName(UDP_SERVER_IP);
            Log.d("UDP", "C: Connecting...");

            //Create new UDP-Socket
            final DatagramSocket socket = new DatagramSocket();

            // Here we convert Java Object to JSON
            final JSONObject fall_json = new JSONObject();
            final JSONObject id_json = new JSONObject();

            //expecting to receive string of size 32
            byte[] buf = new byte[32];
            final DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, UDP_SERVER_PORT);


            //using timer to check the if packet was received and also parsing the string
            TimerTask receivingTask = new TimerTask() {
                @Override
                public void run() {
                    if (run && !socket.isClosed()) {

                        try {
                            socket.receive(packet);
                            String receivedString = new String(packet.getData());
                            Log.d("UDP", "C: Received: '" + receivedString + "'");
                            Toast.makeText(context, "Received: "+receivedString, Toast.LENGTH_SHORT).show();

                            final Long timestamp = System.currentTimeMillis();
                            //saves the received encrypted data and its time of arrival to database
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
                            id_json.put("device id", device_id);
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
                    //get location
//                    Criteria criteria = new Criteria();
//                    String bestProvider = locationManager.getBestProvider(criteria, true);
//                    android.location.Location location = locationManager.getLastKnownLocation(bestProvider);
//                    if(location != null) {
//                        latitude = location.getLatitude();
//                        longitude = location.getLongitude();
//                    }
//                    else{
//                        Log.d("test", "Couldn't find location");
//                    }

                    //getting the user location now with AWARE

                    String [] selections = new String[2];
                    selections[0] = "double_latitude";
                    selections[1] = "double_longitude";
                    Cursor gps_data = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, selections, null, null, "timestamp DESC");
                    gps_data.moveToFirst();
                    String sLatitude = gps_data.getString(gps_data.getColumnIndex("double_latitude"));
                    String sLongitude = gps_data.getString(gps_data.getColumnIndex("double_longitude"));
                    Log.d("test", "LOCATION: lat: "+sLatitude+" long: "+sLongitude);
                    double latitude = Double.parseDouble(sLatitude);
                    double longitude = Double.parseDouble(sLongitude);

                    final Long timestamp = System.currentTimeMillis();
                    fall_json.put("timestamp", timestamp);
                    fall_json.put("latitude", latitude);
                    fall_json.put("longitude", longitude);
                    fall_json.put("device id", device_id);

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
            //Prepare some data to be sent
            byte[] buf = jsonObj.toString().getBytes();

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
