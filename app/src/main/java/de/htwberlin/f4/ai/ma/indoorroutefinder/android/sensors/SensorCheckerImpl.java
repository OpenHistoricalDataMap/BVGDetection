package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.IndoorMeasurementType;

/**
 * SensorCheckerImpl Class which implements the SensorChecker interface
 * <p>
 * Used to check if every required sensor is available on the device
 * <p>
 * Author: Benjamin Kneer
 */

public class SensorCheckerImpl implements SensorChecker {

    private final Context context;

    public SensorCheckerImpl(Context context) {
        this.context = context;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * check if every required sensor for the IndoorMeasurementType is available
     *
     * @param indoorMeasurementType measurementtype to use
     * @return true / false
     */
    @Override
    public boolean checkSensor(IndoorMeasurementType indoorMeasurementType) {
        boolean rotation;
        boolean barometer;
        boolean magnetometer;
        boolean accelerometer;
        boolean stepcounter;

        SensorFactory sensorFactory = new SensorFactoryImpl(context);

        Sensor rotationSensor = sensorFactory.getSensor(SensorType.COMPASS_FUSION, Sensor.SENSOR_RATE_UI);
        rotation = rotationSensor.isSensorAvailable();

        Sensor barometerSensor = sensorFactory.getSensor(SensorType.BAROMETER, Sensor.SENSOR_RATE_UI);
        barometer = barometerSensor.isSensorAvailable();

        Sensor magnetometerSensor = sensorFactory.getSensor(SensorType.MAGNETIC_FIELD, Sensor.SENSOR_RATE_UI);
        magnetometer = magnetometerSensor.isSensorAvailable();

        Sensor accelerometerSensor = sensorFactory.getSensor(SensorType.ACCELEROMETER_SIMPLE, Sensor.SENSOR_RATE_UI);
        accelerometer = accelerometerSensor.isSensorAvailable();

        Sensor stepcounterSensor = sensorFactory.getSensor(SensorType.STEP_DETECTOR, Sensor.SENSOR_RATE_UI);
        stepcounter = stepcounterSensor.isSensorAvailable();


        if (indoorMeasurementType == IndoorMeasurementType.VARIANT_A || indoorMeasurementType == IndoorMeasurementType.VARIANT_B) {
            return rotation && barometer && stepcounter;
        } else if (indoorMeasurementType == IndoorMeasurementType.VARIANT_C || indoorMeasurementType == IndoorMeasurementType.VARIANT_D) {
            return magnetometer && accelerometer && barometer && stepcounter;
        }

        return false;
    }
}
