package com.osvaldo.bluetoothtens;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    Button btn1, btn2, btn3, btn4, btn5, btn6;
    TextView txtString, txtStringLength, sensorView0;
    TextView txtSendorLDR;
    ToggleButton toggleLed, toggleCalibrate;
    Handler bluetoothIn;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    // Helping variables
    int contA = 0, contB = 0, contC = 0, contD = 0, contE = 0, contF = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Link the buttons and textViews to respective views

        txtString = (TextView) findViewById(R.id.txtString);
        txtStringLength = (TextView) findViewById(R.id.textView1);
        toggleLed = (ToggleButton) findViewById(R.id.toggleBtLed);
        toggleCalibrate = (ToggleButton) findViewById(R.id.toggleBtCalibrate);

        btn1 = (Button) findViewById(R.id.btMotor1);
        btn2 = (Button) findViewById(R.id.btMotor2);
        btn3 = (Button) findViewById(R.id.btMotor3);
        btn4 = (Button) findViewById(R.id.btMotor4);
        btn5 = (Button) findViewById(R.id.btMotor5);
        btn6 = (Button) findViewById(R.id.btMotor6);

        sensorView0 = (TextView) findViewById(R.id.sensorView0);

        txtSendorLDR = (TextView) findViewById(R.id.tv_sendorldr);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        //txtString.setText("Received data = " + dataInPrint);
                        txtString.setText("Data received");
                        Log.d("Received data", dataInPrint);
                        int dataLength = dataInPrint.length();							//get length of data received
                        txtStringLength.setText("String size = " + String.valueOf(dataLength));

                        if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
                        {
                            String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
                            String sensor1 = recDataString.substring(6, 10);            //same again...
                            String sensor2 = recDataString.substring(11, 15);
                            String sensor3 = recDataString.substring(16, 20);

                            if(sensor0.equals("1.00"))
                                sensorView0.setText("On");	//update the textviews with sensor values
                            else
                                sensorView0.setText("Off");	//update the textviews with sensor values
                        }
                        Log.d("recDataString", recDataString.toString());
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        toggleLed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.d("TOGGLE LED", "LED ON");
                    mConnectedThread.write("L");

                }else{
                    Log.d("TOGGLE LED", "LED OFF");
                    mConnectedThread.write("O");
                }
            }
        });

        toggleCalibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.d("TOGGLE", "Is checked");
                    mConnectedThread.write("K");
                }else{
                    Log.d("TOGGLE", "Is not checked");
                    mConnectedThread.write("k");
                    contA = 0; contB = 0; contC = 0; contD = 0; contE = 0; contF = 0;
                }
            }
        });

        /********** Button Motor 1 *************/
        btn1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("BTN1", "Bt1 moved down!");
                    if(toggleCalibrate.isChecked()){
                        contA++;
                        mConnectedThread.write("a");
                        Log.d("BTN1 Write", "a");
                    }
                    Log.d("BTN1 contA", ""+ contA);
                    return true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("BTN1", "Bt1 moved UP!");
                    if (toggleCalibrate.isChecked()) {
                        contA++;
                        mConnectedThread.write("a");
                        Log.d("BTN1 Write", "a");
                        if(contA == 4){
                            Log.d("BTN1 contA 4", ""+ contA);
                            toggleCalibrate.setChecked(false);      // Stop calibration
                            contA = 0;
                        }
                    }else{
                        mConnectedThread.write("a");
                        Log.d("BTN1 Write", "a");
                    }
                    Log.d("BTN1 contA", ""+ contA);
                    return true;
                }
                return false;
            }
        });

        /********** Button Motor 2 *************/
        btn2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("BTN2", "Bt2 moved down!");
                    if(toggleCalibrate.isChecked()){
                        contB++;
                        mConnectedThread.write("b");
                        Log.d("BTN2 Write", "b");
                    }
                    Log.d("BTN2 contB", ""+ contB);
                    return true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("BTN2", "Bt2 moved UP!");
                    if (toggleCalibrate.isChecked()) {
                        contB++;
                        mConnectedThread.write("b");
                        Log.d("BTN2 Write", "b");
                        if(contB == 4){
                            Log.d("BTN2 contB 4", ""+ contB);
                            toggleCalibrate.setChecked(false);      // Stop calibration
                            contB = 0;
                        }
                    }else{
                        mConnectedThread.write("b");
                        Log.d("BTN2 Write", "b");
                    }
                    Log.d("BTN2 contB", ""+ contB);
                    return true;
                }
                return false;
            }
        });


        /********** Button Motor 3 *************/
        btn3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("BTN3", "Bt3 moved down!");
                    if(toggleCalibrate.isChecked()){
                        contC++;
                        mConnectedThread.write("c");
                        Log.d("BTN3 Write", "c");
                    }
                    Log.d("BTN3 contC", ""+ contC);
                    return true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("BTN3", "Bt3 moved UP!");
                    if (toggleCalibrate.isChecked()) {
                        contC++;
                        mConnectedThread.write("c");
                        Log.d("BTN3 Write", "c");
                        if(contC == 4){
                            Log.d("BTN3 contC 4", ""+ contC);
                            toggleCalibrate.setChecked(false);      // Stop calibration
                            contC = 0;
                        }
                    }else{
                        mConnectedThread.write("c");
                        Log.d("BTN3 Write", "c");
                    }
                    Log.d("BTN3 contC", ""+ contC);
                    return true;
                }
                return false;
            }
        });


        /********** Button Motor 4 *************/
        btn4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("BTN4", "Bt4 moved down!");
                    if(toggleCalibrate.isChecked()){
                        contD++;
                        mConnectedThread.write("d");
                        Log.d("BTN4 Write", "d");
                    }
                    Log.d("BTN4 contD", ""+ contD);
                    return true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("BTN4", "Bt4 moved UP!");
                    if (toggleCalibrate.isChecked()) {
                        contD++;
                        mConnectedThread.write("d");
                        Log.d("BTN4 Write", "d");
                        if(contD == 4){
                            Log.d("BTN4 contD 4", ""+ contD);
                            toggleCalibrate.setChecked(false);      // Stop calibration
                            contD = 0;
                        }
                    }else{
                        mConnectedThread.write("d");
                        Log.d("BTN4 Write", "d");
                    }
                    Log.d("BTN4 contD", ""+ contD);
                    return true;
                }
                return false;
            }
        });



        /********** Button Motor 5 *************/
        btn5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("BTN5", "Bt5 moved down!");
                    if(toggleCalibrate.isChecked()){
                        contE++;
                        mConnectedThread.write("e");
                        Log.d("BTN5 Write", "e");
                    }
                    Log.d("BTN5 contE", ""+ contE);
                    return true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("BTN5", "Bt5 moved UP!");
                    if (toggleCalibrate.isChecked()) {
                        contE++;
                        mConnectedThread.write("e");
                        Log.d("BTN5 Write", "e");
                        if(contE == 4){
                            Log.d("BTN5 contE 4", ""+ contE);
                            toggleCalibrate.setChecked(false);      // Stop calibration
                            contE = 0;
                        }
                    }else{
                        mConnectedThread.write("e");
                        Log.d("BTN5 Write", "e");
                    }
                    Log.d("BTN5 contE", ""+ contE);
                    return true;
                }
                return false;
            }
        });



        /********** Button Motor 6 *************/
        btn6.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("BTN6", "Bt6 moved down!");
                    if(toggleCalibrate.isChecked()){
                        contF++;
                        mConnectedThread.write("f");
                        Log.d("BTN6 Write", "f");
                    }
                    Log.d("BTN6 contF", ""+ contF);
                    return true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("BTN6", "Bt6 moved UP!");
                    if (toggleCalibrate.isChecked()) {
                        contF++;
                        mConnectedThread.write("f");
                        Log.d("BTN6 Write", "f");
                        if(contF == 4){
                            Log.d("BTN6 contF 4", ""+ contF);
                            toggleCalibrate.setChecked(false);      // Stop calibration
                            contF = 0;
                        }
                    }else{
                        mConnectedThread.write("f");
                        Log.d("BTN6 Write", "f");
                    }
                    Log.d("BTN6 contF", ""+ contF);
                    return true;
                }
                return false;
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivity via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "This device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection failed! Try again.", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}

