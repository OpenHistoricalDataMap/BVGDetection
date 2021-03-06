package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_c;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a.AltitudeModuleA;

/**
 * AltitudeModuleC Class which implements the AltitudeModule interface.
 *
 * Calculate the relative height using the airpressure from barometer sensor
 *
 * No Lowpassfilter is used
 *
 * Author: Benjamin Kneer
 */

public class AltitudeModuleC extends AltitudeModuleA {

    public AltitudeModuleC(Context context, float airPressure, float threshold) {
        super(context, airPressure, threshold);
    }
}
