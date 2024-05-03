package de.htwberlin.f4.ai.ma.indoorroutefinder.edge;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Room;

/**
 * Created by Johann Winter
 */

class EdgeImpl implements Edge {

    private final Room roomA;
    private final Room roomB;
    private final List<String> stepCoordList;
    private boolean accessible;
    private float weight;
    private String additionalInfo;


    // Constructor for an Edge without given stepCoordList
    EdgeImpl(Room roomA, Room roomB, boolean accessible, float weight) {
        this.roomA = roomA;
        this.roomB = roomB;
        this.accessible = accessible;
        this.weight = weight;
        this.stepCoordList = new ArrayList<>();
    }

    // Constructor for an Edge with given stepCoordList
    EdgeImpl(Room roomA, Room roomB, boolean accessible, List<String> stepCoordList, float weight, String additionalInfo) {
        this.roomA = roomA;
        this.roomB = roomB;
        this.accessible = accessible;
        this.weight = weight;
        this.stepCoordList = stepCoordList;
        this.additionalInfo = additionalInfo;
    }


    @Override
    public Room getNodeA() {
        return this.roomA;
    }

    @Override
    public Room getNodeB() {
        return this.roomB;
    }

    @Override
    public boolean getAccessibility() {
        return this.accessible;
    }

    @Override
    public void setAccessibility(boolean accessibly) {
        this.accessible = accessibly;
    }

    @Override
    public float getWeight() {
        return this.weight;
    }

    @Override
    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public List<String> getStepCoordsList() {
        return this.stepCoordList;
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
