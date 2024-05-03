package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import java.util.List;

/**
 * Created by Johann Winter
 */

class FingerprintImpl implements Fingerprint {

    private final List<SignalSample> signalSampleList;

    FingerprintImpl(String ssid, List<SignalSample> signalSampleList) {
        this.signalSampleList = signalSampleList;
    }

    FingerprintImpl(List<SignalSample> signalSampleList) {
        this.signalSampleList = signalSampleList;
    }

    public List<SignalSample> getSignalSampleList() {
        return this.signalSampleList;
    }

}
