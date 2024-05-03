package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information;

/**
 * Created by Johann Winter
 */

class AccessPointInformationImpl implements AccessPointInformation {

    private final String macAddress;
    private final int rssi;

    AccessPointInformationImpl(String macAddress, int rssi) {
        this.macAddress = macAddress;
        this.rssi = rssi;
    }

    public int getRssi() {
        return this.rssi;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

}