package de.htwberlin.f4.ai.ma.indoorroutefinder.edge;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;

/**
 * Created by Johann Winter
 * <p>
 * This interface is designed for managing edges between two nodes ("Wege").
 */

public interface Edge {


    /**
     * Getter for the first (start-) node
     *
     * @return the first node
     */
    Room getNodeA();

    /**
     * Getter for the second (end-) node
     *
     * @return the second node
     */
    Room getNodeB();

    /**
     * Getter for the accessibility of an edge
     *
     * @return returns true if the way is accessible
     */
    boolean getAccessibility();

    /**
     * Setter for the accessibility of an edge
     *
     * @param accessable true if accessible, false if not
     */
    void setAccessibility(boolean accessable);

    /**
     * Getter for the weight of an edge (distance between nodeA and nodeB)
     *
     * @return the distance in meters
     */
    float getWeight();

    /**
     * Setter for the weight of an edge (distance between nodeA and nodeB)
     *
     * @param weight the distance in meters
     */
    void setWeight(float weight);


    /**
     * Getter for the list of stepcoordinates of an edge.
     * See entry "Wegvermessung" in main menu
     *
     * @return the list of stepcoordinates
     */
    List<String> getStepCoordsList();


    /**
     * Getter for the additional field (for later purposes)
     *
     * @return the additional information string
     */
    String getAdditionalInfo();

    /**
     * Setter for the additional field (for later purposes)
     *
     * @param additionalInfo the additional information string
     */
    void setAdditionalInfo(String additionalInfo);
}
