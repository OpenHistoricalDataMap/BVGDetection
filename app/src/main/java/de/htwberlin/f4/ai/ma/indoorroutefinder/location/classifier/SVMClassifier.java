package de.htwberlin.f4.ai.ma.indoorroutefinder.location.classifier;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import umich.cse.yctung.androidlibsvm.LibSVM;

/**
 * SVMClassifier is a class for training and predicting using the Support Vector Machine (SVM) algorithm.
 * It uses the LibSVM library for Android.
 */
public class SVMClassifier implements Classifier {
    public static final String SVM_CLASSIFIER = "SVMClassifier";
    private final double[][] trainData;
    private final String[] trainLabels;
    private final double C;
    private final double gamma;
    private final KernelType kernel;
    private final LibSVM svm;
    private final Map<String, Integer> labelToClass;
    private final Map<Integer, String> classToLabel;
    private final Context context;
    private File modelFile;

    /**
     * Constructs an SVMClassifier with the given training data, labels, and SVM parameters.
     *
     * @param context     the Android context
     * @param trainData   the training data as a 2D array of integers
     * @param trainLabels the training labels as an array of strings
     * @param C           the regularization parameter
     * @param gamma       the kernel coefficient
     * @param kernel      the kernel type (LINEAR or RBF)
     */
    public SVMClassifier(Context context, double[][] trainData, String[] trainLabels, double C, double gamma, KernelType kernel) {
        this.trainData = trainData;
        this.trainLabels = trainLabels;
        this.C = C;
        this.gamma = gamma;
        this.kernel = kernel;
        this.context = context;

        this.svm = LibSVM.getInstance();
        this.labelToClass = new HashMap<>();
        this.classToLabel = new HashMap<>();

        prepareLabels();
        trainModel();
    }

    /**
     * Prepares the label mappings from strings to integers and vice versa.
     */
    private void prepareLabels() {
        int classIndex = 0;
        for (String label : trainLabels) {
            if (!labelToClass.containsKey(label)) {
                labelToClass.put(label, classIndex);
                classToLabel.put(classIndex, label);
                classIndex++;
            }
        }
        Log.d(SVM_CLASSIFIER, "Label to class mapping: " + labelToClass);
        Log.d(SVM_CLASSIFIER, "Class to label mapping: " + classToLabel);
    }

    /**
     * Trains the SVM model using the training data and labels.
     */
    private void trainModel() {
        Log.d(SVM_CLASSIFIER, "Training SVM model...");
        StringBuilder trainingDataBuilder = new StringBuilder();
        for (int i = 0; i < trainData.length; i++) {
            trainingDataBuilder.append(labelToClass.get(trainLabels[i]));
            for (int j = 0; j < trainData[i].length; j++) {
                trainingDataBuilder.append(" ").append(j + 1).append(":").append(trainData[i][j]);
            }
            trainingDataBuilder.append("\n");
        }

        String trainingDataString = trainingDataBuilder.toString();
        try {
            // Create a temporary file to store training data
            File trainFile = File.createTempFile("svm_train_", ".txt", context.getCacheDir());
            FileWriter writer = new FileWriter(trainFile);
            writer.write(trainingDataString);
            writer.close();
            Log.d(SVM_CLASSIFIER, "Training data written to temporary file: " + trainFile.getAbsolutePath());

            // Create a temporary file to store the model
            modelFile = File.createTempFile("svm_model_", ".txt", context.getCacheDir());
            String svmParams = getSVMParams();
            Log.d(SVM_CLASSIFIER, "SVM parameters: " + svmParams);

            // Train the SVM model using the training data
            svm.train(svmParams + " " + trainFile.getAbsolutePath() + " " + modelFile.getAbsolutePath());
            Log.d(SVM_CLASSIFIER, "Model trained and saved to: " + modelFile.getAbsolutePath());

            // Delete the temporary training data file
            trainFile.delete();
        } catch (IOException e) {
            Log.d(SVM_CLASSIFIER, "Error training SVM model: " + e.getMessage());
        }
    }

    /**
     * Constructs the SVM parameters string for the training process.
     * The Locale.US parameter is used to ensure that the decimal separator is a dot (.) instead of a comma (,).
     * This is important because LibSVM expects the parameters to be in the US format, regardless of the device's locale settings.
     *
     * @return the SVM parameters string
     */
    private String getSVMParams() {
        String kernelType;
        switch (kernel) {
            case LINEAR:
                kernelType = "0";
                break;
            case RBF:
                kernelType = "2";
                break;
            default:
                throw new IllegalArgumentException("Unsupported kernel type: " + kernel);
        }
        return String.format(Locale.US, "-s 0 -t %s -c %.6f -g %.6f", kernelType, C, gamma);
    }

    /**
     * Predicts the label for a given test instance.
     *
     * @param testInstance the test instance as an array of integers
     * @return the predicted label as a string
     */
    @Override
    public String predict(double[] testInstance) {
        Log.d(SVM_CLASSIFIER, "Predicting label for test instance: " + Arrays.toString(testInstance));
        StringBuilder testInstanceBuilder = new StringBuilder();
        testInstanceBuilder.append("0"); // dummy label for prediction
        for (int i = 0; i < testInstance.length; i++) {
            testInstanceBuilder.append(" ").append(i + 1).append(":").append(testInstance[i]);
        }
        String testInstanceString = testInstanceBuilder.toString();
        try {
            // Create a temporary file to store the test instance
            File testFile = File.createTempFile("svm_test_", ".txt", context.getCacheDir());
            FileWriter writer = new FileWriter(testFile);
            writer.write(testInstanceString);
            writer.close();
            Log.d(SVM_CLASSIFIER, "Test instance written to temporary file: " + testFile.getAbsolutePath());

            // Create a temporary file to store the prediction results
            File predictFile = File.createTempFile("svm_predict_", ".txt", context.getCacheDir());
            svm.predict(testFile.getAbsolutePath() + " " + modelFile.getAbsolutePath() + " " + predictFile.getAbsolutePath());

            // Read the prediction result from the temporary file
            BufferedReader reader = new BufferedReader(new FileReader(predictFile));
            String line;
            String predictedLabel = null;
            while ((line = reader.readLine()) != null) {
                predictedLabel = line;
            }
            reader.close();
            Log.d(SVM_CLASSIFIER, "Predicted label: " + predictedLabel);

            // Delete the temporary files
            testFile.delete();
            predictFile.delete();

            // Convert the predicted class index to the corresponding label
            double predictedClass = Double.parseDouble(predictedLabel.trim());
            return classToLabel.get((int) predictedClass);
        } catch (IOException e) {
            Log.d(SVM_CLASSIFIER, "Error predicting label: " + e.getMessage());
        }
        return null;
    }

    public enum KernelType {
        LINEAR,
        RBF
    }
}
