package com.example.keith.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by keith on 7/6/18.
 */

public class MyBlueToothService {
    private static final String TAG = "MyBluetoothService";

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private Activity act;
    private CallBack cb;

    public MyBlueToothService(Activity act, BluetoothSocket socket, CallBack cb) {
        this.act = act;
        this.cb = cb;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    //Service, do this in a thread
    public void receive() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        //when you close the socket we leave
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);

                //TODO Send the obtained bytes to the UI activity.
                String info = new String(mmBuffer, "UTF-8");
                LogData(info);

            } catch (IOException e) {
                LogData("Input stream was disconnected");
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
        LogData("Exiting");
    }

    //Client
    //Call this from the main activity to send data to the remote device.
    public boolean send(String info) {
        byte[] bytes = new byte[0];
        try {
            bytes = info.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException" + e);
            LogData("UnsupportedEncodingException ");
            return false;
        }

        try {
            //TODO maybe do this on seperate thread?
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
            LogData("Error occurred when sending data");
            return false;
        }
        return true;
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
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