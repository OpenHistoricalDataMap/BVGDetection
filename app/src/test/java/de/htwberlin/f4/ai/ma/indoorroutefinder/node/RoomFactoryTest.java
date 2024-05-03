package de.htwberlin.f4.ai.ma.indoorroutefinder.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintFactory;

/**
 * Created by Johann Winter
 */
public class RoomFactoryTest {

    /**
     * Test for the successful creation of a Node
     */
    @Test
    public void createInstance() throws Exception {

        String testNodeID = "TestNode";
        String testDescription = "TestDescription";
        Fingerprint testFingerprint = FingerprintFactory.createInstance("testWifi", null);
        String testCoordinates = "testCoordinates";
        String testPicturePath = "/test/test.jpg";
        String testAdditionalInfo = "-PLACEHOLDER-";

        Room input = new RoomImpl(testNodeID, testDescription, testFingerprint, testCoordinates, testPicturePath, testAdditionalInfo);
        Room output;

        output = RoomFactory.createInstance(testNodeID, testDescription, testFingerprint, testCoordinates, testPicturePath, testAdditionalInfo);

        assertEquals(input.getRoomName(), output.getRoomName());
        assertEquals(input.getDescription(), output.getDescription());
        assertEquals(input.getFingerprint(), output.getFingerprint());
        assertEquals(input.getCoordinates(), output.getCoordinates());
        assertEquals(input.getPicturePath(), output.getPicturePath());
        assertEquals(input.getAdditionalInfo(), output.getAdditionalInfo());

    }
}