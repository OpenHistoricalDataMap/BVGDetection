package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection;

/**
 * StepDirectionDetectListener Interface
 * <p>
 * simple listener for stepdirectiondetect
 * <p>
 * Author: Benjamin Kneer
 */
public interface StepDirectionDetectListener {

    // inform listener about last stepdirection
    void onDirectionDetect(StepDirection stepDirection);
}
