package de.htwberlin.f4.ai.ma.indoorroutefinder.room;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;

/**
 * Created by Johann Winter
 * <p>
 * Factory for creating node objects
 */
public class RoomFactory {

    public static Room createInstance(String id, String description, Fingerprint fingerprint, String coordinates, String picturePath, String additionalInfo) {
        return new RoomImpl(id, description, fingerprint, coordinates, picturePath, additionalInfo);
    }
}
