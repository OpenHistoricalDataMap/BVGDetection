package de.htwberlin.f4.ai.ma.indoorroutefinder.node;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;

/**
 * Created by Johann Winter
 * <p>
 * Factory for creating node objects
 */
public class NodeFactory {

    public static Node createInstance(String id, String description, Fingerprint fingerprint, String coordinates, String picturePath, String additionalInfo) {
        return new NodeImpl(id, description, fingerprint, coordinates, picturePath, additionalInfo);
    }
}
