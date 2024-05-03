package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import java.util.List;

/**
 * Created by Johann Winter
 */

class FingerprintImpl implements Fingerprint {

    private final String ssid;
    private final List<SignalSample> signalSampleList;

    FingerprintImpl(String ssid, List<SignalSample> signalSampleList) {
        this.ssid = ssid;
        this.signalSampleList = signalSampleList;
    }

    public String getSsid() {
        return this.ssid;
    }

    public List<SignalSample> getSignalSampleList() {
        return this.signalSampleList;
    }

}
