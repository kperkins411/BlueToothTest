package com.example.keith.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private CheckBox cb_Server;
    private Button b_action;
    private Button b_disconnect;
    private Spinner sp_devices;

    private EditText et_toSend;
    private Button b_send;
    private TextView tv_results;
    private boolean b_server = false;

    //handles all the bluetooth setup
    //including getting adapters and devices
    private BT_Setup btSetup;

    private String results;
    enum TXT_TYPE{RED,GREEN,BLUE,NORMAL};       //used to color textview lines

    private AcceptThread mtAccept;
    private ConnectThread mtConnect;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if we enabled bluetooth then get the adapter
        if (requestCode==BT_Setup.REQUEST_ENABLE_BT && resultCode==RESULT_OK){
            //TODO repeat whatever failed since bluetooth was off
            setupSimpleSpinner();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cb_Server = (CheckBox)findViewById(R.id.cb_Server);
        cb_Server.setChecked(b_server);
        b_action = (Button) findViewById(R.id.b_action);
        b_disconnect = (Button) findViewById(R.id.b_disconnect);
        sp_devices = (Spinner) findViewById(R.id.sp_devices);

        et_toSend = (EditText) findViewById(R.id.et_toSend);
        b_send = (Button) findViewById(R.id.b_send);

        tv_results = (TextView) findViewById(R.id.tv_results);
        tv_results.setMovementMethod(new ScrollingMovementMethod());
        results = new String("Results go here");
        tv_results.setText(results);
        results="";

        btSetup = new BT_Setup(this);

        //if bluetooth not on this spinner will never be properly created
        setupSimpleSpinner();

        enableUI(false);

        //get rid of annoying keyboard that pops up cause I have a TextView as first element
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void toggleServer(View view) {
        b_server = cb_Server.isChecked();

        if(b_server)
            b_action.setText("Start");
        else
            b_action.setText("Connect");

        int vis = (b_server)?View.GONE:View.VISIBLE;
        sp_devices.setVisibility(vis);

    }

    public void do_send(View view) {
        String info = et_toSend.getText().toString();
        display(TXT_TYPE.GREEN, info);

        if (b_server) {
            mtAccept.send(info);
        } else {
            mtConnect.send(info);
        }
    }

    public void do_action(View view) {
        //clear stuff out
        results = "";
        tv_results.setText(results);

        //see if bluetooth there
        BluetoothAdapter mBluetoothAdapter = btSetup.getBTAdapter();
        Boolean enableUI = (mBluetoothAdapter!=null);

        //see if we want to be a server
        b_server = cb_Server.isChecked();

        if (b_server) {
            //launch a thread to wait for connection
            mtAccept = new AcceptThread(this, myCallback);
            mtAccept.start();
        }
        else {
            BluetoothDevice mBluetoothDevice = btSetup.getBTDevice(sp_devices.getSelectedItem().toString());
            if (mBluetoothDevice == null) {
                display(TXT_TYPE.GREEN, "Cannot get BT device");
            }
            else{
                mtConnect = new ConnectThread(this, mBluetoothDevice,mBluetoothAdapter,myCallback  );
                mtConnect.start();
            }
        }

        enableUI(enableUI);
    }

    public void do_disconnect(View view) {
        try {
            if (b_server) {
                mtAccept.cancel();
                mtAccept.join();
                display("joined Accept thread");
            }
            else {
                mtConnect.cancel();
                mtConnect.join();
                display("joined Connect thread");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        enableUI(false);
    }

    public void enableUI(boolean bEnable){
        b_disconnect.setEnabled(bEnable);
        et_toSend.setEnabled(bEnable );
        b_send.setEnabled(bEnable );

        sp_devices.setEnabled(!bEnable );

        b_action.setEnabled(!bEnable);
        cb_Server.setEnabled(!bEnable);
    }

    /**
     * get a list of the names of the bluetooth devices
     * put them in the spinner
     */
    private void setupSimpleSpinner() {
        ArrayList<String> myList = btSetup.getBTDevicesList();
        if (myList != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    myList);

            sp_devices.setAdapter(arrayAdapter);
        }
    }

    public void display(String info){
        display(TXT_TYPE.NORMAL,info);
    }
    public void display(TXT_TYPE tt, String info){
        switch(tt){
            case RED:
                info = "<font color='#EE0000'>" + info + "</font>";
                break;
            case GREEN:
                info = "<font color='#00EE00'>" + info + "</font>";
                break;
            case BLUE:
                info = "<font color='#0000EE'>" + info + "</font>";
                break;
        }

        results=results +"<br/>" + info;
        tv_results.setText(Html.fromHtml(results));
    }

    CallBack myCallback = new CallBack(){

        @Override
        public void displayinTV(String info) {
            display(TXT_TYPE.BLUE,info);
        }

        @Override
        public void onCommandReceived(String command) {

        }
    };
}
