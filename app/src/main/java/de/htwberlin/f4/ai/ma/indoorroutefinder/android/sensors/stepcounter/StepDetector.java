package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.stepcounter;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

import java.sql.Timestamp;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.calibrate.CalibratePersistance;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.calibrate.CalibratePersistanceImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.calibrate.CalibrateViewImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.CalibrationData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;

/**
 * StepDetector Class which implements the Sensor and SensorEventListener Interface
 * <p>
 * Used Android Sensor: Sensor.TYPE_STEP_DETECTOR
 * <p>
 * Author: Benjamin Kneer
 */

public class StepDetector implements Sensor, SensorEventListener {

    private static final SensorType SENSORTYPE = SensorType.STEP_DETECTOR;
    private final SensorManager sensorManager;
    private final int sensorRate;
    private final Context context;
    private SensorListener listener;
    private android.hardware.Sensor stepCounterSensor;
    private Integer stepCount;
    private SensorData sensorData;
    private boolean firstRun;
    private long lastStepTimestamp;
    private int stepPeriod;


    public StepDetector(Context context, int sensorRate) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorData = new SensorData();
        sensorData.setSensorType(SENSORTYPE);
        firstRun = true;
        lastStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        this.sensorRate = sensorRate;
        this.context = context;
    }

    /**
     * load stepperiod from settings
     *
     * @return period
     */
    private int loadStepPeriod() {
        CalibratePersistance calibratePersistance = new CalibratePersistanceImpl(context);
        CalibrationData calibrationData = calibratePersistance.load();
        int period;

        // make sure to NOT load period from persistance in calibration activity.
        // thats required in case user misscalibrated and got a period of some seconds...
        // in this case the faildetecture mechanism would prevent a new calibration
        if (calibrationData != null && !(context instanceof CalibrateViewImpl)) {
            period = calibrationData.getStepPeriod();
        } else {
            period = 750;
        }

        return period;
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
        stepCount = 0;
        stepCounterSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR);
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, sensorRate);
            stepPeriod = loadStepPeriod();
        }
    }


    /**
     * Unregister sensor listener
     */
    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, stepCounterSensor);
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
        return sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR) != null;
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
     * reports step count
     *
     * @param sensorEvent sensor event
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long currentStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();

        // ignore first event of this sensor, because it's fired instantly when sensor is started
        if (firstRun) {
            firstRun = false;
            return;
        }

        // try to reduce fail detects
        if (currentStepTimestamp - lastStepTimestamp < (stepPeriod - (stepPeriod / 2))) {
            return;
        }

        if (sensorEvent.sensor.getType() == android.hardware.Sensor.TYPE_STEP_DETECTOR) {
            stepCount++;
            float[] values = new float[]{stepCount};

            long timeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            long calcTimestamp = (sensorEvent.timestamp / 1000000L) + timeOffset;

            sensorData = new SensorData();
            sensorData.setSensorType(SENSORTYPE);
            sensorData.setTimestamp(calcTimestamp);
            sensorData.setValues(values);

            if (listener != null) {
                listener.valueChanged(sensorData);
            }

            lastStepTimestamp = currentStepTimestamp;
        }
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {

    }
}
