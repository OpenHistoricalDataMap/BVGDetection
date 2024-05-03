package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection;

import android.app.Activity;
import android.content.Context;
import android.view.Surface;

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

/**
 * StepDirectionModuleImpl Class which implements the StepDirectionModule Interface
 * <p>
 * used to detect the last step direction by analyzing the accelerometer_linear data
 * <p>
 * Author: Benjamin Kneer
 */
public class StepDirectionModuleImpl implements StepDirectionModule {

    private static final float THRESHOLD_POSITIVE = 2.75f;
    private static final float THRESHOLD_NEGATIVE = -2.75f;
    private final SensorFactory sensorFactory;
    private final SensorDataModel dataModel;
    private final Context context;
    private long lastStepTimestamp;
    private Sensor sensor;


    public StepDirectionModuleImpl(Context context) {
        lastStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        sensorFactory = new SensorFactoryImpl(context);
        dataModel = new SensorDataModelImpl();
        this.context = context;
        initSensor();
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * return the last detected step direction
     *
     * @return stepdirection
     */
    @Override
    public StepDirection getLastStepDirection() {
        long currentStepTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        StepDirection direction = null;

        // get all data from laststep to now
        Map<SensorType, List<SensorData>> intervalMap = dataModel.getDataInInterval(lastStepTimestamp, currentStepTimestamp);
        // get all SensorType.ACCELEROMETER_LINEAR data
        List<SensorData> intervalValues = intervalMap.get(SensorType.ACCELEROMETER_LINEAR);
        // if we have data, check for direction
        if (intervalValues != null) {
            // find last peak on x axis
            SensorData[] peaksX = findPeaks(intervalValues, 0);
            SensorData highPeakX = peaksX[0];
            SensorData lowPeakX = peaksX[1];
            float peakDiffX = highPeakX.getValues()[0] + Math.abs(lowPeakX.getValues()[0]);
            // find last peak on y axis
            SensorData[] peaksY = findPeaks(intervalValues, 1);
            SensorData highPeakY = peaksY[0];
            SensorData lowPeakY = peaksY[1];
            float peakDiffY = highPeakY.getValues()[1] + Math.abs(lowPeakY.getValues()[1]);
            int screenRotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();

            // handle screen orientation
            if (screenRotation == Surface.ROTATION_0) {
                // check which axis movement happened last

                // movement along y axis happend last -> forward / backward
                if (highPeakX.getTimestamp() < highPeakY.getTimestamp()) {
                    // make sure the highest peak is really on y axis
                    if (peakDiffY > peakDiffX) {
                        // if we have a highpeak, followed by a lowpeak -> forward
                        if (highPeakY.getTimestamp() < lowPeakY.getTimestamp()) {
                            direction = StepDirection.FORWARD;
                        }
                        // if we have a lowpeak, followed by a highpeak -> backward
                        else if (lowPeakY.getTimestamp() < highPeakY.getTimestamp()) {
                            direction = StepDirection.BACKWARD;
                        }
                    }
                    // the last movement was along y axis but with a lower peak than x,
                    // so we assume its because of noise or user movement and ignore the peak
                    else if (peakDiffY < peakDiffX) {
                        // if we have a highpeak, followed by a lowpeak -> right
                        if (highPeakX.getTimestamp() < lowPeakX.getTimestamp()) {
                            direction = StepDirection.RIGHT;
                        }
                        // if we have a lowpeak, followed by a highpeak -> left
                        else if (lowPeakX.getTimestamp() < highPeakX.getTimestamp()) {
                            direction = StepDirection.LEFT;
                        }
                    }
                }

                // movement along x axis happened last -> left / right
                else if (highPeakX.getTimestamp() > highPeakY.getTimestamp()) {
                    // make sure highest peak is really on x axis
                    if (peakDiffX > peakDiffY) {
                        // if we have a highpeak, followed by a lowpeak -> right
                        if (highPeakX.getTimestamp() < lowPeakX.getTimestamp()) {
                            direction = StepDirection.RIGHT;
                        }
                        // if we have a lowpeak, followed by a highpeak -> left
                        else if (lowPeakX.getTimestamp() < highPeakX.getTimestamp()) {
                            direction = StepDirection.LEFT;
                        }
                    }

                    // the last movement was along x axis but with a lower peak than y,
                    // so we assume its because of noise or user movement and ignore the peak
                    else if (peakDiffX < peakDiffY) {
                        // if we have a highpeak, followed by a lowpeak -> forward
                        if (highPeakY.getTimestamp() < lowPeakY.getTimestamp()) {
                            direction = StepDirection.FORWARD;
                        }
                        // if we have a lowpeak, followed by a highpeak -> backward
                        else if (lowPeakY.getTimestamp() < highPeakY.getTimestamp()) {
                            direction = StepDirection.BACKWARD;
                        }
                    }
                }
            } else if (screenRotation == Surface.ROTATION_90) {

                // check which axis movement happened last

                // movement along x axis happend last -> forward / backward
                if (highPeakY.getTimestamp() < highPeakX.getTimestamp()) {
                    // make sure the highest peak is really on x axis
                    if (peakDiffX > peakDiffY) {
                        // if we have a highpeak, followed by a lowpeak -> forward
                        if (highPeakX.getTimestamp() < lowPeakX.getTimestamp()) {
                            direction = StepDirection.FORWARD;
                        }
                        // if we have a lowpeak, followed by a highpeak -> backward
                        else if (lowPeakX.getTimestamp() < highPeakX.getTimestamp()) {
                            direction = StepDirection.BACKWARD;
                        }
                    }
                    // the last movement was along x axis but with a lower peak than y,
                    // so we assume its because of noise or user movement and ignore the peak
                    else if (peakDiffX < peakDiffY) {
                        // if we have a highpeak, followed by a lowpeak -> left
                        if (highPeakY.getTimestamp() < lowPeakY.getTimestamp()) {
                            direction = StepDirection.LEFT;
                        }
                        // if we have a lowpeak, followed by a highpeak -> right
                        else if (lowPeakY.getTimestamp() < highPeakY.getTimestamp()) {
                            direction = StepDirection.RIGHT;
                        }
                    }
                }

                // movement along y axis happened last -> left / right
                else if (highPeakY.getTimestamp() > highPeakX.getTimestamp()) {
                    // make sure highest peak is really on y axis
                    if (peakDiffY > peakDiffX) {
                        // if we have a highpeak, followed by a lowpeak -> left
                        if (highPeakY.getTimestamp() < lowPeakY.getTimestamp()) {
                            direction = StepDirection.LEFT;
                        }
                        // if we have a lowpeak, followed by a highpeak -> right
                        else if (lowPeakY.getTimestamp() < highPeakY.getTimestamp()) {
                            direction = StepDirection.RIGHT;
                        }
                    }

                    // the last movement was along y axis but with a lower peak than x,
                    // so we assume its because of noise or user movement and ignore the peak
                    else if (peakDiffY < peakDiffX) {
                        // if we have a highpeak, followed by a lowpeak -> forward
                        if (highPeakX.getTimestamp() < lowPeakX.getTimestamp()) {
                            direction = StepDirection.FORWARD;
                        }
                        // if we have a lowpeak, followed by a highpeak -> backward
                        else if (lowPeakX.getTimestamp() < highPeakX.getTimestamp()) {
                            direction = StepDirection.BACKWARD;
                        }
                    }
                }
            } else if (screenRotation == Surface.ROTATION_180) {
                // check which axis movement happened last

                // movement along y axis happend last -> forward / backward
                if (highPeakX.getTimestamp() < highPeakY.getTimestamp()) {
                    // make sure the highest peak is really on y axis
                    if (peakDiffY > peakDiffX) {
                        // if we have a lowpeak, followed by a highpeak -> forward
                        if (highPeakY.getTimestamp() > lowPeakY.getTimestamp()) {
                            direction = StepDirection.FORWARD;
                        }
                        // if we have a highpeak, followed by a lowpeak -> backward
                        else if (lowPeakY.getTimestamp() > highPeakY.getTimestamp()) {
                            direction = StepDirection.BACKWARD;
                        }
                    }
                    // the last movement was along y axis but with a lower peak than x,
                    // so we assume its because of noise or user movement and ignore the peak
                    else if (peakDiffY < peakDiffX) {
                        // if we have a lowpeak, followed by a highpeak -> right
                        if (highPeakX.getTimestamp() > lowPeakX.getTimestamp()) {
                            direction = StepDirection.RIGHT;
                        }
                        // if we have a highpeak, followed by a lowpeak -> left
                        else if (lowPeakX.getTimestamp() > highPeakX.getTimestamp()) {
                            direction = StepDirection.LEFT;
                        }
                    }
                }

                // movement along x axis happened last -> left / right
                else if (highPeakX.getTimestamp() > highPeakY.getTimestamp()) {
                    // make sure highest peak is really on x axis
                    if (peakDiffX > peakDiffY) {
                        // if we have a lowpeak, followed by a highpeak -> right
                        if (highPeakX.getTimestamp() > lowPeakX.getTimestamp()) {
                            direction = StepDirection.RIGHT;
                        }
                        // if we have a highpeak, followed by a lowpeak -> left
                        else if (lowPeakX.getTimestamp() > highPeakX.getTimestamp()) {
                            direction = StepDirection.LEFT;
                        }
                    }

                    // the last movement was along x axis but with a lower peak than y,
                    // so we assume its because of noise or user movement and ignore the peak
                    else if (peakDiffX < peakDiffY) {
                        // if we have a lowpeak, followed by a highpeak -> forward
                        if (highPeakY.getTimestamp() > lowPeakY.getTimestamp()) {
                            direction = StepDirection.FORWARD;
                        }
                        // if we have a highpeak, followed by a lowpeak -> backward
                        else if (lowPeakY.getTimestamp() > highPeakY.getTimestamp()) {
                            direction = StepDirection.BACKWARD;
                        }
                    }
                }
            } else if (screenRotation == Surface.ROTATION_270) {
                // check which axis movement happened last

                // movement along x axis happend last -> forward / backward
                if (highPeakY.getTimestamp() < highPeakX.getTimestamp()) {
                    // make sure the highest peak is really on x axis
                    if (peakDiffX > peakDiffY) {
                        // if we have a lowpeak, followed by a highpeak -> forward
                        if (highPeakX.getTimestamp() > lowPeakX.getTimestamp()) {
                            direction = StepDirection.FORWARD;
                        }
                        // if we have a highpeak, followed by a lowpeak -> backward
                        else if (lowPeakX.getTimestamp() > highPeakX.getTimestamp()) {
                            direction = StepDirection.BACKWARD;
                        }
                    }
                    // the last movement was along x axis but with a lower peak than y,
                    // so we assume its because of noise or user movement and ignore the peak
                    else if (peakDiffX < peakDiffY) {
                        // if we have a lowpeak, followed by a highpeak -> left
                        if (highPeakY.getTimestamp() > lowPeakY.getTimestamp()) {
                            direction = StepDirection.LEFT;
                        }
                        // if we have a highpeak, followed by a lowpeak -> right
                        else if (lowPeakY.getTimestamp() > highPeakY.getTimestamp()) {
                            direction = StepDirection.RIGHT;
                        }
                    }
                }

                // movement along y axis happened last -> left / right
                else if (highPeakY.getTimestamp() > highPeakX.getTimestamp()) {
                    // make sure highest peak is really on y axis
                    if (peakDiffY > peakDiffX) {
                        // if we have a lowpeak, followed by a highpeak -> left
                        if (highPeakY.getTimestamp() > lowPeakY.getTimestamp()) {
                            direction = StepDirection.LEFT;
                        }
                        // if we have a highpeak, followed by a lowpeak -> right
                        else if (lowPeakY.getTimestamp() > highPeakY.getTimestamp()) {
                            direction = StepDirection.RIGHT;
                        }
                    }

                    // the last movement was along y axis but with a lower peak than x,
                    // so we assume its because of noise or user movement and ignore the peak
                    else if (peakDiffY < peakDiffX) {
                        // if we have a lowpeak, followed by a highpeak -> forward
                        if (highPeakX.getTimestamp() > lowPeakX.getTimestamp()) {
                            direction = StepDirection.FORWARD;
                        }
                        // if we have a highpeak, followed by a lowpeak -> backward
                        else if (lowPeakX.getTimestamp() > highPeakX.getTimestamp()) {
                            direction = StepDirection.BACKWARD;
                        }
                    }
                }
            }
        }

        lastStepTimestamp = currentStepTimestamp;

        return direction;
    }


    /**
     * return the used sensor
     *
     * @return sensor
     */
    @Override
    public Sensor getSensor() {
        return sensor;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Class Methods                                       *
     *                                                                                   *
     *************************************************************************************/


    /**
     * start sensor and register listener.
     * could be improved using lowpass filter
     */
    private void initSensor() {
        // saving data from accelerator_linear sensor, so we can check for step direction
        sensor = sensorFactory.getSensor(SensorType.ACCELEROMETER_LINEAR, Sensor.SENSOR_RATE_FASTEST);
        sensor.setListener(dataModel::insertData);

        sensor.start();
    }


    /**
     * find low and highpeaks for the specified axis
     *
     * @param dataList list holding all sensor data
     * @param axis     axis to analyze
     * @return result[0] = highpeak
     * result[1] = lowpeak
     */
    private SensorData[] findPeaks(List<SensorData> dataList, int axis) {
        SensorData highPeak = new SensorData();
        SensorData lowPeak = new SensorData();
        SensorData[] result = new SensorData[2];
        boolean foundLowPeak = false;
        boolean foundHighPeak = false;

        // start at the end from data
        for (int i = dataList.size() - 1; i > 0; i--) {
            SensorData data = dataList.get(i);

            // check for high peak
            // value has to be greater than the threshold and old highpeak
            if (data.getValues()[axis] >= THRESHOLD_POSITIVE && data.getValues()[axis] > highPeak.getValues()[axis]) {
                highPeak = data;
                // check if the previous data is smaller, so we can be sure it's a highpeak
                if (dataList.get(i - 1).getValues()[axis] < highPeak.getValues()[axis]) {
                    foundHighPeak = true;
                }
            }
            // check for low peak
            else if (data.getValues()[axis] <= THRESHOLD_NEGATIVE && data.getValues()[axis] < lowPeak.getValues()[axis]) {
                lowPeak = data;
                // check if the previous data is bigger, so we can be sure it's a lowpeak
                if (dataList.get(i - 1).getValues()[axis] > lowPeak.getValues()[axis]) {
                    foundLowPeak = true;
                }
            }

            if (foundLowPeak && foundHighPeak) {
                break;
            }
        }
        result[0] = highPeak;
        result[1] = lowPeak;

        return result;
    }
}
