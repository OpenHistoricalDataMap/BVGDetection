package de.htwberlin.f4.ai.ma.indoorroutefinder.node;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;

/**
 * Created by Johann Winter
 */
class NodeImpl implements Node {

    private String id;
    private final String description;
    private final Fingerprint fingerprint;
    private String coordinates;
    private final String picturePath;
    private String additionalInfo;


    NodeImpl(String id, String description, Fingerprint fingerprint, String coordinates, String picturePath, String additionalInfo) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
        this.picturePath = picturePath;
        this.fingerprint = fingerprint;
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
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

}