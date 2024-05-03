package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors;

/**
 * SensorData Class
 * <p>
 * small model class to store sensor values, sensortype and corrected unix timestamp
 * <p>
 * Author: Benjamin Kneer
 */

public class SensorData {

    private long timestamp;
    private float[] values;
    private SensorType sensorType;

    public SensorData() {
        // initialize with 0 so we dont have to deal with null pointers in case
        // we want to retrieve values but sensor hasn't delivered any yet
        values = new float[]{0.0f, 0.0f, 0.0f};
    }

    // little helper constructor for testing data
    public SensorData(SensorType sensorType, long timestamp, float x, float y, float z) {
        this.sensorType = sensorType;
        values = new float[3];
        this.timestamp = timestamp;
        values[0] = x;
        values[1] = y;
        values[2] = z;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Get & Set                                           *
     *                                                                                   *
     *************************************************************************************/


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float[] getValues() {
        return values;
    }

    public void setValues(float[] values) {
        this.values = values;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }
}
