package de.htwberlin.f4.ai.ma.indoorroutefinder;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private int nodeDatabaseID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_roomlist));
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_fingerprint_list, contentFrameLayout);

        measurementsListView = findViewById(R.id.fingerprintListListview);

        fingerprintListAdapter = new FingerprintListAdapter(this, accessPointNames, accessPointStrength, accesPointSSIDs);
        measurementsListView.setAdapter(fingerprintListAdapter);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        loadDbData();

        Intent intent = getIntent();
        if (intent.hasExtra("nodeDatabaseID")) {
            System.out.println("FOUND ROOM ID!");
            nodeDatabaseID = (int) intent.getExtras().get("nodeDatabaseID");
        } else {
            System.out.println("DON'T FOUND ROOM ID!");
            // TODO
        }

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
        if (Objects.equals(nodeDatabaseID, "")) return;

        accessPointInformationList.clear();
        accessPointNames.clear();
        accessPointStrength.clear();
        accesPointSSIDs.clear();

        // TODO: Implement getAccessPointInformationForMeasurement
//        accessPointInformationList = databaseHandler.getAccessPointInformationForMeasurement(nodeDatabaseID);
        List<AccessPointInformation> accessPointInformationList = new ArrayList<>();


        for (AccessPointInformation ap : accessPointInformationList) {
            Log.d(FINGERPRINT_LIST_ACTIVITY, String.valueOf(ap));
            accessPointNames.add(ap.getSSID());
            accessPointStrength.add(ap.getRSSI() + " dBm");
            accesPointSSIDs.add(ap.getBSSID());
        }

        fingerprintListAdapter.notifyDataSetChanged();
    }
}


