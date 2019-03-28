package com.thomaspfeiffer.ledstrip;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeviceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceFragment extends Fragment implements DiscoverTask.DiscoverCallback {

    final String LOG_TAG = DeviceFragment.class.getSimpleName();

    ArrayList<Device> devices;

    static boolean deviceDetectionRun = false;

    public DeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View deviceView = inflater.inflate(R.layout.fragment_device, container, false);

        // Lookup the recyclerview in fragment layout
        RecyclerView rvDevices = (RecyclerView) deviceView.findViewById(R.id.rvDevices);
        // Set layout manager to position the items
        rvDevices.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize contacts
        devices = Device.createDeviceList(20);


        // set item animator to DefaultAnimator
        rvDevices.setItemAnimator(new DefaultItemAnimator());
        // That's all!

        return deviceView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!deviceDetectionRun) {
            searchForDevices(view);
            deviceDetectionRun = true;
        }
    }


    /**
     * method for Discover task callback
     */
    public void onFoundDevice() {
        // we found a LED strip!
        // what do we do now?

        // for debug only
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
        String ipString = preferences.getString(Constants.PREFERENCES_IP_ADDR, "");
        Log.v(LOG_TAG, "onDeviceFound() ipAddr: " + ipString);
    }

    /**
     * Start DiscoverTask to search for devices on local network.
     */
    public void searchForDevices(View view) {
        Log.v(LOG_TAG, "starting DiscoverTask");
        new DiscoverTask(getContext(), this, view).execute(null, null);
    }

}
