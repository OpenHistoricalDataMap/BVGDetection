package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement;


import java.util.Map;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.CalibrationData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection.StepDirectionDetectListener;

/**
 * IndoorMeasurement Interface
 * <p>
 * Used to determine the position and handle all sensor stuff
 * <p>
 * Calculate position in cartesian coordinate system using dead reckoning
 * <p>
 * Author: Benjamin Kneer
 */
public interface IndoorMeasurement {

    // calibrate steplength (m), stepperiod (ms), airpressure and some other stuff
    void calibrate(CalibrationData calibrationData);

    // start PositionModule for position calculcation
    void start();

    // stop sensors and PositionModule
    void stop();

    // start 1..x specific sensors with the specified sample rate
    void startSensors(int sensorRate, SensorType... sensorType);

    // get the coordinates in WKT FORMAT
    String getCoordinates();

    // set the listener which receives updates from sensors
    void setSensorListener(SensorListener listener);

    // set the stepdirectiondetect listener
    void setStepDirectionListener(StepDirectionDetectListener listener);

    // get the last values of every registered sensor, so we can read them
    // at a specific time
    Map<SensorType, SensorData> getLastSensorValues();
}
