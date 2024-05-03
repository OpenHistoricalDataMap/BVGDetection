package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information;

/**
 * Created by Johann Winter
 * <p>
 * This interface is designed for AccessPointInformations which are recorded
 * every second for every accesspoint while fingerprinting process.
 */
public interface AccessPointInformation {

    /**
     * Getter for the MAC-address of an access point
     *
     * @return the MAC-address string
     */
    String getBSSID();

    /**
     * Getter for the signal strength (RSSI) in dBm of an access point
     *
     * @return the signal strength (RSSI) in dBm
     */
    int getRSSI();

    /**
     * Getter for the SSID of an access point
     *
     * @return the SSID string
     */
    String getSSID();
}
