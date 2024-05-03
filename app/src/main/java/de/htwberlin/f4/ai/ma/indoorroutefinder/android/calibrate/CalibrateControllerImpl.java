package de.htwberlin.f4.ai.ma.indoorroutefinder.android.calibrate;


import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.CalibrationData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.IndoorMeasurement;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.IndoorMeasurementFactory;

/**
 * CalibrationControllerImpl Class which implements the CalibrationController Interface.
 * <p>
 * Used for steplength calibration
 * <p>
 * Author: Benjamin Kneer
 */

public class CalibrateControllerImpl implements CalibrateController {

    private CalibrateView view;
    private IndoorMeasurement indoorMeasurement;

    // current step count
    private int stepCount;
    // for calculating the average step period
    private List<Long> stepTimes;
    // calculated step length
    private float stepLength;
    // calculated step period
    private int stepPeriod;


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * triggered by clicking start button in the first step of calibration.
     * Create the IndoorMeasureImpl and register listener for handling the
     * steps registered by step_detector sensor
     */
    @Override
    public void onStartStepSetupClick() {
        // initialize fields
        stepCount = 0;
        stepTimes = new ArrayList<>();
        // create IndoorMeasureMentImpl and register listener
        indoorMeasurement = IndoorMeasurementFactory.getIndoorMeasurement(view.getContext());
        indoorMeasurement.setSensorListener(sensorData -> {
            if (sensorData.getSensorType() == SensorType.STEP_DETECTOR) {
                // get step count from sensor
                stepCount++;
                // handle first step bug
                if (stepCount == 1) {
                    // get step count from sensor
                    stepCount++;
                    // update view with new stepcount
                    view.updateStepCount(stepCount);
                    // save timestamp of step for later step period calculation
                    stepTimes.add(sensorData.getTimestamp() - 700);
                }
                // update view with new stepcount
                view.updateStepCount(stepCount);
                // save timestamp of step for later step period calculation
                stepTimes.add(sensorData.getTimestamp());
            }
        });
        // start step_detector sensor
        indoorMeasurement.startSensors(Sensor.SENSOR_RATE_FASTEST, SensorType.STEP_DETECTOR);
        view.updateStepCount(stepCount);
    }


    /**
     * triggered by clicking stop button in the first step of calibration.
     * Stop the IndoorMeasureMent component so all sensors will be stopped
     */
    @Override
    public void onStopStepSetupClick() {
        if (indoorMeasurement != null) {
            indoorMeasurement.stop();
        }
    }


    /**
     * navigate to next step of calibration.
     * Load correct layout depending on calibration step number
     *
     * @param currentStep number of the current calibration step
     */
    @Override
    public void onNextClicked(int currentStep) {
        // stop all sensors
        if (indoorMeasurement != null) {
            indoorMeasurement.stop();
        }
        // load second step
        if (currentStep == 1) {
            view.loadCalibrateStep(2);
            // calculate the average step period
            stepPeriod = calculateAverageStepperiod(stepTimes);
            // update view
            view.updateAverageStepperiod(stepPeriod);
        }
        // load third step
        else if (currentStep == 2) {
            view.loadCalibrateStep(3);

            if (indoorMeasurement != null) {
                // register listener for compass sensor
                indoorMeasurement.setSensorListener(sensorData -> {
                    if (sensorData.getSensorType() == SensorType.COMPASS_FUSION) {
                        view.updateAzimuth((int) sensorData.getValues()[0]);
                    }

                });
            }
            // start compass sensor
            indoorMeasurement.startSensors(Sensor.SENSOR_RATE_UI, SensorType.COMPASS_FUSION);
        }
        // calibration is done
        else if (currentStep == 3) {
            if (indoorMeasurement != null) {
                indoorMeasurement.stop();
            }
            // save the calibration data
            saveSettings();
        }
    }


    /**
     * navigate to previous step of calibration.
     * Load correct layout depending on calibration step number
     *
     * @param currentStep number of calibration step
     */
    @Override
    public void onBackClicked(int currentStep) {
        // stop all sensors
        if (indoorMeasurement != null) {
            indoorMeasurement.stop();
        }
        // load first step
        if (currentStep == 2) {
            view.loadCalibrateStep(1);
        }
        // load second step
        else if (currentStep == 3) {
            view.loadCalibrateStep(2);
            view.updateAverageStepperiod(stepPeriod);
        }
    }


    /**
     * triggered by clicking the + button in first step.
     * used in case a step wasn't registered by sensor
     */
    @Override
    public void onStepIncreaseClicked() {
        stepCount++;
        view.updateStepCount(stepCount);
    }


    /**
     * triggered by clicking the - button in the first step.
     * used in case a step was registered by sensor which wasnt a real step.
     */
    @Override
    public void onStepDecreaseClicked() {
        stepCount--;
        view.updateStepCount(stepCount);
    }


    /**
     * triggered when user edits the travelled distance in step 2.
     * Calculate steplength
     *
     * @param distance distance in meters
     */
    @Override
    public void onDistanceChange(float distance) {
        if (stepCount != 0 && distance != 0) {
            stepLength = distance / stepCount;
            view.updateAverageStepdistance(stepLength);
        }
    }


    /**
     * triggered by activity onPause().
     * stop all sensors
     */
    @Override
    public void onPause() {
        if (indoorMeasurement != null) {
            indoorMeasurement.stop();
        }
    }


    /**
     * set the correspondending view
     *
     * @param view CalibrationView
     */
    @Override
    public void setView(CalibrateView view) {
        this.view = view;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Class Methods                                       *
     *                                                                                   *
     *************************************************************************************/


    /**
     * Calculate the average step period by using the saved step timestamps.
     * Used to minimize fail detection of step sensor
     *
     * @param timestamps list of step timestamp
     * @return step period
     */
    private int calculateAverageStepperiod(List<Long> timestamps) {
        List<Long> stepDurations = new ArrayList<>();
        int result = 0;

        // calculate the durations between each step
        for (int i = 0; i < timestamps.size() - 1; i++) {
            Long start = timestamps.get(i);
            Long end = timestamps.get(i + 1);
            Long difference = end - start;
            stepDurations.add(difference);
        }

        // sum up all durations
        Long durationSum = 0L;
        for (Long duration : stepDurations) {
            durationSum += duration;
        }
        // calculate avg
        if (!stepDurations.isEmpty()) {
            result = (int) (durationSum / (stepDurations.size()));
        }

        return result;
    }


    /**
     * save the calibration settings (steplength & stepperiod)
     */
    private void saveSettings() {
        CalibratePersistance calibratePersistance = new CalibratePersistanceImpl(view.getContext());
        CalibrationData calibrationData = new CalibrationData(stepLength, stepPeriod);
        calibratePersistance.save(calibrationData);
    }

}
