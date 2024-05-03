package de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.edges;

/**
 * EdgeDetailsController Interface
 * <p>
 * Used for managing edge details
 * <p>
 * Author: Benjamin Kneer
 */

public interface EdgeDetailsController {

    // set the responsible view
    void setView(EdgeDetailsView view);

    // set start and target node
    void setNodes(String startNodeId, String targetNodeId);

    // triggered by clicking on delete button
    void onDeleteClicked();

    // triggered by clicking on save button
    void onSaveClicked();

    // triggered by changing handycap status of the edge
    void onHandycapChanged(boolean handycapFriendly);

    // triggered by entering an info for the edge
    void onEdgeInfoChanged(String info);

    // activity event
    void onResume();
}
