package de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure;

/**
 * MeasureCalibrationListener Interface
 * <p>
 * Used to inform about calculated average airpressure
 * <p>
 * Author: Benjamin Kneer
 */

public interface MeasureCalibrationListener {

    // calculation finished
    void onFinish(float airPressure);
}
