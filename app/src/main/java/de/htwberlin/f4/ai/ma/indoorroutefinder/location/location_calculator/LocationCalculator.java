package de.htwberlin.f4.ai.ma.indoorroutefinder.location.location_calculator;

import com.google.common.collect.Multimap;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.location.calculations.RestructedNode;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;

/**
 * Created by Johann Winter
 * <p>
 * This interface if for calculating a node from a given fingerprint.
 * It is used to locate the user.
 */
public interface LocationCalculator {

    /**
     * Calculate a node from a given fingerprint
     *
     * @param fingerprint the input fingerprint to be compared with all existent nodes to get the position
     * @return the ID (name) of the resulting node
     */
    String calculateNodeId(Fingerprint fingerprint);

    /**
     * Get a list of AccessPointInformation by passing a list of SignalSample (unwrap).
     *
     * @param signalSampleList a list of SignalSamples
     * @return a list of AccesspointInformations
     */
    List<AccessPointInformation> getSignalStrengths(List<SignalSample> signalSampleList);


    /**
     * Rewrite the nodelist to restrucetd nodes and delete weak MAC addresses
     *
     * @param allRooms list of all nodes
     * @return a list of restructed nodes
     */
    List<RestructedNode> calculateNewNodeDataset(List<Room> allRooms);


    /**
     * Create a multimap with MAC address and signal strength values.
     *
     * @param room        input node
     * @param macAdresses a list of MAC addresses
     * @return a multimap with mac address and signal strengths
     */
    Multimap<String, Double> getMultiMap(Room room, List<String> macAdresses);


    /**
     * Get (extract) all MAC-addresses from a specific node
     *
     * @param room the node
     * @return list of unique MAC addresses
     */
    List<String> getMacAddresses(Room room);

}