package de.htwberlin.f4.ai.ma.android.sensors.stepcounter;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import java.sql.Timestamp;

import de.htwberlin.f4.ai.ma.android.calibrate.CalibratePersistance;
import de.htwberlin.f4.ai.ma.android.calibrate.CalibratePersistanceImpl;
import de.htwberlin.f4.ai.ma.android.measure.CalibrationData;
import de.htwberlin.f4.ai.ma.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.android.sensors.SensorListener;
import de.htwberlin.f4.ai.ma.android.sensors.Sensor;
import de.htwberlin.f4.ai.ma.android.sensors.SensorType;

/**
 * Created by benni on 23.07.2017.
 */

public class StepCounter implements Sensor, SensorEventListener{

    private static final SensorType SENSORTYPE = SensorType.STEPCOUNTER;

    private SensorManager sensorManager;
    private SensorListener listener;
    private android.hardware.Sensor stepCounterSensor;

    private Integer stepCount;
    private SensorData sensorData;

    private boolean firstRun;
    private long lastStepTimestamp;
    private int sensorRate;
    private int stepPeriod;
    private static final int STEPPERIODTOLERANCE = 100;

    private Context context;


    public StepCounter(Context context, int sensorRate) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorData = new SensorData();
        sensorData.setSensorType(SENSORTYPE);
        firstRun = true;
        lastStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        this.sensorRate = sensorRate;
        this.context = context;
    }

    private int loadStepPeriod() {
        CalibratePersistance calibratePersistance = new CalibratePersistanceImpl(context);
        CalibrationData calibrationData = calibratePersistance.load();
        if (calibrationData != null) {
            stepPeriod = calibrationData.getStepPeriod();
        } else {
            stepPeriod = 500;
        }

        return stepPeriod;
    }

    @Override
    public void start() {
        stepCount = 0;
        stepCounterSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR);
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, sensorRate);
            loadStepPeriod();
        }
    }

    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, stepCounterSensor);
        }
    }

    @Override
    public SensorData getValues() {
        return sensorData;
    }


    @Override
    public boolean isSensorAvailable() {
        if (sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR) == null) {
            return false;
        }

        return true;
    }

    @Override
    public void setListener(SensorListener listener) {
        this.listener = listener;
    }

    @Override
    public SensorType getSensorType() {
        return SENSORTYPE;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long currentStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();

        if (firstRun) {
            firstRun = false;
            return;
        }

        // if a step was fail detected
        if (currentStepTimestamp - lastStepTimestamp < (stepPeriod - STEPPERIODTOLERANCE)) {
            Log.d("tmp", "failstep");
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