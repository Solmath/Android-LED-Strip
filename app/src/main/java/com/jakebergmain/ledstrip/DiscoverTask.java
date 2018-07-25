package com.jakebergmain.ledstrip;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Created by jake on 2/7/16.
 */
public class DiscoverTask extends AsyncTask<Void, Void, byte[]> {

    private final String LOG_TAG = DiscoverTask.class.getSimpleName();

    private WeakReference<Context> mContext;
    private WeakReference<DiscoverCallback> mCallback;
    
    private ProgressDialog progressDialog;

    /**
     * A task for discovering LED strips on the local network
     * @param context context
     * @param callback callback implementation so we can say if we found a device
     */
    DiscoverTask(Context context, DiscoverCallback callback){
        this.mContext = new WeakReference<>(context);
        this.mCallback = new WeakReference<>(callback);
    }

    public interface DiscoverCallback {
        void onFoundDevice();
    }

    protected void onPreExecute(){

        Context context = mContext.get();
        if (context == null)
            return;

        // progress bar
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Searching for devices on local network");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    protected void onPostExecute(byte[] result){
        progressDialog.dismiss();

        if(result == null)
            return;

        Context context = mContext.get();
        DiscoverCallback callback = mCallback.get();
        if (context == null || callback == null)
            return;

        try {
            // set ip addr in SharedPreferences
            SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
            preferences.edit()
                    .putString(Constants.PREFERENCES_IP_ADDR, InetAddress.getByAddress(result).toString())
                    .apply();

            callback.onFoundDevice();

        } catch (Exception e){
            e.printStackTrace();
            // set ip addr to null in SharedPreferences
            SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
            preferences.edit()
                    .putString(Constants.PREFERENCES_IP_ADDR, null)
                    .apply();
        }


    }

    protected byte[] doInBackground(Void... params){
        DatagramSocket socket = null;

        final int PORT = 2390;
        final int RESPONSE_PORT = 55056;

        String packetContents = "0:0:0";

        try {
            // open a socket
            socket = new DatagramSocket(RESPONSE_PORT);
            // get broadcast address to send packet to all devices on network
            InetAddress address = getBroadcastAddress();
            // packet contents
            byte[] bytes = packetContents.getBytes();
            // send a packet with above contents to all on local network to specified port
            if (address != null) {
                Log.v(LOG_TAG, "sending packet to " + address.toString());
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
                socket.send(packet);

                // listen for a response
                byte[] response = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(response, response.length);
                socket.setSoTimeout(1000);

                String text = "";
                int count = 0;
                // keep listening and sending packets until the LED strip responds
                while (!text.equals("acknowledged")) {
                    try {
                        Log.v(LOG_TAG, "Listening for a response");
                        socket.receive(responsePacket);
                        text = new String(response, 0, responsePacket.getLength());
                        Log.v(LOG_TAG, "Received packet.  contents: " + text);
                    } catch (SocketTimeoutException e) {
                        Log.w(LOG_TAG, "Socket timed out");
                        socket.send(packet);
                    }
                    count++;

                    // nothing is responding so we throw and connection exception
                    if (count > 30) {
                        throw new ConnectException("Cannot find and connect to any LED strips.");
                    }
                }

                // found a LED strip get the ip address of it and return it
                InetAddress ipAddr = responsePacket.getAddress();
                return ipAddr.getAddress();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in DiscoverTask doInBackground()");
            e.printStackTrace();
        } finally {
            if(socket != null) {
                socket.close();
            }
        }

        return null;
    }


    /**
     * I have no clue how this works.  All I know is it return the Broadcast Address.
     * @return InetAddress
     * @throws IOException IOException
     */
    private InetAddress getBroadcastAddress() throws IOException {
        Context context = mContext.get();
        if (context == null)
            return null;


        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            DhcpInfo dhcp = wifi.getDhcpInfo();
            // handle null somehow

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

            return InetAddress.getByAddress(quads);
        }

        return null;
    }
}
