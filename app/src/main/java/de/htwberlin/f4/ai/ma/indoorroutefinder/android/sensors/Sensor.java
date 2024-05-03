package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors;

/**
 * Sensor Interface
 * <p>
 * used by all sensors
 * <p>
 * Author: Benjamin Knner
 */

public interface Sensor {

    // some sample rate constants
    int SENSOR_RATE_UI = 50;
    int SENSOR_RATE_MEASUREMENT = 20;
    int SENSOR_RATE_FASTEST = 0;

    // start the sensor
    void start();

    // stop the sensor
    void stop();

    // get latest sensor values
    SensorData getValues();

    // check if the sensor is available
    boolean isSensorAvailable();

    // set a sensor listener
    void setListener(SensorListener listener);

    // get the sensor type
    SensorType getSensorType();

}
