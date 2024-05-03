package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.Sensor;

/**
 * StepDirectionModule Interface
 * <p>
 * used to detect the last step direction
 * <p>
 * Author: Benjamin Kneer
 */
public interface StepDirectionModule {

    // return the last detected step direction
    StepDirection getLastStepDirection();

    // return the used sensor
    Sensor getSensor();
}
