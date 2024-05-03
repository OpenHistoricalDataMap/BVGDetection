package de.htwberlin.f4.ai.ma.indoorroutefinder.android.record;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.sensors.SensorType;

/**
 * RecordController Interface
 * <p>
 * used for recording sensor values
 * <p>
 * Author: Benjamin Kneer
 */

public interface RecordController {

    // set the responsible RecordView
    void setView(RecordView view);

    // triggered by clicked on start button
    void onStartClicked(List<SensorType> sensors);

    // triggered by clicking on stop button
    void onStopClicked();

    // triggered by activity onPause()
    void onPause();

    // triggered by changing the save period
    void onSavePeriodChanged(int value);
}
