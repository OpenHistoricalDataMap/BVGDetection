package de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;


/**
 * Created by Johann Winter
 * <p>
 * Thanks to Carola Walter
 */
public class EuclideanDistance {


    /**
     * Calculate the euclidean distances
     *
     * @param restructedNodes         a list of restructed Nodes
     * @param accessPointInformations a list of measured AccesspointInformations
     * @return a sorted list of distances
     */
    public static List<String> calculateDistance(List<RestructedNode> restructedNodes, List<AccessPointInformation> accessPointInformations) {
        List<DistanceClass> distanceClassList = new ArrayList<>();

        for (int i = 0; i < restructedNodes.size(); i++) {

            List<Collection<Double>> matchingSignalStrengths = new ArrayList<>();
            List<Integer> measuredSignalStrength = new ArrayList<>();
            for (int j = 0; j < accessPointInformations.size(); j++) {
                boolean contains = restructedNodes.get(i).restructedSignals.containsKey(accessPointInformations.get(j).getMacAddress());
                if (contains) {
                    matchingSignalStrengths.add(restructedNodes.get(i).restructedSignals.get(accessPointInformations.get(j).getMacAddress()));
                    measuredSignalStrength.add(accessPointInformations.get(j).getRssi());
                }
            }

            if (matchingSignalStrengths.size() >= 2) {
                List<Iterator<Double>> signalIterators = new ArrayList<>();

                for (int j = 0; j < matchingSignalStrengths.size(); j++) {
                    signalIterators.add(matchingSignalStrengths.get(j).iterator());
                }

                while (signalIterators.get(0).hasNext()) {
                    double distance = 0d;

                    for (int j = 0; j < matchingSignalStrengths.size(); j++) {
                        Double restructedSignalStregnth = signalIterators.get(j).next();
                        distance += Math.pow(measuredSignalStrength.get(j) - restructedSignalStregnth, 2);
                    }

                    distance /= (double) matchingSignalStrengths.size();
                    distance = Math.sqrt(distance);

                    DistanceClass distanceClass = new DistanceClass();
                    distanceClass.name = restructedNodes.get(i).id;
                    distanceClass.distance = distance;
                    distanceClassList.add(distanceClass);
                }
            }
        }
        return sortSelection(distanceClassList);
    }

    /**
     * Sort euclidean distances
     *
     * @param distanceList list of DistanceClasses to sort
     * @return the sorted list
     */
    private static List<String> sortSelection(List<DistanceClass> distanceList) {
        for (int i = 0; i < distanceList.size() - 1; i++) {
            for (int j = i + 1; j < distanceList.size(); j++) {
                if (distanceList.get(i).distance > distanceList.get(j).distance) {
                    DistanceClass temp = distanceList.get(i);
                    distanceList.set(i, distanceList.get(j));
                    distanceList.set(j, temp);
                }
            }
        }

        List<String> distanceNameList = new ArrayList<>();
        for (DistanceClass poi : distanceList) {
            distanceNameList.add(poi.name);
        }
        return distanceNameList;
    }

}