package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.accelerometer.AccelerometerLinear;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.accelerometer.AccelerometerSimple;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.barometer.Barometer;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.compass.CompassFusion;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.compass.CompassSimple;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.gravity.GravitySensor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.gyroscope.Gyroscope;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.gyroscope.GyroscopeUncalibrated;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.magneticfield.MagneticFieldSensor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.stepcounter.StepDetector;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.temperature.Thermometer;

/**
 * SensorFactoryImpl class which implements the SensorFactory Interface
 * <p>
 * Used to create the correct sensors
 * <p>
 * Author: Benjamin Kneer
 */

public class SensorFactoryImpl implements SensorFactory {

    private final Context context;
    
    public SensorFactoryImpl(Context context) {
        this.context = context;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    @Override
    public Sensor getSensor(SensorType sensorType, int sensorRate) {

        switch (sensorType) {
            case ACCELEROMETER_LINEAR:
                return new AccelerometerLinear(context, sensorRate);
            case ACCELEROMETER_SIMPLE:
                return new AccelerometerSimple(context, sensorRate);
            case BAROMETER:
                return new Barometer(context, sensorRate);
            case COMPASS_FUSION:
                return new CompassFusion(context, sensorRate);
            case COMPASS_SIMPLE:
                return new CompassSimple(context, sensorRate);
            case GRAVITY:
                return new GravitySensor(context, sensorRate);
            case GYROSCOPE:
                return new Gyroscope(context, sensorRate);
            case GYROSCOPE_UNCALIBRATED:
                return new GyroscopeUncalibrated(context, sensorRate);
            case MAGNETIC_FIELD:
                return new MagneticFieldSensor(context, sensorRate);
            case STEP_DETECTOR:
                return new StepDetector(context, sensorRate);
            case THERMOMETER:
                return new Thermometer(context, sensorRate);
            default:
                return null;
        }
    }
}
