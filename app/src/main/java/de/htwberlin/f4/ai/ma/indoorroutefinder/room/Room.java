package de.htwberlin.f4.ai.ma.indoorroutefinder.room;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;

/**
 * Created by Johann Winter
 * <p>
 * This Interface is used to manage nodes ("Orte").
 */
public interface Room {

    /**
     * Getter for the ID (name) of a node
     *
     * @return the ID (name)
     */
    String getRoomName();

    /**
     * Setter for the ID (name) of a node
     *
     * @param roomName the ID (name)
     */
    void setRoomName(String roomName);

    /**
     * Getter for the description of a node
     *
     * @return the description string
     */
    String getDescription();

    /**
     * Getter for the fingerprint of a node
     *
     * @return the fingerprint object
     */
    Fingerprint getFingerprint();

    /**
     * Getter for the coordinates of a node.
     * The coordinates (x,y,z) will be writte to a string
     *
     * @return the coordinates string
     */
    String getCoordinates();

    /**
     * Setter for the coordinates of a node.
     *
     * @param coordinates the coordinates string
     */
    void setCoordinates(String coordinates);

    /**
     * Getter for the path of the picture belonging to the node.
     * The path will point to external storage of the device
     *
     * @return the path to the picture file
     */
    String getPicturePath();

    /**
     * Getter for additional information of a node.
     * For later purposes.
     *
     * @return the additional information string
     */
    String getAdditionalInfo();

    /**
     * Setter for additional information of a node.
     * For later purposes.
     *
     * @param additionalInfo the additional information string
     */
    void setAdditionalInfo(String additionalInfo);

    /**
     * Getter for the roomDatabaseID of a node.
     * For later purposes.
     *
     * @return the roomDatabaseID
     */
    int getRoomDatabaseID();

    /**
     * Setter for the roomDatabaseID of a node.
     * For later purposes.
     *
     * @param roomDatabaseID the roomDatabaseID
     */
    void setRoomDatabaseID(int roomDatabaseID);

}

