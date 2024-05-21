package de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations;

import androidx.annotation.NonNull;

import com.google.common.collect.Multimap;

/**
 * Created by Johann Winter
 * <p>
 * Thanks to Carola Walter
 */

public class RestructedNode {
    String id;
    Multimap<String, Double> restructedSignals;

    public RestructedNode(String id, Multimap<String, Double> restructedSignals) {
        this.id = id;
        this.restructedSignals = restructedSignals;
    }

    public String getId() {
        return this.id;
    }

    public Multimap<String, Double> getRestructedSignals() {
        return this.restructedSignals;
    }

    @NonNull
    @Override
    public String toString() {
        return "RestructedNode{" +
                "id='" + id + '\'' +
                ", restructedSignals=" + restructedSignals +
                '}';
    }
}