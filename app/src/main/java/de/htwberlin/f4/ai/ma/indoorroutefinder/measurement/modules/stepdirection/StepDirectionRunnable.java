package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection;

/**
 * StepDirectionRunnable class which implements the Runnable Interface
 * <p>
 * used for stepdetection in a thread
 * <p>
 * Author: Benjamin Kneer
 */
public class StepDirectionRunnable implements Runnable {

    private StepDirectionDetectListener listener;
    private final StepDirectionModule stepDirectionDetect;


    public StepDirectionRunnable(StepDirectionModule stepDirectionDetect) {
        this.stepDirectionDetect = stepDirectionDetect;
    }


    /************************************************************************************
     *                                                                                   *
     *                               Interface Methods                                   *
     *                                                                                   *
     *************************************************************************************/


    @Override
    public void run() {
        StepDirection direction = stepDirectionDetect.getLastStepDirection();

        if (listener != null) {
            listener.onDirectionDetect(direction);
        }
    }


    /************************************************************************************
     *                                                                                   *
     *                               Class Methods                                       *
     *                                                                                   *
     *************************************************************************************/


    public void setListener(StepDirectionDetectListener listener) {
        this.listener = listener;
    }

}
