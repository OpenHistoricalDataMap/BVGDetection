package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.Set;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.AsyncResponse;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintTask;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator.LocationCalculator;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator.LocationCalculatorFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;
import smile.classification.KNN;

/**
 * Created by Johann Winter
 * <p>
 * This activity is for locating the user ("Standort ermitteln").
 */
public class LocationActivity extends BaseActivity implements AsyncResponse {

    ImageButton locateButton;
    ImageView locationImageview;
    TextView locationTextview;
    TextView descriptionTextview;
    TextView infobox;
    TextView wifiFilter;
    ProgressBar progressBar;
    Context context;
    boolean movingAverage;
    boolean kalmanFilter;
    boolean euclideanDistance;
    boolean knnAlgorithm;
    int knnValue;
    int movingAverageOrder;
    int kalmanValue;
    private DatabaseHandler databaseHandler;
    private SharedPreferences sharedPreferences;
    private WifiManager wifiManager;
    private boolean verboseMode;
    private boolean useSSIDfilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_location));
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_location, contentFrameLayout);

        context = this;

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        locateButton = (ImageButton) findViewById(R.id.locate_1s_button);
        locationImageview = (ImageView) findViewById(R.id.location_imageview);
        locationTextview = (TextView) findViewById(R.id.location_textview);
        descriptionTextview = (TextView) findViewById(R.id.description_textview_location);
        infobox = (TextView) findViewById(R.id.infobox_location);
        wifiFilter = (TextView) findViewById(R.id.wifi_filter);
        progressBar = (ProgressBar) findViewById(R.id.location_progressbar);
        locateButton.setImageResource(R.drawable.locate_1s_button);


        // Get preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        movingAverage = sharedPreferences.getBoolean("pref_movingAverage", true);
        kalmanFilter = sharedPreferences.getBoolean("pref_kalman", false);
        euclideanDistance = sharedPreferences.getBoolean("pref_euclideanDistance", false);
        knnAlgorithm = sharedPreferences.getBoolean("pref_knnAlgorithm", true);
        verboseMode = sharedPreferences.getBoolean("verbose_mode", false);
        useSSIDfilter = sharedPreferences.getBoolean("use_ssid_filter", false);

        movingAverageOrder = Integer.parseInt(sharedPreferences.getString("pref_movivngAverageOrder", "3"));
        knnValue = Integer.parseInt(sharedPreferences.getString("pref_knnNeighbours", "3"));
        kalmanValue = Integer.parseInt(sharedPreferences.getString("pref_kalmanValue", "2"));

        Set<String> defaultWifiNetworks = sharedPreferences.getStringSet("default_wifi_network", new HashSet<String>());

        if (useSSIDfilter) {
            for (String network : defaultWifiNetworks) {
                String wifiText = wifiFilter.getText().toString();
                wifiText += network + ", ";
                wifiFilter.setText(wifiText);
            }
            wifiFilter.setText(wifiFilter.getText().subSequence(0, wifiFilter.getText().length() - 2));
        } else {
            wifiFilter.setText("Kein Filter aktiviert (siehe Einstellungen)");
        }


        progressBar.setVisibility(View.INVISIBLE);

        locateButton.setOnClickListener(v -> findLocation());

        double[][] X = {
                {10, 10, 10}, {10, 10, 10}, {90, 90, 90}, {90, 90, 90},
                {10, 10, 10}, {10, 10, 10}, {90, 90, 90}, {90, 90, 90}
        };

        int[] y = {0, 0, 1, 1, 0, 0, 1, 1};

        KNN<double[]> knn = KNN.fit(X, y, 3);

        double[][] test = {
                {10, 10, 10}, {49, 49, 49}, {49, 50, 50}, {51, 51, 51}, {90, 90, 90}
        };

        StringBuilder predictions = new StringBuilder();
        for (double[] t : test) {
            int prediction = knn.predict(t);
            predictions.append("Prediction: ").append(prediction == 0 ? "Class 0" : "Class 1").append("\n");
        }

        Log.d("KNN", predictions.toString());


    }

    /**
     * Create a fingerprint
     */
    // TODO: Changed numberOfMeasurements to number of measurements
    private void findLocation() {
        locateButton.setEnabled(false);
        locateButton.setImageResource(R.drawable.locate_1s_button_inactive);
        locationImageview.setVisibility(View.INVISIBLE);
        locationTextview.setText(getString(R.string.searching_node_text));
        descriptionTextview.setText("");

        FingerprintTask fingerprintTask;

        Set<String> ssidFilterNew = null;

        if (verboseMode) {
            if (useSSIDfilter) {
                ssidFilterNew = sharedPreferences.getStringSet("default_wifi_network", new HashSet<String>());
            }
            fingerprintTask = new FingerprintTask(ssidFilterNew, 1, wifiManager, true, progressBar, null, infobox);
        } else {
            if (useSSIDfilter) {
                ssidFilterNew = sharedPreferences.getStringSet("default_wifi_network", new HashSet<String>());
            }
            fingerprintTask = new FingerprintTask(ssidFilterNew, 1, wifiManager, true, progressBar, null);
        }

        fingerprintTask.delegate = this;
        fingerprintTask.execute();
    }


    /**
     * If the background FingerprintTask is finished, display results
     *
     * @param seconds     the measured time
     * @param fingerprint the fingerprint measured before
     */
    @Override
    public void processFinish(Fingerprint fingerprint, int seconds) {
        if (fingerprint != null) {

            LocationCalculator locationCalculator = LocationCalculatorFactory.createInstance(this);
            final String foundNode = locationCalculator.calculateNodeId(fingerprint);

            if (foundNode != null) {

                locationTextview.setText(foundNode);
                locationImageview.setVisibility(View.VISIBLE);

                Log.d("LocationActivity", "Found node: " + foundNode);

                descriptionTextview.setText(databaseHandler.getRoom(foundNode).getDescription());

                final String picturePath = databaseHandler.getRoom(foundNode).getPicturePath();

                if (picturePath != null) {
                    Glide.with(context).load(picturePath).into(locationImageview);
                } else {
                    Glide.with(context).load(R.drawable.unknown).into(locationImageview);
                }

                locationImageview.setOnClickListener(view -> {
                    Intent intent = new Intent(getApplicationContext(), MaxPictureActivity.class);
                    intent.putExtra("picturePath", picturePath);
                    intent.putExtra("nodeID", foundNode);
                    startActivity(intent);
                });


            } else {
                locationTextview.setText(getString(R.string.no_node_found_text));
            }
        } else {
            locationTextview.setText(getString(R.string.please_try_again));
        }
        progressBar.setVisibility(View.INVISIBLE);

        locateButton.setEnabled(true);
        locateButton.setImageResource(R.drawable.locate_1s_button);
    }
}
