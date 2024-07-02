package de.htwberlin.f4.ai.ma.indoorroutefinder.location.classifier;

/**
 * Interface for a generic classifier.
 */
public interface Classifier {
    /**
     * Predicts the label for a given test instance.
     *
     * @param testInstance the test instance as an array of integers
     * @return the predicted label as a string
     */
    String predict(double[] testInstance);
}
