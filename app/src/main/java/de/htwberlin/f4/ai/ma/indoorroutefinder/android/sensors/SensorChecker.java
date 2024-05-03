package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.IndoorMeasurementType;

/**
 * SensorChecker Interface
 * <p>
 * Used to check if every required sensor is available on the device
 * <p>
 * Author: Benjamin Kneer
 */

public interface SensorChecker {

    // check if every required sensor for the IndoorMeasurementType is available
    boolean checkSensor(IndoorMeasurementType indoorMeasurementType);
}
