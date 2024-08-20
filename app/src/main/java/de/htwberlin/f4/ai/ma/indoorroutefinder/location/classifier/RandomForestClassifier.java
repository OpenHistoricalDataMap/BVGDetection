package de.htwberlin.f4.ai.ma.indoorroutefinder.location.classifier;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This class implements a simple Random Forest algorithm in Java.
 * The implementation is inspired by the Python code example from
 * <a href="https://konfuzio.com/en/random-forest/#pure-python-implementierung-von-einem-random-forest">...</a>.
 */
public class RandomForestClassifier implements Classifier {
    private final int nTrees;
    private final int maxDepth;
    private final int minSamplesSplit;
    private final int maxFeatures;
    private final List<Node> trees;

    /**
     * Constructs a RandomForestClassifier.
     *
     * @param trainData   Training data.
     * @param trainLabels Labels for the training data.
     * @param nTrees      Number of trees in the forest.
     */
    public RandomForestClassifier(double[][] trainData, String[] trainLabels, int nTrees, int rfMaxFeatures, int rfMaxDepth) {
        this.nTrees = nTrees;
        this.maxDepth = rfMaxDepth;
        this.minSamplesSplit = 2;
        this.maxFeatures = rfMaxFeatures;
        this.trees = new ArrayList<>();
        Log.d("Random Forest Classifier", "Initializing RandomForestClassifier with " + nTrees + " trees.");
        fit(trainData, trainLabels);
    }

    /**
     * Fits the RandomForest model on the given data.
     *
     * @param X Training data.
     * @param y Labels for the training data.
     */
    public void fit(double[][] X, String[] y) {
        Log.d("Random Forest Classifier", "Fitting the model.");
        int nSamples = X.length;
        int nFeatures = X[0].length;

        for (int i = 0; i < nTrees; i++) {
            Log.d("Random Forest Classifier", "Building tree " + (i + 1) + " of " + nTrees + ".");
            int[] sampleIndices = randomSampleIndices(nSamples);
            double[][] XBootstrap = new double[sampleIndices.length][nFeatures];
            String[] yBootstrap = new String[sampleIndices.length];

            for (int j = 0; j < sampleIndices.length; j++) {
                XBootstrap[j] = X[sampleIndices[j]];
                yBootstrap[j] = y[sampleIndices[j]];
            }

            Node tree = buildTree(XBootstrap, yBootstrap, 0);
            trees.add(tree);
        }
        Log.d("Random Forest Classifier", "Model fitting completed.");
    }

    /**
     * Generates random sample indices for bootstrap sampling.
     *
     * @param nSamples Number of samples.
     * @return Array of random sample indices.
     */
    private int[] randomSampleIndices(int nSamples) {
        Random rand = new Random();
        int[] indices = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            indices[i] = rand.nextInt(nSamples);
        }
        Log.d("Random Forest Classifier", "Generated random sample indices for bootstrap sampling.");
        return indices;
    }

    /**
     * Builds a decision tree recursively.
     *
     * @param X     Training data.
     * @param y     Labels for the training data.
     * @param depth Current depth of the tree.
     * @return Root node of the built tree.
     */
    private Node buildTree(double[][] X, String[] y, int depth) {
        int nSamples = X.length;
        int nFeatures = X[0].length;

        if (depth >= maxDepth || nSamples < minSamplesSplit || isPure(y)) {
            Log.d("Random Forest Classifier", "Stopping condition met at depth " + depth + ".");
            return new Node(null, null, null, null, mostCommonLabel(y));
        }

        int[] featureIndices = randomFeatureIndices(nFeatures);
        double bestGain = 0;
        int bestFeatureIndex = -1;
        double bestThreshold = 0;

        for (int featureIndex : featureIndices) {
            double[] thresholds = uniqueValues(X, featureIndex);
            for (double threshold : thresholds) {
                double gain = informationGain(y, X, featureIndex, threshold);
                if (gain > bestGain) {
                    bestGain = gain;
                    bestFeatureIndex = featureIndex;
                    bestThreshold = threshold;
                }
            }
        }

        if (bestGain == 0) {
            Log.d("Random Forest Classifier", "No information gain. Returning leaf node.");
            return new Node(null, null, null, null, mostCommonLabel(y));
        }

        int[] leftIndices = split(X, bestFeatureIndex, bestThreshold, true);
        int[] rightIndices = split(X, bestFeatureIndex, bestThreshold, false);

        Log.d("Random Forest Classifier", "Splitting on feature " + bestFeatureIndex + " at threshold " + bestThreshold + ".");
        Node left = buildTree(subset(X, leftIndices), subset(y, leftIndices), depth + 1);
        Node right = buildTree(subset(X, rightIndices), subset(y, rightIndices), depth + 1);

        return new Node(bestFeatureIndex, bestThreshold, left, right, null);
    }

    /**
     * Checks if all elements in the array are the same.
     *
     * @param y Array of labels.
     * @return True if all elements are the same, false otherwise.
     */
    private boolean isPure(String[] y) {
        String first = y[0];
        for (String value : y) {
            if (!value.equals(first)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the most common label in the array.
     *
     * @param y Array of labels.
     * @return Most common label.
     */
    private String mostCommonLabel(String[] y) {
        Map<String, Integer> counts = new HashMap<>();
        for (String value : y) {
            counts.put(value, counts.getOrDefault(value, 0) + 1);
        }
        return Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * Generates random feature indices for selecting a subset of features.
     *
     * @param nFeatures Total number of features.
     * @return Array of random feature indices.
     */
    private int[] randomFeatureIndices(int nFeatures) {
        Random rand = new Random();
        int[] indices = new int[Math.min(maxFeatures, nFeatures)];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = rand.nextInt(nFeatures);
        }
        Log.d("Random Forest Classifier", "Generated random feature indices.");
        return indices;
    }

    /**
     * Finds unique values of a feature in the dataset.
     *
     * @param X            Training data.
     * @param featureIndex Index of the feature.
     * @return Array of unique values.
     */
    private double[] uniqueValues(double[][] X, int featureIndex) {
        Set<Double> values = new HashSet<>();
        for (double[] sample : X) {
            values.add(sample[featureIndex]);
        }
        double[] result = new double[values.size()];
        int i = 0;
        for (double value : values) {
            result[i++] = value;
        }
        return result;
    }

    /**
     * Calculates information gain for a split.
     *
     * @param y            Labels for the training data.
     * @param X            Training data.
     * @param featureIndex Index of the feature to split on.
     * @param threshold    Threshold value for the split.
     * @return Information gain.
     */
    private double informationGain(String[] y, double[][] X, int featureIndex, double threshold) {
        double parentEntropy = entropy(y);

        int[] leftIndices = split(X, featureIndex, threshold, true);
        int[] rightIndices = split(X, featureIndex, threshold, false);

        if (leftIndices.length == 0 || rightIndices.length == 0) {
            return 0;
        }

        double leftEntropy = entropy(subset(y, leftIndices));
        double rightEntropy = entropy(subset(y, rightIndices));

        double leftWeight = (double) leftIndices.length / y.length;
        double rightWeight = (double) rightIndices.length / y.length;

        double childEntropy = leftWeight * leftEntropy + rightWeight * rightEntropy;
        return parentEntropy - childEntropy;
    }

    /**
     * Calculates the entropy of a set of labels.
     *
     * @param y Labels.
     * @return Entropy.
     */
    private double entropy(String[] y) {
        Map<String, Integer> counts = new HashMap<>();
        for (String value : y) {
            counts.put(value, counts.getOrDefault(value, 0) + 1);
        }

        double entropy = 0;
        for (int count : counts.values()) {
            double p = (double) count / y.length;
            entropy -= p * Math.log(p) / Math.log(2);
        }

        return entropy;
    }

    /**
     * Splits the dataset based on a feature and a threshold.
     *
     * @param X            Training data.
     * @param featureIndex Index of the feature to split on.
     * @param threshold    Threshold value for the split.
     * @param left         If true, split left, otherwise split right.
     * @return Array of indices of the split data.
     */
    private int[] split(double[][] X, int featureIndex, double threshold, boolean left) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < X.length; i++) {
            if ((left && X[i][featureIndex] <= threshold) || (!left && X[i][featureIndex] > threshold)) {
                indices.add(i);
            }
        }
        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Creates a subset of the dataset based on the given indices.
     *
     * @param X       Training data.
     * @param indices Indices for the subset.
     * @return Subset of the training data.
     */
    private double[][] subset(double[][] X, int[] indices) {
        double[][] subset = new double[indices.length][X[0].length];
        for (int i = 0; i < indices.length; i++) {
            subset[i] = X[indices[i]];
        }
        return subset;
    }

    /**
     * Creates a subset of the labels based on the given indices.
     *
     * @param y       Labels.
     * @param indices Indices for the subset.
     * @return Subset of the labels.
     */
    private String[] subset(String[] y, int[] indices) {
        String[] subset = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            subset[i] = y[indices[i]];
        }
        return subset;
    }

    /**
     * Predicts the label for a given instance.
     *
     * @param x Instance to predict.
     * @return Predicted label.
     */
    @Override
    public String predict(double[] x) {
        Map<String, Integer> votes = new HashMap<>();
        for (Node tree : trees) {
            String prediction = predictTree(x, tree);
            votes.put(prediction, votes.getOrDefault(prediction, 0) + 1);
        }
        Log.d("Random Forest Classifier", "Prediction completed.");
        return Collections.max(votes.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * Predicts the label for a given instance using a single decision tree.
     *
     * @param x    Instance to predict.
     * @param tree Decision tree.
     * @return Predicted label.
     */
    private String predictTree(double[] x, Node tree) {
        if (tree.value != null) {
            return tree.value;
        }
        if (x[tree.featureIndex] <= tree.threshold) {
            return predictTree(x, tree.left);
        } else {
            return predictTree(x, tree.right);
        }
    }

    /**
     * Node class representing a single decision tree node.
     */
    private static class Node {
        Integer featureIndex;
        Double threshold;
        Node left;
        Node right;
        String value;

        /**
         * Constructs a Node.
         *
         * @param featureIndex Index of the feature to split on.
         * @param threshold    Threshold value for the split.
         * @param left         Left child node.
         * @param right        Right child node.
         * @param value        Label value if the node is a leaf.
         */
        Node(Integer featureIndex, Double threshold, Node left, Node right, String value) {
            this.featureIndex = featureIndex;
            this.threshold = threshold;
            this.left = left;
            this.right = right;
            this.value = value;
        }
    }
}
