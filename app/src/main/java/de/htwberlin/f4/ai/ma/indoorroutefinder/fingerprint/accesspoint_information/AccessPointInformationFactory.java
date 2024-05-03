package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information;

/**
 * Created by Johann Winter
 * <p>
 * Factory for creating AccessPointInformations
 */

public class AccessPointInformationFactory {

    public static AccessPointInformation createInstance(String macAddress, int RSSI) {
        return new AccessPointInformationImpl(macAddress, RSSI);
    }
}
