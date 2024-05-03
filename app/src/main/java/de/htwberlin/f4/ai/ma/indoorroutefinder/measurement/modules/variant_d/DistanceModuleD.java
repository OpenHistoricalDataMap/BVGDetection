package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_d;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_c.DistanceModuleC;

/**
 * DistanceModuleD Class which implements the DistanceModule Interface
 * <p>
 * Simply calculate distance by using the previously calibrated
 * step length. Change distance if stair toggle is active
 * <p>
 * Author: Benjamin Kneer
 */
public class DistanceModuleD extends DistanceModuleC {

    public DistanceModuleD(Context context, float stepLength) {
        super(context, stepLength);
    }
}
