package de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations;

import android.annotation.SuppressLint;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Johann Winter
 * <p>
 * Thanks to Carola Walter
 * <p>
 * The Kalman filter
 */

public class KalmanFilter {

    @SuppressLint("CheckResult")
    public static List<RestructedNode> calculateCalman(int kalmanValue, List<RestructedNode> restructedNodeList) {

        List<RestructedNode> calculatedNodes = new ArrayList<>();
        Multimap<String, Double> calculatedMultiMap;

        for (int i = 0; i < restructedNodeList.size(); i++) {
            RestructedNode restructedNode = restructedNodeList.get(i);
            calculatedMultiMap = ArrayListMultimap.create();

            double average;
            double Xk, Pk, Kk, Pkt, Xkt, value, deviation;

            for (String Key : restructedNode.getRestructedSignals().keySet()) {
                int counter = 0;
                int tempAverage = 0;

                Double[] Values = restructedNode.getRestructedSignals().get(Key).toArray(new Double[0]);

                for (Double Signal : Values) {
                    if (Signal != null) {
                        counter++;
                        tempAverage += Signal;
                    }
                }
                average = (double) tempAverage / (double) counter;

                Xk = average;
                Pk = kalmanValue;

                deviation = calculateDeviation(Values, average, counter);

                for (Double aDouble : Values) {

                    if (aDouble != null) {
                        value = aDouble;
                    } else {
                        value = average;
                    }

                    Kk = (Pk) / (Pk + deviation);
                    Pkt = ((double) 1 - Kk) * Pk;
                    Pk = Pkt;
                    Xkt = Xk + Kk * (value - Xk);
                    Xk = Xkt;

                    calculatedMultiMap.put(Key, Xkt);
                }
            }
            calculatedNodes.add(new RestructedNode(restructedNode.getId(), calculatedMultiMap));
        }

        return calculatedNodes;
    }

    private static double calculateDeviation(Double[] values, double average, double count) {
        int x = 0;
        for (Double value : values) {
            if (value != null) {
                x += (int) Math.pow((value - average), 2);
            } else {
                x += (int) Math.pow((0.0), 2);
            }
        }
        double temp = ((double) 1 / ((double) count - 1)) * x;
        return Math.sqrt(temp);
    }
}