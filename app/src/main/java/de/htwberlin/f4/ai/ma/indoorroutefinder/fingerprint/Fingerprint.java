package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import java.util.List;

/**
 * Created by Johann Winter
 * <p>
 * This interface is for fingerprints.
 */

public interface Fingerprint {

    /**
     * Getter for the list of SignalSamples which contain the measured signal data of a fingerprint
     *
     * @return the list of SignalSamples
     */
    List<SignalSample> getSignalSampleList();
}
