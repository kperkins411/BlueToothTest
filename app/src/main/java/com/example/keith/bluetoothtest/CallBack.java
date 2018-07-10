package com.example.keith.bluetoothtest;

/**
 * Created by keith on 7/6/18.
 */

//declare as inner class in mainactivity
    //pass to thread, have thread callback on methods
    //using runOnUiThread to interact with UI
public interface CallBack {
    void displayinTV(String info);
    void onCommandReceived(String command);

}
