package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.api.SendDataToAPI;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformationFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.RoomFactory;


public class BluetoothActivity extends BaseActivity {

    public static final String BLUETOOTH_ACTIVITY = "BluetoothActivity";
    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String MESSAGE_SPLIT = "_NEW_MESSAGE_";
    private static final String MESSAGE_END = "_MESSAGE_END_";
    private static final int STATE_MESSAGE_RECEIVED = 5;
    private final ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>();
    String result = "";
    List<String> results = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceListAdapter;
    private Button listenButton, sendButton, listDevicesButton, sendDataToApiButton;
    private ListView deviceListView;
    private TextView statusTextView;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private AcceptThread acceptThread;
    private DatabaseHandler databaseHandler;
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == STATE_MESSAGE_RECEIVED) {
                byte[] readBuff = (byte[]) msg.obj;
                String tempMsg = new String(readBuff, 0, msg.arg1);

                if (!tempMsg.isEmpty()) result += tempMsg;

                if (tempMsg.contains(MESSAGE_END)) {

                    int countNewNodes = 0;

                    results = Arrays.asList(result.split(MESSAGE_SPLIT));

                    for (int i = 0; i < results.size(); i++) {
                        if (results.get(i).endsWith(MESSAGE_END)) {
                            results.set(i, results.get(i).substring(0, results.get(i).length() - (MESSAGE_END).length()));
                        }
                    }

                    Log.d(BLUETOOTH_ACTIVITY, "Results: " + results);

                    for (String result : results) {
                        if (result.isEmpty()) continue;
                        try {
                            JSONObject json = new JSONObject(result);
                            List<AccessPointInformation> accessPointInformationList = new ArrayList<>();
                            JSONArray scan = new JSONArray(json.getString("ScanResults"));
                            for (int i = 0; i < scan.length(); i++) {
                                JSONObject scanEntry = scan.getJSONObject(i);
                                String SSID = scanEntry.getString("SSID");
                                String BSSID = scanEntry.getString("BSSID");
                                int level = scanEntry.getInt("Level");
                                AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(BSSID, level, SSID);
                                accessPointInformationList.add(accessPointInformation);
                            }

                            SignalSample signalSample = new SignalSample(json.getLong("Timestamp"), accessPointInformationList);
                            List<SignalSample> signalSampleList = new ArrayList<>();
                            signalSampleList.add(signalSample);
                            Fingerprint fingerprint = FingerprintFactory.createInstance(signalSampleList);
                            fingerprint.setDeviceID(json.getString("DeviceID"));
                            Room room = RoomFactory.createInstance(json.getString("Room"), "", fingerprint, "", "", "");
                            Log.d(BLUETOOTH_ACTIVITY, "Room: " + room);
                            countNewNodes += databaseHandler.insertOrUpdateRoom(room);
                        } catch (Exception ignored) {
                        }
                    }

                    result = "";

                    String toastMsg = "Es wurden " + countNewNodes + " von " + results.size() + " hinzugefügt.";
                    Toast.makeText(BluetoothActivity.this, toastMsg, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Bluetooth Communication");
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_bluetooth, contentFrameLayout);

        initViews();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // TODO: Check if bluetooth is supported
//        if (bluetoothAdapter == null) {
//            // Bluetooth is not supported on this device
//            // Handle this case
//            return;
//        }

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        setupListeners();
    }

    private void initViews() {
        listenButton = findViewById(R.id.listenButton);
        sendButton = findViewById(R.id.sendButton);
        listDevicesButton = findViewById(R.id.listDevices);
        deviceListView = findViewById(R.id.listView);
        statusTextView = findViewById(R.id.statusText);
        sendDataToApiButton = findViewById(R.id.send_to_api_button);

        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceListAdapter);
    }

    private void setupListeners() {
        listDevicesButton.setOnClickListener(view -> listPairedDevices());

        listenButton.setOnClickListener(view -> startServer());

        deviceListView.setOnItemClickListener((adapterView, view, i, l) -> {
            BluetoothDevice device = pairedDevices.get(i);
            connectToBluetoothDevice(device);
        });

        sendButton.setOnClickListener(view -> {
            sendMessage();
        });

        sendDataToApiButton.setOnClickListener(view -> {
            Log.d(BLUETOOTH_ACTIVITY, "Send data to API button pressed");
            sendDataToApi();
        });
    }

    @SuppressLint("MissingPermission")
    private void listPairedDevices() {
        Set<BluetoothDevice> pairedDevicesSet = bluetoothAdapter.getBondedDevices();
        pairedDevices.clear();
        deviceListAdapter.clear();
        if (!pairedDevicesSet.isEmpty()) {
            for (BluetoothDevice device : pairedDevicesSet) {
                pairedDevices.add(device);
                deviceListAdapter.add(device.getName());
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void startServer() {
        if (acceptThread != null) {
            acceptThread.cancel();
        }
        acceptThread = new AcceptThread();
        acceptThread.start();
        statusTextView.setText("Listening for incoming connections...");
    }


    @SuppressLint({"SetTextI18n", "MissingPermission"})
    private void connectToBluetoothDevice(BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
        }
        connectThread = new ConnectThread(device);
        connectThread.start();
        statusTextView.setText("Connecting to " + device.getName() + "...");
    }

    @SuppressLint("SetTextI18n")
    private void sendMessage() {
        Log.d(BLUETOOTH_ACTIVITY, "Connected thread: " + connectedThread);
        if (connectedThread != null) {

            JSONArray data = databaseHandler.getAllMeasurementsInJSON();
            for (int i = 0; i < data.length(); i++) {
                try {
                    String element = data.get(i).toString();
                    Log.d(BLUETOOTH_ACTIVITY, element);
                    byte[] byteElements = element.getBytes();
                    if (i != 0) connectedThread.write(MESSAGE_SPLIT.getBytes());
                    connectedThread.write(byteElements);
                } catch (JSONException ignored) {

                }
            }
            connectedThread.write(MESSAGE_END.getBytes());

        } else {
            statusTextView.setText("Not connected to any device");
        }
    }

    private void sendDataToApi() {
        Log.d(BLUETOOTH_ACTIVITY, "Sending data to API");
        String jsonData = databaseHandler.getAllMeasurementsInJSON().toString();
        SendDataToAPI.Endpoint endpoint = SendDataToAPI.Endpoint.ADD_MEASUREMENTS;
        SendDataToAPI sendDataToAPI = new SendDataToAPI(endpoint, jsonData, SendDataToAPI.RequestMethod.POST, response -> {
            Log.d(BLUETOOTH_ACTIVITY, "API Response: " + response);
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "API Response: " + response, Toast.LENGTH_SHORT).show());
        });
        sendDataToAPI.execute();
    }


    private void connected(BluetoothSocket socket) {
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                connectedThread = new ConnectedThread(socket);
                connectedThread.start();
                statusTextView.setText("Connected");
            }
        });
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                Log.d(BLUETOOTH_ACTIVITY, "Socket's listen() method failed", e);
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.d(BLUETOOTH_ACTIVITY, "Socket's accept() method failed", e);
                    break;
                }
                if (socket != null) {
                    connected(socket);
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.d(BLUETOOTH_ACTIVITY, "Could not close the connect socket", e);
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.d(BLUETOOTH_ACTIVITY, "Could not close the connect socket", e);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.d(BLUETOOTH_ACTIVITY, "Socket's create() method failed", e);
            }
            socket = tmp;
        }

        @SuppressLint("SetTextI18n")
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
            } catch (IOException connectException) {
                Log.d(BLUETOOTH_ACTIVITY, "Could not connect to the device", connectException);
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.d(BLUETOOTH_ACTIVITY, "Could not close the client socket", closeException);
                    runOnUiThread(() -> statusTextView.setText("Error... Please try again"));
                }
                return;
            }
            connected(socket);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.d(BLUETOOTH_ACTIVITY, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(BLUETOOTH_ACTIVITY, "Error occurred when creating input stream", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        @SuppressLint("SetTextI18n")
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.d(BLUETOOTH_ACTIVITY, "Input stream was disconnected", e);
                    runOnUiThread(() -> statusTextView.setText("Error... Please try again"));
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.d(BLUETOOTH_ACTIVITY, "Error occurred when sending data", e);
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.d(BLUETOOTH_ACTIVITY, "Could not close the connect socket", e);
            }
        }
    }
}
