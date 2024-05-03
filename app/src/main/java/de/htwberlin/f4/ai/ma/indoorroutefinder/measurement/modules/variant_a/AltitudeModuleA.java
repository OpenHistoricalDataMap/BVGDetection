package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a;

import android.content.Context;
import android.hardware.SensorManager;

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
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.AltitudeModule;

/**
 * AltitudeModuleA Class which implements the AltitudeModule interface.
 * <p>
 * Calculate the relative height using the airpressure from barometer sensor
 * <p>
 * No lowpass filter used
 * <p>
 * Author: Benjamin Kneer
 */
public class AltitudeModuleA implements AltitudeModule {


    protected SensorDataModel dataModel;
    protected SensorFactory sensorFactory;
    protected float airPressure;
    protected Sensor airPressureSensor;
    protected long lastStepTimestamp;
    protected float lastAltitude;
    protected Context context;
    protected float threshold;

    public AltitudeModuleA(Context context, float airPressure, float threshold) {
        this.context = context;
        dataModel = new SensorDataModelImpl();
        sensorFactory = new SensorFactoryImpl(context);
        this.airPressure = airPressure;
        lastAltitude = calcAltitude(airPressure);
        lastStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        this.threshold = threshold;
    }


    /**
     * calculate altitude using the airpressure
     *
     * @param pressure airpressure
     * @return altitude in meters
     */
    private float calcAltitude(float pressure) {
        // SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * calculate the altitude change
     *
     * @return altitude difference in meters
     */
    @Override
    public float getAltitude() {
        float currentAirPressure;
        float currentAltitude;
        float altitudeDiff = 0.0f;
        long currentStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        // calculation
        // just picking the last value
        Map<SensorType, List<SensorData>> intervalData = dataModel.getData();
        List<SensorData> dataValues = intervalData.get(SensorType.BAROMETER);
        if (dataValues != null && !dataValues.isEmpty()) {
            currentAirPressure = dataValues.get(dataValues.size() - 1).getValues()[0];
            currentAltitude = calcAltitude(currentAirPressure);
            altitudeDiff = currentAltitude - lastAltitude;
            // set new values
            lastStepTimestamp = currentStepTimestamp;
            // check for threshold
            if (Math.abs(altitudeDiff) >= threshold) {
                lastAltitude = currentAltitude;
            } else {
                altitudeDiff = 0;
            }
        }

        return altitudeDiff;
    }

    /**
     * start sensor and register listener
     */
    @Override
    public void start() {
        airPressureSensor = sensorFactory.getSensor(SensorType.BAROMETER, Sensor.SENSOR_RATE_MEASUREMENT);
        airPressureSensor.setListener(newValue -> dataModel.insertData(newValue));
        airPressureSensor.start();

    }

    /**
     * stop sensor and unregister listener
     */
    @Override
    public void stop() {
        if (airPressureSensor != null) {
            airPressureSensor.stop();
        }
    }
}
