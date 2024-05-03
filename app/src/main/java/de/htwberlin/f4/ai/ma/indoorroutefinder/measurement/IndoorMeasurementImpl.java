package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.CalibrationData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorFactoryImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.PositionModule;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection.StepDirectionDetectListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection.StepDirectionModule;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection.StepDirectionModuleImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection.StepDirectionRunnable;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a.PositionModuleA;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_b.PositionModuleB;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_c.PositionModuleC;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_d.PositionModuleD;


/**
 * IndoorMeasurementImpl Class which implements the IndoorMeasurement Interface
 * <p>
 * Used to determine the position and handle all sensor stuff
 * <p>
 * Author: Benjamin Kneer
 */
public class IndoorMeasurementImpl implements IndoorMeasurement {

    // delay for the direction detection in ms
    private static final int DIRECTION_DETECT_DELAY = 50;
    private final SensorFactory sensorFactory;
    private final List<Sensor> sensorList;
    private final Context context;
    private SensorListener sensorListener;
    private CalibrationData calibrationData;
    private PositionModule positionModule;
    private StepDirectionDetectListener stepDirectionListener;
    // for direction detection
    private Handler timerHandler;
    private StepDirectionRunnable stepDirectionRunnable;


    public IndoorMeasurementImpl(Context context) {
        this.context = context;
        sensorFactory = new SensorFactoryImpl(context);
        sensorList = new ArrayList<>();
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * save the calibration data
     *
     * @param calibrationData calibration data
     */
    @Override
    public void calibrate(CalibrationData calibrationData) {
        this.calibrationData = calibrationData;
    }


    /**
     * start PositionModule for position calculcation and stepdirection detect
     */
    @Override
    public void start() {
        if (calibrationData.getUseStepDirection()) {
            timerHandler = new Handler(Looper.getMainLooper());
            StepDirectionModule directionDetect = new StepDirectionModuleImpl(context);
            stepDirectionRunnable = new StepDirectionRunnable(directionDetect);
            if (stepDirectionListener != null) {
                stepDirectionRunnable.setListener(stepDirectionListener);
            }
            // add the sensor from direction detect to our sensorlist, so we can stop it later
            sensorList.add(directionDetect.getSensor());
        }

        // create different position module, depending on chosen variant
        if (calibrationData.getIndoorMeasurementType() == IndoorMeasurementType.VARIANT_A) {
            positionModule = new PositionModuleA(context, calibrationData);
            positionModule.start();
        } else if (calibrationData.getIndoorMeasurementType() == IndoorMeasurementType.VARIANT_B) {
            positionModule = new PositionModuleB(context, calibrationData);
            positionModule.start();
        } else if (calibrationData.getIndoorMeasurementType() == IndoorMeasurementType.VARIANT_C) {
            positionModule = new PositionModuleC(context, calibrationData);
            positionModule.start();
        } else if (calibrationData.getIndoorMeasurementType() == IndoorMeasurementType.VARIANT_D) {
            positionModule = new PositionModuleD(context, calibrationData);
            positionModule.start();
        }
    }


    /**
     * stop sensors and PositionModule and stepdirection thread
     */
    @Override
    public void stop() {
        // stop all sensors controlled by this class
        for (Sensor sensor : sensorList) {
            sensor.stop();
        }
        if (positionModule != null) {
            positionModule.stop();
        }
        if (timerHandler != null) {
            timerHandler.removeCallbacks(stepDirectionRunnable);
        }

        sensorList.clear();
    }


    /**
     * start 1..x specific sensors with the specified sample rate
     *
     * @param sensorRate sample rate in ms
     * @param sensorType list of sensors tostart
     */
    @Override
    public void startSensors(int sensorRate, SensorType... sensorType) {
        // start sensors and register listener
        for (final SensorType type : sensorType) {
            Sensor sensor = sensorFactory.getSensor(type, sensorRate);
            sensor.setListener(newValue -> {
                if (sensorListener != null) {
                    sensorListener.valueChanged(newValue);
                }
            });
            sensor.start();
            sensorList.add(sensor);
        }
    }


    /**
     * get the coordinates in WKT FORMAT
     *
     * @return wkt coordinate string
     */
    @Override
    public String getCoordinates() {

        // we delay the analysis of stepdirection and do it in a thread, in case the step is detected before
        // the low / high peak of the movement happened
        if (timerHandler != null) {
            timerHandler.postDelayed(stepDirectionRunnable, DIRECTION_DETECT_DELAY);
        }

        float[] result = null;

        // get position from PositionModule
        if (positionModule != null) {
            result = positionModule.calculatePosition();
        }

        // transform into wkt string
        return WKT.coordToStr(result);
    }


    /**
     * set the listener which receives updates from sensors
     *
     * @param listener sensorlistener
     */
    @Override
    public void setSensorListener(SensorListener listener) {
        sensorListener = listener;
    }


    /**
     * set the stepdirectiondetect listener
     *
     * @param listener stepdirectiondetect listener
     */
    @Override
    public void setStepDirectionListener(StepDirectionDetectListener listener) {
        stepDirectionListener = listener;
    }


    /**
     * get the last values of every registered sensor, so we can read them
     * at a specific time.
     *
     * @return latest sensor values mapped by sensortype
     */
    @Override
    public Map<SensorType, SensorData> getLastSensorValues() {
        Map<SensorType, SensorData> sensorValues = new HashMap<>();

        for (Sensor sensor : sensorList) {
            sensorValues.put(sensor.getSensorType(), sensor.getValues());
        }

        return sensorValues;
    }
}
