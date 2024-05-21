package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformationFactory;


/**
 * Created by Johann Winter
 * <p>
 * This class generates a WiFi fingerprint by measuring signal strengths for a given WiFi name (SSID).
 * It takes as parameter: SSID ("wifiName"), measuring time in seconds ("seconds"), a WifiManager,
 * a boolean if the result should be averaged, and a TextView and ProgressBar for displaying Progress.
 * The TextView and ProgressBar can be NULL. If the wifiName parameter is NULL, all SSIDs will be recorded.
 * <p>
 * The verbose mode constructor takes an extra TextView as parameter, which will display the live
 * measuring data (usually the infobox TextView on the bottom of the NodeRecordEditActivity, the
 * LocationActivity or the RouteFinderActivity.
 */
public class FingerprintTask extends AsyncTask<Void, Integer, Fingerprint> {

    public static final String FINGERPRINT_TASK = "FingerprintTask";
    private static final HashMap<String, Long> timestampMap = new HashMap<>();
    private final int scanCount;
    @SuppressLint("StaticFieldLeak")
    private final ProgressBar progressBar;
    @SuppressLint("StaticFieldLeak")
    private final TextView progressTextview;
    private final Set<String> wifiName;
    private final WifiManager wifiManager;
    public AsyncResponse delegate = null;
    @SuppressLint("StaticFieldLeak")
    private TextView verboseOutputTextview;
    private int seconds;
    private Multimap<String, Integer> multiMap;
    private List<SignalSample> signalSampleList;
    private List<AccessPointInformation> accessPointInformationList;
    private final boolean calculateAverage;

    // Normal mode constructor
    public FingerprintTask(final Set<String> wifiName, final int scanCount, final WifiManager wifiManager, final Boolean calculateAverage,
                           final @Nullable ProgressBar progressBar, final @Nullable TextView progressTextview) {
        this.wifiManager = wifiManager;
        this.wifiName = wifiName;
        this.calculateAverage = calculateAverage;
        this.progressBar = progressBar;
        this.progressTextview = progressTextview;
        this.scanCount = scanCount;
    }

    // Verbose mode constructor
    public FingerprintTask(final Set<String> wifiName, final int scanCount, final WifiManager wifiManager, final Boolean calculateAverage,
                           final @Nullable ProgressBar progressBar, final @Nullable TextView progressTextview, final TextView verboseOutputTextview) {
        this.wifiManager = wifiManager;
        this.wifiName = wifiName;
        this.calculateAverage = calculateAverage;
        this.progressBar = progressBar;
        this.progressTextview = progressTextview;
        this.verboseOutputTextview = verboseOutputTextview;
        this.scanCount = scanCount;
    }


    @Override
    @SuppressLint({"MissingPermission", "CheckResult"})
    protected Fingerprint doInBackground(Void... voids) {
        for (int i = 0; i < scanCount; i++) {

            accessPointInformationList = new ArrayList<>();

            // If task gets aborted
            if (isCancelled()) {
                return null;
            }

            wifiManager.startScan();
            List<ScanResult> wifiScanList = wifiManager.getScanResults();


            // When measuring for just one second, compare the timestamps of the scan results
            // with the last saved scan data, and cancel measurement if more than 50% are deprecated.
            if (scanCount == 1) {
                System.out.println("######################################################################");

                float deprecatedAccessPoints = 0;
                float numberOfAccessPoints = wifiScanList.size();

                for (ScanResult scanResult : wifiScanList) {
                    if (timestampMap.get(scanResult.BSSID) != null && Objects.equals(timestampMap.get(scanResult.BSSID), scanResult.timestamp)) {
                        deprecatedAccessPoints++;
                        System.out.println("+++++ NOT_UPDATED:  " + scanResult.timestamp + " <-> " + timestampMap.get(scanResult.BSSID));
                    } else {
                        timestampMap.put(scanResult.BSSID, scanResult.timestamp);
                    }
                }
                System.out.println("### TimestampMap.size= " + timestampMap.size());
                System.out.println("Old AP data: " + deprecatedAccessPoints + " of " + numberOfAccessPoints);

                // 50%
                float deprecationTreshold = 0.5f;
                if (deprecatedAccessPoints > (numberOfAccessPoints * deprecationTreshold)) {
                    System.out.println("++++++++++++++++ Mehr als die Hälfte der Messwerte veraltet!");
                    System.out.println("######################################################################");
                    return null;
                }
                System.out.println("######################################################################");
            }


            Log.d("Fingerprinting... ", "found networks (total): " + wifiScanList.size());

            for (final ScanResult sr : wifiScanList) {
                // If the wifiName was defined, filter for only this SSID
                if (wifiName != null) {
                    if (wifiName.contains(sr.SSID)) {
                        Log.d("Fingerprinting... ", "MAC: " + sr.BSSID + "   Strength: " + sr.level + " dBm         timestamp: " + sr.timestamp);
                        AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(sr.BSSID, sr.level, sr.SSID);
                        accessPointInformationList.add(accessPointInformation);
                        multiMap.put(sr.BSSID, sr.level);
                    }
                    // No SSID filter while scanning
                } else {
                    Log.d("Fingerprinting... ", "MAC: " + sr.BSSID + "   Strength: " + sr.level + " dBm         timestamp: " + sr.timestamp);
                    AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(sr.BSSID, sr.level, sr.SSID);
                    accessPointInformationList.add(accessPointInformation);
                    multiMap.put(sr.BSSID, sr.level);
                }
            }
            publishProgress(i);

            wifiScanList.clear();

            long timestampSeconds = System.currentTimeMillis() / 1000;
            System.out.println("TIME");
            System.out.println(timestampSeconds);
            SignalSample signalSample = new SignalSample(timestampSeconds, accessPointInformationList);
            System.out.println(signalSample);
            signalSampleList.add(signalSample);


            if (i != (scanCount - 1)) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Log.d(FINGERPRINT_TASK, e.toString());
                }
            }
        }

        if (calculateAverage) {
            // Calculate average values
            List<SignalSample> signalSamples = AverageSignalCalculator.calculateAverageSignal(multiMap);
            return FingerprintFactory.createInstance(signalSamples);
        } else {
            return FingerprintFactory.createInstance(signalSampleList);
        }

    }

    @Override
    protected void onCancelled() {
        delegate.processFinish(null, 0);
    }

    @Override
    protected void onPreExecute() {
        if (progressBar != null) {
            progressBar.setMax(scanCount);
        }
        multiMap = ArrayListMultimap.create();
        signalSampleList = new ArrayList<>();
        accessPointInformationList = new ArrayList<>();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (progressBar != null) {
            progressBar.setProgress(values[0] + 1);
        }

        // Verbose-Mode (show measuring data in UI)
        if (verboseOutputTextview != null) {
            StringBuilder textviewString = new StringBuilder();

            for (int i = 0; i < accessPointInformationList.size(); i++) {
                // Clip the output at 6 Accesspoints, because of the limited space of the infobox.
                if (i <= 7) {
                    textviewString.append(accessPointInformationList.get(i).getBSSID()).append("  ").append(accessPointInformationList.get(i).getRSSI()).append("       ");
                }
            }
            verboseOutputTextview.setText(textviewString.toString());
            if (progressTextview != null) {
                progressTextview.setText(String.valueOf(scanCount - values[0]));
            }

            // Normal mode
        } else {
            if (progressTextview != null) {
                progressTextview.setText(String.valueOf(scanCount - values[0]));
            }
        }
    }

    @Override
    protected void onPostExecute(Fingerprint fingerprint) {
        // Used for console output, sorted by accesspoint
        /*
        for (String mac : testData.keySet()) {
            String output = "";
            for (Integer rssi : testData.get(mac)) {
                output += rssi + " ";
            }
            System.out.println(mac + ": " + output);
        }*/

        delegate.processFinish(fingerprint, scanCount);
    }

}

