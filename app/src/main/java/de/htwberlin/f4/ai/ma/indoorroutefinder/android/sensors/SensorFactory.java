package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors;

/**
 * SensorFactory interface to create the required sensors
 * <p>
 * Author: Benjamin Kneer
 */

public interface SensorFactory {

    // get the corret sensor
    Sensor getSensor(SensorType sensorType, int sensorRate);
}
