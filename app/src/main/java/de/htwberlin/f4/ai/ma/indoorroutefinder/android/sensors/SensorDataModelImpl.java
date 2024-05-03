package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SensorDataModelImpl Class which implements the SensorDataModel Interface
 * <p>
 * Model Class for storing all sensor data in a map (by sensortype)
 * <p>
 * Author: Benjamin Kneer
 */

public class SensorDataModelImpl implements SensorDataModel {

    private final Map<SensorType, List<SensorData>> data;

    public SensorDataModelImpl() {
        data = new HashMap<>();
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * get sensors data mapped by sensortype
     *
     * @return Map containing all sensor data
     */
    @Override
    public Map<SensorType, List<SensorData>> getData() {
        return data;
    }


    /**
     * since we calculate the position every step, we need to calculate the orientation etc
     * by using values in a specific time interval. Filter all values with the timestamp >=
     * start and timestamp <= end
     *
     * @param start start timestamp
     * @param end   end timestamp
     * @return filtered map
     */
    @Override
    public Map<SensorType, List<SensorData>> getDataInInterval(long start, long end) {
        Map<SensorType, List<SensorData>> result = new HashMap<>();

        // loop through all sensor datas
        for (Map.Entry<SensorType, List<SensorData>> entry : data.entrySet()) {
            SensorType sensorType = entry.getKey();
            List<SensorData> sensorDataFiltered = new ArrayList<>();
            List<SensorData> sensorDataUnfiltered = entry.getValue();
            // check if sensordata is in interval
            for (SensorData data : sensorDataUnfiltered) {
                if ((data.getTimestamp() >= start) && (data.getTimestamp() <= end)) {
                    // add sensordata to filtered list
                    sensorDataFiltered.add(data);
                }
            }
            result.put(sensorType, sensorDataFiltered);
        }

        return result;
    }


    /**
     * insert sensor data into model
     *
     * @param sensorData data to insert
     */
    @Override
    public void insertData(SensorData sensorData) {
        boolean keyExists = false;

        // check if the key already exists
        // if yes, we just have to get the list by key
        // and add the value to the list
        for (Map.Entry<SensorType, List<SensorData>> entry : data.entrySet()) {
            SensorType sensorType = entry.getKey();

            if (sensorType == sensorData.getSensorType()) {
                keyExists = true;
                List<SensorData> valueList = entry.getValue();
                valueList.add(sensorData);
            }
        }
        // if the key doesn't exist, we have to create
        // a new list for the sensor values
        if (!keyExists) {
            List<SensorData> valueList = new ArrayList<>();
            valueList.add(sensorData);
            data.put(sensorData.getSensorType(), valueList);
        }
    }


}
