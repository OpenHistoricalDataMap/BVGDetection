package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra.DijkstraAlgorithm;
import de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra.DijkstraAlgorithmFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.AsyncResponse;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintTask;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator.LocationCalculator;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator.LocationCalculatorFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Room;
import de.htwberlin.f4.ai.ma.indoorroutefinder.nodelist.NodeListAdapter;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.wifi_scanner.WifiScanner;
import de.htwberlin.f4.ai.ma.indoorroutefinder.wifi_scanner.WifiScannerFactory;

/**
 * Created by Johann Winter
 * <p>
 * This activity provides the route finder functionality ("Route finden").
 */
public class RouteFinderActivity extends BaseActivity implements AsyncResponse {

    Spinner destinationNodeSpinner;
    ImageButton locateButton;
    ImageButton findRouteButton;
    ListView navigationResultListview;
    List<String> itemsStartNodeSpinner;
    CheckBox accessibilityCheckbox;
    List<String> navigationResultsList;
    List<Room> allRooms;
    DatabaseHandler databaseHandler;
    NodeListAdapter resultListAdapter;
    List<String> nodeNames;
    List<String> nodeDescriptions;
    List<String> nodePicturePaths;
    boolean verboseMode;
    WifiManager wifiManager;
    private Spinner startNodeSpinner;
    private ArrayList<String> itemsDestNodeSpinner;
    private TextView totalDistanceTextview;
    private TextView infobox;
    private SharedPreferences sharedPreferences;
    private String selectedStartNode;
    private String lastSelectedStartNode;
    private String defaultWifi;
    private boolean useSSIDfilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_findroute));
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_routefinder, contentFrameLayout);

        startNodeSpinner = (Spinner) findViewById(R.id.start_node_spinner);
        destinationNodeSpinner = (Spinner) findViewById(R.id.destination_node_spinner);
        locateButton = (ImageButton) findViewById(R.id.locate_button);
        findRouteButton = (ImageButton) findViewById(R.id.find_route_button);
        navigationResultListview = (ListView) findViewById(R.id.navigation_result_listview);
        accessibilityCheckbox = (CheckBox) findViewById(R.id.accessibility_checkbox_navi);
        totalDistanceTextview = (TextView) findViewById(R.id.total_distance_textview);
        infobox = (TextView) findViewById(R.id.infobox_routefinder);

        itemsStartNodeSpinner = new ArrayList<>();
        itemsDestNodeSpinner = new ArrayList<>();
        navigationResultsList = new ArrayList<>();
        nodeNames = new ArrayList<>();
        nodeDescriptions = new ArrayList<>();
        nodePicturePaths = new ArrayList<>();

        lastSelectedStartNode = "";
        findRouteButton.setImageResource(R.drawable.find_route_button);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        databaseHandler = DatabaseHandlerFactory.getInstance(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        allRooms = databaseHandler.getAllNodes();

        useSSIDfilter = sharedPreferences.getBoolean("use_ssid_filter", false);
        defaultWifi = sharedPreferences.getString("default_wifi_network", null);

        locateButton.setImageResource(R.drawable.locate);

        // Fill the spinners with Nodes
        for (Room room : allRooms) {
            itemsStartNodeSpinner.add(room.getRoomName());
            itemsDestNodeSpinner.add(room.getRoomName());
        }

        // Disable connect-button if spinnerB has no elements (spinnerA has one or less elements)
        if (itemsStartNodeSpinner.size() < 2) {
            findRouteButton.setImageResource(R.drawable.find_route_button_inactive);
            findRouteButton.setEnabled(false);
        }

        // Set adapters and attach the lists
        final ArrayAdapter<String> adapterA = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsStartNodeSpinner);
        startNodeSpinner.setAdapter(adapterA);
        final ArrayAdapter<String> adapterB = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsDestNodeSpinner);
        destinationNodeSpinner.setAdapter(adapterB);
        resultListAdapter = new NodeListAdapter(this, nodeNames, nodeDescriptions, nodePicturePaths);
        navigationResultListview.setAdapter(resultListAdapter);

        // Exclude on spinnerA selected item on spinnerB
        startNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedStartNode = startNodeSpinner.getSelectedItem().toString();

                if (!selectedStartNode.equals(lastSelectedStartNode)) {
                    if (!lastSelectedStartNode.isEmpty()) {
                        itemsDestNodeSpinner.add(lastSelectedStartNode);
                    }
                    itemsDestNodeSpinner.remove(selectedStartNode);
                    adapterB.notifyDataSetChanged();
                    lastSelectedStartNode = selectedStartNode;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // Get WiFis around and ask the user which to use
        locateButton.setOnClickListener(view -> {
            locateButton.setImageResource(R.drawable.locate_inactive);
            if (useSSIDfilter) {
                // If default WiFi is not set in preferences
                if (defaultWifi == null) {
                    locateButton.setEnabled(false);

                    WifiScanner wifiScanner = WifiScannerFactory.createInstance();
                    final List<String> wifiNamesList = wifiScanner.getAvailableNetworks(wifiManager, true);

                    final CharSequence[] wifiArray = new CharSequence[wifiNamesList.size()];
                    for (int i = 0; i < wifiArray.length; i++) {
                        wifiArray[i] = wifiNamesList.get(i);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(getString(R.string.select_wifi));
                    builder.setItems(wifiArray, (dialog, which) -> findLocation(wifiNamesList.get(which)));
                    builder.setCancelable(false);
                    builder.show();
                    // If default WiFi is set in preferences
                } else {
                    findLocation(defaultWifi);
                }
            } else {
                findLocation(null);
            }
        });

        // Start the route finding process
        findRouteButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                nodeNames.clear();
                nodeDescriptions.clear();
                nodePicturePaths.clear();

                boolean accessible = accessibilityCheckbox.isChecked();

                DijkstraAlgorithm dijkstraAlgorithm = DijkstraAlgorithmFactory.createInstance(getApplicationContext(), accessible);
                dijkstraAlgorithm.execute(selectedStartNode);
                List<String> route = dijkstraAlgorithm.getPath(destinationNodeSpinner.getSelectedItem().toString());

                if (route == null) {
                    nodeNames.add(getString(R.string.no_route_found));
                    nodeDescriptions.add("");
                    nodePicturePaths.add("");
                    resultListAdapter.notifyDataSetChanged();
                    totalDistanceTextview.setText("");
                } else {
                    float totalDistance = 0;
                    for (int i = 0; i < route.size(); i++) {
                        nodeNames.add(route.get(i));
                        nodeDescriptions.add(databaseHandler.getNode(route.get(i)).getDescription());
                        nodePicturePaths.add(databaseHandler.getNode(route.get(i)).getPicturePath());

                        // Add distance (weight) to the results list
                        if (i + 1 < route.size()) {
                            Room roomA = databaseHandler.getNode(route.get(i));
                            Room roomB = databaseHandler.getNode(route.get(i + 1));

                            Edge e = databaseHandler.getEdge(roomA, roomB);
                            nodeNames.add("\t" + e.getWeight() + " m");
                            nodeDescriptions.add("");
                            nodePicturePaths.add("");
                            totalDistance += e.getWeight();
                        }
                    }

                    resultListAdapter.notifyDataSetChanged();
                    totalDistanceTextview.setText("Gesamtstrecke: " + totalDistance + " m");

                    // Click on Item -> show Node in NodeEditActivity
                    navigationResultListview.setOnItemClickListener((parent, view1, position, id) -> {
                        // Only nodes will be clickable, not the separators
                        if (position % 2 == 0) {
                            Intent intent = new Intent(getApplicationContext(), NodeShowActivity.class);
                            intent.putExtra("nodeName", navigationResultListview.getAdapter().getItem(position).toString());
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }


    /**
     * Try to find the location and set it to the start-spinner
     *
     * @param wifiName the WiFi to measure
     */
    private void findLocation(String wifiName) {
        verboseMode = sharedPreferences.getBoolean("verbose_mode", false);
        FingerprintTask fingerprintTask;
        if (verboseMode) {
            fingerprintTask = new FingerprintTask(wifiName, 1, wifiManager, true, null, null, infobox);
        } else {
            fingerprintTask = new FingerprintTask(wifiName, 1, wifiManager, true, null, null);
        }
        fingerprintTask.delegate = this;
        fingerprintTask.execute();
    }

    /**
     * When the fingerprinting background task finished
     *
     * @param fingerprint the Fingerprint from the AsyncTask
     * @param seconds     the time of measurement in seconds
     */
    @Override
    public void processFinish(Fingerprint fingerprint, int seconds) {
        if (fingerprint != null) {
            LocationCalculator locationCalculator = LocationCalculatorFactory.createInstance(this);
            String foundNode = locationCalculator.calculateNodeId(fingerprint);
            if (foundNode != null) {
                Toast toast = Toast.makeText(this, getString(R.string.location) + foundNode, Toast.LENGTH_SHORT);
                toast.show();
                int index = itemsStartNodeSpinner.indexOf(foundNode);
                startNodeSpinner.setSelection(index);
            } else {
                Toast toast = Toast.makeText(this, getString(R.string.no_location_found), Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT);
            toast.show();
        }
        locateButton.setEnabled(true);
        locateButton.setImageResource(R.drawable.locate);
    }
}
