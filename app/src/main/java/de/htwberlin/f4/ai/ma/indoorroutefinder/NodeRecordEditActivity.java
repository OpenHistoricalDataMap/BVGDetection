package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.deviceID.UniqueIDManager;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.AsyncResponse;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintTask;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.FileUtilities;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.JSON.JSONWriter;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.RoomFactory;


/**
 * Created by Johann Winter
 * <p>
 * This class handles the recording and editing of nodes.
 * <p>
 * Icon sources:
 * <a href="https://www.iconfinder.com/icons/322425/camera_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/115789/trash_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/809537/diskette_guardar_multimedia_save_save_disk_technology_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/1608681/exchange_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/2135802/communication_network_tower_wifi_wifi_tower_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/492103/directions_location_navigation_search_socialmedia_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/2135924/location_map_navigation_pointer_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/352562/navigation_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/339913/help_info_information_notice_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/2135801/communication_internet_network_wifi_icon">...</a>
 * <a href="https://www.iconfinder.com/icons/2075795/arrow_below_down_low_icon">...</a>
 * <a href="https://www.flaticon.com/free-icon/fingerprint-with-crosshair-focus_25927">...</a>
 * <a href="http://icons.iconarchive.com/icons/custom-icon-design/flatastic-1/48/export-icon.png">...</a>
 * <a href="http://icons.iconarchive.com/icons/custom-icon-design/flatastic-1/48/import-icon.png">...</a>
 * <a href="https://thenounproject.com/search/?q=connect&i=1227146">...</a>
 */
public class NodeRecordEditActivity extends BaseActivity implements AsyncResponse {

    public static final String NODE_RECORD_EDIT_ACTIVITY = "NodeRecordEditActivity";
    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 3;
    private static final int CAM_REQUEST = 1;
    private final File sdCard = Environment.getExternalStorageDirectory();
    private final Context context = this;
    String[] permissions;
    TextView initialWifiTextview;
    TextView initialWifiLabelTextview;
    TextView coordinatesLabelTextview;
    ImageButton showFingerprintButton;
    ImageButton captureButton;
    ImageButton saveNodeButton;
    RelativeLayout buttonsLayout;
    private String picturePath;
    private String oldNodeId = null;
    private List<String> oldPicturePaths;
    private int measures;
    private int progressStatus = 0;
    private ProgressBar progressBar;
    private JSONWriter JSONWriter;
    private TextView progressTextview;
    private TextView infobox;
    private ImageButton recordButton;
    private ImageView cameraImageview;
    private AutoCompleteTextView nodeIdEdittext;
    private String[] nodeIdEdittextSuggestions;
    private EditText descriptionEdittext;
    private EditText coordinatesEdittext;
    private DatabaseHandler databaseHandler;
    private SharedPreferences sharedPreferences;
    private Spinner measureCount;
    private boolean pictureTaken;
    private boolean takingPictureAtTheMoment;
    private boolean showingBigPictureAtTheMoment;
    private boolean updateMode = false;
    private boolean verboseMode;
    private boolean useSSIDfilter;
    private Room roomToUpdate;
    private WifiManager wifiManager;
    private Timestamp timestamp;
    private Fingerprint fingerprint = null;
    private FingerprintTask fingerprintTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_node_record_edit, contentFrameLayout);
        setTitle(getString(R.string.title_activity_recordedit_rec));


        List<String> permissionList = new ArrayList<>();

        permissionList.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionList.add(Manifest.permission.BLUETOOTH);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionList.add(Manifest.permission.ACCESS_WIFI_STATE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionList.add(Manifest.permission.ACCESS_WIFI_STATE);
            permissionList.add(Manifest.permission.CHANGE_WIFI_STATE);
        } else {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionList.add(Manifest.permission.ACCESS_WIFI_STATE);
            permissionList.add(Manifest.permission.CHANGE_WIFI_STATE);
        }

        String[] permissions = permissionList.toArray(new String[0]);

        // Check permissions
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(NodeRecordEditActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }

        databaseHandler = DatabaseHandlerFactory.getInstance(this);
        JSONWriter = new JSONWriter();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        oldPicturePaths = new ArrayList<>();


        recordButton = findViewById(R.id.record_button);
        captureButton = findViewById(R.id.capture_button);
        saveNodeButton = findViewById(R.id.save_node_button);
        showFingerprintButton = findViewById(R.id.show_fingerprint_button);
        cameraImageview = findViewById(R.id.camera_imageview);
        descriptionEdittext = findViewById(R.id.description_edittext);
//        nodeIdEdittext = findViewById(R.id.record_id_edittext);
        coordinatesEdittext = findViewById(R.id.coordinates_edittext);
        progressTextview = findViewById(R.id.progress_textview);
        initialWifiTextview = findViewById(R.id.initial_wifi_textview);
        initialWifiLabelTextview = findViewById(R.id.initial_wifi_label_textview);
        coordinatesLabelTextview = findViewById(R.id.coordinates_label_textview_editmode);
        infobox = findViewById(R.id.infobox_record_edit);
        progressBar = findViewById(R.id.progress_bar);
        measureCount = findViewById(R.id.measure_count_dropdown);

        List<Room> roomsList = databaseHandler.getAllRooms();
        nodeIdEdittextSuggestions = roomsList.stream()
                .map(Room::getRoomName)
                .toArray(String[]::new);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nodeIdEdittextSuggestions);
        nodeIdEdittext = findViewById(R.id.record_id_edittext);
        nodeIdEdittext.setAdapter(adapter);
        nodeIdEdittext.setThreshold(1);

        buttonsLayout = findViewById(R.id.buttons_layout_rec_and_edit);

        picturePath = null;

        pictureTaken = false;
        takingPictureAtTheMoment = false;
        showingBigPictureAtTheMoment = false;

        useSSIDfilter = sharedPreferences.getBoolean("use_ssid_filter", false);
        if (useSSIDfilter) {
            String ssidFilter = sharedPreferences.getString("default_wifi_network", null);
            initialWifiTextview.setText(ssidFilter);
        } else {
            initialWifiTextview.setText(getString(R.string.no_ssid_filter));
        }

        recordButton.setImageResource(R.drawable.fingerprint);
        captureButton.setImageResource(R.drawable.camera);

        progressBar.setVisibility(View.INVISIBLE);
        progressTextview.setVisibility(View.INVISIBLE);
        coordinatesLabelTextview.setVisibility(View.INVISIBLE);
        coordinatesEdittext.setVisibility(View.INVISIBLE);


        // Minutes selection dropdown
        List<Integer> minutesList = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutesList.add(i + 1);
        }
        ArrayAdapter<Integer> minutesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, minutesList);
        measureCount.setAdapter(minutesAdapter);


        // Check if Update-Mode is enabled
        // TODO: Change nodeId to roomDatabaseID
        Intent intent = getIntent();
        if (intent.hasExtra("nodeId")) {
            updateMode = true;
            setTitle(getString(R.string.title_activity_recordedit_edit));
            oldNodeId = (String) intent.getExtras().get("nodeId");
            roomToUpdate = databaseHandler.getRoom(oldNodeId);
            nodeIdEdittext.setText(roomToUpdate.getRoomName());
            descriptionEdittext.setText(roomToUpdate.getDescription());
            picturePath = roomToUpdate.getPicturePath();

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.END_OF, R.id.save_node_button);

            ImageButton deleteNodeButton = new ImageButton(this);
            deleteNodeButton.setLayoutParams(params);
            deleteNodeButton.setImageResource(R.drawable.trash_node);
            buttonsLayout.addView(deleteNodeButton);

            if (roomToUpdate.getFingerprint() != null) {
                recordButton.setImageResource(R.drawable.fingerprint_done);
                showFingerprintButton.setImageResource(R.drawable.info);

//                initialWifiTextview.setText(roomToUpdate.getFingerprint().getSsid());
                initialWifiTextview.setText(getString(R.string.no_ssid_filter));

//                if (roomToUpdate.getFingerprint().getSsid() == null) {
//                    initialWifiTextview.setText(getString(R.string.no_ssid_filter));
//                }
            } else {
                initialWifiTextview.setText("-");
            }

            if (!roomToUpdate.getCoordinates().isEmpty()) {
                coordinatesEdittext.setVisibility(View.VISIBLE);
                coordinatesLabelTextview.setVisibility(View.VISIBLE);
                coordinatesEdittext.setText(roomToUpdate.getCoordinates());
            }

            if (picturePath == null) {
                Glide.with(this).load(R.drawable.unknown).into(cameraImageview);
            } else {
                Glide.with(this).load(picturePath).into(cameraImageview);
            }

            deleteNodeButton.setOnClickListener(view -> new AlertDialog.Builder(view.getContext())
                    .setTitle(getString(R.string.delete_entry_title_question))
                    .setMessage("Soll der Ort \"" + oldNodeId + "\" wirklich gelöscht werden?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                        if (roomToUpdate.getPicturePath() != null) {
                            File imageFile = new File(roomToUpdate.getPicturePath());
                            imageFile.delete();
                        }
                        databaseHandler.deleteRoom(roomToUpdate);

                        finish();
                        Intent intent1 = new Intent(context, NodeListActivity.class);
                        startActivity(intent1);
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show());

            showFingerprintButton.setOnClickListener(view -> {
                Intent intent12 = new Intent(context, ShowFingerprintActivity.class);
                intent12.putExtra("nodeID", roomToUpdate.getRoomName());
                startActivity(intent12);
            });
        }


        saveNodeButton.setImageResource(R.drawable.save);

        recordButton.setOnClickListener(v -> {
            if (nodeIdEdittext.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), getString(R.string.please_enter_node_name), Toast.LENGTH_SHORT).show();
            } else {
                recordButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                progressTextview.setVisibility(View.VISIBLE);
                recordButton.setImageResource(R.drawable.fingerprint_low_contrast);
                measures = measureCount.getSelectedItemPosition() + 1;

                verboseMode = sharedPreferences.getBoolean("verbose_mode", false);
                String ssidFilterString = null;

                if (verboseMode) {
                    if (useSSIDfilter) {
                        ssidFilterString = sharedPreferences.getString("default_wifi_network", null);
                    }
                    fingerprintTask = new FingerprintTask(ssidFilterString, measures, wifiManager, false, progressBar, progressTextview, infobox);
                } else {
                    if (useSSIDfilter) {
                        ssidFilterString = sharedPreferences.getString("default_wifi_network", null);
                    }
                    infobox.setText(getString(R.string.please_stay));
                    fingerprintTask = new FingerprintTask(ssidFilterString, measures, wifiManager, false, progressBar, progressTextview);
                }

                fingerprintTask.delegate = NodeRecordEditActivity.this;
                fingerprintTask.execute();
            }
        });


        captureButton.setOnClickListener(view -> {
            if (nodeIdEdittext.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), getString(R.string.please_enter_node_name), Toast.LENGTH_SHORT).show();
            } else {
                takingPictureAtTheMoment = true;
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                timestamp = new Timestamp(System.currentTimeMillis());
                File file = FileUtilities.getFile(NodeRecordEditActivity.this, nodeIdEdittext.getText().toString(), timestamp);
                System.out.println(file);
                Uri photoUri = FileProvider.getUriForFile(NodeRecordEditActivity.this, getPackageName() + ".provider", file);
                System.out.println(photoUri);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, CAM_REQUEST);
            }
        });


        cameraImageview.setOnClickListener(view -> {
            showingBigPictureAtTheMoment = true;
            if (pictureTaken) {
                Intent intent13 = new Intent(getApplicationContext(), MaxPictureActivity.class);
                intent13.putExtra("picturePath", picturePath);
                intent13.putExtra("nodeID", nodeIdEdittext.getText().toString());
                startActivity(intent13);
            } else if (roomToUpdate != null && roomToUpdate.getPicturePath() != null) {

                Intent intent13 = new Intent(getApplicationContext(), MaxPictureActivity.class);
                intent13.putExtra("picturePath", roomToUpdate.getPicturePath());
                intent13.putExtra("nodeID", roomToUpdate.getRoomName());
                startActivity(intent13);
            }
        });

        saveNodeButton.setOnClickListener(view -> {
            if (updateMode) {
                checkUpdatedNodeID();
            } else {
                saveNewNode();
            }
        });
    }


    /**
     * If the fingerprinting background task finished
     *
     * @param fp the Fingerprint from the AsyncTask
     */
    @Override
    public void processFinish(Fingerprint fp, int seconds) {
        fingerprint = fp;
        fingerprint.setDeviceID(UniqueIDManager.getUniqueID(this));
        infobox.setText(R.string.record_and_edit_infobox);

        recordButton.setImageResource(R.drawable.fingerprint_done);
        progressBar.setVisibility(View.INVISIBLE);
        progressTextview.setVisibility(View.INVISIBLE);
    }


    /**
     * When returning from Camera Activity after taking a picture
     * if a picture was taken (and not just returned),
     * save the path and show the picture in the imageview.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            nodeIdEdittext.setEnabled(false);
            pictureTaken = true;
            takingPictureAtTheMoment = false;

            oldPicturePaths.add(picturePath);

            long realTimestamp = timestamp.getTime();
            picturePath = context.getFilesDir() + "/IndoorPositioning/Pictures/" + nodeIdEdittext.getText() + "_" + realTimestamp + ".jpg";
            Glide.with(this).load(picturePath).into(cameraImageview);
        }
    }


    /**
     * Create and persist the new Node.
     */
    private void saveNewNode() {
        if (nodeIdEdittext.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_enter_node_name), Toast.LENGTH_SHORT).show();
        } else {

            final String picPathToSave;
            if (pictureTaken) {
                long realTimestamp = timestamp.getTime();
                picPathToSave = context.getFilesDir() + "/IndoorPositioning/Pictures/" + nodeIdEdittext.getText() + "_" + realTimestamp + ".jpg";
            } else {
                picPathToSave = null;
            }

            final String roomName = nodeIdEdittext.getText().toString();
            final String nodeDescription = descriptionEdittext.getText().toString();

            // If no fingerprint has been captured...
            if (fingerprint == null) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.no_fingerprint_title_text))
                        .setMessage("Soll der Ort \"" + nodeIdEdittext.getText().toString() + "\" wirklich ohne Fingerprint erstellt werden?")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            final Room room = RoomFactory.createInstance(roomName, nodeDescription, null, "", picPathToSave, "");
                            JSONWriter.writeJSON(room);
                            databaseHandler.insertOrUpdateRoom(room);
                            Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();
                            deleteOldPictures();
                            resetUiElements();
                            askForNewNode();
                        })
                        .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                // If a fingerprint has been captured...
            } else {
                final Room room = RoomFactory.createInstance(roomName, nodeDescription, fingerprint, "", picPathToSave, "");
                JSONWriter.writeJSON(room);
                Log.d(NODE_RECORD_EDIT_ACTIVITY, "Fingerprint: " + fingerprint.toString());
                databaseHandler.insertOrUpdateRoom(room);
                progressStatus = 0;
                progressTextview.setText(String.valueOf(progressStatus));
                progressBar.setProgress(progressStatus);
                Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();
                deleteOldPictures();
                askForNewNode();
            }

        }
    }


    /**
     * Check if the given nodeID is valid
     */
    private void checkUpdatedNodeID() {
        if (nodeIdEdittext.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_enter_node_name), Toast.LENGTH_SHORT).show();
        } else {
            if (oldNodeId.equals(nodeIdEdittext.getText().toString())) {
                // old id == new id -> update.
                saveUpdatedNode();
            } else {
                if (databaseHandler.checkIfRoomExists(nodeIdEdittext.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.node_already_exists_toast), Toast.LENGTH_LONG).show();
                } else {
                    saveUpdatedNode();
                }
            }
        }
    }


    /**
     * Save an updated Node to database
     */
    private void saveUpdatedNode() {
        final String picPathToSave;
        final String nodeID = nodeIdEdittext.getText().toString();
        final String nodeDescription = descriptionEdittext.getText().toString();
        final String coordinates = coordinatesEdittext.getText().toString();

        if (pictureTaken) {
            long realTimestamp = timestamp.getTime();
            picPathToSave = context.getFilesDir() + "/IndoorPositioning/Pictures/" + nodeIdEdittext.getText() + "_" + realTimestamp + ".jpg";
        } else {
            if (picturePath == null) {
                picPathToSave = null;
            } else {
                picPathToSave = picturePath;
            }
        }

        // If no new fingerprint was captured
        if (fingerprint == null) {
            // If an old fingerprint exists
            if (roomToUpdate.getFingerprint() != null) {
                final Room room = RoomFactory.createInstance(nodeID, nodeDescription, roomToUpdate.getFingerprint(), coordinates, picPathToSave, roomToUpdate.getAdditionalInfo());
                JSONWriter.writeJSON(room);
                databaseHandler.updateRoom(room, oldNodeId);
                Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();

                finish();
                Intent intent = new Intent(context, NodeListActivity.class);
                startActivity(intent);

                // If no new fingerprint was taken and no old exists
            } else {

                //if (nodeToUpdate.getFingerprint() == null) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.no_fingerprint_title_text))
                        .setMessage("Soll der Ort \"" + nodeIdEdittext.getText() + "\" wirklich ohne Fingerprint gespeichert werden?")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            final Room room = RoomFactory.createInstance(nodeID, nodeDescription, null, coordinates, picPathToSave, "");
                            JSONWriter.writeJSON(room);
                            databaseHandler.updateRoom(room, oldNodeId);
                            Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();
                            deleteOldPictures();
                            resetUiElements();
                        })
                        .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            // If a new fingerprint was taken
        } else {
            final Room room = RoomFactory.createInstance(nodeID, nodeDescription, fingerprint, coordinates, picPathToSave, roomToUpdate.getAdditionalInfo());
            JSONWriter.writeJSON(room);
            databaseHandler.updateRoom(room, oldNodeId);
            Toast.makeText(context, getString(R.string.node_saved_toast), Toast.LENGTH_LONG).show();
            deleteOldPictures();
            finish();
            Intent intent = new Intent(context, NodeListActivity.class);
            startActivity(intent);
        }
    }


    /**
     * Deletes old picture(s) if a new was taken.
     */
    private void deleteOldPictures() {
        for (String picturePath : oldPicturePaths) {
            if (picturePath != null) {
                File imageFile = new File(picturePath);
                imageFile.delete();
            }
        }
    }


    /**
     * Reset buttons. textfields, and progress.
     * Cancel the fingerprinting backgroundtask.
     */
    private void resetUiElements() {
        if (fingerprintTask != null) {
            fingerprintTask.cancel(false);
        }
        progressStatus = 0;
        progressTextview.setText(String.valueOf(progressStatus));
        progressBar.setProgress(progressStatus);
        recordButton.setEnabled(true);
        //recordTimeText.setEnabled(true);
        nodeIdEdittext.setEnabled(true);
        descriptionEdittext.setEnabled(true);
    }


    /**
     * Ask, if new node should be created. If not, finish() and go to NodeListActivity to show all nodes
     */
    private void askForNewNode() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.record_another_node_title_text))
                .setMessage(getString(R.string.record_another_node_question))
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    finish();
                    startActivity(getIntent());
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    finish();
                    Intent intent = new Intent(context, NodeListActivity.class);
                    startActivity(intent);
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    /**
     * Check for permissions
     *
     * @param context the context
     * @return boolean, if all permissions are given
     */
    private boolean hasPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("----- Permission not granted: " + permission);
                    return false;
                }
            }
        }
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check again for permissions if they were not granted
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(NodeRecordEditActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Stop recording thread if the Activity is stopped,
     * except the user left the activity for taking a picture.
     */
    @Override
    protected void onStop() {
        super.onStop();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();

        if (isScreenOn) {
            if (!takingPictureAtTheMoment && !showingBigPictureAtTheMoment) {
                if (fingerprintTask != null) {
                    fingerprintTask.cancel(false);
                }
            }
        }
    }
}