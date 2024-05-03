package de.htwberlin.f4.ai.ma.indoorroutefinder.android.record;

import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorDataModel;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorDataModelImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.IndoorMeasurement;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.IndoorMeasurementFactory;


/**
 * RecordControllerImpl Class which implements the RecordController Interface
 * <p>
 * used for recording sensor values
 * <p>
 * Author: Benjamin Kneer
 */

public class RecordControllerImpl implements RecordController {

    public static final String RECORD_CONTROLLER_IMPL = "RecordControllerImpl";
    private RecordView view;
    private IndoorMeasurement indoorMeasurement;
    private SensorDataModel sensorDataModel;
    private Handler timerHandler;
    private Runnable recordRunnable;
    private int savePeriod;


    public RecordControllerImpl() {
        savePeriod = 0;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * set the responsible RecordView
     *
     * @param view RecordView
     */
    @Override
    public void setView(RecordView view) {
        this.view = view;
    }


    /**
     * triggered by clicked on start button.
     * start sensors and register listeners
     */
    @Override
    public void onStartClicked(List<SensorType> sensors) {

        sensorDataModel = new SensorDataModelImpl();
        indoorMeasurement = IndoorMeasurementFactory.getIndoorMeasurement(view.getContext());
        // register sensor listener
        indoorMeasurement.setSensorListener(sensorData -> {
            SensorType sensorType = sensorData.getSensorType();

            switch (sensorType) {

                case ACCELEROMETER_SIMPLE:
                    view.updateAcceleration(sensorData.getValues());
                    break;
                case ACCELEROMETER_LINEAR:
                    view.updateAccelerationLinear(sensorData.getValues());
                    break;
                case GRAVITY:
                    view.updateGravity(sensorData.getValues());
                    break;
                case GYROSCOPE:
                    view.updateGyroscope(sensorData.getValues());
                    break;
                case GYROSCOPE_UNCALIBRATED:
                    view.updateGyroscopeUncalibrated(sensorData.getValues());
                    break;
                case MAGNETIC_FIELD:
                    view.updateMagneticField(sensorData.getValues());
                    break;
                case COMPASS_FUSION:
                    view.updateCompassFusion(sensorData.getValues()[0]);
                    break;
                case COMPASS_SIMPLE:
                    view.updateCompassSimple(sensorData.getValues()[0]);
                    break;
                case BAROMETER:
                    view.updatePressure(sensorData.getValues()[0]);
                    break;
                default:
                    break;
            }
        });

        SensorType[] sensorArray = new SensorType[sensors.size()];
        sensorArray = sensors.toArray(sensorArray);

        // start sensors
        indoorMeasurement.startSensors(Sensor.SENSOR_RATE_UI,
                sensorArray);

        // start the timer for recording data
        startTimer();
    }


    /**
     * triggered by clicking on stop button.
     * Stop all sensors and save data
     */
    @Override
    public void onStopClicked() {
        stopMeasurement();
        stopTimer();
        saveRecordData();
    }


    /**
     * triggered by activity onPause()
     */
    @Override
    public void onPause() {
        stopMeasurement();
        stopTimer();
    }


    /**
     * triggered by changing the save period
     *
     * @param value new saveperiod value
     */
    @Override
    public void onSavePeriodChanged(int value) {
        savePeriod = value;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Class Methods                                       *
     *                                                                                   *
     *************************************************************************************/


    /**
     * start the timer for saving sensor data
     */
    private void startTimer() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        // get lowpass filter value from settings
        float lowpassFilterValue = Float.parseFloat(sharedPreferences.getString("pref_lowpass_value", "0.1"));
        // create a new thread handler
        timerHandler = new Handler(Looper.getMainLooper());
        recordRunnable = new RecordRunnable(sensorDataModel, indoorMeasurement, timerHandler, savePeriod, lowpassFilterValue);
        // start thread with 100ms delay
        timerHandler.postDelayed(recordRunnable, 100);
    }


    /**
     * stop the timer for saving sensor data
     */
    private void stopTimer() {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(recordRunnable);
        }
    }


    /**
     * stop all sensors
     */
    private void stopMeasurement() {
        if (indoorMeasurement != null) {
            indoorMeasurement.stop();
        }
    }

    /**
     * Write Sensordata to file in csv format
     * <p>
     * sensortype;timestamp;value[0];value[1];value[2]
     */
    private void saveRecordData() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/IndoorPositioning/SensorData");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, timestamp.getTime() + ".txt");
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(file);
            Map<SensorType, List<SensorData>> data = sensorDataModel.getData();
            // loop through the sensortypes
            for (Map.Entry<SensorType, List<SensorData>> entry : data.entrySet()) {
                SensorType sensorType = entry.getKey();
                List<SensorData> sensorValues = entry.getValue();
                // loop through the sensordata list
                for (SensorData valueEntry : sensorValues) {
                    // build string to write
                    StringBuilder builder = new StringBuilder();
                    builder.append(sensorType).append(";").append(valueEntry.getTimestamp());

                    for (int i = 0; i < valueEntry.getValues().length; i++) {
                        builder.append(";").append(valueEntry.getValues()[i]);
                    }

                    builder.append(";");
                    // write string to file
                    outputStream.write(builder.toString().getBytes());
                    outputStream.write(System.lineSeparator().getBytes());
                }
            }
            outputStream.close();
        } catch (Exception e) {
            Log.d(RECORD_CONTROLLER_IMPL, e.toString());
        }
    }
}
