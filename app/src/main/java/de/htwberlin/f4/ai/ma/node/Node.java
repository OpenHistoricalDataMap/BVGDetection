package de.htwberlin.f4.ai.ma.node;

import java.util.List;

// TODO: Interface beschreiben

public interface Node {

    String getId();
    void setId(String id);

    String getDescription();
    void setDescription(String description);

    Fingerprint getFingerprint();
    void setFingerprint(Fingerprint fingerprint);

    //List<SignalInformation> getSignalInformationList();
    //void setSignalInformationList(List<SignalInformation> signalInformationList);

    String getCoordinates();
    void setCoordinates(String coordinates);

    String getPicturePath();
    void setPicturePath(String picturePath);

    String getAdditionalInfo();
    void setAdditionalInfo(String additionalInfo);

}

