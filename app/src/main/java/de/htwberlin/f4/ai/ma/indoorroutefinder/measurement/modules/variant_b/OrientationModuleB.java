package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_b;

import android.content.Context;

import java.util.List;
import java.util.Map;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.LowPassFilter;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a.OrientationModuleA;

/**
 * OrientationModuleB Class which implements the OrientationModule interface
 * <p>
 * Calculate current heading / azimuth so the system knows the direction
 * of the user's movement
 * <p>
 * Sensor: CompassFusion
 * <p>
 * lowpass filter used
 * <p>
 * Author: Benjamin Kneer
 */
public class OrientationModuleB extends OrientationModuleA {

    float lowpassFilterValue;

    public OrientationModuleB(Context context, float lowpassFilterValue) {
        super(context);
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
        compass = sensorFactory.getSensor(SensorType.COMPASS_FUSION, Sensor.SENSOR_RATE_MEASUREMENT);
        compass.setListener(newValue -> {
            Map<SensorType, List<SensorData>> sensorData = dataModel.getData();
            List<SensorData> oldValues = sensorData.get(SensorType.COMPASS_FUSION);
            if (oldValues != null) {
                float[] latestValue = oldValues.get(oldValues.size() - 1).getValues();
                float filteredAzimuth = LowPassFilter.filter(latestValue[0], newValue.getValues()[0], lowpassFilterValue);
                float filteredPitch = LowPassFilter.filter(latestValue[1], newValue.getValues()[1], lowpassFilterValue);
                float filteredRoll = LowPassFilter.filter(latestValue[2], newValue.getValues()[2], lowpassFilterValue);
                newValue.setValues(new float[]{filteredAzimuth, filteredPitch, filteredRoll});
            }
            dataModel.insertData(newValue);
        });
        compass.start();
    }


}
