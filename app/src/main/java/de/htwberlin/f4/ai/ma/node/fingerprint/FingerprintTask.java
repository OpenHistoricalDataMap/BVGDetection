package de.htwberlin.f4.ai.ma.node.fingerprint;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import de.htwberlin.f4.ai.ma.node.fingerprint.signalstrength.SignalStrength;
import de.htwberlin.f4.ai.ma.node.fingerprint.signalstrength.SignalStrengthImpl;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Created by Johann Winter
 */


public class FingerprintTask extends AsyncTask<Void, Integer, Fingerprint> {

    private ProgressBar progressBar;
    private TextView progressTextview;
    private String wifiName;
    private int seconds;
    private WifiManager wifiManager;
    private Multimap<String, Integer> multiMap;
    private List<SignalInformation> signalInformationList;
    private List<SignalStrength> signalStrengthList;
    private boolean calculateAverage = false;
    public AsyncResponse delegate = null;


    public FingerprintTask(final String wifiName, final int seconds, final WifiManager wifiManager, Boolean calculateAverage, ProgressBar progressBar, TextView progressTextview) {
        this.wifiManager = wifiManager;
        this.wifiName = wifiName;
        this.seconds = seconds;
        this.calculateAverage = calculateAverage;
        this.progressBar = progressBar;
        this.progressTextview = progressTextview;
    }


    @Override
    protected Fingerprint doInBackground(Void... voids) {
            for (int i = 0; i < seconds; i++) {

                signalStrengthList = new ArrayList<>();

                // If task gets aborted
                if (isCancelled()) {
                    return null;
                }

                publishProgress(i);

                wifiManager.startScan();
                List<ScanResult> wifiScanList = wifiManager.getScanResults();

                for (final ScanResult sr : wifiScanList) {
                    if (sr.SSID.equals(wifiName)) {

                        multiMap.put(sr.BSSID, sr.level);

                        SignalStrength signalStrength = new SignalStrengthImpl(sr.BSSID, sr.level);
                        signalStrengthList.add(signalStrength);
                    }
                }
                wifiScanList.clear();

                SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy-hh.mm.ss", Locale.getDefault());
                String format = s.format(new Date());
                SignalInformation signalInformation = new SignalInformation(format, signalStrengthList);
                signalInformationList.add(signalInformation);


                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (calculateAverage) {
                // Calculate average values
                List<SignalInformation> signalInformations = AverageSignalCalculator.calculateAverageSignal(multiMap);
                return new FingerprintImpl(wifiName, signalInformations);
            }

            else {
                return new FingerprintImpl(wifiName, signalInformationList);
            }

    }

    @Override
    protected void onCancelled() {
        delegate.processFinish(null, 0);
    }

    @Override
    protected void onPreExecute() {
        if (progressBar != null) {
            progressBar.setMax(seconds);
        }
        multiMap = ArrayListMultimap.create();
        signalInformationList = new ArrayList<>();
        signalStrengthList = new ArrayList<>();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (progressBar != null) {
            progressBar.setProgress(values[0] + 1);
        }
        if (progressTextview != null) {
            progressTextview.setText(String.valueOf(seconds - values[0]));
        }
    }

    @Override
    protected void onPostExecute(Fingerprint fingerprint) {
        delegate.processFinish(fingerprint, seconds);
    }


}
