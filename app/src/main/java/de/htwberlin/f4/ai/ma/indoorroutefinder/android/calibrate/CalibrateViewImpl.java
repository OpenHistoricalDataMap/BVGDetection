package de.htwberlin.f4.ai.ma.indoorroutefinder.android.calibrate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;

/**
 * CalibrateViewImpl Class which implements the CalibrateView interface
 * <p>
 * Used for Steplength and Stepperiod calibration
 * <p>
 * Author: Benjamin Kneer
 */

public class CalibrateViewImpl extends BaseActivity implements CalibrateView {

    private final CalibrateController controller;
    private TextView stepCountView;
    private TextView stepDistanceView;
    private TextView stepPeriodView;
    private TextView azimuthView;
    private ImageView compassView;
    private Button btnStepIncStepOne;
    private Button btnStepDecStepOne;
    private Button btnNextStepOne;
    private Button btnStartStepOne;
    private Button btnStopStepOne;
    private Button btnNextStepTwo;


    public CalibrateViewImpl() {
        controller = new CalibrateControllerImpl();
        controller.setView(this);
    }


    /************************************************************************************
     *                                                                                   *
     *                               Activity Methods                                    *
     *                                                                                   *
     *************************************************************************************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Kalibrieren");
        loadStepOneView();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (controller != null) {
            controller.onPause();
        }
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    /**
     * update the step count
     *
     * @param stepCount current step count
     */
    @Override
    public void updateStepCount(int stepCount) {
        stepCountView.setText(String.valueOf(stepCount));
    }


    /**
     * load a specific calibration step
     *
     * @param setupStep number of the calibration setup step
     */
    @Override
    public void loadCalibrateStep(int setupStep) {
        switch (setupStep) {
            case 1:
                loadStepOneView();
                break;
            case 2:
                loadStepTwoView();
                break;
            case 3:
                loadStepThreeView();
                break;
            default:
                break;
        }
    }


    /**
     * update the average stepdistance
     *
     * @param distance average stepdistance
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void updateAverageStepdistance(float distance) {
        stepDistanceView.setText(getString(R.string.step_setup_average_stepdistance) + " " + distance);
    }


    /**
     * update the average stepperiod
     *
     * @param period average step period
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void updateAverageStepperiod(int period) {
        stepPeriodView.setText(getString(R.string.step_setup_average_stepperiod) + " " + period + " ms");
    }


    /**
     * update the azimuth
     *
     * @param azimuth azimuth value
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void updateAzimuth(int azimuth) {
        azimuthView.setText(azimuth + "°");
        compassView.setRotation(-azimuth);
    }


    /**
     * get the view's context
     *
     * @return Context
     */
    @Override
    public Context getContext() {
        return this;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Class Methods                                       *
     *                                                                                   *
     *************************************************************************************/


    /**
     * Load view for the first calibration setup step
     */
    private void loadStepOneView() {
        // find contentframe
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        // remove old content
        contentFrameLayout.removeAllViews();
        // inflate new layout
        View view = getLayoutInflater().inflate(R.layout.activity_calibrate_step1, contentFrameLayout);

        stepCountView = (TextView) view.findViewById(R.id.coordinates_calibrate_tv_step);

        /***********        find UI Elements and set listeners          ************/

        btnStepIncStepOne = (Button) view.findViewById(R.id.coordinates_calibrate_btn_stepinc);
        btnStepIncStepOne.setOnClickListener(v -> {
            if (controller != null) {
                controller.onStepIncreaseClicked();
            }
        });
        btnStepIncStepOne.setEnabled(false);

        btnStepDecStepOne = (Button) view.findViewById(R.id.coordinates_calibrate_btn_stepdec);
        btnStepDecStepOne.setOnClickListener(v -> {
            if (controller != null) {
                controller.onStepDecreaseClicked();
            }
        });
        btnStepDecStepOne.setEnabled(false);

        btnNextStepOne = (Button) view.findViewById(R.id.coordinates_calibrate_next_1);
        btnNextStepOne.setOnClickListener(view1 -> {
            if (controller != null) {
                controller.onNextClicked(1);
            }
        });
        btnNextStepOne.setEnabled(false);

        btnStartStepOne = (Button) view.findViewById(R.id.coordinates_calibrate_btn_stepstart);
        btnStartStepOne.setOnClickListener(v -> {
            if (controller != null) {
                controller.onStartStepSetupClick();
            }
            btnStepIncStepOne.setEnabled(false);
            btnStepDecStepOne.setEnabled(false);
            btnStartStepOne.setEnabled(false);
            btnNextStepOne.setEnabled(true);
            btnStopStepOne.setEnabled(true);
        });

        btnStopStepOne = (Button) view.findViewById(R.id.coordinates_calibrate_btn_stepstop);
        btnStopStepOne.setOnClickListener(v -> {
            if (controller != null) {
                controller.onStopStepSetupClick();
            }
            btnStepIncStepOne.setEnabled(true);
            btnStepDecStepOne.setEnabled(true);
            btnStartStepOne.setEnabled(true);
            btnStopStepOne.setEnabled(false);
        });
        btnStopStepOne.setEnabled(false);
    }


    /**
     * Load view for the second calibration setup step
     */
    private void loadStepTwoView() {
        // find contentframe
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        // remove old content
        contentFrameLayout.removeAllViews();
        // inflate new layout
        View view = getLayoutInflater().inflate(R.layout.activity_calibrate_step2, contentFrameLayout);

        /************        find UI Elements and set listeners          ************/

        btnNextStepTwo = (Button) view.findViewById(R.id.coordinates_calibrate_next_2);
        btnNextStepTwo.setOnClickListener(view1 -> {
            if (controller != null) {
                controller.onNextClicked(2);
            }
        });
        btnNextStepTwo.setEnabled(false);

        Button btnBackStepTwo = (Button) view.findViewById(R.id.coordinates_calibrate_back_2);
        btnBackStepTwo.setOnClickListener(view12 -> {
            if (controller != null) {
                controller.onBackClicked(2);
            }
        });

        final EditText etDistance = (EditText) view.findViewById(R.id.coordinates_calibrate_distance);
        etDistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // check if there is any text, !.equals() doesn't seem to work
                if (!etDistance.getText().toString().isEmpty()) {
                    controller.onDistanceChange(Float.parseFloat(etDistance.getText().toString()));
                    btnNextStepTwo.setEnabled(true);
                }
            }
        });

        stepDistanceView = (TextView) view.findViewById(R.id.coordinates_calibrate_avgstepdistance);
        stepPeriodView = (TextView) view.findViewById(R.id.coordinates_calibrate_avgstepperiod);
    }


    /**
     * Load view for the third calibration setup step
     */
    private void loadStepThreeView() {

        // find content frame
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        // remove old content
        contentFrameLayout.removeAllViews();
        // inflate new layout
        View view = getLayoutInflater().inflate(R.layout.activity_calibrate_step3, contentFrameLayout);

        /************        find UI Elements and set listeners          ************/

        Button btnNext = (Button) view.findViewById(R.id.coordinates_calibrate_next_3);
        btnNext.setOnClickListener(view1 -> {
            loadMeasurement();
            if (controller != null) {
                controller.onNextClicked(3);
            }
        });

        Button btnBack = (Button) view.findViewById(R.id.coordinates_calibrate_back_3);
        btnBack.setOnClickListener(view12 -> {
            if (controller != null) {
                controller.onBackClicked(3);
            }
        });

        compassView = (ImageView) view.findViewById(R.id.coordinates_calibrate_compass_iv);
        azimuthView = (TextView) view.findViewById(R.id.coordinates_calibrate_compass_value);
    }
}
