package de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.barcode.BarcodeCaptureActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.WKT;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Room;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;


/**
 * MeasureViewImpl Class which implements the MeasureView Interface
 * <p>
 * View for showing Measuring details, nodes..
 * <p>
 * Author: Benjamin Kneer
 */

public class MeasureViewImpl extends BaseActivity implements MeasureView {

    private final List<String> nodeNames = new ArrayList<>();
    private final List<Room> roomList = new ArrayList<>();
    private final MeasureController controller;
    private TextView compassView;
    private ImageView compassImageView;
    private TextView stepCounterView;
    private TextView distanceView;
    private TextView coordinatesView;
    private TextView startNodeCoordinatesView;
    private TextView targetNodeCoordinatesView;
    private TextView edgeDistanceView;
    private Button btnStart;
    private Button btnStop;
    private Button btnAdd;
    private Spinner startNodeSpinner;
    private ImageView startNodeImage;
    private ImageView targetNodeImage;
    private ImageView handycapImage;
    private CheckBox nullpointStartCb;
    private ArrayAdapter<String> startAdapter;

    public MeasureViewImpl() {
        controller = new MeasureControllerImpl();
        controller.setView(this);
    }


    /************************************************************************************
     *                                                                                   *
     *                               Activity Events                                     *
     *                                                                                   *
     *************************************************************************************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Wegvermessung");

        // find content frame
        final FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        // inflate correct layout
        getLayoutInflater().inflate(R.layout.activity_measure, contentFrameLayout);

        // get all nodes from database
        DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(getContext());
        List<Room> tmpRoomList = databaseHandler.getAllNodes();

        // save the node ids
        for (Room room : tmpRoomList) {
            nodeNames.add(room.getRoomName());
            roomList.add(room);
        }

        /************        find UI Elements and set listeners          ************/

        compassView = (TextView) findViewById(R.id.coordinates_measure_compass);
        compassImageView = (ImageView) findViewById(R.id.coordinates_measure_compass_iv);
        stepCounterView = (TextView) findViewById(R.id.coordinates_measure_stepvalue);
        distanceView = (TextView) findViewById(R.id.coordinates_measure_distancevalue);
        edgeDistanceView = (TextView) findViewById(R.id.coordinates_measure_edge_distance);

        coordinatesView = (TextView) findViewById(R.id.coordinates_measure_coordinates);

        SwitchCompat stairsToggle = (SwitchCompat) findViewById(R.id.coordinates_measure_stairs);
        stairsToggle.setOnCheckedChangeListener((compoundButton, b) -> {
            if (controller != null) {
                controller.onStairsToggle(b);
            }
        });

        startNodeImage = (ImageView) findViewById(R.id.coordinates_measure_start_image);
        targetNodeImage = (ImageView) findViewById(R.id.coordinates_measure_target_image);
        ImageView edgeArrow = (ImageView) findViewById(R.id.coordinates_measure_arrow);

        startNodeImage.setOnClickListener(view -> {
            if (controller != null) {
                controller.onStartNodeImageClicked();
            }
        });

        targetNodeImage.setOnClickListener(view -> {
            if (controller != null) {
                controller.onTargetNodeImageClicked();
            }
        });

        edgeArrow.setOnClickListener(view -> {
            if (controller != null) {
                controller.onEdgeDetailsClicked();
            }
        });

        handycapImage = (ImageView) findViewById(R.id.coordinates_measure_handycapped);
        // load handycap image as default
        Glide.with(getContext())
                .load(R.drawable.barrierefrei)
                .into(handycapImage);

        ImageView locateWifiImage = (ImageView) findViewById(R.id.coordinates_measure_locate_wifi);
        locateWifiImage.setOnClickListener(view -> {
            if (controller != null) {
                controller.onLocateWifiClicked();
            }
        });

        ImageView locateQrImage = (ImageView) findViewById(R.id.coordinates_measure_locate_qr);
        locateQrImage.setOnClickListener(view -> {
            if (controller != null) {
                controller.onLocateQrClicked();
            }
        });

        startNodeCoordinatesView = (TextView) findViewById(R.id.coordinates_measure_start_coords);
        targetNodeCoordinatesView = (TextView) findViewById(R.id.coordinates_measure_target_coords);

        nullpointStartCb = (CheckBox) findViewById(R.id.coordinates_measure_nullpoint_start);

        nullpointStartCb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (controller != null) {
                controller.onNullpointCheckedStartNode(b);
            }
        });

        btnStart = (Button) findViewById(R.id.coordinates_measure_start);
        btnStart.setOnClickListener(v -> {
            if (controller != null) {
                controller.onStartClicked();
            }
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            btnAdd.setEnabled(true);
            nullpointStartCb.setEnabled(false);
        });
        btnStart.setEnabled(false);

        btnStop = (Button) findViewById(R.id.coordinates_measure_stop);
        btnStop.setOnClickListener(v -> {
            if (controller != null) {
                controller.onStopClicked();
            }
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            btnAdd.setEnabled(false);
            nullpointStartCb.setEnabled(true);
        });
        btnStop.setEnabled(false);

        btnAdd = (Button) findViewById(R.id.coordinates_measure_add);
        btnAdd.setOnClickListener(v -> {
            if (controller != null) {
                controller.onStepClicked();
            }
        });
        btnAdd.setEnabled(false);

        startNodeSpinner = (Spinner) findViewById(R.id.coordinates_measure_startnode);
        // fill adapter with nodenames
        startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nodeNames);
        startNodeSpinner.setAdapter(startAdapter);
        startNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Room startRoom = roomList.get(i);
                if (startRoom.getPicturePath() == null) {
                    startNodeImage.setImageResource(R.drawable.unknown);
                } else {
                    Uri imageUri = Uri.parse(startRoom.getPicturePath());
                    File image = new File(imageUri.getPath());

                    if (image.exists()) {
                        //using glide to reduce ui lag
                        Glide.with(getContext())
                                .load(startRoom.getPicturePath())
                                .into(startNodeImage);
                    }

                }

                controller.onStartNodeSelected(startRoom);

                nullpointStartCb.setChecked(startRoom.getAdditionalInfo() != null && startRoom.getAdditionalInfo().contains("NULLPOINT"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner targetNodeSpinner = (Spinner) findViewById(R.id.coordinates_measure_targetnode);
        ArrayAdapter<String> targetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nodeNames);
        targetNodeSpinner.setAdapter(targetAdapter);
        targetNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Room targetRoom = roomList.get(i);
                if (targetRoom.getPicturePath() == null) {
                    targetNodeImage.setImageResource(R.drawable.unknown);
                } else {

                    Uri imageUri = Uri.parse(targetRoom.getPicturePath());
                    File image = new File(imageUri.getPath());

                    if (image.exists()) {
                        //using glide to reduce ui lag
                        Glide.with(getContext())
                                .load(targetRoom.getPicturePath())
                                .into(targetNodeImage);
                    }
                }

                controller.onTargetNodeSelected(targetRoom);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check if we found a qr code
        if (requestCode == 1) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    if (controller != null) {
                        controller.onQrResult(barcode.displayValue);
                    }
                } else {
                    if (controller != null) {
                        controller.onQrResult("");
                    }
                }
            } else Log.e("tmp", "error");
        } else super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (controller != null) {
            controller.onResume();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (controller != null) {
            controller.onPause();
        }
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * update the azimuth angle view
     *
     * @param azimuth azimuth angle
     */
    @Override
    public void updateAzimuth(float azimuth) {
        double roundAzimuth = Math.round(azimuth * 100.0) / 100.0;
        compassView.setText(roundAzimuth + " °");
        compassImageView.setRotation(-azimuth);
    }


    /**
     * update the stepcount view
     *
     * @param stepCount current step count
     */
    @Override
    public void updateStepCount(int stepCount) {
        stepCounterView.setText(String.valueOf(stepCount));
    }


    /**
     * update the distance view
     *
     * @param distance calculated distance
     */
    @Override
    public void updateDistance(float distance) {
        double roundDistance = Math.round(distance * 100.0) / 100.0;
        distanceView.setText(String.valueOf(roundDistance));
    }


    /**
     * update the current position view
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void updateCoordinates(float x, float y, float z) {
        double roundX = Math.round(x * 100.0) / 100.0;
        double roundY = Math.round(y * 100.0) / 100.0;
        double roundZ = Math.round(z * 100.0) / 100.0;

        coordinatesView.setText(roundX + " | " + roundY + " | " + roundZ);
    }


    /**
     * update the startnode coordinates view
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void updateStartNodeCoordinates(float x, float y, float z) {
        double roundX = Math.round(x * 100.0) / 100.0;
        double roundY = Math.round(y * 100.0) / 100.0;
        double roundZ = Math.round(z * 100.0) / 100.0;

        startNodeCoordinatesView.setText(roundX + " | " + roundY + " | " + roundZ);
    }


    /**
     * update the targetnode coordinates view
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void updateTargetNodeCoordinates(float x, float y, float z) {
        double roundX = Math.round(x * 100.0) / 100.0;
        double roundY = Math.round(y * 100.0) / 100.0;
        double roundZ = Math.round(z * 100.0) / 100.0;

        targetNodeCoordinatesView.setText(roundX + " | " + roundY + " | " + roundZ);
    }


    /**
     * update edge informations
     *
     * @param edge Edge object
     */
    @Override
    public void updateEdge(Edge edge) {
        // check if handycap friendly
        if (edge.getAccessibility()) {
            // load handycap image
            Glide.with(getContext())
                    .load(R.drawable.barrierefrei)
                    .into(handycapImage);
        } else {
            // load handycap image
            Glide.with(getContext())
                    .load(R.drawable.nicht_barrierefrei1)
                    .into(handycapImage);
        }
        // update distance
        float edgeDistance = edge.getWeight();
        edgeDistanceView.setText(String.valueOf(Math.round(edgeDistance * 100.0f) / 100.0f));
    }


    /**
     * enable start button
     */
    @Override
    public void enableStart() {
        if (btnStart != null) {
            btnStart.setEnabled(true);
        }
    }


    /**
     * disable start button
     */
    @Override
    public void disableStart() {
        if (btnStart != null) {
            btnStart.setEnabled(false);
        }
    }


    /**
     * disable stop button
     */
    @Override
    public void disableStop() {
        if (btnStop != null) {
            btnStop.setEnabled(false);
        }
    }


    /**
     * disable step add button
     */
    @Override
    public void disableAdd() {
        if (btnAdd != null) {
            btnAdd.setEnabled(false);
        }
    }


    /**
     * used when we receive startnode from wifi scan or qr code
     *
     * @param room found node
     */
    @Override
    public void setStartNode(Room room) {
        // check if we know node already and stored it in list
        boolean found = false;
        for (String nodeName : nodeNames) {
            if (room.getRoomName().equals(nodeName)) {
                found = true;
                break;
            }
        }
        // it is a new node
        if (!found) {
            // update adapter
            startAdapter.add(room.getRoomName());
            // update nodename list
            roomList.add(room);
            // set node as selected
            startNodeSpinner.setSelection(roomList.indexOf(room));
            if (room.getCoordinates() != null && !room.getCoordinates().isEmpty()) {
                float[] coordinates = WKT.strToCoord(room.getCoordinates());
                updateStartNodeCoordinates(coordinates[0], coordinates[1], coordinates[2]);
            } else {
                updateStartNodeCoordinates(0.0f, 0.0f, 0.0f);
            }

        }
        // it is an existing node
        else {
            // find correct node object stored in nodelist, so we can set spinner selection
            Room foundRoom = null;
            for (Room tmpRoom : roomList) {
                if (tmpRoom.getRoomName().equals(room.getRoomName())) {
                    foundRoom = tmpRoom;
                    break;
                }
            }
            // set node as selected
            startNodeSpinner.setSelection(roomList.indexOf(foundRoom));
            if (room.getCoordinates() != null && !room.getCoordinates().isEmpty()) {
                float[] coordinates = WKT.strToCoord(room.getCoordinates());
                updateStartNodeCoordinates(coordinates[0], coordinates[1], coordinates[2]);
            } else {
                updateStartNodeCoordinates(0.0f, 0.0f, 0.0f);
            }
        }
        // check if the node is a nullpoint
        nullpointStartCb.setChecked(room.getAdditionalInfo() != null && room.getAdditionalInfo().contains("NULLPOINT"));
    }


    /**
     * get the view's context
     *
     * @return context
     */
    @Override
    public Context getContext() {
        return this;
    }
}
