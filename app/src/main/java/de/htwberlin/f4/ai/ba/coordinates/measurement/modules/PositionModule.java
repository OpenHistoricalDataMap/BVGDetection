package de.htwberlin.f4.ai.ba.coordinates.measurement.modules;

/**
 * Created by benni on 03.08.2017.
 */

public interface PositionModule {

    float[] calculatePosition();
    void start();
    void stop();
}
