package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules;

/**
 * AltitudeModule Interface
 * <p>
 * used to get the relative altitude / altitude change
 * <p>
 * Author: Benjamin Kneer
 */
public interface AltitudeModule {

    // get the relative altitude
    float getAltitude();

    // start module
    void start();

    // stop module
    void stop();
}
