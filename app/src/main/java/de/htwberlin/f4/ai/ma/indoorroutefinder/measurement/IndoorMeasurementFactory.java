package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement;

import android.content.Context;

/**
 * IndoorMeasurementFactory Class
 * <p>
 * Create IndoorMeasurement Instance
 * <p>
 * Author: Benjamin Kneer
 */
public class IndoorMeasurementFactory {

    public static IndoorMeasurement getIndoorMeasurement(Context context) {
        return new IndoorMeasurementImpl(context);
    }
}
