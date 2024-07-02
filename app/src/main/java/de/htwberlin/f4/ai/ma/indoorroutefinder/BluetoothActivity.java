package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.api.SendDataToAPI;
import de.htwberlin.f4.ai.ma.indoorroutefinder.decentralized.BluetoothHelper;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

/**
 * Activity for handling Bluetooth communication.
 */
public class BluetoothActivity extends BaseActivity {

    public static final String BLUETOOTH_ACTIVITY = "BluetoothActivity";
    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String MESSAGE_SPLIT = "_NEW_MESSAGE_";
    private static final String MESSAGE_END = "_MESSAGE_END_";
    private static final int STATE_MESSAGE_RECEIVED = 5;
    private final ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>();
    private final Handler uiHandler = new Handler();
    private String result = "";
    private List<String> results = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceListAdapter;
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == STATE_MESSAGE_RECEIVED) {
                byte[] readBuff = (byte[]) msg.obj;
                String tempMsg = new String(readBuff, 0, msg.arg1);

                if (!tempMsg.isEmpty()) result += tempMsg;

                if (tempMsg.contains(MESSAGE_END)) {
                    results = Arrays.asList(result.split(MESSAGE_SPLIT));
                    int countNewNodes = BluetoothHelper.processReceivedMessages(results, databaseHandler, BluetoothActivity.this);

                    result = "";
                    String toastMsg = "Es wurden " + countNewNodes + " von " + results.size() + " hinzugefügt.";
                    Toast.makeText(BluetoothActivity.this, toastMsg, Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    private ListView deviceListView;
    private TextView statusTextView;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private AcceptThread acceptThread;
    private DatabaseHandler databaseHandler;
    private Button listenButton, sendButton, listDevicesButton, sendDataToApiButton, receiveDataFromApiButton;

    /**
     * Converts a timestamp string to Unix timestamp.
     *
     * @param timestampStr The timestamp string to convert.
     * @return The Unix timestamp.
     */
    public static int convertToUnixTimestamp(String timestampStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        try {
            Date date = sdf.parse(timestampStr);
            long unixTimestamp = date.getTime() / 1000;
            return (int) unixTimestamp - 7200;
        } catch (ParseException e) {
            Log.d(BLUETOOTH_ACTIVITY, "Failed to parse date", e);
            return -1;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Bluetooth Communication");
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_bluetooth, contentFrameLayout);

        initViews();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        databaseHandler = DatabaseHandlerFactory.getInstance(this);
        setupListeners();
    }

    /**
     * Initializes the views in the activity.
     */
    private void initViews() {
        listenButton = findViewById(R.id.listenButton);
        sendButton = findViewById(R.id.sendButton);
        listDevicesButton = findViewById(R.id.listDevices);
        deviceListView = findViewById(R.id.listView);
        statusTextView = findViewById(R.id.statusText);
        sendDataToApiButton = findViewById(R.id.send_to_api_button);
        receiveDataFromApiButton = findViewById(R.id.receive_data_from_api);

        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceListAdapter);
    }

    /**
     * Sets up listeners for UI elements.
     */
    private void setupListeners() {
        listDevicesButton.setOnClickListener(view -> listPairedDevices());
        listenButton.setOnClickListener(view -> startServer());
        deviceListView.setOnItemClickListener((adapterView, view, i, l) -> {
            BluetoothDevice device = pairedDevices.get(i);
            connectToBluetoothDevice(device);
        });
        sendButton.setOnClickListener(view -> sendMessage());
        sendDataToApiButton.setOnClickListener(view -> sendDataToApi());
        receiveDataFromApiButton.setOnClickListener(view -> receiveDataFromApi());
    }

    /**
     * Lists the paired Bluetooth devices.
     */
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

    /**
     * Starts the Bluetooth server.
     */
    @SuppressLint("SetTextI18n")
    private void startServer() {
        if (acceptThread != null) {
            acceptThread.cancel();
        }
        acceptThread = new AcceptThread();
        acceptThread.start();
        statusTextView.setText("Listening for incoming connections...");
    }

    /**
     * Connects to a Bluetooth device.
     *
     * @param device The Bluetooth device to connect to.
     */
    @SuppressLint({"SetTextI18n", "MissingPermission"})
    private void connectToBluetoothDevice(BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
        }
        connectThread = new ConnectThread(device);
        connectThread.start();
        statusTextView.setText("Connecting to " + device.getName() + "...");
    }

    /**
     * Sends a message via Bluetooth.
     */
    @SuppressLint("SetTextI18n")
    private void sendMessage() {
        if (connectedThread != null) {
            JSONArray data = databaseHandler.getAllMeasurementsInJSON();
            for (int i = 0; i < data.length(); i++) {
                try {
                    String element = data.get(i).toString();
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

    /**
     * Sends data to the API.
     */
    private void sendDataToApi() {
        JSONArray jsonData = databaseHandler.getAllMeasurementsInJSON();
        SendDataToAPI.Endpoint endpoint = SendDataToAPI.Endpoint.ADD_MULTIPLE_MEASUREMENTS;
        SendDataToAPI sendDataToAPI = new SendDataToAPI(endpoint, jsonData.toString(), SendDataToAPI.RequestMethod.POST, response -> {
            Log.d(BLUETOOTH_ACTIVITY, "API Response: " + response);
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "API Response: " + response, Toast.LENGTH_SHORT).show());
        });
        sendDataToAPI.execute();
    }

    /**
     * Receives data from the API.
     */
    private void receiveDataFromApi() {
        statusTextView.setText("Connecting to the API ...");
        JSONArray jsonData = new JSONArray();
        SendDataToAPI.Endpoint endpoint = SendDataToAPI.Endpoint.GET_ALL_MEASUREMENTS;
        SendDataToAPI sendDataToAPI = new SendDataToAPI(endpoint, jsonData.toString(), SendDataToAPI.RequestMethod.GET, response -> {
            runOnUiThread(() -> statusTextView.setText("Got fingerprints from the API."));
            try {
                JSONArray responseArray = new JSONArray(response);
                BluetoothHelper.processApiResponse(responseArray, databaseHandler, uiHandler, statusTextView);
            } catch (JSONException e) {
                Log.d(BLUETOOTH_ACTIVITY, "Error parsing JSON response", e);
            }
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "API Response: " + response, Toast.LENGTH_SHORT).show());
        });
        sendDataToAPI.execute();
    }

    /**
     * Handles Bluetooth connection.
     *
     * @param socket The Bluetooth socket.
     */
    private void connected(BluetoothSocket socket) {
        runOnUiThread(() -> {
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
            statusTextView.setText("Connected");
        });
    }

    /**
     * Thread for accepting incoming Bluetooth connections.
     */
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

    /**
     * Thread for connecting to a Bluetooth device.
     */
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

    /**
     * Thread for managing a Bluetooth connection.
     */
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
