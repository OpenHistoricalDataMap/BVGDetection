package de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.edges;

/**
 * StepData Class
 * <p>
 * used to store details for each step so we can save it later in edge object
 * <p>
 * Author: Benjamin Kneer
 */
public class StepData {

    private float[] coords;


    public StepData() {
        coords = new float[]{0.0f, 0.0f, 0.0f};
    }

    public void setStepName() {
    }

    public float[] getCoords() {
        return coords;
    }

    public void setCoords(float[] coords) {
        this.coords = coords;
    }

}
