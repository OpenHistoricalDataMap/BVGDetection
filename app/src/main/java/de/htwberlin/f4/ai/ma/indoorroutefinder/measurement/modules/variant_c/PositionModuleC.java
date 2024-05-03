package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_c;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.CalibrationData;
import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a.PositionModuleA;

/**
 * PositionModuleC Class which implements the PositionModule Interface
 * <p>
 * Used for IndoorMeasurementType.VARIANT_C
 * <p>
 * Orientation: CompassSimple (Accelerometer + Magnetic field sensor)
 * Altitude: Barometer
 * Distance: Steplength
 * <p>
 * No Lowpass filter used
 * <p>
 * Author: Benjamin Kneer
 */
public class PositionModuleC extends PositionModuleA {

    public PositionModuleC(Context context, CalibrationData calibrationData) {
        super(context, calibrationData);

        altitudeModule = new AltitudeModuleC(context, calibrationData.getAirPressure(), calibrationData.getBarometerThreshold());
        distanceModule = new DistanceModuleC(context, calibrationData.getStepLength());
        orientationModule = new OrientationModuleC(context);
    }
}
