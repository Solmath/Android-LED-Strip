package com.thomaspfeiffer.ledstrip;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Created by jake on 2/7/16.
 */
public class ChangeColorTask extends AsyncTask<String, Void, Integer> {

    private final static int SUCCESS = 0;
    private final static int FAILURE = 1;

    private final String LOG_TAG = ChangeColorTask.class.getSimpleName();

    private WeakReference<SharedPreferences> mPrefs;

    ChangeColorTask(SharedPreferences prefs){
        this.mPrefs = new WeakReference<>(prefs);
    }

    public void onPostExecute(Integer result) {
        if (result == SUCCESS){
            Log.v(LOG_TAG, "success");
        } else {
            Log.e(LOG_TAG, "error");
        }
    }

    public Integer doInBackground(String... params){

        final int PORT = 2390;
        final int RESPONSE_PORT = 55056;

        SharedPreferences prefs = mPrefs.get();

        if (prefs == null)
            return FAILURE;

        Log.v(LOG_TAG, params[0]);

        DatagramSocket socket = null;

        InetAddress address;
        try {
            String ipAddrString = prefs.getString(Constants.PREFERENCES_IP_ADDR, "");
            // remove slash at the beginning
            ipAddrString = ipAddrString.substring(1);
            address = InetAddress.getByName(ipAddrString);
        } catch (Exception e) {
            Log.w(LOG_TAG, "No valid IP in SharedPreferences");
            e.printStackTrace();
            return FAILURE;
        }

        // packet as a string
        String packetContents = params[0];

        try {
            // open a socket
            socket = new DatagramSocket(RESPONSE_PORT);
            // packet contents
            byte[] bytes = packetContents.getBytes();
            // send a packet with above contents to specified ip and port
            if (address != null) {
                Log.v(LOG_TAG, "sending packet to " + address.toString());
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
                socket.send(packet);

                // listen for a response
                byte[] response = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(response, response.length);
                socket.setSoTimeout(1000);

                String text;
                try {
                    Log.v(LOG_TAG, "Listening for a response");
                    socket.receive(responsePacket);
                    text = new String(response, 0, responsePacket.getLength());
                    Log.v(LOG_TAG, "Received packet.  contents: " + text);
                } catch (SocketTimeoutException e) {
                    Log.w(LOG_TAG, "Socket timed out");

                    return FAILURE;
                }

                if (text.equals("acknowledged")) {
                    return SUCCESS;
                }
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in ChangeColorTask doInBackground()");
            e.printStackTrace();

            return FAILURE;
        } finally {
            if(socket != null) {
                socket.close();
            }
        }

        return FAILURE;

    }
}
