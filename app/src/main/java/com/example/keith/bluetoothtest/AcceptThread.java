package com.example.keith.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.keith.bluetoothtest.Constants;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by keith on 7/6/18.
 */

class AcceptThread extends Thread {
    private static final String TAG ="AcceptThread" ;
    private final BluetoothServerSocket mmServerSocket; //waits for connections
    private  BluetoothSocket mmSocket;                  //does actual communications
    private AppCompatActivity act;
    private CallBack cb;
    private AtomicBoolean doWork;
    private MyBlueToothService  mmBTS;
    Lock mmBTS_lock;    //used to ensure mmBTS constructed before use

    public AcceptThread(AppCompatActivity act, BluetoothAdapter mBluetoothAdapter ,CallBack cb ) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord
                    (Constants.COMMAND_NAME, UUID.fromString(Constants.COMMAND_UUID));
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        } 
        mmServerSocket = tmp;
        this.act = act;
        this.cb = cb;
        mmBTS_lock = new ReentrantLock();

        doWork = new AtomicBoolean(true);
    }

    public void run() {
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                mmSocket = mmServerSocket.accept();
            } catch (IOException e) {
                LogData("Socket's accept() method failed");
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            LogData("Socket's accepted");
            if (mmSocket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(mmSocket);
                break;
            }
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

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        LogData("Canceling thread");
        doWork.set(false);
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close mmServerSocket", e);
        }
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close mmSocket", e);
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        LogData("about to work");

        //make sure mmBTS is either fully constructed or null when used
        mmBTS_lock.lock();
        try {
            //create the clientSide Workhorse
            mmBTS = new MyBlueToothService(act,mmSocket, cb);
        }finally {
            mmBTS_lock.unlock();
        }

        while (doWork.get()){
            mmBTS.receive();
        }
        LogData("done working leaving");
    }
}