package de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator;

import android.content.Context;

/**
 * Created by Johann Winter
 * <p>
 * Factory for creating LocationCalculator objects
 */
public class LocationCalculatorFactory {

    public static LocationCalculator createInstance(Context context) {
        return new LocationCalculatorImpl(context);
    }
}
