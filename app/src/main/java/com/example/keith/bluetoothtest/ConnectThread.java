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
class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private Activity act;
    private CallBack cb;
    private AtomicBoolean doWork;
    private BluetoothAdapter mBluetoothAdapter;
    private MyBlueToothService  mmBTS;
    Lock mmBTS_lock;    //used to ensure mmBTS constructed before use

    public ConnectThread(Activity act, BluetoothDevice device,BluetoothAdapter mBluetoothAdapter, CallBack cb) {

        mmDevice = device;
        try{
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            mmSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.COMMAND_UUID));
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }

        this.act = act;
        this.cb = cb;
        this.mBluetoothAdapter = mBluetoothAdapter;
        mmBTS_lock = new ReentrantLock();

//        doWork = new AtomicBoolean(true);
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
        LogData("Successful connection!");

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageMyConnectedSocket(mmSocket);
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        LogData("about to create mmBTS");

        //make sure mmBTS is either fully constructed or null when used
        mmBTS_lock.lock();
        try {
            //create the clientSide Workhorse
            mmBTS = new MyBlueToothService(act,mmSocket, cb);
        }finally {
            mmBTS_lock.unlock();
        }

        LogData("done working leaving");
    }

    public void send(String info){
        mmBTS_lock.lock();
        try {
            if (mmBTS == null) {
                LogData("mmBTS is null cannot send");
                return;
            }
        }finally {
            mmBTS_lock.unlock();
        }

        //if here mmBTS is fully constructed
        mmBTS.send(info);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        LogData("Canceling thread");
//        doWork.set(false);
        try {
            mmSocket.close();
        } catch (IOException e) {
            LogData("Canceling thread,  mmSocket.close() threw exception");
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    //pass some info back to calling thread
    private void LogData(final String s){
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cb.displayinTV(s);
            }
        });
    }
}