package com.example.keith.bluetoothtest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.keith.bluetoothtest.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.example.keith.bluetoothtest.Constants;

/**
 * Created by keith on 7/6/18.
 */
class ConnectThread extends BaseThread {
    private static final String TAG = "ConnectThread";
    private final BluetoothDevice mmDevice;
    protected BluetoothAdapter mBluetoothAdapter;


    public ConnectThread(Activity act, BluetoothDevice device,BluetoothAdapter mBluetoothAdapter, CallBack cb) {
        super(act,cb);

        this.mmDevice = device;

        try{
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // A UUID identifies a service that is available on a particular device. Our service
            //is defined by Constants.COMMAND_UUID
            mmSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.COMMAND_UUID));
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }

        this.mBluetoothAdapter = mBluetoothAdapter;

        is_mmSocket_connected = new AtomicBoolean(false);
    }

    public void run() {
        if (mBluetoothAdapter == null) {
            LogData("Bummer, mBluetoothAdapter is null");
            return;
        }
        
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            LogData("Socket's connect() method failed, are you paired?");
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                LogData("Socket's close() method also failed");
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            LogData("Exiting thread");
            return;
        }
        //indicate that its OK to use
        is_mmSocket_connected.set(true);
        LogData("Successful connection!");

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageMyConnectedSocket(mmSocket);
    }
}