package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information;

/**
 * Created by Johann Winter
 */
class AccessPointInformationImpl implements AccessPointInformation {

    private final String bssid;
    private final int rssi;
    private final String ssid;

    AccessPointInformationImpl(String bssid, int rssi, String ssid) {
        this.bssid = bssid;
        this.rssi = rssi;
        this.ssid = ssid;
    }

    public int getRSSI() {
        return this.rssi;
    }

    @Override
    public String getSSID() {
        return this.ssid;
    }

    public String getBSSID() {
        return this.bssid;
    }

}