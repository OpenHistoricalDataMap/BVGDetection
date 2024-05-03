package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.variant_a;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.DistanceModule;

/**
 * DistanceModuleA Class which implements the DistanceModule Interface
 * <p>
 * Simply calculate distance by using the previously calibrated
 * step length. Change distance if stair toggle is active
 * <p>
 * Author: Benjamin Kneer
 */
public class DistanceModuleA implements DistanceModule {

    private static final float STAIRHIGH = 0.17f;
    protected float stepLength;
    protected Context context;

    public DistanceModuleA(Context context, float stepLength) {
        this.context = context;
        this.stepLength = stepLength;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * get distance depending on stairs / no stairs
     *
     * @param stairs true / false
     * @return distance
     */
    @Override
    public float getDistance(boolean stairs) {
        float distance = stepLength;

        if (stairs) {
            distance = distance - (2 * STAIRHIGH);
        }

        return distance;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
