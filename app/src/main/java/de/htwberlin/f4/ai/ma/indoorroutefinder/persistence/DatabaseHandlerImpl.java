package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.EdgeFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformationFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.RoomFactory;


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
    private static final String EDGES_TABLE = "edges";
    private static final String EDGE_ID = "id";
    private static final String EDGE_NODE_A = "nodeA";
    private static final String EDGE_NODE_B = "nodeB";
    private static final String EDGE_ACCESSIBILITY = "accessibility";
    private static final String EDGE_STEPLIST = "steplist";
    private static final String EDGE_WEIGHT = "weight";
    private static final String EDGE_ADDITIONAL_INFO = "additional_info";
    private static final String TABLE_ROOMS = "rooms";
    private static final String ROOM_ID = "room_id";
    private static final String ROOM_NAME = "room_name";
    private static final String ROOM_DESCRIPTION = "description";
    private static final String ROOM_COORDINATES = "coordinates";
    private static final String ROOM_PICTURE_PATH = "picture_path";
    private static final String ROOM_ADDITIONAL_INFO = "additional_info";
    // Tabelle measurements
    private static final String TABLE_MEASUREMENTS = "measurements";
    private static final String MEASUREMENT_ID = "measurement_id";
    private static final String TIMESTAMP = "timestamp";
    private static final String ROOM_ID_FK = "room_id";
    private static final String DEVICE_ID = "device_id";
    // Tabelle routers
    private static final String TABLE_ROUTERS = "routers";
    private static final String ROUTER_ID = "router_id";
    private static final String SSID = "ssid";
    private static final String BSSID = "bssid";
    // Tabelle measurement_router
    private static final String TABLE_MEASUREMENT_ROUTER = "measurement_router";
    private static final String RSSI = "signal_strength";
    private final Context context;

    // Constructor
    DatabaseHandlerImpl(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create the rooms table
        String createRoomsTableQuery = "CREATE TABLE " + TABLE_ROOMS + " (" +
                ROOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ROOM_COORDINATES + " VARCHAR(255)," +
                ROOM_DESCRIPTION + " VARCHAR(255)," +
                ROOM_PICTURE_PATH + " VARCHAR(255)," +
                ROOM_ADDITIONAL_INFO + " VARCHAR(255)," +
                ROOM_NAME + " VARCHAR(255) UNIQUE)";

        // Create the measurements table
        String createMeasurementsTableQuery = "CREATE TABLE " + TABLE_MEASUREMENTS + " (" +
                MEASUREMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TIMESTAMP + " TIMESTAMP NOT NULL," +
                DEVICE_ID + " VARCHAR(255) NOT NULL," +
                ROOM_ID_FK + " INT NOT NULL," +
                "FOREIGN KEY(" + ROOM_ID_FK + ") REFERENCES " + TABLE_ROOMS + "(" + ROOM_ID + "))";

        // Create the routers table
        String createRoutersTableQuery = "CREATE TABLE " + TABLE_ROUTERS + " (" +
                ROUTER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SSID + " VARCHAR(255)," +
                BSSID + " VARCHAR(255) UNIQUE)";

        // Create the measurement_router table
        String createMeasurementRouterTableQuery = "CREATE TABLE " + TABLE_MEASUREMENT_ROUTER + " (" +
                MEASUREMENT_ID + " INT," +
                ROUTER_ID + " INT," +
                RSSI + " INT," +
                "PRIMARY KEY (" + MEASUREMENT_ID + ", " + ROUTER_ID + ")," +
                "FOREIGN KEY(" + MEASUREMENT_ID + ") REFERENCES " + TABLE_MEASUREMENTS + "(" + MEASUREMENT_ID + ")," +
                "FOREIGN KEY(" + ROUTER_ID + ") REFERENCES " + TABLE_ROUTERS + "(" + ROUTER_ID + "))";

        // Edges table
        String createEdgeTableQuery = "CREATE TABLE " + EDGES_TABLE + " (" +
                EDGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                EDGE_NODE_A + " TEXT," +
                EDGE_NODE_B + " TEXT," +
                EDGE_ACCESSIBILITY + " TEXT," +
                EDGE_STEPLIST + " TEXT," +
                EDGE_WEIGHT + " REAL," +
                EDGE_ADDITIONAL_INFO + " TEXT);";


        db.execSQL(createEdgeTableQuery);
        db.execSQL(createRoomsTableQuery);
        db.execSQL(createMeasurementsTableQuery);
        db.execSQL(createRoutersTableQuery);
        db.execSQL(createMeasurementRouterTableQuery);
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
     * @param room the room to insert
     */
    @SuppressLint("Range")
    @Override
    public int insertOrUpdateRoom(Room room) {
        int numberOfNewFingerprints = 0;
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues roomValues = new ContentValues();
        roomValues.put(ROOM_NAME, room.getRoomName());
        roomValues.put(ROOM_DESCRIPTION, room.getDescription());
        roomValues.put(ROOM_COORDINATES, room.getCoordinates());
        roomValues.put(ROOM_PICTURE_PATH, room.getPicturePath());
        roomValues.put(ROOM_ADDITIONAL_INFO, room.getAdditionalInfo());

        Cursor cursor = database.rawQuery("SELECT " + ROOM_ID + " FROM " + TABLE_ROOMS + " WHERE " + ROOM_NAME + " = ?", new String[]{room.getRoomName()});
        long roomId;
        if (cursor.moveToFirst()) {
            roomId = cursor.getInt(cursor.getColumnIndex(ROOM_ID));
            database.update(TABLE_ROOMS, roomValues, ROOM_ID + " = ?", new String[]{String.valueOf(roomId)});
            cursor.close();
        } else {
            roomId = database.insert(TABLE_ROOMS, null, roomValues);
            cursor.close();
        }

        if (roomId != -1) {
            Fingerprint fingerprint = room.getFingerprint();

            if (fingerprint != null) {

                String deviceId = fingerprint.getDeviceID();

                for (SignalSample sample : fingerprint.getSignalSampleList()) {
                    Log.d("INSERT_FINGERPRINT", "Inserting fingerprint for room " + room.getRoomName() + " with timestamp " + sample.getTimestamp() + " and device ID " + deviceId);

                    long timestamp = sample.getTimestamp();


                    Cursor existingMeasurementCursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENTS +
                            " WHERE " + DEVICE_ID + " = ? AND " + TIMESTAMP + " = ?", new String[]{deviceId, String.valueOf(timestamp)});

                    if (existingMeasurementCursor.getCount() == 0) {

                        ContentValues sampleValues = new ContentValues();
                        sampleValues.put(TIMESTAMP, sample.getTimestamp());
                        sampleValues.put(DEVICE_ID, fingerprint.getDeviceID());
                        sampleValues.put(ROOM_ID_FK, roomId);

                        long measurementId = database.insert(TABLE_MEASUREMENTS, null, sampleValues);

                        if (measurementId != -1) {
                            for (AccessPointInformation accessPoint : sample.getAccessPointInformationList()) {
                                cursor = database.rawQuery("SELECT " + ROUTER_ID + " FROM " + TABLE_ROUTERS + " WHERE " + BSSID + " = ?", new String[]{accessPoint.getBSSID()});
                                long routerId;
                                if (cursor.moveToFirst()) {
                                    routerId = cursor.getLong(cursor.getColumnIndex(ROUTER_ID));
                                } else {
                                    ContentValues routerValues = new ContentValues();
                                    routerValues.put(BSSID, accessPoint.getBSSID());
                                    routerValues.put(SSID, accessPoint.getSSID());
                                    routerId = database.insert(TABLE_ROUTERS, null, routerValues);
                                }
                                cursor.close();

                                ContentValues measurementRouterValues = new ContentValues();
                                measurementRouterValues.put(MEASUREMENT_ID, measurementId);
                                measurementRouterValues.put(ROUTER_ID, routerId);
                                measurementRouterValues.put(RSSI, accessPoint.getRSSI());
                                database.insert(TABLE_MEASUREMENT_ROUTER, null, measurementRouterValues);
                            }
                            numberOfNewFingerprints++;
                        }
                    } else {
                        Log.d(DATABASE_HANDLER_IMPL, "Existing measurement found. Skipping.");
                    }
                    existingMeasurementCursor.close();
                }
            }
        }

        database.close();
        return numberOfNewFingerprints;
    }


    /**
     * Update the information for a room (not the fingerprints!)
     *
     * @param room        the new Node
     * @param oldRoomName the original nodeID (name) which will be changed
     */
    public void updateRoom(Room room, String oldRoomName) {

        for (Edge e : getAllEdges()) {
            if (e.getNodeA().getRoomName().equals(oldRoomName)) {
                updateEdge(e, EDGE_NODE_A, room.getRoomName());
            } else if (e.getNodeB().getRoomName().equals(oldRoomName)) {
                updateEdge(e, EDGE_NODE_B, room.getRoomName());
            }
        }

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(ROOM_NAME, room.getRoomName());
        updatedValues.put(ROOM_DESCRIPTION, room.getDescription());
        updatedValues.put(ROOM_COORDINATES, room.getCoordinates());
        updatedValues.put(ROOM_PICTURE_PATH, room.getPicturePath());
        updatedValues.put(ROOM_ADDITIONAL_INFO, room.getAdditionalInfo());

        int rowsAffected = database.update(TABLE_ROOMS, updatedValues, ROOM_NAME + " = ?", new String[]{oldRoomName});

        if (rowsAffected > 0) {
            Log.d("UPDATE_ROOM", "Room with name " + oldRoomName + " updated successfully.");
        } else {
            Log.d("UPDATE_ROOM", "Room with name " + oldRoomName + " not found in the database.");
        }

        database.close();
    }


    /**
     * Get a List of all Nodes
     *
     * @return a list of all Nodes
     */
    @SuppressLint("Range")
    @Override
    public List<Room> getAllRooms() {
        List<Room> allRooms = new ArrayList<>();

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ROOMS, null);

        if (cursor.moveToFirst()) {
            do {
                String roomName = cursor.getString(cursor.getColumnIndex(ROOM_NAME));
                int roomID = cursor.getInt(cursor.getColumnIndex(ROOM_ID));
                String description = cursor.getString(cursor.getColumnIndex(ROOM_DESCRIPTION));
                String coordinates = cursor.getString(cursor.getColumnIndex(ROOM_COORDINATES));
                String picturePath = cursor.getString(cursor.getColumnIndex(ROOM_PICTURE_PATH));
                String additionalInfo = cursor.getString(cursor.getColumnIndex(ROOM_ADDITIONAL_INFO));

                Cursor fingerprintCursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENTS + " WHERE " + ROOM_ID_FK + " = ?", new String[]{String.valueOf(roomID)});

                List<SignalSample> signalSamples = new ArrayList<>();

                if (fingerprintCursor.moveToFirst()) {
                    do {
                        long timestamp = fingerprintCursor.getLong(fingerprintCursor.getColumnIndex(TIMESTAMP));
                        int measurementId = fingerprintCursor.getInt(fingerprintCursor.getColumnIndex(MEASUREMENT_ID));


                        Cursor accessPointCursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENT_ROUTER + " WHERE " + MEASUREMENT_ID + " = ?", new String[]{String.valueOf(measurementId)});

                        List<AccessPointInformation> accessPointInformationList = new ArrayList<>();

                        if (accessPointCursor.moveToFirst()) {
                            do {
                                String bssid = "";
                                int rssi = accessPointCursor.getInt(accessPointCursor.getColumnIndex(RSSI));
                                String ssid = "";
                                int routerId = accessPointCursor.getInt(accessPointCursor.getColumnIndex(ROUTER_ID));

                                Cursor routerCursor = database.rawQuery("SELECT * FROM " + TABLE_ROUTERS + " WHERE " + ROUTER_ID + " = ?", new String[]{String.valueOf(routerId)});

                                if (routerCursor.moveToFirst()) {
                                    bssid = routerCursor.getString(routerCursor.getColumnIndex(BSSID));
                                    ssid = routerCursor.getString(routerCursor.getColumnIndex(SSID));
                                }

                                routerCursor.close();

                                accessPointInformationList.add(AccessPointInformationFactory.createInstance(bssid, rssi, ssid));
                            } while (accessPointCursor.moveToNext());
                        }

                        accessPointCursor.close();

                        signalSamples.add(new SignalSample(timestamp, accessPointInformationList, measurementId));
                    } while (fingerprintCursor.moveToNext());
                }

                fingerprintCursor.close();

                Room room = RoomFactory.createInstance(roomName, description, FingerprintFactory.createInstance(signalSamples), coordinates, picturePath, additionalInfo);
                allRooms.add(room);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return allRooms;
    }

    @SuppressLint("Range")
    @Override
    public List<AccessPointInformation> getAllAccessPoints() {
        List<AccessPointInformation> accessPointInformationList = new ArrayList<>();

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ROUTERS, null);

        if (cursor.moveToFirst()) {
            do {
                int router_id = cursor.getInt(cursor.getColumnIndex(ROUTER_ID));
                String ssid = cursor.getString(cursor.getColumnIndex(SSID));
                String bssid = cursor.getString(cursor.getColumnIndex(BSSID));

                AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(bssid, 0, ssid);

                accessPointInformationList.add(accessPointInformation);


            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return accessPointInformationList;
    }

    /**
     * Get a single room from the database by its name.
     *
     * @param roomName The name of the room to retrieve.
     * @return The room with the specified name, or null if not found.
     */
    @SuppressLint("Range")
    @Override
    public Room getRoom(String roomName) {
        Room room = null;

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ROOMS + " WHERE " + ROOM_NAME + " = ?", new String[]{roomName});

        if (cursor.moveToFirst()) {
            String description = cursor.getString(cursor.getColumnIndex(ROOM_DESCRIPTION));
            String coordinates = cursor.getString(cursor.getColumnIndex(ROOM_COORDINATES));
            String picturePath = cursor.getString(cursor.getColumnIndex(ROOM_PICTURE_PATH));
            String additionalInfo = cursor.getString(cursor.getColumnIndex(ROOM_ADDITIONAL_INFO));

            Cursor fingerprintCursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENTS + " WHERE " + ROOM_ID_FK + " = ?", new String[]{String.valueOf(roomName)});

            List<SignalSample> signalSamples = new ArrayList<>();

            if (fingerprintCursor.moveToFirst()) {
                do {
                    long timestamp = fingerprintCursor.getLong(fingerprintCursor.getColumnIndex(TIMESTAMP));

                    Cursor accessPointCursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENT_ROUTER + " WHERE " + MEASUREMENT_ID + " = ?", new String[]{String.valueOf(fingerprintCursor.getInt(fingerprintCursor.getColumnIndex(MEASUREMENT_ID)))});

                    List<AccessPointInformation> accessPointInformationList = new ArrayList<>();

                    if (accessPointCursor.moveToFirst()) {
                        do {
                            String bssid = accessPointCursor.getString(accessPointCursor.getColumnIndex(BSSID));
                            int rssi = accessPointCursor.getInt(accessPointCursor.getColumnIndex(RSSI));
                            String ssid = ""; // Assuming ssid is stored somewhere

                            accessPointInformationList.add(AccessPointInformationFactory.createInstance(bssid, rssi, ssid));
                        } while (accessPointCursor.moveToNext());
                    }

                    accessPointCursor.close();

                    signalSamples.add(new SignalSample(timestamp, accessPointInformationList));
                } while (fingerprintCursor.moveToNext());
            }

            fingerprintCursor.close();

            room = RoomFactory.createInstance(roomName, description, FingerprintFactory.createInstance(signalSamples), coordinates, picturePath, additionalInfo);
        }

        cursor.close();
        database.close();

        return room;
    }


    /**
     * Check if a node already exists
     *
     * @param nodeID the name of the node
     * @return boolean, if node exists
     */
    public boolean checkIfRoomExists(String nodeID) {
        return getRoom(nodeID) != null;
    }


    /**
     * Delete a single node
     *
     * @param room the node to be deleted
     */
    // TODO: delete edges which contain the node and clean database...
    @Override
    public void deleteRoom(Room room) {
        SQLiteDatabase database = this.getWritableDatabase();

        int rowsAffected = database.delete(TABLE_ROOMS, ROOM_NAME + " = ?", new String[]{room.getRoomName()});

        if (rowsAffected > 0) {
            Log.d("DELETE_ROOM", "Room with name " + room.getRoomName() + " deleted successfully.");
        } else {
            Log.d("DELETE_ROOM", "Room with name " + room.getRoomName() + " not found in the database.");
        }

        database.close();

        this.deleteAllUnusedMeasurements();
        this.deleteAllUnusedRouter();
    }

    @Override
    public List<Room> getAllNodesForRoom(String roomName) {
        return null;
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

        values.put(EDGE_NODE_A, edge.getNodeA().getRoomName());
        values.put(EDGE_NODE_B, edge.getNodeB().getRoomName());
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

        Log.d("DB: insert_EDGE", edge.getNodeA().getRoomName() + " " + edge.getNodeB().getRoomName());

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
            contentValues.put(EDGE_NODE_B, edge.getNodeB().getRoomName());

        } else if (nodeToBeUpdated.equals(EDGE_NODE_B)) {
            contentValues.put(EDGE_NODE_B, value);
            contentValues.put(EDGE_NODE_A, edge.getNodeA().getRoomName());
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

        database.update(EDGES_TABLE, contentValues, EDGE_NODE_A + "='" + edge.getNodeA().getRoomName() + "' AND " + EDGE_NODE_B + "='" + edge.getNodeB().getRoomName() + "'", null);

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

        database.update(EDGES_TABLE, contentValues, EDGE_NODE_A + "='" + edge.getNodeA().getRoomName() + "' AND " + EDGE_NODE_B + "='" + edge.getNodeB().getRoomName() + "'", null);

        database.close();
    }


    /**
     * Get single edge
     *
     * @param roomA the startnode of the edge
     * @param roomB the endnode of the edge
     * @return the edge
     */
    public Edge getEdge(Room roomA, Room roomB) {
        String selectQuery = "SELECT * FROM " + EDGES_TABLE + " WHERE " + EDGE_NODE_A + "='" + roomA.getRoomName() + "' AND " + EDGE_NODE_B + "='" + roomB.getRoomName() + "' OR " +
                EDGE_NODE_A + "='" + roomB.getRoomName() + "' AND " + EDGE_NODE_B + "='" + roomA.getRoomName() + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            boolean accessible = cursor.getInt(3) == 1;
            Room room1 = getRoom(cursor.getString(1));
            Room room2 = getRoom(cursor.getString(2));

            String stepListString = cursor.getString(4);
            List<String> stepList = new ArrayList<>(Arrays.asList(stepListString.split("\t")));

            Edge edge = EdgeFactory.createInstance(room1, room2, accessible, stepList, cursor.getFloat(5), cursor.getString(6));

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

                Room roomA = getRoom(cursor.getString(1));
                Room roomB = getRoom(cursor.getString(2));

                String stepListString = cursor.getString(4);
                List<String> stepList = new ArrayList<>(Arrays.asList(stepListString.split("\t")));

                Edge edge = EdgeFactory.createInstance(roomA, roomB, accessible, stepList, cursor.getFloat(5), cursor.getString(6));

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
        String selectQuery = "SELECT * FROM " + EDGES_TABLE + " WHERE " + EDGE_NODE_A + " ='" + edge.getNodeA().getRoomName() + "' AND " + EDGE_NODE_B + " ='" + edge.getNodeB().getRoomName() + "' " +
                " OR " + EDGE_NODE_A + " ='" + edge.getNodeB().getRoomName() + "' AND " + EDGE_NODE_B + " ='" + edge.getNodeA().getRoomName() + "' ";

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
        String deleteQuery = "DELETE FROM " + EDGES_TABLE + " WHERE " + EDGE_NODE_A + " ='" + edge.getNodeA().getRoomName() + "' AND " + EDGE_NODE_B + " ='" + edge.getNodeB().getRoomName() + "'"
                + " OR " + EDGE_NODE_A + " ='" + edge.getNodeB().getRoomName() + "' AND " + EDGE_NODE_B + " ='" + edge.getNodeA().getRoomName() + "' ";

        Log.d("DB: delete_EDGE", edge.getNodeA().getRoomName() + " " + edge.getNodeB().getRoomName());

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
                boolean ignored = exportFolder.mkdirs();
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

    //------------------- H E L P E R   M E T H O D S ------------------------------------------------------------

    /**
     * Delete all router entries that do not have corresponding entries in TABLE_MEASUREMENT_ROUTER.
     */
    public void deleteAllUnusedRouter() {
        SQLiteDatabase database = this.getWritableDatabase();

        try {
            database.execSQL("DELETE FROM " + TABLE_ROUTERS + " WHERE " + ROUTER_ID +
                    " NOT IN (SELECT DISTINCT " + ROUTER_ID + " FROM " + TABLE_MEASUREMENT_ROUTER + ")");
            Log.d("DELETE_UNUSED_ROUTER", "Deleted all unused router entries successfully.");
        } catch (Exception e) {
            Log.e("DELETE_UNUSED_ROUTER", "Error deleting unused router entries: " + e.getMessage());
        }

        database.close();
    }

    /**
     * Delete all measurement entries that do not have corresponding entries in TABLE_MEASUREMENT_ROUTER and TABLE_ROOMS.
     */
    public void deleteAllUnusedMeasurements() {
        SQLiteDatabase database = this.getWritableDatabase();

        try {
            database.execSQL("DELETE FROM " + TABLE_MEASUREMENTS + " WHERE " + ROOM_ID +
                    " NOT IN (SELECT DISTINCT " + ROOM_ID + " FROM " + TABLE_ROOMS + ")");
            Log.d("DELETE_UNUSED_MEASUREMENTS", "Deleted all unused measurement entries successfully.");
        } catch (Exception e) {
            Log.e("DELETE_UNUSED_MEASUREMENTS", "Error deleting unused measurement entries: " + e.getMessage());
        }

        try {
            database.execSQL("DELETE FROM " + TABLE_MEASUREMENT_ROUTER + " WHERE " + MEASUREMENT_ID +
                    " NOT IN (SELECT DISTINCT " + MEASUREMENT_ID + " FROM " + TABLE_MEASUREMENTS + ")");
            Log.d("DELETE_UNUSED_MEASUREMENTS", "Deleted all unused measurement entries successfully.");
        } catch (Exception e) {
            Log.e("DELETE_UNUSED_MEASUREMENTS", "Error deleting unused measurement entries: " + e.getMessage());
        }

        database.close();
    }

    @SuppressLint("Range")
    @Override
    public List<SignalSample> getAllMeasurementsForRoom(String roomName) {
        List<SignalSample> signalSamples = new ArrayList<>();

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ROOMS + " WHERE " + ROOM_NAME + " = ?", new String[]{roomName});

        if (cursor.moveToFirst()) {
            Log.d("GET_MEASUREMENTS", "Room found with name " + roomName);
            int roomId = cursor.getInt(cursor.getColumnIndex(ROOM_ID));

            Cursor fingerprintCursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENTS + " WHERE " + ROOM_ID_FK + " = ?", new String[]{String.valueOf(roomId)});


            if (fingerprintCursor.moveToFirst()) {
                Log.d("GET_MEASUREMENTS", "Fingerprint found for room " + roomName);
                do {
                    long timestamp = fingerprintCursor.getLong(fingerprintCursor.getColumnIndex(TIMESTAMP));

                    int measurementId = fingerprintCursor.getInt(fingerprintCursor.getColumnIndex(MEASUREMENT_ID));

                    Cursor accessPointCursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENT_ROUTER + " WHERE " + MEASUREMENT_ID + " = ?", new String[]{String.valueOf(measurementId)});

                    List<AccessPointInformation> accessPointInformationList = new ArrayList<>();

                    if (accessPointCursor.moveToFirst()) {
                        do {
                            String bssid = "";
                            int rssi = accessPointCursor.getInt(accessPointCursor.getColumnIndex(RSSI));
                            String ssid = ""; // Assuming ssid is stored somewhere
                            int routerId = accessPointCursor.getInt(accessPointCursor.getColumnIndex(ROUTER_ID));

                            Cursor routerCursor = database.rawQuery("SELECT * FROM " + TABLE_ROUTERS + " WHERE " + ROUTER_ID + " = ?", new String[]{String.valueOf(routerId)});

                            if (routerCursor.moveToFirst()) {
                                bssid = routerCursor.getString(routerCursor.getColumnIndex(BSSID));
                                ssid = routerCursor.getString(routerCursor.getColumnIndex(SSID));
                            }

                            routerCursor.close();

                            accessPointInformationList.add(AccessPointInformationFactory.createInstance(bssid, rssi, ssid));
                        } while (accessPointCursor.moveToNext());
                    }

                    accessPointCursor.close();

                    signalSamples.add(new SignalSample(timestamp, accessPointInformationList, measurementId));
                } while (fingerprintCursor.moveToNext());
            }

            fingerprintCursor.close();
        }

        cursor.close();
        database.close();

        return signalSamples;
    }

    @SuppressLint("Range")
    @Override
    public List<AccessPointInformation> getAccessPointInformationForMeasurement(int measurementID) {
        List<AccessPointInformation> accessPointInformations = new ArrayList<>();

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENT_ROUTER + " WHERE " + MEASUREMENT_ID + " = ?", new String[]{String.valueOf(measurementID)});

        if (cursor.moveToFirst()) {
            do {

                int routerId = cursor.getInt(cursor.getColumnIndex(ROUTER_ID));
                int rssi = cursor.getInt(cursor.getColumnIndex(RSSI));

                Cursor routerCursor = database.rawQuery("SELECT * FROM " + TABLE_ROUTERS + " WHERE " + ROUTER_ID + " = ?", new String[]{String.valueOf(routerId)});

                if (routerCursor.moveToFirst()) {
                    String bssid = routerCursor.getString(routerCursor.getColumnIndex(BSSID));
                    String ssid = routerCursor.getString(routerCursor.getColumnIndex(SSID));
                    accessPointInformations.add(AccessPointInformationFactory.createInstance(bssid, rssi, ssid));
                }

                routerCursor.close();

            } while (cursor.moveToNext());
            cursor.close();
        }

        database.close();
        return accessPointInformations;
    }

    @Override
    public void deleteMeasurement(int measurementID) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_MEASUREMENTS, MEASUREMENT_ID + " = ?", new String[]{String.valueOf(measurementID)});
        database.close();
        this.deleteAllUnusedMeasurements();
        this.deleteAllUnusedRouter();
    }

    @SuppressLint("Range")
    @Override
    public JSONArray getAllMeasurementsInJSON() {
        SQLiteDatabase database = this.getReadableDatabase();
        JSONArray measurementsArray = new JSONArray();

        Cursor measurementCursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENTS, null);
        if (measurementCursor.moveToFirst()) {
            do {
                long measurementId = measurementCursor.getLong(measurementCursor.getColumnIndex(MEASUREMENT_ID));
                String deviceID = measurementCursor.getString(measurementCursor.getColumnIndex(DEVICE_ID));
                String timestamp = measurementCursor.getString(measurementCursor.getColumnIndex(TIMESTAMP));
                String roomName = getRoomName(database, measurementId);

                Cursor routerCursor = database.rawQuery("SELECT * FROM " + TABLE_MEASUREMENT_ROUTER +
                        " INNER JOIN " + TABLE_ROUTERS + " ON " + TABLE_MEASUREMENT_ROUTER + "." + ROUTER_ID +
                        " = " + TABLE_ROUTERS + "." + ROUTER_ID +
                        " WHERE " + MEASUREMENT_ID + " = ?", new String[]{String.valueOf(measurementId)});

                JSONArray scanResultsArray = new JSONArray();

                if (routerCursor.moveToFirst()) {
                    do {
                        String ssid = routerCursor.getString(routerCursor.getColumnIndex(SSID));
                        String bssid = routerCursor.getString(routerCursor.getColumnIndex(BSSID));
                        int level = routerCursor.getInt(routerCursor.getColumnIndex(RSSI));

                        JSONObject scanResultObject = new JSONObject();
                        try {
                            // SSID -> ssid
                            // BSSID -> bssid
                            // Level -> signal_strength
                            scanResultObject.put("ssid", ssid);
                            scanResultObject.put("bssid", bssid);
                            scanResultObject.put("signal_strength", level);
                        } catch (JSONException e) {
                            Log.d(DATABASE_HANDLER_IMPL, "Error creating JSON object for scan result.");
                            Log.e(DATABASE_HANDLER_IMPL, Objects.requireNonNull(e.getMessage()));
                        }
                        scanResultsArray.put(scanResultObject);
                    } while (routerCursor.moveToNext());
                }

                routerCursor.close();

                JSONObject measurementObject = new JSONObject();
                try {
                    // Room -> room_name
                    // Timestamp -> timestamp
                    // DeviceID -> device_id
                    // ScanResults -> routers

                    if (scanResultsArray.length() != 0) {
                        measurementObject.put("room_name", roomName);
                        measurementObject.put("timestamp", timestamp);
//                        measurementObject.put("timestamp", LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(timestamp)), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        measurementObject.put("device_id", deviceID);
                        measurementObject.put("routers", scanResultsArray);
                    }


                } catch (JSONException e) {
                    Log.d(DATABASE_HANDLER_IMPL, "Error creating JSON object for measurement.");
                    Log.e(DATABASE_HANDLER_IMPL, Objects.requireNonNull(e.getMessage()));
                }
                measurementsArray.put(measurementObject);
            } while (measurementCursor.moveToNext());
        }

        measurementCursor.close();
        database.close();

        return measurementsArray;
    }

    @SuppressLint("Range")
    private String getRoomName(SQLiteDatabase database, long measurementId) {
        Cursor roomCursor = database.rawQuery("SELECT " + ROOM_NAME + " FROM " + TABLE_ROOMS +
                " WHERE " + ROOM_ID + " = (SELECT " + ROOM_ID_FK + " FROM " + TABLE_MEASUREMENTS +
                " WHERE " + MEASUREMENT_ID + " = ?)", new String[]{String.valueOf(measurementId)});
        String roomName = "";
        if (roomCursor.moveToFirst()) {
            roomName = roomCursor.getString(roomCursor.getColumnIndex(ROOM_NAME));
        }
        roomCursor.close();
        return roomName;
    }

}

