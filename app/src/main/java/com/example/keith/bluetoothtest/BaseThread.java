package com.example.keith.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by keith on 7/7/18.
 */

public class BaseThread extends Thread {
    private static final String TAG = "BaseThread";
    private    final Activity act;
    private    final CallBack cb;
    protected  final AtomicBoolean is_mmSocket_connected;
    protected  MyBlueToothService  mmBTS;
    protected  BluetoothSocket mmSocket;                  //does actual communications

    public BaseThread(Activity act, CallBack cb) {
        this.act = act;
        this.cb = cb;
        is_mmSocket_connected = new AtomicBoolean(false);
    }

    protected void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        LogData("about to work");

        //create the clientSide Workhorse
        mmBTS = new MyBlueToothService(act,mmSocket, cb);

        //following loops until error
        mmBTS.receive();

        LogData("done working leaving");
    }
    //this classes sole purpose is to launch a quick
    //thread to send info and then bail out, this keeps
    //this class from locking up the UI thread if we close
    //the socket and then try to send on the UI thread
    private class SendThread extends Thread {
        String info;
        SendThread(String info) {
            super(info);
            this.info = info;
            start();
        }
        public void run() {
            mmBTS.send(info);
        }

    }

    public void send(String info){
        if (is_mmSocket_connected.get() == false) {
            Log.e(TAG,"mmBTS is null cannot send");
            LogData("mmBTS is null cannot send");
            return;
        }

        //if here mmBTS is fully constructed
        //send info to paired device on a seperate thread
        SendThread thd = new SendThread(info);
    }

    // Closes the client socket and causes the thread to finish.
    // (all blocking calls are on a socket, any socket exception exits code)
    public void cancel() {
        LogData("Canceling thread");
        try {
            mmSocket.close();
        } catch (IOException e) {
            LogData("Canceling thread,  mmSocket.close() threw exception");
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
    //pass some info back to calling thread
    protected void LogData(final String s){
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cb.displayinTV(s);
            }
        });
    }
}
