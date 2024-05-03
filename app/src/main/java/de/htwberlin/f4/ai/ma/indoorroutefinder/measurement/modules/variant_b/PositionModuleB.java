package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_b;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.CalibrationData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a.PositionModuleA;

/**
 * PositionModuleB Class which implements the PositionModule Interface
 * <p>
 * Used for IndoorMeasurementType.VARIANT_B
 * <p>
 * Orientation: CompassFusion (Rotation Vector)
 * Altitude: Barometer
 * Distance: Steplength
 * <p>
 * Lowpass filter used (weighted smoothing)
 * <p>
 * Author: Benjamin Kneer
 */
public class PositionModuleB extends PositionModuleA {

    public PositionModuleB(Context context, CalibrationData calibrationData) {
        super(context, calibrationData);

        altitudeModule = new AltitudeModuleB(context, calibrationData.getAirPressure(), calibrationData.getLowpassFilterValue(), calibrationData.getBarometerThreshold());
        distanceModule = new DistanceModuleB(context, calibrationData.getStepLength());
        orientationModule = new OrientationModuleB(context, calibrationData.getLowpassFilterValue());
    }

}
