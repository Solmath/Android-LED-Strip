package com.thomaspfeiffer.ledstrip;

import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Device {
    private InetAddress mIP;
    private String mName;
    private ledType mType;

    public Device(InetAddress IP, String Name){
        mIP = IP;
        mName = Name;
        mType =ledType.RGB;
    }

    public InetAddress getIP(){
        return mIP;
    }

    public String getName(){
        return mName;
    }

    private static int lastDeviceId = 0;

    // Dummy method to create a data source
    // Tutorial: https://guides.codepath.com/android/using-the-recyclerview
    public static ArrayList<Device> createDeviceList(int numDevices) {
        ArrayList<Device> devices = new ArrayList<Device>();

        for (int i = 1; i <= numDevices; i++){
            byte[] ipAddr = new byte[]{127, 0, 0, 1};
            try {
                devices.add(new Device(InetAddress.getByAddress(ipAddr), "Gerät " + ++lastDeviceId));
            }
            catch(UnknownHostException e) {
            }

        }

        return devices;
    }
}

enum ledType
{
    RGB,
    RGBW,
    CCT;
}
