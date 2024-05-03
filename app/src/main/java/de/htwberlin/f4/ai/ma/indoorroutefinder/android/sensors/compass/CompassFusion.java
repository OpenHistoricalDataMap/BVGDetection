package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.compass;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.view.Surface;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorListener;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;


/**
 * CompassFusion Class which implements the Sensor and SensorEventListener Interface
 * <p>
 * Used Android Sensor: Sensor.TYPE_ROTATION_VECTOR
 * <p>
 * Author: Benjamin Kneer
 */

public class CompassFusion implements SensorEventListener, de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor {

    private static final SensorType SENSORTYPE = SensorType.COMPASS_FUSION;

    private final SensorManager sensorManager;
    private final float[] rotationMatrix = new float[16];
    private final int sensorRate;
    private final Context context;
    private SensorListener listener;
    private Sensor rotationSensor;
    private float[] orientation = new float[3];
    private SensorData sensorData;

    public CompassFusion(Context context, int sensorRate) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorData = new SensorData();
        sensorData.setSensorType(SENSORTYPE);
        this.sensorRate = sensorRate;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Sensor Interface Methods                            *
     *                                                                                   *
     *************************************************************************************/


    /**
     * Get sensor from SensorManager and register Listener
     */
    @Override
    public void start() {
        rotationSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ROTATION_VECTOR);
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, sensorRate);
        }
    }


    /**
     * Unregister sensor listener
     */
    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, rotationSensor);
        }
    }


    /**
     * Get the latest sensor values
     *
     * @return latest sensor values
     */
    @Override
    public SensorData getValues() {
        return sensorData;
    }


    /**
     * Check if the sensor is available on the device
     *
     * @return available true / false
     */
    @Override
    public boolean isSensorAvailable() {
        return sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null;
    }


    /**
     * set the custom sensor listener
     *
     * @param listener sensorlistener
     */
    @Override
    public void setListener(SensorListener listener) {
        this.listener = listener;
    }


    /**
     * get the type of sensor
     *
     * @return sensortype
     */
    @Override
    public SensorType getSensorType() {
        return SENSORTYPE;
    }


    /************************************************************************************
     *                                                                                   *
     *                      SensorEventListener Interface Methods                        *
     *                                                                                   *
     *************************************************************************************/


    /**
     * Copy sensor values and create SensorData Object with sensortype, correct timestamp and
     * sensor values
     * <p>
     * The rotation vector represents the orientation of the device as a combination of an angle and
     * an axis, in which the device has rotated through an angle θ around an axis <x, y, z>
     * <p>
     * The result of rotation vector is used to calculate a rotation matrix for calculating
     * azimuth, pitch and and roll using the getOrientation() Method
     * <p>
     * values[0]: azimuth
     * values[1]: pitch
     * values[2]: roll
     *
     * @param sensorEvent sensor event
     * @noinspection ReassignedVariable
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == android.hardware.Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);

            float[] remapped = new float[16];

            // handle portrait and landscape mode
            // source: https://stackoverflow.com/questions/18782829/android-sensormanager-strange-how-to-remapcoordinatesystem

            int screenRotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
            int axisX = SensorManager.AXIS_X;
            int axisY = SensorManager.AXIS_Y;

            switch (screenRotation) {
                case Surface.ROTATION_0:
                    axisX = SensorManager.AXIS_X;
                    axisY = SensorManager.AXIS_Y;
                    break;
                case Surface.ROTATION_90:
                    axisX = SensorManager.AXIS_Y;
                    axisY = SensorManager.AXIS_MINUS_X;
                    break;
                case Surface.ROTATION_180:
                    axisX = SensorManager.AXIS_MINUS_X;
                    axisY = SensorManager.AXIS_MINUS_Y;
                    break;
                case Surface.ROTATION_270:
                    axisX = SensorManager.AXIS_MINUS_Y;
                    axisY = SensorManager.AXIS_X;
                    break;
                default:
                    break;

            }
            // remap coordinate system according to screen orientation
            SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remapped);

            // default we assume the phone is "laying" on its back in portrait mode, so the azimuth is calculcated
            // related to the default y axis of the phone (points out from the top edge)
            // for example: phone is laying on its back towards north -> shows 0°
            // spin phone while still laying on its back -> change in azimuth

            // original azimuth values are within [-180,180]
            orientation = SensorManager.getOrientation(remapped, orientation);

            float azimuth = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
            float pitch = (float) (Math.toDegrees(orientation[1]));
            float roll = (float) (Math.toDegrees(orientation[2]));

            // detect if the phone is "standing" (selfie camera on top or bottom edge) and screen is facing the user.
            // the normal camera on the backside of the phone points away from the user.
            // if yes, we have to remap the axis.
            // Now the azimuth is calculated relative to the camera(on the backside) / z axis of the phone.
            // we remap the z axis on the y axis and calculate the correct azimuth
            // IMPORTANT: SCREEN MUST POINT TOWARDS USER
            if (pitch <= -70) {
                SensorManager.remapCoordinateSystem(remapped, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapped);
                orientation = SensorManager.getOrientation(remapped, orientation);
                azimuth = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
            }

            float[] values = new float[]{azimuth, pitch, roll};

            long timeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            long calcTimestamp = (sensorEvent.timestamp / 1000000L) + timeOffset;

            sensorData = new SensorData();
            sensorData.setSensorType(SENSORTYPE);
            sensorData.setTimestamp(calcTimestamp);
            sensorData.setValues(values);

            if (listener != null) {
                listener.valueChanged(sensorData);
            }
        }
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {

    }
}
