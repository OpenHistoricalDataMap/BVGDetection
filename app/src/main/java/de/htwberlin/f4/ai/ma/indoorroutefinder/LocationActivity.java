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

/**
 * Created by Johann Winter
 * <p>
 * This activity is for locating the user ("Standort ermitteln").
 */
public class LocationActivity extends BaseActivity implements AsyncResponse {

    private ImageButton locateButton;
    private ImageView locationImageview;
    private TextView locationTextview;
    private TextView descriptionTextview;
    private TextView infobox;
    private ProgressBar progressBar;
    private Context context;
    private DatabaseHandler databaseHandler;
    private WifiManager wifiManager;
    private boolean verboseMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_location));
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_location, contentFrameLayout);

        context = this;

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        locateButton = findViewById(R.id.locate_1s_button);
        locationImageview = findViewById(R.id.location_imageview);
        locationTextview = findViewById(R.id.location_textview);
        descriptionTextview = findViewById(R.id.description_textview_location);
        infobox = findViewById(R.id.infobox_location);
        TextView wifiFilter = findViewById(R.id.wifi_filter);
        TextView locattionSettingsTextview = findViewById(R.id.locattion_settings_textview);
        progressBar = findViewById(R.id.location_progressbar);
        locateButton.setImageResource(R.drawable.locate_1s_button);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean movingAverage = sharedPreferences.getBoolean("pref_movingAverage", true);
        boolean kalmanFilter = sharedPreferences.getBoolean("pref_kalman", false);
        boolean euclideanDistance = sharedPreferences.getBoolean("pref_euclideanDistance", false);
        boolean knnAlgorithm = sharedPreferences.getBoolean("pref_knnAlgorithm", true);
        verboseMode = sharedPreferences.getBoolean("verbose_mode", false);
        int movingAverageOrder = Integer.parseInt(sharedPreferences.getString("pref_movivngAverageOrder", "3"));
        int knnValue = Integer.parseInt(sharedPreferences.getString("pref_knnNeighbours", "3"));
        int kalmanValue = Integer.parseInt(sharedPreferences.getString("pref_kalmanValue", "2"));
        boolean useAllRouters = sharedPreferences.getBoolean("use_all_routers", false);
        String algorithm = sharedPreferences.getString("pref_algorithm", "svm");
        Set<String> defaultWifiNetworks = sharedPreferences.getStringSet("default_wifi_network", new HashSet<>());

        String knnDistanceMetric = sharedPreferences.getString("pref_knn_distance_metric", "euclidean");
        String knnWeightType = sharedPreferences.getString("pref_knn_weight_type", "uniform");
        float svmC = Float.parseFloat(sharedPreferences.getString("pref_svm_c", "1.0"));
        float svmGamma = Float.parseFloat(sharedPreferences.getString("pref_svm_gamma", "0.1"));
        String svmKernel = sharedPreferences.getString("pref_svm_kernel", "linear");
        int rfTrees = Integer.parseInt(sharedPreferences.getString("pref_rf_trees", "10"));


        if (!useAllRouters) {
            for (String network : defaultWifiNetworks) {
                String wifiText = wifiFilter.getText().toString();
                wifiText += network + ", ";
                wifiFilter.setText(wifiText);
            }
            wifiFilter.setText(wifiFilter.getText().subSequence(0, wifiFilter.getText().length() - 2));
        } else {
            wifiFilter.setText("Kein Filter aktiviert (siehe Einstellungen)");
        }


        if (algorithm.equals("svm")) {
            String text = "Support-Vector-Machine Einstellungen:\n";
            text += "SVM-C: " + svmC + "\n";
            text += "SVM-Gamma: " + svmGamma + "\n";
            text += "SVM-Kernel: " + svmKernel + "\n";
            locattionSettingsTextview.setText(text);
        } else if (algorithm.equals("knn")) {
            String text = "K-Nearst-Neighbour Einstellungen:\n";
            text += "KNN-Nachbarn: " + knnValue + "\n";
            text += "KNN-Distanzmetrik: " + knnDistanceMetric + "\n";
            text += "KNN-Gewichtung: " + knnWeightType + "\n";
            locattionSettingsTextview.setText(text);
        } else {
            String text = "Random-Forest Einstellungen:\n";
            text += "Anzahl Bäume: " + rfTrees + "\n";
            locattionSettingsTextview.setText(text);
        }

        progressBar.setVisibility(View.INVISIBLE);
        locateButton.setOnClickListener(v -> findLocation());
    }

    /**
     * Initiates the process to find the user's location by creating a fingerprint.
     */
    private void findLocation() {
        Log.d("LocationActivity", "Starting location process");
        locateButton.setEnabled(false);
        locateButton.setImageResource(R.drawable.locate_1s_button_inactive);
        locationImageview.setVisibility(View.INVISIBLE);
        locationTextview.setText(getString(R.string.searching_node_text));
        descriptionTextview.setText("");

        FingerprintTask fingerprintTask;

        if (verboseMode) {
            fingerprintTask = new FingerprintTask(1, wifiManager, false, progressBar, null, infobox);
        } else {
            fingerprintTask = new FingerprintTask(1, wifiManager, false, progressBar, null);
        }

        fingerprintTask.delegate = this;
        fingerprintTask.execute();
    }

    /**
     * Displays the results after the background FingerprintTask is finished.
     *
     * @param seconds     the measured time
     * @param fingerprint the fingerprint measured before
     */
    @Override
    public void processFinish(Fingerprint fingerprint, int seconds) {
        if (fingerprint != null) {
            Log.d("LocationActivity", "Fingerprint obtained, processing location");
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
                Log.d("LocationActivity", "No node found");
            }
        } else {
            locationTextview.setText(getString(R.string.please_try_again));
            Log.d("LocationActivity", "Fingerprint is null, please try again");
        }
        progressBar.setVisibility(View.INVISIBLE);
        locateButton.setEnabled(true);
        locateButton.setImageResource(R.drawable.locate_1s_button);
    }

}
