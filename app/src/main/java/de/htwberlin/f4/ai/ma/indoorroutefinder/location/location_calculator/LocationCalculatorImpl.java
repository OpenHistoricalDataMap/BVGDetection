package de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;

/**
 * Created by Johann Winter
 */
class LocationCalculatorImpl implements LocationCalculator {

    public static final String LOCATION_CALCULATOR_IMPL = "LocationCalculatorImpl";
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
    @Deprecated
    public String calculateNodeIdOld(Fingerprint fingerprint) {

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
        List<Room> nodesWithFingerprint = new ArrayList<>();
        for (Room n : databaseHandler.getAllRooms()) {
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

    public String calculateNodeId(Fingerprint fingerprint) {

        Log.d(LOCATION_CALCULATOR_IMPL, "Calculating node ID from fingerprint: " + fingerprint.toString());

        List<SignalSample> signalSampleList = fingerprint.getSignalSampleList();
//        Log.d(LOCATION_CALCULATOR_IMPL, "Fingerprint: " + fingerprint.toString());
//
//        /*
//        bssid='da:bf:c0:0e:1e:17', rssi=-47, ssid='MicroPython-0e1e17'}, AccessPointInformationImpl{
//        bssid='dc:b8:08:c9:04:a0', rssi=-54, ssid='eduroam'}, AccessPointInformationImpl{
//        bssid='dc:b8:08:c9:04:a1', rssi=-54, ssid='HowToUseEduroam'}, AccessPointInformationImpl{
//        bssid='dc:b8:08:c9:04:a2', rssi=-54, ssid='Gast@HTW'}, AccessPointInformationImpl{
//        bssid='e4:fa:c4:fc:34:26', rssi=-66, ssid='Rechnernetze'}, AccessPointInformationImpl{
//        bssid='dc:b8:08:c9:01:b0', rssi=-61, ssid='eduroam'}, AccessPointInformationImpl{
//        bssid='00:09:9a:00:b6:43', rssi=-78, ssid='ELTX1001901'}, AccessPointInformationImpl{
//        bssid='dc:b8:08:c8:fe:e2', rssi=-88, ssid='Gast@HTW'}]}]}, roomName='test'}
//         */
//
//        // Measurement Room WH_C_625
//        AccessPointInformation accessPointInformation1 = AccessPointInformationFactory.createInstance("da:bf:c0:0e:1e:17", -47, "MicroPython-0e1e17");
//        AccessPointInformation accessPointInformation2 = AccessPointInformationFactory.createInstance("dc:b8:08:c9:04:a0", -54, "eduroam");
//        AccessPointInformation accessPointInformation3 = AccessPointInformationFactory.createInstance("dc:b8:08:c9:04:a1", -54, "HowToUseEduroam");
//        AccessPointInformation accessPointInformation4 = AccessPointInformationFactory.createInstance("dc:b8:08:c9:04:a2", -54, "Gast@HTW");
//        AccessPointInformation accessPointInformation5 = AccessPointInformationFactory.createInstance("e4:fa:c4:fc:34:26", -66, "Rechnernetze");
//        AccessPointInformation accessPointInformation6 = AccessPointInformationFactory.createInstance("dc:b8:08:c9:01:b0", -61, "eduroam");
//        AccessPointInformation accessPointInformation7 = AccessPointInformationFactory.createInstance("00:09:9a:00:b6:43", -78, "ELTX1001901");
//        AccessPointInformation accessPointInformation8 = AccessPointInformationFactory.createInstance("dc:b8:08:c8:fe:e2", -88, "Gast@HTW");
//
//
//        List<AccessPointInformation> accessPointInformationList = new ArrayList<>();
//        accessPointInformationList.add(accessPointInformation1);
//        accessPointInformationList.add(accessPointInformation2);
//        accessPointInformationList.add(accessPointInformation3);
//        accessPointInformationList.add(accessPointInformation4);
//        accessPointInformationList.add(accessPointInformation5);
//        accessPointInformationList.add(accessPointInformation6);
//        accessPointInformationList.add(accessPointInformation7);
//        accessPointInformationList.add(accessPointInformation8);
//
//
//        SignalSample signalSample = new SignalSample(1L, accessPointInformationList);
//        List<SignalSample> signalSampleList = new ArrayList<>();
//        signalSampleList.add(signalSample);
//        fingerprint = FingerprintFactory.createInstance(fingerprint.getSignalSampleList());

        boolean movingAverage = sharedPreferences.getBoolean("pref_movingAverage", true);
        boolean kalmanFilter = sharedPreferences.getBoolean("pref_kalman", true);
        boolean euclideanDistance = sharedPreferences.getBoolean("pref_euclideanDistance", true);
        boolean knnAlgorithm = sharedPreferences.getBoolean("pref_knnAlgorithm", true);

        int movingAverageOrder = Integer.parseInt(sharedPreferences.getString("pref_movivngAverageOrder", "3"));
        int knnValue = Integer.parseInt(sharedPreferences.getString("pref_knnNeighbours", "3"));
        int kalmanValue = Integer.parseInt(sharedPreferences.getString("pref_kalmanValue", "2"));

//        Log.d(LOCATION_CALCULATOR_IMPL, "Moving average: " + movingAverage);
//        Log.d(LOCATION_CALCULATOR_IMPL, "Kalman filter: " + kalmanFilter);
//        Log.d(LOCATION_CALCULATOR_IMPL, "Euclidean distance: " + euclideanDistance);
//        Log.d(LOCATION_CALCULATOR_IMPL, "KNN algorithm: " + knnAlgorithm);
//        Log.d(LOCATION_CALCULATOR_IMPL, "Moving average order: " + movingAverageOrder);
//        Log.d(LOCATION_CALCULATOR_IMPL, "KNN value: " + knnValue);
//        Log.d(LOCATION_CALCULATOR_IMPL, "Kalman value: " + kalmanValue);

        String foundNode = null;

        // Load all rooms which have a fingerprint
        List<Room> roomsWithFingerprint = new ArrayList<>();
        for (Room room : databaseHandler.getAllRooms()) {
//            Log.d(LOCATION_CALCULATOR_IMPL, room.toString());
            if (room.getFingerprint() != null) {
                roomsWithFingerprint.add(room);
            }
        }

//        Log.d(LOCATION_CALCULATOR_IMPL, roomsWithFingerprint.toString());

        List<RestructedNode> restructedNodeList = calculateNewNodeDataset(roomsWithFingerprint);
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
//                    Log.d(LOCATION_CALCULATOR_IMPL, "KNN algorithm");
//                    Log.d(LOCATION_CALCULATOR_IMPL, "knnValue: " + knnValue);
//                    Log.d(LOCATION_CALCULATOR_IMPL, "distanceNames: " + distanceNames);
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
                String macAdress = accessPointInformation.getBSSID();
                int signalStrength = accessPointInformation.getRSSI();
                String ssid = accessPointInformation.getSSID();
                AccessPointInformation aps = AccessPointInformationFactory.createInstance(macAdress, signalStrength, ssid);
                accessPointInformations.add(aps);
            }
        }
        return accessPointInformations;
    }


    /**
     * Rewrite the nodelist to restrucetd Nodes and delete weak MAC addresses
     *
     * @param allRooms list of all nodes
     * @return restructed node list
     */
    @SuppressLint("CheckResult")
    @Deprecated
    public List<RestructedNode> calculateNewNodeDatasetOld(List<Room> allRooms) {
        List<String> macAddresses;
        int count;

        List<RestructedNode> restructedNodes = new ArrayList<>();
        Multimap<String, Double> multiMap;

        for (Room room : allRooms) {
            count = room.getFingerprint().getSignalSampleList().size();
            double minValue = (((double) 1 / (double) 3) * (double) count);
            macAddresses = getMacAddresses(room);
            multiMap = getMultiMap(room, macAddresses);

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
            RestructedNode restructedNode = new RestructedNode(room.getRoomName(), multiMap);
            restructedNodes.add(restructedNode);
        }
        return restructedNodes;
    }

    /**
     * Rewrite the room list to restructured nodes and delete weak MAC addresses
     *
     * @param allRooms list of all rooms
     * @return restructured node list
     */
    @SuppressLint("CheckResult")
    public List<RestructedNode> calculateNewNodeDataset(List<Room> allRooms) {
        List<String> macAddresses;
        int count;

        List<RestructedNode> restructuredNodes = new ArrayList<>();
        Multimap<String, Double> multiMap;

        for (Room room : allRooms) {
            Fingerprint fingerprint = room.getFingerprint();
            if (fingerprint != null) {
//                Log.d("LOCATION_CALCULATOR_IMPL", "Processing room: " + room.getRoomName());

                count = fingerprint.getSignalSampleList().size();
                double minValue = (((double) 1 / (double) 3) * (double) count);
//                Log.d("LOCATION_CALCULATOR_IMPL", "Number of signal samples: " + count);
//                Log.d("LOCATION_CALCULATOR_IMPL", "Minimum value for MAC address retention: " + minValue);

                macAddresses = getMacAddresses(room);
//                Log.d("LOCATION_CALCULATOR_IMPL", "MAC addresses before filtering: " + macAddresses);

                multiMap = getMultiMap(room, macAddresses);
//                Log.d("LOCATION_CALCULATOR_IMPL", "MultiMap before filtering: " + multiMap);

                // Delete weak addresses
                for (String macAddress : macAddresses) {
                    int countValue = 0;

                    for (Double signalValue : multiMap.get(macAddress)) {
                        if (signalValue != null) {
                            countValue++;
                        }
                    }
//                    Log.d("LOCATION_CALCULATOR_IMPL", "Count for MAC address " + macAddress + ": " + countValue);
                    if (countValue <= minValue) {
                        multiMap.removeAll(macAddress);
//                        Log.d("LOCATION_CALCULATOR_IMPL", "Removed weak MAC address: " + macAddress);
                    }
                }
//                Log.d("LOCATION_CALCULATOR_IMPL", "MultiMap after filtering: " + multiMap);

                // Fill restructured nodes
                RestructedNode restructuredNode = new RestructedNode(room.getRoomName(), multiMap);
                restructuredNodes.add(restructuredNode);
//                Log.d("LOCATION_CALCULATOR_IMPL", "Added restructured node for room: " + room.getRoomName());
            }
        }
//        Log.d("LOCATION_CALCULATOR_IMPL", "Final list of restructured nodes: " + restructuredNodes);
        return restructuredNodes;
    }


    /**
     * Create a multimap with MAC address and signal strength values
     *
     * @param room        input node
     * @param macAdresses list of MAC addresses
     * @return multimap with mac addresses and signal strengths
     */
    @SuppressLint("CheckResult")
    public Multimap<String, Double> getMultiMap(Room room, List<String> macAdresses) {
        Multimap<String, Double> multiMap = ArrayListMultimap.create();
        for (SignalSample signalInfo : room.getFingerprint().getSignalSampleList()) {
            HashSet<String> actuallyMacAdresses = new HashSet<>();
            for (AccessPointInformation accessPointInformation : signalInfo.getAccessPointInformationList()) {
                multiMap.put(accessPointInformation.getBSSID(), (double) accessPointInformation.getRSSI());
                actuallyMacAdresses.add(accessPointInformation.getBSSID());
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
     * @param room the node
     * @return list of unique MAC addresses
     */
    public List<String> getMacAddresses(Room room) {
        HashSet<String> macAdresses = new HashSet<>();
        for (SignalSample signalSample : room.getFingerprint().getSignalSampleList()) {
            for (AccessPointInformation accessPointInformation : signalSample.getAccessPointInformationList()) {
                macAdresses.add(accessPointInformation.getBSSID());
            }
        }
        return new ArrayList<>(macAdresses);
    }

}
