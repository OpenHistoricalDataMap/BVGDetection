package de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra;

import android.content.Context;

/**
 * Created by Johann Winter
 * <p>
 * Factory for creating DijkstraAlgorithm objects
 */

public class DijkstraAlgorithmFactory {

    public static DijkstraAlgorithm createInstance(Context context, boolean accessible) {
        return new DijkstraAlgorithmImpl(context, accessible);
    }
}
