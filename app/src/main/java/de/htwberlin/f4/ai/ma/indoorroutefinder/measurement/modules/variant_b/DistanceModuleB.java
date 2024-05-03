package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_b;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a.DistanceModuleA;

/**
 * DistanceModuleB Class which implements the DistanceModule Interface
 * <p>
 * Simply calculate distance by using the previously calibrated
 * step length. Change distance if stair toggle is active
 * <p>
 * Author: Benjamin Kneer
 */
public class DistanceModuleB extends DistanceModuleA {

    public DistanceModuleB(Context context, float stepLength) {
        super(context, stepLength);
    }
}
