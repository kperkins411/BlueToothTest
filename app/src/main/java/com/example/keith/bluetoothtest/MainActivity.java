package com.example.keith.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
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

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;

    private String results;
    enum TXT_TYPE{RED,GREEN,BLUE,NORMAL};       //used to color textview lines

    private AcceptThread mtAccept;
    private ConnectThread mtConnect;

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

        showBTDevices();

        enableUI(false);

        //get rid of annoying keyboard that pops up cause I have a TextView as first element
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void toggleServer(View view) {
        b_server = !b_server;

        if(b_server)
            b_action.setText("Start");
        else
            b_action.setText("Connect");

        int vis = (b_server)?View.GONE:View.VISIBLE;
        sp_devices.setVisibility(vis);

    }

    public void do_send(View view) {
        String info = et_toSend.getText().toString();
        display(TXT_TYPE.GREEN,info);
        mtConnect.send(info);
    }

    public void do_action(View view) {
        //clear stuff out
        results = "";
        tv_results.setText(results);

        //see if bluetooth there
        Boolean enableUI = getBTAdapter();

        //see if we want to be a server
        b_server = cb_Server.isChecked();

        if (b_server) {
            //launch a thread to wait for connection
            mtAccept = new AcceptThread(this, mBluetoothAdapter, myCallback);
            mtAccept.start();
        }
        else {
            if (!getBTDevice(sp_devices.getSelectedItem().toString())) {
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
        et_toSend.setEnabled(bEnable && !b_server);
        b_send.setEnabled(bEnable && !b_server);

        sp_devices.setEnabled(!bEnable );

        b_action.setEnabled(!bEnable);
        cb_Server.setEnabled(!bEnable);
    }

    private boolean getBTDevice(String name){

        //get a list of devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String dev_name = device.getName();
                boolean bres = name.equals(dev_name);
                if (name.equals(dev_name))
                {
                    mBluetoothDevice = device;
                    return true;
                }
            }
        }
        return false;
    }

    private void setupSimpleSpinner(ArrayList<String> myList) {
        //create a data adapter to fill above spinner with choices

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                myList );

        sp_devices.setAdapter(arrayAdapter);

        //respond when spinner clicked
        sp_devices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public static final int SELECTED_ITEM = 0;

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long rowid) {
                if (arg0.getChildAt(SELECTED_ITEM) != null) {
//                    ((TextView) arg0.getChildAt(SELECTED_ITEM)).setTextColor(Color.WHITE);
//                    Toast.makeText(MainActivity.this, (String) arg0.getItemAtPosition(pos), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void showBTDevices(){
        if(!getBTAdapter())
            return;

        //list of all the bluetooth devices we know about
        ArrayList<String> myList = new ArrayList<String>();

        //get a list of devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                myList.add(device.getName());
            }
        }

        //show in spinner
        setupSimpleSpinner(myList);
    }
    private boolean getBTAdapter(){
        //only get it once
        if (mBluetoothAdapter != null)
            return true;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter== null){
            display(TXT_TYPE.RED,"Device does not support bluetooth");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()){
            display(TXT_TYPE.RED,"Please enable bluetooth");
            return false;
        }

        return (mBluetoothAdapter != null);
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
