package de.htwberlin.f4.ai.ma.indoorroutefinder.android.calibrate;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.CalibrationData;

/**
 * CalibratePersistance Interface
 * <p>
 * Used for saving / loading the calibration
 * <p>
 * Author: Benjamin Kneer
 */

public interface CalibratePersistance {

    // load the calibration
    CalibrationData load();

    // save the calibration
    void save(CalibrationData calibrationData);
}
