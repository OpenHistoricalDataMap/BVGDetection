package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import java.util.List;

/**
 * Created by Johann Winter
 */

class FingerprintImpl implements Fingerprint {

    private final List<SignalSample> signalSampleList;
    private String deviceID;

    FingerprintImpl(List<SignalSample> signalSampleList) {
        this.signalSampleList = signalSampleList;
    }

    public List<SignalSample> getSignalSampleList() {
        return this.signalSampleList;
    }

    @Override
    public String getDeviceID() {
        return this.deviceID;
    }

    @Override
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    @Override
    public String toString() {
        return "FingerprintImpl{" +
                "signalSampleList=" + signalSampleList +
                '}';
    }
}
