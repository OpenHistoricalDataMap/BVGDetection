package de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;

/**
 * Utility class for location calculator methods.
 */
public class LocationCalculatorUtils {

    /**
     * Retrieves the signal strength (RSSI) for a specific BSSID from a list of access point information.
     * If the BSSID is not found in the given list and the mode is "Ignorieren", it will attempt to find
     * the RSSI from a reference fingerprint.
     * If the BSSID is not found and the mode is "0", it returns 0. Otherwise, it returns -100.
     *
     * @param apInfos              the list of access point information
     * @param bssid                the BSSID to find
     * @param mode                 the mode for handling missing BSSID ("Ignorieren" or "0")
     * @param referenceFingerprint the reference fingerprint to check if the BSSID is not found in apInfos
     * @return the signal strength (RSSI) for the specified BSSID, or a default value if not found
     */
    public static int getSignalStrength(List<AccessPointInformation> apInfos, String bssid, String mode, Fingerprint referenceFingerprint) {
        for (AccessPointInformation apInfo : apInfos) {
            if (apInfo.getBSSID().equals(bssid)) {
                return apInfo.getRSSI();
            }
        }
        if (Objects.equals(mode, "Ignorieren") && referenceFingerprint != null) {
            for (SignalSample sample : referenceFingerprint.getSignalSampleList()) {
                for (AccessPointInformation refApInfo : sample.getAccessPointInformationList()) {
                    if (refApInfo.getBSSID().equals(bssid)) {
                        return refApInfo.getRSSI();
                    }
                }
            }
        }
        return (Objects.equals(mode, "0")) ? 0 : -100;
    }


    /**
     * Scales RSSI values.
     *
     * @param rssiValues the RSSI values to scale
     * @param minRssi    the minimum RSSI value
     * @param strategy   the scaling strategy
     * @param alpha      the alpha parameter for scaling
     * @param beta       the beta parameter for scaling
     * @return the scaled values
     */
    public static double[] scaleValues(int[] rssiValues, int minRssi, String strategy, double alpha, double beta) {
        double[] scaledValues;
        switch (strategy) {
            case "exponential":
                scaledValues = exponentialRepresentation(rssiValues, minRssi, alpha);
                break;
            case "powed":
                scaledValues = powedRepresentation(rssiValues, minRssi, beta);
                break;
            case "positive":
                scaledValues = positiveValuesRepresentation(rssiValues, minRssi);
                break;
            case "none":
                scaledValues = Arrays.stream(rssiValues).asDoubleStream().toArray();
                break;
            default:
                throw new IllegalArgumentException("Invalid scaling strategy: " + strategy);
        }
        if (!strategy.equals("none")) {
            scaledValues = normalize(scaledValues);
        }
        return scaledValues;
    }

    /**
     * Normalizes values.
     *
     * @param values the values to normalize
     * @return the normalized values
     */
    public static double[] normalize(double[] values) {
        double minVal = Arrays.stream(values).min().getAsDouble();
        double maxVal = Arrays.stream(values).max().getAsDouble();
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (values[i] - minVal) / (maxVal - minVal);
        }
        return result;
    }

    /**
     * Represents RSSI values as positive values.
     *
     * @param rssiValues the RSSI values to represent
     * @param minRssi    the minimum RSSI value
     * @return the positive values representation
     */
    public static double[] positiveValuesRepresentation(int[] rssiValues, int minRssi) {
        double[] result = new double[rssiValues.length];
        for (int i = 0; i < rssiValues.length; i++) {
            result[i] = rssiValues[i] - minRssi;
        }
        return result;
    }

    /**
     * Represents RSSI values exponentially.
     *
     * @param rssiValues the RSSI values to represent
     * @param minRssi    the minimum RSSI value
     * @param alpha      the alpha parameter
     * @return the exponential representation
     */
    public static double[] exponentialRepresentation(int[] rssiValues, int minRssi, double alpha) {
        double[] positiveValues = positiveValuesRepresentation(rssiValues, minRssi);
        double[] result = new double[positiveValues.length];
        for (int i = 0; i < positiveValues.length; i++) {
            result[i] = Math.exp(positiveValues[i] / alpha);
        }
        return result;
    }

    /**
     * Represents RSSI values as power values.
     *
     * @param rssiValues the RSSI values to represent
     * @param minRssi    the minimum RSSI value
     * @param beta       the beta parameter
     * @return the power representation
     */
    public static double[] powedRepresentation(int[] rssiValues, int minRssi, double beta) {
        double[] positiveValues = positiveValuesRepresentation(rssiValues, minRssi);
        double[] result = new double[positiveValues.length];
        for (int i = 0; i < positiveValues.length; i++) {
            result[i] = Math.pow(positiveValues[i], beta) / Math.pow(Math.abs(minRssi), beta);
        }
        return result;
    }

    /**
     * Replaces weak signals with a default value.
     *
     * @param rssi      the RSSI value
     * @param threshold the threshold for weak signals
     * @return the replaced RSSI value
     */
    public static int replaceWeakSignals(int rssi, int threshold) {
        return rssi < threshold ? -100 : rssi;
    }

    /**
     * Filters rare routers from signal samples based on a threshold.
     *
     * @param signalSamples the list of signal samples
     * @param threshold     the threshold for filtering
     * @return the filtered list of signal samples
     */
    public static List<SignalSample> filterRareRouters(List<SignalSample> signalSamples, double threshold) {
        Map<String, Integer> routerCounts = new HashMap<>();
        int totalMeasurements = signalSamples.size();

        for (SignalSample sample : signalSamples) {
            for (AccessPointInformation apInfo : sample.getAccessPointInformationList()) {
                routerCounts.put(apInfo.getBSSID(), routerCounts.getOrDefault(apInfo.getBSSID(), 0) + 1);
            }
        }

        double minRequiredCount = threshold * totalMeasurements;
        List<SignalSample> filteredSamples = new ArrayList<>();

        for (SignalSample sample : signalSamples) {
            List<AccessPointInformation> filteredAPInfos = sample.getAccessPointInformationList().stream()
                    .filter(apInfo -> routerCounts.get(apInfo.getBSSID()) >= minRequiredCount)
                    .collect(Collectors.toList());

            if (!filteredAPInfos.isEmpty()) {
                filteredSamples.add(new SignalSample(sample.getTimestamp(), filteredAPInfos));
            }
        }

        return filteredSamples;
    }

    /**
     * Filters access point information by SSID.
     *
     * @param apInfos             the list of access point information
     * @param defaultWifiNetworks the list of default Wi-Fi networks
     * @param useAllRouters       flag indicating whether to use all routers
     * @return the filtered list of access point information
     */
    public static List<AccessPointInformation> filterBySSID(List<AccessPointInformation> apInfos, String[] defaultWifiNetworks, boolean useAllRouters) {
        if (useAllRouters) {
            return apInfos;
        }

        return apInfos.stream()
                .filter(apInfo -> Arrays.stream(defaultWifiNetworks).anyMatch(ssid -> apInfo.getSSID().equals(ssid)))
                .collect(Collectors.toList());
    }
}
