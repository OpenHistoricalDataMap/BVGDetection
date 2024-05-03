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
 */
public class MovingAverage {

    /**
     * Calculate moving average algorithm depending on the order three or five.
     *
     * @param order              the order (3 or 5)
     * @param restructedNodeList the list of restructed Nodes
     * @return the list of restructed Nodes
     */
    @SuppressLint("CheckResult")
    public static List<RestructedNode> calculate(List<RestructedNode> restructedNodeList, int order) {
        List<RestructedNode> calculatedNodes = new ArrayList<>();
        Multimap<String, Double> calculatedMultiMap;

        for (int i = 0; i < restructedNodeList.size(); i++) {
            RestructedNode restructedNode = restructedNodeList.get(i);
            calculatedMultiMap = ArrayListMultimap.create();

            double average;

            for (String Key : restructedNode.restructedSignals.keySet()) {
                int counter = 0;
                int tempAverage = 0;
                Double[] Values = restructedNode.restructedSignals.get(Key).toArray(new Double[0]);

                for (Double Signal : Values) {
                    if (Signal != null) {
                        counter++;
                        tempAverage += Signal;
                    }
                }
                average = (double) tempAverage / (double) counter;

                for (int j = 0; j < Values.length; j++) {

                    if (order == 3) {
                        if (j >= 2) {
                            double t1, t2, t3;
                            if (Values[j - 2] != null) {
                                t1 = Values[j - 2];
                            } else {
                                t1 = average;
                            }
                            if (Values[j - 1] != null) {
                                t2 = Values[j - 1];
                            } else {
                                t2 = average;
                            }
                            if (Values[j] != null) {
                                t3 = Values[j];
                            } else {
                                t3 = average;
                            }

                            double movingAverageValue = ((double) 1 / (double) 3) * (t1 + t2 + t3);
                            calculatedMultiMap.put(Key, movingAverageValue);
                        }

                    } else if (order == 5) {
                        if (j >= 4) {
                            double t1, t2, t3, t4, t5;
                            if (Values[j - 4] != null) {
                                t1 = Values[j - 4];
                            } else {
                                t1 = average;
                            }
                            if (Values[j - 3] != null) {
                                t2 = Values[j - 3];
                            } else {
                                t2 = average;
                            }
                            if (Values[j - 2] != null) {
                                t3 = Values[j - 2];
                            } else {
                                t3 = average;
                            }
                            if (Values[j - 1] != null) {
                                t4 = Values[j - 1];
                            } else {
                                t4 = average;
                            }
                            if (Values[j] != null) {
                                t5 = Values[j];
                            } else {
                                t5 = average;
                            }

                            double movingAverageValue = ((double) 1 / (double) 5) * (t1 + t2 + t3 + t4 + t5);
                            calculatedMultiMap.put(Key, movingAverageValue);
                        }
                    }
                }
            }
            calculatedNodes.add(new RestructedNode(restructedNode.id, calculatedMultiMap));
        }
        return calculatedNodes;
    }
}