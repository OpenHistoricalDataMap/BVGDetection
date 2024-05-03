package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_b;

import android.content.Context;

import java.util.List;
import java.util.Map;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.LowPassFilter;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a.AltitudeModuleA;

/**
 * AltitudeModuleB Class which implements the AltitudeModule interface.
 * <p>
 * Calculate the relative height using the airpressure from barometer sensor
 * <p>
 * Lowpassfilter is used
 * <p>
 * Author: Benjamin Kneer
 */
public class AltitudeModuleB extends AltitudeModuleA {


    private final float lowpassFilterValue;


    public AltitudeModuleB(Context context, float airPressure, float lowpassFilterValue, float threshold) {
        super(context, airPressure, threshold);
        this.lowpassFilterValue = lowpassFilterValue;
    }

    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * start sensor, register listener and apply lowpass filter
     */
    @Override
    public void start() {
        airPressureSensor = sensorFactory.getSensor(SensorType.BAROMETER, Sensor.SENSOR_RATE_MEASUREMENT);
        airPressureSensor.setListener(newValue -> {
            Map<SensorType, List<SensorData>> sensorData = dataModel.getData();
            List<SensorData> oldValues = sensorData.get(SensorType.BAROMETER);
            if (oldValues != null) {
                float[] latestValue = oldValues.get(oldValues.size() - 1).getValues();
                float filteredValue = LowPassFilter.filter(latestValue[0], newValue.getValues()[0], lowpassFilterValue);
                newValue.setValues(new float[]{filteredValue});
            }

            dataModel.insertData(newValue);
        });
        airPressureSensor.start();
    }

}
