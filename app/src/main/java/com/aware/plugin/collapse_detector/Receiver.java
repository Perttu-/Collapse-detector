package com.aware.plugin.collapse_detector;


import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Receiver implements Runnable {

    DatagramSocket socket;
    InetAddress address;
    int udp_port;
    boolean run;
    DatabaseHandler db;

    public Receiver(Context context, DatagramSocket soc, InetAddress addr, int port) {
        this.socket = soc;
        this.address = addr;
        this.udp_port = port;
        db = new DatabaseHandler(context);
    }

    public void setRun(boolean pRun) {
        this.run = pRun;
    }

    @Override
    public void run() {

            try {
                byte[] buf = new byte[88];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udp_port);
                socket.setSoTimeout(1000);   // set the timeout in millisecounds.

                while (run) {        // recieve data until closed
                    try {
                        socket.receive(packet);
                        String receivedString = new String(packet.getData());
                        Log.d("UDP", "C: Received: '" + receivedString + "'");

                        final Long timestamp = System.currentTimeMillis();

                        //saves the received data and its time of arrival to database
                        db.addCollapse(new CollapseInfo(timestamp, receivedString));

                    } catch (SocketTimeoutException e) {
                        // timeout exception.
                    }
                }

            } catch (SocketException e1) {
                Log.d("UDP", "Socket closed " + e1);

            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
