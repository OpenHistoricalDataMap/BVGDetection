package de.htwberlin.f4.ai.ma.indoorroutefinder.edge;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;

/**
 * Created by Johann Winter
 * <p>
 * Factory for creating edge objects. There are construction methods, the first for only an edge skeleton,
 * and an other for a full edge. The skeleton is used e.g. for the EdgesManagerActivity,
 * where no step data is recorded.
 */

public class EdgeFactory {

    public static Edge createInstance(Room roomA, Room roomB, boolean accessible, float weight) {
        return new EdgeImpl(roomA, roomB, accessible, weight);
    }

    public static Edge createInstance(Room roomA, Room roomB, boolean accessible, List<String> stepCoordList, float weight, String additionalInfo) {
        return new EdgeImpl(roomA, roomB, accessible, stepCoordList, weight, additionalInfo);
    }
}
