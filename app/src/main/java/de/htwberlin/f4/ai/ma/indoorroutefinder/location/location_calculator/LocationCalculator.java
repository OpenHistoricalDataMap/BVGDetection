package de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;

/**
 * Created by Johann Winter
 * <p>
 * This interface if for calculating a node from a given fingerprint.
 * It is used to locate the user.
 */
public interface LocationCalculator {

    /**
     * Calculate a node from a given fingerprint
     *
     * @param fingerprint the input fingerprint to be compared with all existent nodes to get the position
     * @return the ID (name) of the resulting node
     */
    String calculateNodeId(Fingerprint fingerprint);

}