package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformationFactory;

/**
 * Created by Johann Winter
 * <p>
 * Calculate average values from the capture
 */


class AverageSignalCalculator {

    static List<SignalSample> calculateAverageSignal(Multimap<String, Integer> multiMap) {

        Set<String> BSSIDs = multiMap.keySet();

        final List<SignalSample> signalSampleList = new ArrayList<>();

        for (String s : BSSIDs) {
            int value = 0;
            int counter = 0;

            for (int test : multiMap.get(s)) {
                counter++;
                value += test;
            }
            value = value / counter;

            List<AccessPointInformation> accessPointInformations = new ArrayList<>();
            AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(s, value);
            accessPointInformations.add(accessPointInformation);
            SignalSample signalSample = new SignalSample("", accessPointInformations);
            signalSampleList.add(signalSample);
        }
        return signalSampleList;
    }
}



