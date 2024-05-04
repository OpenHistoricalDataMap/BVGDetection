package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;

/**
 * Created by Johann Winter
 * <p>
 * A SignalSample consists of a timestamp and a list of AccessPointInformation at that time.
 */
public class SignalSample {

    private final long timestamp;
    private final List<AccessPointInformation> accessPointInformationList;
    private int measurementID;

    public SignalSample(long timestamp, List<AccessPointInformation> accessPointInformations) {
        this.timestamp = timestamp;
        this.accessPointInformationList = accessPointInformations;
    }

    public SignalSample(long timestamp, List<AccessPointInformation> accessPointInformations, int measurementID) {
        this.timestamp = timestamp;
        this.accessPointInformationList = accessPointInformations;
        this.measurementID = measurementID;
    }

    /**
     * Getter for the timestamp
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the list of SignalStrengths
     *
     * @return the list of SignalStrengths
     */
    public List<AccessPointInformation> getAccessPointInformationList() {
        return accessPointInformationList;
    }

    @Override
    public String toString() {
        return "SignalSample{" +
                "timestamp='" + timestamp + '\'' +
                ", accessPointInformationList=" + accessPointInformationList +
                '}';
    }

    /**
     * Getter for the measurementID
     *
     * @return the measurementID
     */
    public int getMeasurementID() {
        return measurementID;
    }

    /**
     * Setter for the measurementID
     *
     * @param measurementID the measurementID
     */
    public void setMeasurementID(int measurementID) {
        this.measurementID = measurementID;
    }
}