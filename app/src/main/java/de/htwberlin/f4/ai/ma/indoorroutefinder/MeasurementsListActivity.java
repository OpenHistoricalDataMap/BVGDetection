package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;


public class MeasurementsListActivity extends BaseActivity {

    public static final String MEASUREMENTS_LIST_ACTIVITY = "MeasurementsListActivity";
    //    Room allNodes;
    List<String> nodeNames = new ArrayList<>();
    List<SignalSample> allMeasurements = new ArrayList<>();

    ListView roomListView;
    DatabaseHandler databaseHandler;
    private String roomName = "";
    private ArrayAdapter<String> nodeListAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_roomlist));
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_measurement_list, contentFrameLayout);

        roomListView = findViewById(R.id.measurementsListListview);

        nodeListAdapter = new ArrayAdapter<>(this, R.layout.item_measurement_listview, nodeNames);

        roomListView.setAdapter(nodeListAdapter);

        TextView emptyTextView = findViewById(R.id.empty_list_item_measurement);
        roomListView.setEmptyView(emptyTextView);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        loadDbData();

        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), FingerprintListActivity.class);
                intent.putExtra("measurementID", allMeasurements.get(position).getMeasurementID());
                startActivity(intent);
            }
        });

        roomListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                new AlertDialog.Builder(view.getContext())
                        .setTitle(getString(R.string.delete_entry_title_question))
                        .setMessage("Soll die Messung vom \"" + nodeNames.get(position) + "\" wirklich gelöscht werden?")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO:
//                                databaseHandler.deleteRoom(allNodes.get(position));
                                databaseHandler.deleteMeasurement(allMeasurements.get(position).getMeasurementID());
                                loadDbData();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;


            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("roomID")) {
            System.out.println("FOUND ROOM ID!");
            roomName = (String) Objects.requireNonNull(intent.getExtras()).get("roomID");
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

        if (Objects.equals(roomName, "")) return;

        allMeasurements = null;
        nodeNames.clear();

//        allNodes = databaseHandler.getRoom(roomName);

        allMeasurements = databaseHandler.getAllMeasurementsForRoom(roomName);

        Log.d(MEASUREMENTS_LIST_ACTIVITY, String.valueOf(allMeasurements));

        for (SignalSample signalSample : allMeasurements) {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(signalSample.getTimestamp() * 1000L);
            String date = DateFormat.format("dd.MM.yyyy hh:mm:ss", cal).toString();
            nodeNames.add(date);
        }


//        Log.d("MeasurementsListActivity", String.valueOf(allNodes));

        nodeListAdapter.notifyDataSetChanged();
    }
}


