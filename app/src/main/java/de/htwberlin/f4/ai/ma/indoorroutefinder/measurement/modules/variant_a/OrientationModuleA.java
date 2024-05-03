package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a;

import android.content.Context;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorDataModel;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorDataModelImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorFactoryImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.OrientationModule;

/**
 * OrientationModuleA Class which implements the OrientationModule interface
 * <p>
 * Calculate current heading / azimuth so the system knows the direction
 * of the user's movement
 * <p>
 * Sensor: CompassFusion
 * <p>
 * No lowpass filter used
 * <p>
 * Author: Benjamin Kneer
 */
public class OrientationModuleA implements OrientationModule {

    protected SensorDataModel dataModel;
    protected SensorFactory sensorFactory;
    protected Sensor compass;
    protected long lastStepTimestamp;
    protected Context context;

    public OrientationModuleA(Context context) {
        this.context = context;
        dataModel = new SensorDataModelImpl();
        sensorFactory = new SensorFactoryImpl(context);
        lastStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * get azimuth from compassfusion sensor
     *
     * @return azimuth, pitch, roll
     */
    @Override
    public float[] getOrientation() {
        float[] result = new float[3];
        float currentOrientation;
        long currentStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        // calculation
        // just picking the last value
        Map<SensorType, List<SensorData>> intervalData = dataModel.getData();
        List<SensorData> dataValues = intervalData.get(SensorType.COMPASS_FUSION);
        if (dataValues != null && !dataValues.isEmpty()) {
            currentOrientation = dataValues.get(dataValues.size() - 1).getValues()[0];
            lastStepTimestamp = currentStepTimestamp;

            result[0] = currentOrientation;
            result[1] = dataValues.get(dataValues.size() - 1).getValues()[1];
            result[2] = dataValues.get(dataValues.size() - 1).getValues()[2];
        }

        return result;
    }


    /**
     * start sensor and register listener
     */
    @Override
    public void start() {
        compass = sensorFactory.getSensor(SensorType.COMPASS_FUSION, Sensor.SENSOR_RATE_MEASUREMENT);
        compass.setListener(newValue -> dataModel.insertData(newValue));
        compass.start();
    }


    /**
     * stop sensor and unregister listener
     */
    @Override
    public void stop() {
        if (compass != null) {
            compass.stop();
        }
    }
}
