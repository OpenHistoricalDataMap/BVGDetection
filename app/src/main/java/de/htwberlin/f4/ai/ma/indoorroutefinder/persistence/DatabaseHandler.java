package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence;

import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;

/**
 * Created by Johann Winter
 * <p>
 * This interface offers all necessary database functions to
 * create, edit and delete Nodes and Edges and
 * import / export functionality of the SQLite DB.
 */
public interface DatabaseHandler {

//----------------------- Nodes management ----------------------------

    /**
     * Insert a new node to the database
     *
     * @param room the node
     */
    void insertOrUpdateRoom(Room room);

    /**
     * Update a node in the database
     *
     * @param room      the node to update, including a potential new name (ID)
     * @param oldNodeId the (old) ID (name) of the node which has to be updated
     */
    void updateRoom(Room room, String oldNodeId);

    /**
     * Getter for a list of all existent nodes in the database
     *
     * @return the list of all existent nodes
     */
    List<Room> getAllRooms();

    /**
     * Getter for a single node
     *
     * @param nodeID the name (ID) of the node
     * @return the node object
     */
    Room getRoom(String nodeID);

    /**
     * Checks if a node with the given ID is already existent in database.
     * This method is called before creating a new node.
     *
     * @param nodeID the ID to be checked
     * @return true if a node already exists, false if not
     */
    boolean checkIfRoomExists(String nodeID);

    /**
     * Delete a single node from the database.
     *
     * @param room the node to be deleted
     */
    void deleteRoom(Room room);


//-------------------- Edges management -------------------------------

    /**
     * Insert a new edge to the database
     *
     * @param edge the edge to insert
     */
    void insertEdge(Edge edge);

    /**
     * Getter for a single edge from the database
     *
     * @param roomA the first node of the edge
     * @param roomB the second node of the edge
     * @return the edge object
     */
    Edge getEdge(Room roomA, Room roomB);

    /**
     * Getter for all existent edges in the database
     *
     * @return a list of all edges
     */
    List<Edge> getAllEdges();

    /**
     * Update an edge (everything but not the edge's nodeA and nodeB attribute)
     *
     * @param edge the edge to be updated
     */
    void updateEdge(Edge edge);

    /**
     * Update an edge (only for changing nodeA and nodeB attribute of the edge).
     * This is called when a node gets its name updated, to update the edges which include the node.
     *
     * @param edge            the edge to be updated
     * @param nodeToBeUpdated the node (nodeA or nodeB) of the edge which will be updated
     * @param value           the new value of the selected field (new node ID)
     */
    void updateEdge(Edge edge, String nodeToBeUpdated, String value);

    /**
     * Checks, if an edge is already existent.
     * This is done when trying to create a new edge.
     *
     * @param edge the edge to be checked
     * @return true, if edge already exists
     */
    boolean checkIfEdgeExists(Edge edge);

    /**
     * Delete an edge from the database
     *
     * @param edge the edge to be deleted
     */
    void deleteEdge(Edge edge);


//---------------------- Import / Export of the database -------------------------


    /**
     * Get a database object to work with
     *
     * @return the SQLite database object
     */
    SQLiteDatabase getWritableDatabase();

    /**
     * Import a database file (.db) from the external storage to the application's database.
     * The file must be placed at the path "/IndoorPositioning/Exported/indoor_data.db".
     * This will erase all previous data!
     *
     * @return true if it succeeded
     * @throws IOException a IOException
     */
    boolean importDatabase() throws IOException;

    /**
     * Export the database to a .db file on the external storage.
     * The exported file could be found at "/IndoorPositioning/Exported/indoor_data.db"
     *
     * @return true if it succeeded
     */
    boolean exportDatabase();

    /**
     * Delete all unused routers from the database.
     * This is done to keep the database clean.
     */
    void deleteAllUnusedRouter();

    /**
     * Delete all unused measurements from the database.
     * This is done to keep the database clean.
     */
    void deleteAllUnusedMeasurements();

}
