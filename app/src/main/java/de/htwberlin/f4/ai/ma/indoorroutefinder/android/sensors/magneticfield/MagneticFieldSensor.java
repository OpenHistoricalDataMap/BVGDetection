package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.magneticfield;

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
 * MagneticFieldSensor Class which implements the Sensor and SensorEventListener Interface
 * <p>
 * Used Android Sensor: Sensor.TYPE_MAGNETIC_FIELD
 * <p>
 * Author: Benjamin Kneer
 */

public class MagneticFieldSensor implements SensorEventListener, de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor {

    private static final SensorType SENSORTYPE = SensorType.MAGNETIC_FIELD;
    private final SensorManager sensorManager;
    private final int sensorRate;
    private Sensor magneticFieldSensor;
    private SensorListener listener;
    private SensorData sensorData;


    public MagneticFieldSensor(Context context, int sensorRate) {
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
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticFieldSensor != null) {
            sensorManager.registerListener(this, magneticFieldSensor, sensorRate);
        }
    }


    /**
     * Unregister sensor listener
     */
    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, magneticFieldSensor);
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
        return sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null;
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
     * All values are in micro-Tesla (uT) and measure the ambient magnetic field in the X, Y and Z axis.
     *
     * @param sensorEvent sensor event
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
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
