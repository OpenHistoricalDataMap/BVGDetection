package de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors;

/**
 * SensorListener Interface
 * <p>
 * Used to inform listener about new sensor values
 * <p>
 * Author: Benjamin Kneer
 */

public interface SensorListener {

    void valueChanged(SensorData newValue);
}
