package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules;

/**
 * DistanceModule Interface
 * <p>
 * used to get the distance traveled
 * <p>
 * Author: Benjamin Kneer
 */
public interface DistanceModule {

    // get distance
    float getDistance(boolean stairs);

    // start module
    void start();

    // stop module
    void stop();
}
