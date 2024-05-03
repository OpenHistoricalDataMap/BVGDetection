package de.htwberlin.f4.ai.ma.indoorroutefinder.node;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;

/**
 * Created by Johann Winter
 */
class RoomImpl implements Room {

    private final String description;
    private final Fingerprint fingerprint;
    private final String picturePath;
    private String roomName;
    private String coordinates;
    private String additionalInfo;
    private int roomDatabaseID;


    RoomImpl(String roomName, String description, Fingerprint fingerprint, String coordinates, String picturePath, String additionalInfo) {
        this.roomName = roomName;
        this.description = description;
        this.coordinates = coordinates;
        this.picturePath = picturePath;
        this.fingerprint = fingerprint;
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String getRoomName() {
        return this.roomName;
    }

    @Override
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public Fingerprint getFingerprint() {
        return this.fingerprint;
    }

    @Override
    public String getCoordinates() {
        return this.coordinates;
    }

    @Override
    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String getPicturePath() {
        return this.picturePath;
    }

    @Override
    public String getAdditionalInfo() {
        return this.additionalInfo;
    }

    @Override
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public int getRoomDatabaseID() {
        return this.roomDatabaseID;
    }

    @Override
    public void setRoomDatabaseID(int roomDatabaseID) {
        this.roomDatabaseID = roomDatabaseID;
    }

}