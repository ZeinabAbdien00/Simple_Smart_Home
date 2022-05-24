package com.example.p1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


public class Choosing extends Activity {
    private static final String TAG = "BlueTest5-Controlling";
    private int mMaxChars = 50000;//Default//change this to string..........
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;

    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnected = false;

    private Button mBtnDisconnect;
    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;

    private static final int BT_ENABLE_REQUEST = 10; // This is the code we use for BT Enable
    private static final int SETTINGS = 20;
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int mBufferSize = 50000; //Default
    public static final String DEVICE_EXTRA = "com.example.servoled.SOCKET";
    public static final String DEVICE_UUID = "com.example.servoled.uuid";
    private static final String DEVICE_LIST = "com.example.servoled.devicelist";
    private static final String DEVICE_LIST_SELECTED = "com.example.servoled.devicelistselected";
    public static final String BUFFER_SIZE = "com.example.servoled.buffersize";
    //private int mBufferSize = 50000; //Default

    CardView air_condition ,  lights , door , garage;
    TextView mTxtReceive , date1 , textView;
    final static String air="123";//off

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing);

        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(DEVICE_UUID));
        mMaxChars = b.getInt(BUFFER_SIZE);

        Log.d(TAG, "Ready");

        air_condition = findViewById(R.id.air_condition);
        lights = findViewById(R.id.lights);
        door = findViewById(R.id.door);
        garage = findViewById(R.id.garage);
        mTxtReceive = findViewById(R.id.txtReceive);
        date1 = findViewById(R.id.date1);
        textView = findViewById(R.id.textView);

        Date h = new  Date();
        int hour = h.getHours();

        if(hour>=12)
        {
            textView.setText("Good Evening");
        }else
            {
                textView.setText("Good Morning");

            }


        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        date1.setText(date.toString());

        lights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent led_intent = new Intent(getApplicationContext(), lightActivity.class);
                led_intent.putExtra(DEVICE_EXTRA, mDevice);
                led_intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                led_intent.putExtra(BUFFER_SIZE, mBufferSize);
                startActivity(led_intent);
            }
        });

        door.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent door_intent = new Intent(getApplicationContext(), doorActivity.class);
                door_intent.putExtra(DEVICE_EXTRA, mDevice);
                door_intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                door_intent.putExtra(BUFFER_SIZE, mBufferSize);
                startActivity(door_intent);
            }
        });

        air_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent air_condition_intent = new Intent(getApplicationContext(), tempActivity.class);
                try {
                    mBTSocket.getOutputStream().write(air.getBytes());

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                air_condition_intent.putExtra(DEVICE_EXTRA, mDevice);
                air_condition_intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                air_condition_intent.putExtra(BUFFER_SIZE, mBufferSize);
                startActivity(air_condition_intent);
            }
        });

        garage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent garage_intent = new Intent(getApplicationContext(), garageActivity.class);
                garage_intent.putExtra(DEVICE_EXTRA, mDevice);
                garage_intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                garage_intent.putExtra(BUFFER_SIZE, mBufferSize);
                startActivity(garage_intent);
            }
        });

    }

    private class ReadInput implements Runnable {
        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        String strInput = new String(buffer, 0, i);





                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {//cant inderstand these dotss

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
// TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(Choosing.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554

        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
// Unable to connect to device`
                // e.printStackTrace();
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device.Please turn on your Hardware", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}