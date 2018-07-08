package com.example.keith.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by keith on 7/7/18.
 * Utility class-Handles basic bluetooth setup including
 *  -checks whether bluetooth supported
 *  -getting the BluetoothAdapter for this system
 *  -if activity passed in to the constructor, will pop a dialog asking
 *   user to turn on bluetooth if it is currently off
 *  Also will query and return a list of known BT devices
 *  Also will return a particular device
 */
public class BT_Setup {
    private static final String TAG = "BT_Setup";
    private Activity mAct;

    //used if we have to pop UI, this will be result_code returned
    //in onActivityResult
    public static final int REQUEST_ENABLE_BT = 41;

    //may pop ui on activity if BT disabled
    public BT_Setup(Activity mAct) {
        this.mAct = mAct;
    }
    //will not pop any UI
    public BT_Setup() {
    }

    //may pop ui on act if bluetooth disabled
    public BluetoothAdapter getBTAdapter(){
        //gets the system adapter if one present
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter==null){
            Log.e(TAG,"Device does not support bluetooth");
            return null;
        }

        if (!mBluetoothAdapter.isEnabled()){
            if (mAct!=null){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mAct.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            return null;
        }

        //if here we have it
        return mBluetoothAdapter;
    }

    //will serach for the device named name in list of paired devices
    //if found, its returned, otherwise null
    public BluetoothDevice getBTDevice(String name){
        BluetoothAdapter mBluetoothAdapter = getBTAdapter();
        if (mBluetoothAdapter==null) {
            Log.e(TAG,"in getBTDevice, getBTAdapter returned mBluetoothAdapter=null");
            return null;
        }

        //get a list of devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String dev_name = device.getName();
                boolean bres = name.equals(dev_name);
                if (name.equals(dev_name))
                     return device;
            }
        }
        return null;
    }

    //will get a list of paired devices (to populate spinners)
    //if found, its returned, otherwise null
    public ArrayList<String> getBTDevicesList(){
        BluetoothAdapter mBluetoothAdapter = getBTAdapter();
        if (mBluetoothAdapter==null) {
            Log.e(TAG,"in getBTDevice, getBTAdapter returned mBluetoothAdapter=null");
            return null;
        }

        //list of all the bluetooth devices we know about
        ArrayList<String> myList = new ArrayList<String>();

        //get a list of devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name of each paired device (can also get address).
            for (BluetoothDevice device : pairedDevices) {
                myList.add(device.getName());
            }
        }

        return myList;
    }
}
