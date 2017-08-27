package de.htwberlin.f4.ai.ma.android.measure;

import java.util.List;

import de.htwberlin.f4.ai.ma.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.android.sensors.SensorDataModel;
import de.htwberlin.f4.ai.ma.android.sensors.SensorType;

/**
 * Created by benni on 30.07.2017.
 */

public class MeasureCalibration implements Runnable {

    private SensorDataModel sensorDataModel;
    private MeasureCalibrationListener listener;

    public MeasureCalibration(SensorDataModel sensorDataModel) {
        this.sensorDataModel = sensorDataModel;
    }

    public void setListener(MeasureCalibrationListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        // simply calculating the avg, should be improved
        float pressureAvg = 0.0f;

        // calibrate without lowpass filter

        // calc avg barometer
        List<SensorData> barometerData = sensorDataModel.getData().get(SensorType.BAROMETER);
        if (barometerData != null) {
            float pressureSum = 0.0f;

            // sum up all airpressure values
            for (SensorData data : barometerData) {
                pressureSum += data.getValues()[0];
            }
            // calculate avg
            pressureAvg = pressureSum / barometerData.size();
        }

        /*
        // calibrate with lowpass filter
        else if (indoorMeasurementType == IndoorMeasurementType.VARIANT_B || indoorMeasurementType == IndoorMeasurementType.VARIANT_D) {

            List<SensorData> barometerData = sensorDataModel.getData().get(SensorType.BAROMETER);
            if (barometerData != null) {

                // apply lowpass filter
                for (int i = 1; i < barometerData.size(); i++) {
                    float lastValue = barometerData.get(i-1).getValues()[0];
                    float newValue = barometerData.get(i).getValues()[0];
                    newValue = LowPassFilter.filter(lastValue, newValue, 0.1f);
                    barometerData.get(i).setValues(new float[]{newValue});
                }


                float pressureSum = 0.0f;

                // sum up all airpressure values
                for (SensorData data : barometerData) {
                    pressureSum += data.getValues()[0];
                }
                // calculate avg
                pressureAvg = pressureSum / barometerData.size();
            }
        }*/


        if (listener != null) {
            listener.onFinish(pressureAvg);
        }
    }

}