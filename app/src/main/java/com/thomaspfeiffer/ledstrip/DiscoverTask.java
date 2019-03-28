package com.thomaspfeiffer.ledstrip;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jake on 2/7/16.
 */
public class DiscoverTask extends AsyncTask<Void, Void, List<Device>> {

    private final String LOG_TAG = DiscoverTask.class.getSimpleName();

    private WeakReference<Context> mContext;
    private WeakReference<DiscoverCallback> mCallback;
    private View mView;
    
    private ProgressDialog progressDialog;

    /**
     * A task for discovering LED strips on the local network
     * @param context context
     * @param callback callback implementation so we can say if we found a device
     */
    DiscoverTask(Context context, DiscoverCallback callback, View view){
        this.mContext = new WeakReference<>(context);
        this.mCallback = new WeakReference<>(callback);
        this.mView = view;
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

    protected void onPostExecute(List<Device> devices){
        // TODO: get rid of progressDialog?
        progressDialog.dismiss();

        if(devices == null)
            return;

        RecyclerView rvDevices = mView.findViewById(R.id.rvDevices);
        // Create adapter passing in the sample user data
        DeviceAdapter adapter = new DeviceAdapter(devices);
        // Attach the adapter to the recyclerview to populate items
        rvDevices.setAdapter(adapter);

/*
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
*/

    }

    protected List<Device> doInBackground(Void... params){
        ArrayList<Device> devices = new ArrayList<Device>();

        // TODO: Maybe switch to Multicast (https://www.kompf.de/java/multicast.html)
        DatagramSocket socket = null;

        final int PORT = 2390;
        final int RESPONSE_PORT = 55056;

        // TODO: change message to discover devices
        String packetContents = "0:0:0";

        try {
            // open a socket
            socket = new DatagramSocket(RESPONSE_PORT);
            socket.setSoTimeout(1000);

            // get broadcast address to send packet to all devices on network
            InetAddress address = getBroadcastAddress();
            // packet contents
            byte[] bytes = packetContents.getBytes();
            // send a packet with above contents to all on local network to specified port
            if (address != null) {
                Log.v(LOG_TAG, "sending packet to " + address.toString());
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
                socket.send(packet);
                Log.v(LOG_TAG, "Discover package sent.");

                do {
                    // listen for a response
                    Log.v(LOG_TAG, "Listening for a response");
                    byte[] response = new byte[1024];
                    DatagramPacket responsePacket = new DatagramPacket(response, response.length);
                    socket.receive(responsePacket);

                    InetAddress ipAddr = responsePacket.getAddress();
                    // keep listening and sending packets until the LED strip responds
                    // TODO: create device from response and add it to devices (check for duplicates)
                    devices.add(new Device(ipAddr, "Testdevice"));
                } while (true);
                // found a LED strip get the ip address of it and return it

            }
        } catch (SocketTimeoutException e) {
            Log.w(LOG_TAG, "Socket timed out");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in DiscoverTask doInBackground()");
            e.printStackTrace();
        } finally {
            if(socket != null) {
                socket.close();
            }
        }

        return devices;
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
