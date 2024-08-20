package de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.classifier.KNNClassifier;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.classifier.RandomForestClassifier;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.classifier.SVMClassifier;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;

/**
 * This class calculates the node ID based on a fingerprint.
 */
class LocationCalculatorImpl implements LocationCalculator {

    public static final String LOCATION_CALCULATOR_IMPL = "LocationCalculatorImpl";
    private final DatabaseHandler databaseHandler;
    private final SharedPreferences sharedPreferences;
    private final Context context;

    LocationCalculatorImpl(Context context) {
        this.context = context;
        this.databaseHandler = DatabaseHandlerFactory.getInstance(context);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Calculates the node ID for a given fingerprint.
     *
     * @param fingerprint the fingerprint to be used for calculation
     * @return the calculated node ID
     */
    public String calculateNodeId(Fingerprint fingerprint) {
        String algorithm = sharedPreferences.getString("pref_algorithm", "svm");
        String knnDistanceMetric = sharedPreferences.getString("pref_knn_distance_metric", "euclidean");
        String knnWeightType = sharedPreferences.getString("pref_knn_weight_type", "uniform");
        int knnK = Integer.parseInt(sharedPreferences.getString("pref_knn_k", "3"));
        float svmC = Float.parseFloat(sharedPreferences.getString("pref_svm_c", "1.0"));
        float svmGamma = Float.parseFloat(sharedPreferences.getString("pref_svm_gamma", "0.1"));
        String svmKernel = sharedPreferences.getString("pref_svm_kernel", "linear");
        int rfTrees = Integer.parseInt(sharedPreferences.getString("pref_rf_trees", "10"));
        int rfMaxFeatures = Integer.parseInt(sharedPreferences.getString("pref_rf_max_features", "10"));
        int rfMaxDepth = Integer.parseInt(sharedPreferences.getString("pref_rf_max_depth", "10"));

        String[] defaultWifiNetworks = sharedPreferences.getStringSet("default_wifi_network", new HashSet<>()).toArray(new String[0]);
        boolean useAllRouters = sharedPreferences.getBoolean("use_all_routers", true);
        double routerThreshold = Double.parseDouble(sharedPreferences.getString("router_threshold", "0.25"));
        int signalThreshold = Integer.parseInt(sharedPreferences.getString("signal_threshold", "-100"));
        String scalingStrategy = sharedPreferences.getString("scaling_strategy", "none");
        String missingValues = sharedPreferences.getString("handle_missing", "-100");

        Log.d(LOCATION_CALCULATOR_IMPL, "Configuration loaded");

        double alpha = 24.0;
        double beta = Math.E;

        // Step 1: Filter rooms with fingerprints
        List<Room> roomsWithFingerprint = databaseHandler.getAllRooms().stream()
                .filter(room -> room.getFingerprint() != null)
                .collect(Collectors.toList());

        Log.d(LOCATION_CALCULATOR_IMPL, "Rooms with fingerprints filtered");

        // Step 2: Filter routers by SSID
        List<AccessPointInformation> givenFingerprintAPs = LocationCalculatorUtils.filterBySSID(
                fingerprint.getSignalSampleList().stream()
                        .flatMap(sample -> sample.getAccessPointInformationList().stream())
                        .collect(Collectors.toList()),
                defaultWifiNetworks,
                useAllRouters
        );

        List<String> givenFingerprintBSSIDs = givenFingerprintAPs.stream()
                .map(AccessPointInformation::getBSSID)
                .collect(Collectors.toList());

        List<Room> filteredRooms = new ArrayList<>();
        Map<Room, List<SignalSample>> filteredRoomSamplesMap = new HashMap<>();

        for (Room room : roomsWithFingerprint) {
            List<SignalSample> filteredSamples = LocationCalculatorUtils.filterRareRouters(room.getFingerprint().getSignalSampleList(), routerThreshold);
            if (!filteredSamples.isEmpty()) {
                filteredRooms.add(room);
                filteredRoomSamplesMap.put(room, filteredSamples);
            }
        }

        Log.d(LOCATION_CALCULATOR_IMPL, "Routers filtered by SSID");

        // Step 3: Prepare training data and labels
        List<int[]> trainDataList = new ArrayList<>();
        List<String> trainLabelsList = new ArrayList<>();

        for (Room room : filteredRooms) {
            List<SignalSample> filteredSamples = filteredRoomSamplesMap.get(room);

            if (filteredSamples != null) {
                for (SignalSample sample : filteredSamples) {
                    int[] signalStrengths = new int[givenFingerprintBSSIDs.size()];
                    for (int i = 0; i < givenFingerprintBSSIDs.size(); i++) {
                        String bssid = givenFingerprintBSSIDs.get(i);
                        int signal = LocationCalculatorUtils.getSignalStrength(sample.getAccessPointInformationList(), bssid, missingValues, fingerprint);
                        signalStrengths[i] = LocationCalculatorUtils.replaceWeakSignals(signal, signalThreshold);
                    }
                    trainDataList.add(signalStrengths);
                    trainLabelsList.add(room.getRoomName());
                }
            }
        }

        int[][] trainData = trainDataList.toArray(new int[0][]);
        String[] trainLabels = trainLabelsList.toArray(new String[0]);

        Log.d(LOCATION_CALCULATOR_IMPL, "Training data prepared");

        // Step 4: Prepare test data
        int[] testData = new int[givenFingerprintBSSIDs.size()];
        for (int i = 0; i < givenFingerprintBSSIDs.size(); i++) {
            String bssid = givenFingerprintBSSIDs.get(i);
            int signal = LocationCalculatorUtils.getSignalStrength(givenFingerprintAPs, bssid, missingValues, fingerprint);
            testData[i] = LocationCalculatorUtils.replaceWeakSignals(signal, signalThreshold);
        }

        if (trainData.length == 0 || trainData[0].length == 0 || testData.length == 0) {
            return null;
        }

        Log.d(LOCATION_CALCULATOR_IMPL, "Test data prepared");

        // Step 5: Scale values
        int minRssi = Arrays.stream(trainData).flatMapToInt(Arrays::stream).min().orElse(-100);
        double[][] scaledTrainData = Arrays.stream(trainData)
                .map(row -> LocationCalculatorUtils.scaleValues(row, minRssi, scalingStrategy, alpha, beta))
                .toArray(double[][]::new);
        double[] scaledTestData = LocationCalculatorUtils.scaleValues(testData, minRssi, scalingStrategy, alpha, beta);

        Log.d(LOCATION_CALCULATOR_IMPL, "Values scaled");

        String prediction = null;

        // Prediction
        switch (algorithm) {
            case "knn":
                KNNClassifier.DistanceMetric distanceMetric = knnDistanceMetric.equals("euclidean") ? KNNClassifier.DistanceMetric.EUCLIDEAN : KNNClassifier.DistanceMetric.SORENSEN;
                KNNClassifier.WeightType weightType = knnWeightType.equals("uniform") ? KNNClassifier.WeightType.UNIFORM : KNNClassifier.WeightType.DISTANCE;
                KNNClassifier knnClassifier = new KNNClassifier(scaledTrainData, trainLabels, knnK, distanceMetric, weightType);
                prediction = knnClassifier.predict(scaledTestData);
                break;
            case "svm":
                SVMClassifier.KernelType kernelType = svmKernel.equals("linear") ? SVMClassifier.KernelType.LINEAR : SVMClassifier.KernelType.RBF;
                SVMClassifier svmClassifier = new SVMClassifier(context, scaledTrainData, trainLabels, svmC, svmGamma, kernelType);
                prediction = svmClassifier.predict(scaledTestData);
                break;
            case "random_forest":
                RandomForestClassifier randomForestClassifier = new RandomForestClassifier(scaledTrainData, trainLabels, rfTrees, rfMaxFeatures, rfMaxDepth);
                prediction = randomForestClassifier.predict(scaledTestData);
                break;
        }
        return prediction;
    }
}
