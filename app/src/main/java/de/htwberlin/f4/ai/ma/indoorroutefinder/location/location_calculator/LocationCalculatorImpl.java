package de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformationFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations.EuclideanDistance;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations.KNearestNeighbor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations.KalmanFilter;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations.MovingAverage;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations.RestructedNode;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

/**
 * Created by Johann Winter
 */
class LocationCalculatorImpl implements LocationCalculator {

    private final DatabaseHandler databaseHandler;
    private final SharedPreferences sharedPreferences;
    Context context;

    LocationCalculatorImpl(Context context) {
        this.context = context;
        databaseHandler = DatabaseHandlerFactory.getInstance(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Calculate a node from a fingerprint
     *
     * @param fingerprint the input fingerprint to be compared with all existent nodes' fingerprints to get the position
     * @return the ID (name) of the resulting Node
     */
    public String calculateNodeId(Fingerprint fingerprint) {

        List<SignalSample> signalSampleList = fingerprint.getSignalSampleList();

        boolean movingAverage = sharedPreferences.getBoolean("pref_movingAverage", true);
        boolean kalmanFilter = sharedPreferences.getBoolean("pref_kalman", true);
        boolean euclideanDistance = sharedPreferences.getBoolean("pref_euclideanDistance", true);
        boolean knnAlgorithm = sharedPreferences.getBoolean("pref_knnAlgorithm", true);

        int movingAverageOrder = Integer.parseInt(sharedPreferences.getString("pref_movivngAverageOrder", "3"));
        int knnValue = Integer.parseInt(sharedPreferences.getString("pref_knnNeighbours", "3"));
        int kalmanValue = Integer.parseInt(sharedPreferences.getString("pref_kalmanValue", "2"));

        String foundNode = null;

        // Load all nodes which have a fingerprint
        List<Node> nodesWithFingerprint = new ArrayList<>();
        for (Node n : databaseHandler.getAllNodes()) {
            if (n.getFingerprint() != null) {
                nodesWithFingerprint.add(n);
            }
        }

        List<RestructedNode> restructedNodeList = calculateNewNodeDataset(nodesWithFingerprint);
        List<RestructedNode> calculatedNodeList = new ArrayList<>();

        if (!restructedNodeList.isEmpty()) {
            if (movingAverage) {
                calculatedNodeList = MovingAverage.calculate(restructedNodeList, movingAverageOrder);

            } else if (kalmanFilter) {
                calculatedNodeList = KalmanFilter.calculateCalman(kalmanValue, restructedNodeList);
            }

            if (euclideanDistance) {
                List<AccessPointInformation> accessPointInformations = getSignalStrengths(signalSampleList);

                if (accessPointInformations.isEmpty()) {
                    return null;
                }
                List<String> distanceNames = EuclideanDistance.calculateDistance(calculatedNodeList, accessPointInformations);
                if (knnAlgorithm) {
                    foundNode = KNearestNeighbor.calculateKnn(knnValue, distanceNames);

                } else if (!distanceNames.isEmpty()) {
                    foundNode = distanceNames.get(0);
                }
            }
            return foundNode;
        } else {
            return null;
        }
    }


    /**
     * Get a list of AccessPointInformations by passing a list of SignalSample (unwrap).
     *
     * @param signalSampleList a list of SignalSamples
     * @return a list of AccessPointInformations
     */
    public List<AccessPointInformation> getSignalStrengths(List<SignalSample> signalSampleList) {
        List<AccessPointInformation> accessPointInformations = new ArrayList<>();

        for (SignalSample signalSample : signalSampleList) {
            for (AccessPointInformation accessPointInformation : signalSample.getAccessPointInformationList()) {
                String macAdress = accessPointInformation.getMacAddress();
                int signalStrength = accessPointInformation.getRssi();
                AccessPointInformation aps = AccessPointInformationFactory.createInstance(macAdress, signalStrength);
                accessPointInformations.add(aps);
            }
        }
        return accessPointInformations;
    }


    /**
     * Rewrite the nodelist to restrucetd Nodes and delete weak MAC addresses
     *
     * @param allNodes list of all nodes
     * @return restructed node list
     */
    @SuppressLint("CheckResult")
    public List<RestructedNode> calculateNewNodeDataset(List<Node> allNodes) {
        List<String> macAddresses;
        int count;

        List<RestructedNode> restructedNodes = new ArrayList<>();
        Multimap<String, Double> multiMap;

        for (Node node : allNodes) {
            count = node.getFingerprint().getSignalSampleList().size();
            double minValue = (((double) 1 / (double) 3) * (double) count);
            macAddresses = getMacAddresses(node);
            multiMap = getMultiMap(node, macAddresses);

            //delete weak addresses
            for (String macAddress : macAddresses) {
                int countValue = 0;

                for (Double signalValue : multiMap.get(macAddress)) {
                    if (signalValue != null) {
                        countValue++;
                    }
                }
                if (countValue <= minValue) {
                    multiMap.removeAll(macAddress);
                }
            }
            //fill restructed Nodes
            RestructedNode restructedNode = new RestructedNode(node.getId(), multiMap);
            restructedNodes.add(restructedNode);
        }
        return restructedNodes;
    }


    /**
     * Create a multimap with MAC address and signal strength values
     *
     * @param node        input node
     * @param macAdresses list of MAC addresses
     * @return multimap with mac addresses and signal strengths
     */
    @SuppressLint("CheckResult")
    public Multimap<String, Double> getMultiMap(Node node, List<String> macAdresses) {
        Multimap<String, Double> multiMap = ArrayListMultimap.create();
        for (SignalSample signalInfo : node.getFingerprint().getSignalSampleList()) {
            HashSet<String> actuallyMacAdresses = new HashSet<>();
            for (AccessPointInformation accessPointInformation : signalInfo.getAccessPointInformationList()) {
                multiMap.put(accessPointInformation.getMacAddress(), (double) accessPointInformation.getRssi());
                actuallyMacAdresses.add(accessPointInformation.getMacAddress());
            }
            for (String checkMacAdress : macAdresses) {
                if (!actuallyMacAdresses.contains(checkMacAdress)) {
                    multiMap.put(checkMacAdress, null);
                }
            }
        }
        return multiMap;
    }


    /**
     * Get all mac addresses of a specific node
     *
     * @param node the node
     * @return list of unique MAC addresses
     */
    public List<String> getMacAddresses(Node node) {
        HashSet<String> macAdresses = new HashSet<>();
        for (SignalSample signalSample : node.getFingerprint().getSignalSampleList()) {
            for (AccessPointInformation accessPointInformation : signalSample.getAccessPointInformationList()) {
                macAdresses.add(accessPointInformation.getMacAddress());
            }
        }
        return new ArrayList<>(macAdresses);
    }

}
