package de.htwberlin.f4.ai.ma.indoorroutefinder.android.record;

import android.content.Context;

/**
 * RecordView Interface
 * <p>
 * Used for displaying sensor values
 * <p>
 * Author: Benjamin Kneer
 */

public interface RecordView {

    // update the acceleration sensor view
    void updateAcceleration(float[] values);

    // update the acceleration_linear sensor view
    void updateAccelerationLinear(float[] values);

    // update the gravity sensor view
    void updateGravity(float[] values);

    // update the gyroscope sensor view
    void updateGyroscope(float[] values);

    // update the gyroscope_uncalibrated view
    void updateGyroscopeUncalibrated(float[] values);

    // update the magneticfield sensor view
    void updateMagneticField(float[] values);

    // update the compassfusion view
    void updateCompassFusion(float value);

    // update the compass simple view
    void updateCompassSimple(float value);

    // update the barometer view
    void updatePressure(float value);

    // get the view's context
    Context getContext();
}
