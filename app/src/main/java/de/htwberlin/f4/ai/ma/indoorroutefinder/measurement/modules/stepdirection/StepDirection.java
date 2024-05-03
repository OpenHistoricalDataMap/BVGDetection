package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement.modules.stepdirection;

/**
 * Enumeration for the step direction
 * <p>
 * Author: Benjamin Kneer
 */
public enum StepDirection {

    FORWARD("Vorwärts"),
    BACKWARD("Rückwärts"),
    LEFT("Links"),
    RIGHT("Rechts");

    private final String name;

    StepDirection(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
