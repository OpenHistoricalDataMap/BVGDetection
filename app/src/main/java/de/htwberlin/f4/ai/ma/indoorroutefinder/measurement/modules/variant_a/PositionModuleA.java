package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.CalibrationData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.AltitudeModule;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.DistanceModule;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.OrientationModule;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.PositionModule;

/**
 * PositionModuleA Class which implements the PositionModule Interface
 * <p>
 * Used for IndoorMeasurementType.VARIANT_A
 * <p>
 * Orientation: CompassFusion (Rotation Vector)
 * Altitude: Barometer
 * Distance: Steplength
 * <p>
 * No Lowpass filter
 * <p>
 * Author: Benjamin Kneer
 */
public class PositionModuleA implements PositionModule {

    protected AltitudeModule altitudeModule;
    protected DistanceModule distanceModule;
    protected OrientationModule orientationModule;
    protected Context context;
    protected CalibrationData calibrationData;

    // coordinates[0] = x = east / west
    // coordinates[1] = y = north / south
    // coordinates[2] = z = movement upward / downward
    private final float[] coordinates;


    public PositionModuleA(Context context, CalibrationData calibrationData) {
        altitudeModule = new AltitudeModuleA(context, calibrationData.getAirPressure(), calibrationData.getBarometerThreshold());
        distanceModule = new DistanceModuleA(context, calibrationData.getStepLength());
        orientationModule = new OrientationModuleA(context);
        coordinates = calibrationData.getCoordinates();
        this.calibrationData = calibrationData;
        this.context = context;
    }


    /************************************************************************************
     *                                                                                   *
     *                              Interface Methods                                    *
     *                                                                                   *
     *************************************************************************************/


    /**
     * calculate new position based on previous coordinates
     *
     * @return new calculated cartesian coordinates
     */
    @Override
    public float[] calculatePosition() {
        // calculate new position with these 3 values
        float deltaZ = altitudeModule.getAltitude();
        float distance = distanceModule.getDistance(calibrationData.isStairs());
        float[] orientation = orientationModule.getOrientation();

        // prevent NaN bug in case deltaZ is bigger than distance, because of threshold
        if (deltaZ > distance) {
            deltaZ = distance;
        }

        // beta = azimuth
        double beta = orientation[0];
        double sinbeta = Math.sin(Math.toRadians(beta));
        double cosbeta = Math.cos(Math.toRadians(beta));

        // calculate inclination angle (alpha)
        double alpha = Math.asin(deltaZ / distance);
        double cosalpha = Math.cos(alpha);

        // calculate delta x and delta y
        float deltaX = (float) (distance * cosalpha * sinbeta);
        float deltaY = (float) (distance * cosalpha * cosbeta);

        coordinates[0] += deltaX;
        coordinates[1] += deltaY;
        coordinates[2] += deltaZ;

        return coordinates;
    }


    /**
     * start all modules
     */
    @Override
    public void start() {
        altitudeModule.start();
        distanceModule.start();
        orientationModule.start();
    }


    /**
     * stop all modules
     */
    @Override
    public void stop() {
        altitudeModule.stop();
        distanceModule.stop();
        orientationModule.stop();
    }
}
