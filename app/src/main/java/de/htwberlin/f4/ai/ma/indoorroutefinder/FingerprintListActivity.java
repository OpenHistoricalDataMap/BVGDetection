package de.htwberlin.f4.ai.ma.indoorroutefinder;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintListAdapter;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;


public class FingerprintListActivity extends BaseActivity {

    public static final String FINGERPRINT_LIST_ACTIVITY = "FingerprintListActivity";
    List<AccessPointInformation> accessPointInformationList = new ArrayList<>();
    List<String> accessPointNames = new ArrayList<>();
    List<String> accessPointStrength = new ArrayList<>();
    List<String> accesPointSSIDs = new ArrayList<>();

    ListView measurementsListView;
    FingerprintListAdapter fingerprintListAdapter;
    DatabaseHandler databaseHandler;
    private int measurementID = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_roomlist));
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_fingerprint_list, contentFrameLayout);

        measurementsListView = findViewById(R.id.fingerprintListListview);

        fingerprintListAdapter = new FingerprintListAdapter(this, accessPointNames, accessPointStrength, accesPointSSIDs);
        measurementsListView.setAdapter(fingerprintListAdapter);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        Intent intent = getIntent();
        try {
            measurementID = (int) intent.getExtras().get("measurementID");
        } catch (NullPointerException e) {
            Log.e(FINGERPRINT_LIST_ACTIVITY, "No measurementID found in intent");
        }

        loadDbData();
    }


    /**
     * Refresh the list if the user comes back to this Activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadDbData();
    }


    /**
     * Clear and reload the nodelist
     */
    private void loadDbData() {
        if (measurementID == -1) return;

        accessPointInformationList.clear();
        accessPointNames.clear();
        accessPointStrength.clear();
        accesPointSSIDs.clear();

        accessPointInformationList = databaseHandler.getAccessPointInformationForMeasurement(measurementID);

        for (AccessPointInformation ap : accessPointInformationList) {
            Log.d(FINGERPRINT_LIST_ACTIVITY, String.valueOf(ap));
            accessPointNames.add(ap.getSSID());
            accessPointStrength.add(ap.getRSSI() + " dBm");
            accesPointSSIDs.add(ap.getBSSID());
        }

        fingerprintListAdapter.notifyDataSetChanged();
    }
}


