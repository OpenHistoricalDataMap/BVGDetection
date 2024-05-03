package de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.edges;

import android.content.Context;

import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;

/**
 * EdgeDetailsView Interface
 * <p>
 * Used for managing Edge details
 * <p>
 * Author: Benjamin Kneer
 */

public interface EdgeDetailsView {

    // get the view's context
    Context getContext();

    // update start node
    void updateStartNodeInfo(Room room);

    // update target node
    void updateTargetNodeInfo(Room room);

    // update edge
    void updateEdgeInfo(Edge edge);

    // finish the view
    void finish();
}
