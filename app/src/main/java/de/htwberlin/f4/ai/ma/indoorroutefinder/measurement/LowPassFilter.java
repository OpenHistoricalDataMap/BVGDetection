package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement;

/**
 * Lowpass filter (weighted smoothing) to smooth out noise from sensors.
 * <p>
 * Source: Professional Android Sensor Programming pp. 108
 * <p>
 * Author: Benjamin Kneer
 */
public class LowPassFilter {

    public static float filter(float lastVal, float currentVal, float weightingVal) {
        return lastVal * (1.0f - weightingVal) + currentVal * weightingVal;
    }
}
