package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.gravity;

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
 * GravitySensor Class which implements the Sensor and SensorEventListener Interface
 * <p>
 * Used Android Sensor: Sensor.TYPE_LINEAR_GRAVITY
 * <p>
 * Author: Benjamin Kneer
 */

public class GravitySensor implements SensorEventListener, de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor {

    private static final SensorType SENSORTYPE = SensorType.GRAVITY;

    private final SensorManager sensorManager;
    private final int sensorRate;
    private Sensor gravitySensor;
    private SensorListener listener;
    private SensorData sensorData;


    public GravitySensor(Context context, int sensorRate) {
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
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (gravitySensor != null) {
            sensorManager.registerListener(this, gravitySensor, sensorRate);
        }
    }


    /**
     * Unregister sensor listener
     */
    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, gravitySensor);
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
        return sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null;
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
     * values[0]: Force of gravity along the x axis.
     * values[1]: Force of gravity along the y axis.
     * values[2]: Force of gravity along the z axis.
     * <p>
     * Units are m/s^2
     *
     * @param sensorEvent sensor event
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float[] values = new float[sensorEvent.values.length];
            System.arraycopy(sensorEvent.values, 0, values, 0, sensorEvent.values.length);

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
