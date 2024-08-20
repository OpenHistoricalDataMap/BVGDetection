package de.htwberlin.f4.ai.ma.indoorroutefinder.location.classifier;

import android.util.Log;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KNNClassifier - A simple k-Nearest Neighbors classifier.
 * <p>
 * This implementation is based on the Python example provided by Kenzo Takahashi:
 * <a href="https://kenzotakahashi.github.io/k-nearest-neighbor-from-scratch-in-python.html">...</a>
 */
public class KNNClassifier implements Classifier {
    private static final String TAG = "KNNClassifier";
    private final int n_neighbors;
    private final DistanceMetric distanceMetric;
    private final WeightType weightType;
    private final double[][] X;
    private final String[] y;

    /**
     * Constructor for KNNClassifier.
     *
     * @param X              The training data.
     * @param y              The training labels.
     * @param n_neighbors    The number of neighbors to consider.
     * @param distanceMetric The distance metric to use (EUCLIDEAN or SORENSEN).
     * @param weightType     The type of weighting to use (UNIFORM or DISTANCE).
     */
    public KNNClassifier(double[][] X, String[] y, int n_neighbors, DistanceMetric distanceMetric, WeightType weightType) {
        this.X = X;
        this.y = y;
        this.n_neighbors = n_neighbors;
        this.distanceMetric = distanceMetric;
        this.weightType = weightType;
        Log.d(TAG, "KNNClassifier initialized with " + n_neighbors + " neighbors, distance metric: " + distanceMetric + ", weight type: " + weightType);
    }

    /**
     * Calculate the distance between two data points using the specified distance metric.
     *
     * @param data1 The first data point.
     * @param data2 The second data point.
     * @return The distance between the two points.
     */
    private double distance(double[] data1, double[] data2) {
        switch (this.distanceMetric) {
            case EUCLIDEAN:
                return euclideanDistance(data1, data2);
            case SORENSEN:
                return sorensenDistance(data1, data2);
            default:
                throw new IllegalArgumentException("Distance metric not recognized: should be EUCLIDEAN or SORENSEN");
        }
    }

    /**
     * Calculate the Euclidean distance between two data points.
     *
     * @param x The first data point.
     * @param y The second data point.
     * @return The Euclidean distance between the two points.
     */
    private double euclideanDistance(double[] x, double[] y) {
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            sum += Math.pow(x[i] - y[i], 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * Calculate the Sørensen distance between two data points.
     *
     * @param x The first data point.
     * @param y The second data point.
     * @return The Sørensen distance between the two points.
     */
    private double sorensenDistance(double[] x, double[] y) {
        double numerator = 0.0;
        double denominator = 0.0;
        for (int i = 0; i < x.length; i++) {
            numerator += Math.abs(x[i] - y[i]);
            denominator += Math.abs(x[i]) + Math.abs(y[i]);
        }
        return numerator / denominator;
    }

    /**
     * Compute the weights for the neighbors based on the chosen weighting method.
     *
     * @param distances The list of distances and their corresponding labels.
     * @return A list of weights and their corresponding labels.
     */
    private List<Map.Entry<Double, String>> computeWeights(List<Map.Entry<Double, String>> distances) {
        List<Map.Entry<Double, String>> weightedList = new ArrayList<>();

        if (this.weightType == WeightType.UNIFORM) {
            for (Map.Entry<Double, String> entry : distances) {
                Map.Entry<Double, String> weightedEntry = new AbstractMap.SimpleEntry<>(1.0, entry.getValue());
                weightedList.add(weightedEntry);
            }
        } else if (this.weightType == WeightType.DISTANCE) {
            boolean hasZeroDistance = false;

            for (Map.Entry<Double, String> entry : distances) {
                if (entry.getKey() == 0.0) {
                    hasZeroDistance = true;
                    break;
                }
            }

            if (hasZeroDistance) {
                for (Map.Entry<Double, String> entry : distances) {
                    if (entry.getKey() == 0.0) {
                        Map.Entry<Double, String> weightedEntry = new AbstractMap.SimpleEntry<>(1.0, entry.getValue());
                        weightedList.add(weightedEntry);
                    }
                }
            } else {
                for (Map.Entry<Double, String> entry : distances) {
                    double weight = 1 / entry.getKey();
                    Map.Entry<Double, String> weightedEntry = new AbstractMap.SimpleEntry<>(weight, entry.getValue());
                    weightedList.add(weightedEntry);
                }
            }
        } else {
            throw new IllegalArgumentException("Weight type not recognized: should be UNIFORM or DISTANCE");
        }

        return weightedList;
    }

    /**
     * Predict the label for a single test point.
     *
     * @param test The test point.
     * @return The predicted label.
     */
    @Override
    public String predict(double[] test) {
        Log.d(TAG, "Predicting label for test point: " + arrayToString(test));
        List<Map.Entry<Double, String>> distances = new ArrayList<>();
        for (int i = 0; i < X.length; i++) {
            double dist = distance(X[i], test);
            distances.add(new AbstractMap.SimpleEntry<>(dist, y[i]));
        }
        distances.sort(Map.Entry.comparingByKey());

        List<Map.Entry<Double, String>> neighbors = distances.subList(0, this.n_neighbors);
        List<Map.Entry<Double, String>> weights = computeWeights(neighbors);
        Log.d(TAG, "Computed weights for neighbors: " + weights);

        Map<String, Double> weightsByClass = new HashMap<>();
        for (Map.Entry<Double, String> weight : weights) {
            weightsByClass.put(weight.getValue(), weightsByClass.getOrDefault(weight.getValue(), 0.0) + weight.getKey());
            Log.d(TAG, "Weight for class " + weight.getValue() + ": " + weightsByClass.get(weight.getValue()));
        }

        String predictedLabel = weightsByClass.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        Log.d(TAG, "Predicted label: " + predictedLabel);
        return predictedLabel;
    }

    private String arrayToString(double[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public enum DistanceMetric {
        EUCLIDEAN,
        SORENSEN
    }

    public enum WeightType {
        UNIFORM,
        DISTANCE
    }
}
