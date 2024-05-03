package de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.IndoorMeasurementType;

/**
 * CalibrationData Class
 * <p>
 * stores everything we need for the IndoorMeasurement component
 * <p>
 * -> steplength, stepperiod, airpressure, start coordinates,
 * measurementType, lowpassfilter value, stepdirectionmodule usage,
 * stairs handling, barometer threshold
 * <p>
 * Author: Benjamin Kneer
 */

public class CalibrationData {

    private float stepLength;
    private int stepPeriod;
    private float airPressure;

    // non calibration related stuff

    // start coordinates
    private float[] coordinates;
    // loaded from settings
    private IndoorMeasurementType indoorMeasurementType;
    // loaded from settings
    private float lowpassFilterValue;
    // loaded from settings
    private boolean useStepDirection;
    private boolean stairs;
    // loaded from settings
    private float barometerThreshold;


    /************************************************************************************
     *                                                                                   *
     *                               Constructors                                        *
     *                                                                                   *
     *************************************************************************************/


    public CalibrationData() {
        stepLength = 0.0f;
        stepPeriod = 0;
        airPressure = 0.0f;
        coordinates = new float[3];
    }


    public CalibrationData(float stepLength, int stepPeriod) {
        this.stepLength = stepLength;
        this.stepPeriod = stepPeriod;
        airPressure = 0.0f;
        coordinates = new float[3];
    }


    public CalibrationData(float stepLength, int stepPeriod, float[] coordinates) {
        this.stepLength = stepLength;
        this.stepPeriod = stepPeriod;
        airPressure = 0.0f;
        this.coordinates = coordinates;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Getter & Setter                                     *
     *                                                                                   *
     *************************************************************************************/

    public boolean isStairs() {
        return stairs;
    }

    public void setStairs(boolean stairs) {
        this.stairs = stairs;
    }

    public float getBarometerThreshold() {
        return barometerThreshold;
    }

    public void setBarometerThreshold(float barometerThreshold) {
        this.barometerThreshold = barometerThreshold;
    }

    public boolean getUseStepDirection() {
        return useStepDirection;
    }

    public void setUseStepDirection(boolean useStepDirection) {
        this.useStepDirection = useStepDirection;
    }

    public float getStepLength() {
        return stepLength;
    }

    public void setStepLength(float stepLength) {
        this.stepLength = stepLength;
    }

    public int getStepPeriod() {
        return stepPeriod;
    }

    public void setStepPeriod(int stepPeriod) {
        this.stepPeriod = stepPeriod;
    }

    public float getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(float airPressure) {
        this.airPressure = airPressure;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(float[] coordinates) {
        this.coordinates = coordinates;
    }

    public IndoorMeasurementType getIndoorMeasurementType() {
        return indoorMeasurementType;
    }

    public void setIndoorMeasurementType(IndoorMeasurementType indoorMeasurementType) {
        this.indoorMeasurementType = indoorMeasurementType;
    }

    public float getLowpassFilterValue() {
        return lowpassFilterValue;
    }

    public void setLowpassFilterValue(float lowpassFilterValue) {
        this.lowpassFilterValue = lowpassFilterValue;
    }
}
