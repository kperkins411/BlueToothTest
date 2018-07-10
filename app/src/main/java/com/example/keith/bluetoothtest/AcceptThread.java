package com.example.keith.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.util.Log;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by keith on 7/6/18.
 */

class AcceptThread extends BaseThread {

    private static final String TAG ="AcceptThread" ;
    private final BluetoothServerSocket mmServerSocket; //waits for connections

    public AcceptThread(Activity act, CallBack cb ) {
        super(act, cb);

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
                //indicate that its OK to use
                is_mmSocket_connected.set(true);

                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(mmSocket);
                break;
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        super.cancel();
        try {
            if (mmServerSocket!= null)
                mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close mmServerSocket", e);
        }
    }
}