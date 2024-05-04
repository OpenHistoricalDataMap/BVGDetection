package de.htwberlin.f4.ai.ma.indoorroutefinder.room;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;

/**
 * Created by Johann Winter
 * <p>
 * Factory for creating node objects
 */
public class RoomFactory {

    public static Room createInstance(String roomName, String description, Fingerprint fingerprint, String coordinates, String picturePath, String additionalInfo) {
        return new RoomImpl(roomName, description, fingerprint, coordinates, picturePath, additionalInfo);
    }

    // TODO: Just use this method in the future
    public static Room createInstance(String roomName, String description, Fingerprint fingerprint, String coordinates, String picturePath, String additionalInfo, int roomDatabaseID) {
        return new RoomImpl(roomName, description, fingerprint, coordinates, picturePath, additionalInfo, roomDatabaseID);
    }
}
