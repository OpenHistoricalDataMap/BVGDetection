package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.temperature;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;

/**
 * Thermometer Class which implements the Sensor and SensorEventListener Interface
 * <p>
 * Used Android Sensor: Sensor.TYPE_AMBIENT_TEMPERATURE
 * <p>
 * Note: There is no temerature sensor in nexus 5x
 * <p>
 * Author: Benjamin Kneer
 */

public class Thermometer implements SensorEventListener, de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor {

    private static final SensorType SENSORTYPE = SensorType.THERMOMETER;
    private final SensorManager sensorManager;
    private final int sensorRate;
    private Sensor temperatureSensor;
    private SensorListener listener;
    private float value;
    private SensorData sensorData;


    public Thermometer(Context context, int sensorRate) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorData = new SensorData();
        sensorData.setSensorType(SENSORTYPE);
        this.sensorRate = sensorRate;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Sensor Interface Methods                            *
     *                                                                                   *
     *************************************************************************************/


    /**
     * Get sensor from SensorManager and register Listener
     */
    @Override
    public void start() {
        value = 0.0f;
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (temperatureSensor != null) {
            sensorManager.registerListener(this, temperatureSensor, sensorRate);
        }
    }


    /**
     * Unregister sensor listener
     */
    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, temperatureSensor);
        }
    }


    /**
     * Get the latest sensor values
     *
     * @return latest sensor values
     */
    @Override
    public SensorData getValues() {
        return sensorData;
    }


    /**
     * Check if the sensor is available on the device
     *
     * @return available true / false
     */
    @Override
    public boolean isSensorAvailable() {
        return sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null;
    }


    /**
     * set the custom sensor listener
     *
     * @param listener sensorlistener
     */
    @Override
    public void setListener(SensorListener listener) {
        this.listener = listener;
    }


    /**
     * get the type of sensor
     *
     * @return sensortype
     */
    @Override
    public SensorType getSensorType() {
        return SENSORTYPE;
    }


    /************************************************************************************
     *                                                                                   *
     *                      SensorEventListener Interface Methods                        *
     *                                                                                   *
     *************************************************************************************/


    /**
     * Copy sensor values and create SensorData Object with sensortype, correct timestamp and
     * sensor values
     * <p>
     * values[0]: Ambient air temperature in °C
     *
     * @param sensorEvent sensor event
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            value = sensorEvent.values[0];

            float[] values = new float[]{value};

            long timeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            long calcTimestamp = (sensorEvent.timestamp / 1000000L) + timeOffset;

            sensorData = new SensorData();
            sensorData.setSensorType(SENSORTYPE);
            sensorData.setTimestamp(calcTimestamp);
            sensorData.setValues(values);

            if (listener != null) {
                listener.valueChanged(sensorData);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
