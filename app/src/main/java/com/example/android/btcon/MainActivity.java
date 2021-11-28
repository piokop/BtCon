package com.example.android.btcon;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // UI Initialization
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final TextView textViewInfo = findViewById(R.id.textViewInfo);
        ToggleButton buttonToggle1 = findViewById(R.id.buttonToggle1);
        final ToggleButton buttonToggle2 = findViewById(R.id.buttonToggle2);
        final ToggleButton buttonToggle3 = findViewById(R.id.buttonToggle3);
        final ToggleButton buttonToggle4 = findViewById(R.id.buttonToggle4);
        final ToggleButton buttonToggle5 = findViewById(R.id.buttonToggle5);
        final ToggleButton buttonToggle6 = findViewById(R.id.buttonToggle6);
        buttonToggle1.setEnabled(false);
        buttonToggle2.setEnabled(false);
        buttonToggle3.setEnabled(false);
        buttonToggle4.setEnabled(false);
        buttonToggle5.setEnabled(false);
        buttonToggle6.setEnabled(false);

        final ImageView imageView = findViewById(R.id.imageView);
        imageView.setBackgroundColor(getResources().getColor(R.color.material_white));



        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            String deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            // Move to adapter list
            Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
            startActivity(intent);
        });

        /* Second most important piece of Code. GUI Handler */

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonToggle1.setEnabled(true);
                                buttonToggle2.setEnabled(true);
                                buttonToggle3.setEnabled(true);
                                buttonToggle4.setEnabled(true);
                                buttonToggle5.setEnabled(true);
                                buttonToggle6.setEnabled(true);
                                break;
                            case -1:
                                toolbar.setSubtitle("Device fails to connect");
                                progressBar.setVisibility(View.GONE);


                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        switch (arduinoMsg.toLowerCase()) {

                            case "led is turned on":           // relay 1
                                buttonToggle1.setChecked(true);
                                imageView.setBackgroundColor(getResources().getColor(R.color.material_white));
                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                            case "led is turned off":
                                buttonToggle1.setChecked(false);
                                imageView.setBackgroundColor(getResources().getColor(R.color.material_blue100));
                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;

                            case "led to on":                  // relay 2
                                buttonToggle2.setChecked(true);
                                imageView.setBackgroundColor(getResources().getColor(R.color.material_bluegrey300));
                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                            case "led to off":
                                buttonToggle2.setChecked(false);
                                imageView.setBackgroundColor(getResources().getColor(R.color.material_amber300));
                                break;

                            case "rel3on":                      // relay 3
                                buttonToggle3.setChecked((true));
                                break;
                            case "rel3off":
                                buttonToggle3.setChecked(false);
                                break;

                            case "rel4on":                      //relay 4
                                buttonToggle4.setChecked(true);
                                break;
                            case "rel4off":
                                buttonToggle4.setChecked(false);
                                break;

                            case "rel5on":                      //relay 5
                                buttonToggle5.setChecked(true);
                                break;
                            case "rel5off":
                                buttonToggle5.setChecked(false);
                                break;

                            case"rel6on":                       //relay 6
                                buttonToggle6.setChecked(true);
                                break;
                            case "rel6off":
                                buttonToggle6.setChecked(false);
                                break;
                        }
                        break;
                }
            }
        };

  // switches
        buttonToggle1.setOnClickListener(view -> {
            String cmdText = null;
            if (view.getId() == R.id.buttonToggle1) {
                if (buttonToggle1.isChecked()) {
                    // command for the controller
                    cmdText = "rel1_ON\r";
                } else {
                    cmdText = "rel1_OFF\r";
                }
            }
            // Send command to Arduino board
            assert cmdText != null;
            connectedThread.write(cmdText);
        });


        buttonToggle2.setOnClickListener(view -> {
            String cmdText = null;
            if (view.getId() == R.id.buttonToggle2) {
                if (buttonToggle2.isChecked()) {
                    // command for the controller
                    cmdText = "rel2_ON\r";
                } else {
                    cmdText = "rel2_OFF\r";
                }
            }
            // Send command to Arduino board
            assert cmdText != null;
            connectedThread.write(cmdText);
        });


        buttonToggle3.setOnClickListener(view -> {
            String cmdText = null;
            if (view.getId() == R.id.buttonToggle3) {
                if (buttonToggle3.isChecked()) {
                    // command for the controller
                    cmdText = "rel3_ON\r";
                } else {
                    cmdText = "rel3_OFF\r";
                }
            }
            // Send command to Arduino board
            assert cmdText != null;
            connectedThread.write(cmdText);
        });


        buttonToggle4.setOnClickListener(view -> {
            String cmdText = null;
            if (view.getId() == R.id.buttonToggle4) {
                if (buttonToggle4.isChecked()) {
                    // command for the controller
                    cmdText = "rel4_ON\r";
                } else {
                    cmdText = "rel4_OFF\r";
                }
            }
            // Send command to Arduino board
            assert cmdText != null;
            connectedThread.write(cmdText);
        });


        buttonToggle5.setOnClickListener(view -> {
            String cmdText = null;
            if (view.getId() == R.id.buttonToggle5) {
                if (buttonToggle5.isChecked()) {
                    // command for the controller
                    cmdText = "rel5_ON\r";
                } else {
                    cmdText = "rel5_OFF\r";
                }
            }
            // Send command to Arduino board
            assert cmdText != null;
            connectedThread.write(cmdText);
        });


        buttonToggle6.setOnClickListener(view -> {
            String cmdText = null;
            if (view.getId() == R.id.buttonToggle6) {
                if (buttonToggle6.isChecked()) {
                    // command for the controller
                    cmdText = "rel6_ON\r";
                } else {
                    cmdText = "rel6_OFF\r";
                }
            }
            // Send command to Arduino board
            assert cmdText != null;
            connectedThread.write(cmdText);
        });

    }


    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
//             Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes);
                        Log.e("Arduino Message", readMessage);
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

//            while (true) {
//                try {
//                    // Read from the InputStream
//                    bytes = mmInStream.read(buffer);
//                    String str = new String(buffer, 0, bytes);
//                    Log.i(TAG, "mmInStream - " + str);
//                    // Send the obtained bytes to the UI Activity
//                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
//                } catch (IOException e) {
//                    Log.e(TAG, "disconnected", e);
////                    connectionLost();
//                    break;
//                }
//            }

        }


        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null){
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }



}