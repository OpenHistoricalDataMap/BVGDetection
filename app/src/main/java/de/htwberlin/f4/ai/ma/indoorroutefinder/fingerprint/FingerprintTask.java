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

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformationFactory;

/**
 * Created by Johann Winter, Friedrich Völkers
 * <p>
 * This class generates a WiFi fingerprint by measuring signal strengths for given WiFi names (SSIDs).
 * It takes the following parameters:
 * - Set of SSIDs ("wifiName")
 * - Number of scans ("scanCount")
 * - WifiManager instance
 * - Boolean indicating if results should be averaged
 * - Optional ProgressBar and TextView for displaying progress
 * <p>
 * A verbose mode constructor also takes a TextView for displaying live measuring data.
 */
public class FingerprintTask extends AsyncTask<Void, Integer, Fingerprint> {

    public static final String FINGERPRINT_TASK = "FingerprintTask";
    private static final HashMap<String, Long> timestampMap = new HashMap<>();
    private final int scanCount;
    @SuppressLint("StaticFieldLeak")
    private final ProgressBar progressBar;
    @SuppressLint("StaticFieldLeak")
    private final TextView progressTextview;
    private final WifiManager wifiManager;
    private final boolean calculateAverage;
    public AsyncResponse delegate = null;
    @SuppressLint("StaticFieldLeak")
    private TextView verboseOutputTextview;
    private Multimap<String, Integer> multiMap;
    private List<SignalSample> signalSampleList;
    private List<AccessPointInformation> accessPointInformationList;

    /**
     * Constructor for normal mode.
     *
     * @param scanCount        Number of scans
     * @param wifiManager      WifiManager instance
     * @param calculateAverage Boolean indicating if results should be averaged
     * @param progressBar      Optional ProgressBar for displaying progress
     * @param progressTextview Optional TextView for displaying progress
     */
    public FingerprintTask(final int scanCount, final WifiManager wifiManager, final Boolean calculateAverage,
                           final @Nullable ProgressBar progressBar, final @Nullable TextView progressTextview) {
        this.wifiManager = wifiManager;
        this.calculateAverage = calculateAverage;
        this.progressBar = progressBar;
        this.progressTextview = progressTextview;
        this.scanCount = scanCount;
    }

    /**
     * Constructor for verbose mode.
     *
     * @param scanCount             Number of scans
     * @param wifiManager           WifiManager instance
     * @param calculateAverage      Boolean indicating if results should be averaged
     * @param progressBar           Optional ProgressBar for displaying progress
     * @param progressTextview      Optional TextView for displaying progress
     * @param verboseOutputTextview TextView for displaying live measuring data
     */
    public FingerprintTask(final int scanCount, final WifiManager wifiManager, final Boolean calculateAverage,
                           final @Nullable ProgressBar progressBar, final @Nullable TextView progressTextview, final TextView verboseOutputTextview) {
        this.wifiManager = wifiManager;
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
                Log.d(FINGERPRINT_TASK, "Task cancelled");
                return null;
            }

            wifiManager.startScan();
            List<ScanResult> wifiScanList = wifiManager.getScanResults();
            Log.d(FINGERPRINT_TASK, "ScanResult.size: " + wifiScanList.size());

            // Check for deprecated access points if measuring for one second
            if (scanCount == 1) {
                float deprecatedAccessPoints = 0;
                float numberOfAccessPoints = wifiScanList.size();

                for (ScanResult scanResult : wifiScanList) {
                    if (timestampMap.get(scanResult.BSSID) != null && Objects.equals(timestampMap.get(scanResult.BSSID), scanResult.timestamp)) {
                        deprecatedAccessPoints++;
                    } else {
                        timestampMap.put(scanResult.BSSID, scanResult.timestamp);
                    }
                }

                float deprecationThreshold = 0.5f;
                if (deprecatedAccessPoints > (numberOfAccessPoints * deprecationThreshold)) {
                    Log.d(FINGERPRINT_TASK, "More than half of the measurements are outdated");
                    return null;
                }
            }

            for (final ScanResult sr : wifiScanList) {
//                Log.d(FINGERPRINT_TASK, "MAC: " + sr.BSSID + " Strength: " + sr.level + " dBm Timestamp: " + sr.timestamp + " SSID: " + sr.SSID);
                AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(sr.BSSID, sr.level, sr.SSID);
                accessPointInformationList.add(accessPointInformation);
                multiMap.put(sr.BSSID, sr.level);
            }
            publishProgress(i);

            long timestampSeconds = System.currentTimeMillis() / 1000;
            SignalSample signalSample = new SignalSample(timestampSeconds, accessPointInformationList);
            signalSampleList.add(signalSample);

            if (i != (scanCount - 1)) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Log.d(FINGERPRINT_TASK, e.toString());
                }
            }
        }

        Fingerprint fingerprint = FingerprintFactory.createInstance(signalSampleList);
        Log.d(FINGERPRINT_TASK, "Fingerprint created: " + fingerprint);

        if (calculateAverage) {
            List<SignalSample> signalSamples = AverageSignalCalculator.calculateAverageSignal(multiMap);
            return FingerprintFactory.createInstance(signalSamples);
        } else {
            return FingerprintFactory.createInstance(signalSampleList);
        }
    }

    @Override
    protected void onCancelled() {
        Log.d(FINGERPRINT_TASK, "Task cancelled, processFinish called with null");
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
        Log.d(FINGERPRINT_TASK, "Task pre-execution setup done");
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (progressBar != null) {
            progressBar.setProgress(values[0] + 1);
        }

        // Verbose mode: show measuring data in UI
        if (verboseOutputTextview != null) {
            StringBuilder textviewString = new StringBuilder();

            for (int i = 0; i < accessPointInformationList.size(); i++) {
                if (i <= 7) { // Clip the output at 6 Access Points due to limited space
                    textviewString.append(accessPointInformationList.get(i).getBSSID()).append("  ")
                            .append(accessPointInformationList.get(i).getRSSI()).append("       ");
                }
            }
            verboseOutputTextview.setText(textviewString.toString());
            if (progressTextview != null) {
                progressTextview.setText(String.valueOf(scanCount - values[0]));
            }

        } else {
            if (progressTextview != null) {
                progressTextview.setText(String.valueOf(scanCount - values[0]));
            }
        }
        Log.d(FINGERPRINT_TASK, "Progress updated: " + values[0]);
    }

    @Override
    protected void onPostExecute(Fingerprint fingerprint) {
        Log.d(FINGERPRINT_TASK, "Task completed, processFinish called with fingerprint");
        delegate.processFinish(fingerprint, scanCount);
    }
}
