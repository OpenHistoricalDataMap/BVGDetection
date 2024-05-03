package de.htwberlin.f4.ai.ma.indoorroutefinder.android.calibrate;

import android.content.Context;
import android.content.SharedPreferences;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.CalibrationData;

/**
 * CalibratePersistanceImpl Class
 * <p>
 * Save / load calibration from SharedPreferences
 * <p>
 * Author: Benjamin Kneer
 */

public class CalibratePersistanceImpl implements CalibratePersistance {

    private final Context context;

    public CalibratePersistanceImpl(Context context) {
        this.context = context;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * load the calibration from sharedpreferences
     *
     * @return CalibrationData
     */
    @Override
    public CalibrationData load() {
        CalibrationData calibrationData = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.coordinates_shared_preferences), Context.MODE_PRIVATE);

        // check if key for step length and step period exists
        if (sharedPreferences.contains(context.getString(R.string.coordinates_shared_preferences_steplength)) &&
                sharedPreferences.contains(context.getString(R.string.coordinates_shared_preferences_stepperiod))) {
            // load the calibration
            calibrationData = new CalibrationData();
            calibrationData.setStepLength(sharedPreferences.getFloat(context.getString(R.string.coordinates_shared_preferences_steplength), 0.0f));
            calibrationData.setStepPeriod(sharedPreferences.getInt(context.getString(R.string.coordinates_shared_preferences_stepperiod), 0));
        }

        return calibrationData;
    }


    /**
     * Save calibrationData into sharedpreferences
     *
     * @param calibrationData calibrationData to save
     */
    @Override
    public void save(CalibrationData calibrationData) {
        // just saving steplength and stepperiod
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.coordinates_shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(context.getString(R.string.coordinates_shared_preferences_steplength), calibrationData.getStepLength());
        editor.putInt(context.getString(R.string.coordinates_shared_preferences_stepperiod), calibrationData.getStepPeriod());

        editor.commit();
    }

}
