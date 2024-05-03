package de.htwberlin.f4.ai.ma.indoorroutefinder.android.calibrate;

/**
 * CalibrateController Interface
 * <p>
 * Used for steplength calibration.
 * <p>
 * Author: Benjamin Kneer
 */

public interface CalibrateController {

    // set the correspondending view
    void setView(CalibrateView view);

    // triggered by clicking start button in the first step of calibration
    void onStartStepSetupClick();

    // triggered by clicking stop button in the first step of calibration
    void onStopStepSetupClick();

    // navigate to next step of calibration
    void onNextClicked(int currentStep);

    // navigate to previous step of calibration
    void onBackClicked(int currentStep);

    // triggered by clicking the + button in first step
    // used in case a step wasn't registered by sensor
    void onStepIncreaseClicked();

    // triggered by clicking the - button in the first step
    // used in case a step was registered by sensor which wasnt a real step
    void onStepDecreaseClicked();

    // triggered when user edits the travelled distance in step 2
    void onDistanceChange(float distance);

    // triggered by activity onPause()
    void onPause();
}
