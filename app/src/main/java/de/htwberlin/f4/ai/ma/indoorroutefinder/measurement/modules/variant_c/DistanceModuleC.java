package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_c;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a.DistanceModuleA;

/**
 * DistanceModuleC Class which implements the DistanceModule Interface
 * <p>
 * Simply calculate distance by using the previously calibrated
 * step length. Change distance if stair toggle is active
 * <p>
 * Author: Benjamin Kneer
 */
public class DistanceModuleC extends DistanceModuleA {

    public DistanceModuleC(Context context, float stepLength) {
        super(context, stepLength);
    }
}
