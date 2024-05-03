package de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorDataModel;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;

/**
 * MeasureCalibration Class which implements Runnable Interface
 * <p>
 * measure airpressure and calculates the average
 * <p>
 * Author: Benjamin Kneer
 */

public class MeasureCalibration implements Runnable {

    private final SensorDataModel sensorDataModel;
    private MeasureCalibrationListener listener;

    public MeasureCalibration(SensorDataModel sensorDataModel) {
        this.sensorDataModel = sensorDataModel;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    @Override
    public void run() {
        // simply calculating the avg
        float pressureAvg = 0.0f;

        // calc average airpressure
        // get all barometer sensor data from model
        List<SensorData> barometerData = sensorDataModel.getData().get(SensorType.BAROMETER);
        if (barometerData != null) {
            float pressureSum = 0.0f;

            // sum up all airpressure values
            for (SensorData data : barometerData) {
                pressureSum += data.getValues()[0];
            }
            // calculate average
            pressureAvg = pressureSum / barometerData.size();
        }

        // inform listener about calculated airpressure average
        if (listener != null) {
            listener.onFinish(pressureAvg);
        }
    }


    /************************************************************************************
     *                                                                                   *
     *                               Class Methods                                       *
     *                                                                                   *
     *************************************************************************************/


    public void setListener(MeasureCalibrationListener listener) {
        this.listener = listener;
    }
}
