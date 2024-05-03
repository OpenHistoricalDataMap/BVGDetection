package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.EdgeFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.NodeFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.JSON.JSONConverter;


/**
 * Created by Johann Winter
 * <p>
 * Handles the SQLite Database operations for inserting, editing and deleting
 * nodes and edges.
 * Handles the import / export functionality.
 */
class DatabaseHandlerImpl extends SQLiteOpenHelper implements DatabaseHandler {


    // Static variables

    public static final String DATABASE_HANDLER_IMPL = "DatabaseHandlerImpl";
    private static final String DATABASE_NAME = "indoor_data.db";
    private static final int DATABASE_VERSION = 1;
    private static final String NODES_TABLE = "nodes";
    private static final String EDGES_TABLE = "edges";
    private static final String NODE_ID = "id";
    private static final String NODE_DESCRIPTION = "description";
    private static final String NODE_WIFI_NAME = "wifi_name";
    private static final String NODE_SIGNALINFORMATIONLIST = "signalinformationlist";
    private static final String NODE_COORDINATES = "coordinates";
    private static final String NODE_PICTURE_PATH = "picture_path";
    private static final String NODE_ADDITIONAL_INFO = "additional_info";
    private static final String EDGE_ID = "id";
    private static final String EDGE_NODE_A = "nodeA";
    private static final String EDGE_NODE_B = "nodeB";
    private static final String EDGE_ACCESSIBILITY = "accessibility";
    private static final String EDGE_STEPLIST = "steplist";
    private static final String EDGE_WEIGHT = "weight";
    private static final String EDGE_ADDITIONAL_INFO = "additional_info";
    private final JSONConverter jsonConverter = new JSONConverter();
    private final Context context;


    // Constructor
    DatabaseHandlerImpl(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // Nodes table
        String createNodeTableQuery = "CREATE TABLE " + NODES_TABLE + " (" +
                NODE_ID + " TEXT PRIMARY KEY," +
                NODE_DESCRIPTION + " TEXT," +
                NODE_WIFI_NAME + " TEXT, " +
                NODE_SIGNALINFORMATIONLIST + " TEXT," +
                NODE_COORDINATES + " TEXT," +
                NODE_PICTURE_PATH + " TEXT," +
                NODE_ADDITIONAL_INFO + " TEXT)";

        // Edges table
        String createEdgeTableQuery = "CREATE TABLE " + EDGES_TABLE + " (" +
                EDGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                EDGE_NODE_A + " TEXT," +
                EDGE_NODE_B + " TEXT," +
                EDGE_ACCESSIBILITY + " TEXT," +
                EDGE_STEPLIST + " TEXT," +
                EDGE_WEIGHT + " REAL," +
                EDGE_ADDITIONAL_INFO + " TEXT);";


        db.execSQL(createNodeTableQuery);
        db.execSQL(createEdgeTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }


    //----------------- N O D E S ------------------------------------------------------------------------------------------

    /**
     * Insert a new Node
     *
     * @param node the node to insert
     */
    public void insertNode(Node node) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(NODE_ID, node.getId());
        values.put(NODE_DESCRIPTION, node.getDescription());

        // If the Node has a fingerprint
        if (node.getFingerprint() != null) {
            values.put(NODE_WIFI_NAME, node.getFingerprint().getSsid());
            values.put(NODE_SIGNALINFORMATIONLIST, jsonConverter.convertSignalSampleListToJSON(node.getFingerprint().getSignalSampleList()));
        }

        values.put(NODE_COORDINATES, node.getCoordinates());
        values.put(NODE_PICTURE_PATH, node.getPicturePath());
        values.put(NODE_ADDITIONAL_INFO, node.getAdditionalInfo());

        database.insert(NODES_TABLE, null, values);

        Log.d("DB: insert_node:id:", node.getId());

        database.close();
    }

    /**
     * Update a Node
     *
     * @param node      the new Node
     * @param oldNodeId the original nodeID (name) which will be changed
     */
    public void updateNode(Node node, String oldNodeId) {

        // At first, update Edges which contain the updated Node
        for (Edge e : getAllEdges()) {
            if (e.getNodeA().getId().equals(oldNodeId)) {
                updateEdge(e, EDGE_NODE_A, node.getId());
            } else if (e.getNodeB().getId().equals(oldNodeId)) {
                updateEdge(e, EDGE_NODE_B, node.getId());
            }
        }

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(NODE_ID, node.getId());
        contentValues.put(NODE_DESCRIPTION, node.getDescription());
        contentValues.put(NODE_COORDINATES, node.getCoordinates());
        contentValues.put(NODE_ADDITIONAL_INFO, node.getAdditionalInfo());

        // If the Node has a fingerprint
        if (node.getFingerprint() != null) {
            contentValues.put(NODE_WIFI_NAME, node.getFingerprint().getSsid());
            contentValues.put(NODE_SIGNALINFORMATIONLIST, jsonConverter.convertSignalSampleListToJSON(node.getFingerprint().getSignalSampleList()));
        }

        contentValues.put(NODE_PICTURE_PATH, node.getPicturePath());

        Log.d("DB: update_node: id:", node.getId());

        database.update(NODES_TABLE, contentValues, NODE_ID + "='" + oldNodeId + "'", null);

        database.close();
    }


    /**
     * Get a List of all Nodes
     *
     * @return a list of all Nodes
     */
    public List<Node> getAllNodes() {
        List<Node> allNodes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + NODES_TABLE;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Fingerprint fingerprint;

                // Check if fingerprintImpl exists, else create null object for fingerprintImpl
                if (cursor.getString(3) == null) {
                    fingerprint = null;
                } else {
                    fingerprint = FingerprintFactory.createInstance(cursor.getString(2), jsonConverter.convertJsonToSignalSampleList(cursor.getString(3)));
                }

                Node node = NodeFactory.createInstance(cursor.getString(0), cursor.getString(1), fingerprint, cursor.getString(4), cursor.getString(5), cursor.getString(6));

                allNodes.add(node);
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.close();
        return allNodes;
    }


    /**
     * Get a single Node
     *
     * @param nodeID the name of the Node
     */
    public Node getNode(String nodeID) {
        SQLiteDatabase database = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + NODES_TABLE + " WHERE " + NODE_ID + " ='" + nodeID + "'";
        Node node = null;
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            Fingerprint fingerprint;

            // Check if fingerprint exists, else create NULL object for fingerprint
            if (cursor.getString(3) == null) {
                fingerprint = null;
            } else {
                fingerprint = FingerprintFactory.createInstance(cursor.getString(2), jsonConverter.convertJsonToSignalSampleList(cursor.getString(3)));
            }

            node = NodeFactory.createInstance(cursor.getString(0), cursor.getString(1), fingerprint, cursor.getString(4), cursor.getString(5), cursor.getString(6));
            Log.d("DB: select_node", nodeID);
            cursor.close();
        }
        database.close();
        return node;
    }


    /**
     * Check if a node already exists
     *
     * @param nodeID the name of the node
     * @return boolean, if node exists
     */
    public boolean checkIfNodeExists(String nodeID) {
        return getNode(nodeID) != null;
    }


    /**
     * Delete a single node
     *
     * @param node the node to be deleted
     */
    public void deleteNode(Node node) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteNodeQuery = "DELETE FROM " + NODES_TABLE + " WHERE " + NODE_ID + " ='" + node.getId() + "'";
        String deleteBelongingEdgeQuery = "DELETE FROM " + EDGES_TABLE + " WHERE " + EDGE_NODE_A + " ='" + node.getId() + "' OR " + EDGE_NODE_B + " ='" + node.getId() + "'";

        Log.d("DB: delete_node", node.getId());

        database.execSQL(deleteNodeQuery);
        database.execSQL(deleteBelongingEdgeQuery);
    }


    //----------- E D G E S -------------------------------------------------------------------------------------

    /**
     * Insert an edge
     *
     * @param edge the edge to be inserted
     */
    public void insertEdge(Edge edge) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(EDGE_NODE_A, edge.getNodeA().getId());
        values.put(EDGE_NODE_B, edge.getNodeB().getId());
        values.put(EDGE_ACCESSIBILITY, edge.getAccessibility());

        StringBuilder stepListSb = new StringBuilder();
        for (String string : edge.getStepCoordsList()) {
            stepListSb.append(string);
            stepListSb.append("\t");
        }

        values.put(EDGE_STEPLIST, stepListSb.toString());
        values.put(EDGE_WEIGHT, edge.getWeight());
        values.put(EDGE_ADDITIONAL_INFO, edge.getAdditionalInfo());

        database.insert(EDGES_TABLE, null, values);

        Log.d("DB: insert_EDGE", edge.getNodeA().getId() + " " + edge.getNodeB().getId());

        database.close();
    }

    /**
     * Update an edge (only for changing nodeA and nodeB attribute of the edge).
     *
     * @param edge            the edge to be updated
     * @param nodeToBeUpdated the edge's nodeA or nodeB
     * @param value           the ID (name) of the node
     */
    public void updateEdge(Edge edge, String nodeToBeUpdated, String value) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (nodeToBeUpdated.equals(EDGE_NODE_A)) {
            contentValues.put(EDGE_NODE_A, value);
            contentValues.put(EDGE_NODE_B, edge.getNodeB().getId());

        } else if (nodeToBeUpdated.equals(EDGE_NODE_B)) {
            contentValues.put(EDGE_NODE_B, value);
            contentValues.put(EDGE_NODE_A, edge.getNodeA().getId());
        }

        StringBuilder stepListSb = new StringBuilder();
        for (String string : edge.getStepCoordsList()) {
            stepListSb.append(string);
            stepListSb.append("\t");
        }

        contentValues.put(EDGE_ACCESSIBILITY, edge.getAccessibility());
        contentValues.put(EDGE_STEPLIST, stepListSb.toString());
        contentValues.put(EDGE_WEIGHT, edge.getWeight());
        contentValues.put(EDGE_ADDITIONAL_INFO, edge.getAdditionalInfo());

        database.update(EDGES_TABLE, contentValues, EDGE_NODE_A + "='" + edge.getNodeA().getId() + "' AND " + EDGE_NODE_B + "='" + edge.getNodeB().getId() + "'", null);

        database.close();
    }


    /**
     * Update an edge (everything but edge's nodeA and nodeB attribute)
     *
     * @param edge the edge to be updated
     */
    public void updateEdge(Edge edge) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        StringBuilder stepListSb = new StringBuilder();
        for (String string : edge.getStepCoordsList()) {
            stepListSb.append(string);
            stepListSb.append("\t");
        }

        contentValues.put(EDGE_ACCESSIBILITY, edge.getAccessibility());
        contentValues.put(EDGE_STEPLIST, stepListSb.toString());
        contentValues.put(EDGE_WEIGHT, edge.getWeight());
        contentValues.put(EDGE_ADDITIONAL_INFO, edge.getAdditionalInfo());

        database.update(EDGES_TABLE, contentValues, EDGE_NODE_A + "='" + edge.getNodeA().getId() + "' AND " + EDGE_NODE_B + "='" + edge.getNodeB().getId() + "'", null);

        database.close();
    }


    /**
     * Get single edge
     *
     * @param nodeA the startnode of the edge
     * @param nodeB the endnode of the edge
     * @return the edge
     */
    public Edge getEdge(Node nodeA, Node nodeB) {
        String selectQuery = "SELECT * FROM " + EDGES_TABLE + " WHERE " + EDGE_NODE_A + "='" + nodeA.getId() + "' AND " + EDGE_NODE_B + "='" + nodeB.getId() + "' OR " +
                EDGE_NODE_A + "='" + nodeB.getId() + "' AND " + EDGE_NODE_B + "='" + nodeA.getId() + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            boolean accessible = cursor.getInt(3) == 1;
            Node node1 = getNode(cursor.getString(1));
            Node node2 = getNode(cursor.getString(2));

            String stepListString = cursor.getString(4);
            List<String> stepList = new ArrayList<>(Arrays.asList(stepListString.split("\t")));

            Edge edge = EdgeFactory.createInstance(node1, node2, accessible, stepList, cursor.getFloat(5), cursor.getString(6));

            cursor.close();
            database.close();
            return edge;
        }
        return null;
    }


    /**
     * Get a list of all edges
     *
     * @return the list of edges
     */
    public List<Edge> getAllEdges() {
        List<Edge> allEdges = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + EDGES_TABLE;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                boolean accessible = cursor.getInt(3) == 1;

                Node nodeA = getNode(cursor.getString(1));
                Node nodeB = getNode(cursor.getString(2));

                String stepListString = cursor.getString(4);
                List<String> stepList = new ArrayList<>(Arrays.asList(stepListString.split("\t")));

                Edge edge = EdgeFactory.createInstance(nodeA, nodeB, accessible, stepList, cursor.getFloat(5), cursor.getString(6));

                allEdges.add(edge);

            } while (cursor.moveToNext());
            cursor.close();
            database.close();
        }
        return allEdges;
    }


    /**
     * Check if an edge already exists
     *
     * @param edge the edge to be checked
     * @return boolean, if edge exists
     */
    public boolean checkIfEdgeExists(Edge edge) {
        String selectQuery = "SELECT * FROM " + EDGES_TABLE + " WHERE " + EDGE_NODE_A + " ='" + edge.getNodeA().getId() + "' AND " + EDGE_NODE_B + " ='" + edge.getNodeB().getId() + "' " +
                " OR " + EDGE_NODE_A + " ='" + edge.getNodeB().getId() + "' AND " + EDGE_NODE_B + " ='" + edge.getNodeA().getId() + "' ";

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            return true;
        }

        cursor.close();
        database.close();
        return false;
    }


    /**
     * Delete a single edge
     *
     * @param edge the edge to be deleted
     */
    public void deleteEdge(Edge edge) {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM " + EDGES_TABLE + " WHERE " + EDGE_NODE_A + " ='" + edge.getNodeA().getId() + "' AND " + EDGE_NODE_B + " ='" + edge.getNodeB().getId() + "'"
                + " OR " + EDGE_NODE_A + " ='" + edge.getNodeB().getId() + "' AND " + EDGE_NODE_B + " ='" + edge.getNodeA().getId() + "' ";

        Log.d("DB: delete_EDGE", edge.getNodeA().getId() + " " + edge.getNodeB().getId());

        database.execSQL(deleteQuery);
        database.close();
    }


    //------------- I M P O R T ------------------------------------------------------------------

    /**
     * Copies the database file at "/IndoorPositioning/Exported/indoor_data.db" over the current
     * internal application database (existing data will be overwritten!).
     *
     * @return return-code: true means successful, false unsuccessful
     */
    public boolean importDatabase() throws IOException {

        String DB_FILEPATH = context.getApplicationInfo().dataDir + "/databases/indoor_data.db";

        // Close the SQLiteOpenHelper so it will commit the created empty database to internal storage
        close();

        File oldDb = new File(DB_FILEPATH);
        File newDb = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IndoorPositioning/Exported/indoor_data.db");

        if (newDb.exists()) {
            System.out.println("+++ new db exists");
            FileUtilities.copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
            // Access the copied database so SQLiteHelper will cache it and mark it as created
            getWritableDatabase().close();
            return true;
        }
        return false;
    }


    //------------------- E X P O R T ------------------------------------------------------------


    /**
     * Export the database to SDCARD location "/IndoorPositioning/Exported/indoor_data.db".
     *
     * @return boolean, if action was successful
     */
    public boolean exportDatabase() {
        try {

            File exportFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IndoorPositioning/Exported");
            if (!exportFolder.exists()) {
                exportFolder.mkdirs();
            }

            if (exportFolder.canWrite()) {
                String currentDBPath = context.getDatabasePath("indoor_data.db").getPath();

                String exportFilename = "indoor_data.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(exportFolder, exportFilename);

                if (currentDB.exists()) {
                    FileUtilities.copyFile(new FileInputStream(currentDB), new FileOutputStream(backupDB));
                    return true;
                }

            }
        } catch (Exception e) {
            Log.d(DATABASE_HANDLER_IMPL, e.toString());
        }
        return false;
    }

}

