package de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;


/**
 * Created by Johann Winter
 * <p>
 * Thanks to tognitos.
 * <p>
 * The Dijkstra algorithm maps the common node and edge objects to its own DijkstraNode and
 * DijkstraEdge objects in order to avoid loading the model objects to the algorithm's logic.
 */

class DijkstraAlgorithmImpl implements DijkstraAlgorithm {

    // DijkstraNodes and DijkstraEdges
    final List<DijkstraNode> dijkstraNodes;
    private final List<DijkstraEdge> dijkstraEdges;
    private final DatabaseHandler databaseHandler;
    private final boolean accessible;
    // Settled and unsettled dijkstraNodes (seen / not seen)
    private Set<DijkstraNode> settledNodes;
    private Set<DijkstraNode> unSettledNodes;
    // (Smallest) predecessors
    private Map<DijkstraNode, DijkstraNode> predecessors;
    // Smallest distance (weight) for the node
    private Map<DijkstraNode, Double> distance;


    DijkstraAlgorithmImpl(Context context, boolean accessible) {
        databaseHandler = DatabaseHandlerFactory.getInstance(context);
        this.accessible = accessible;
        this.dijkstraNodes = mapNodes(databaseHandler.getAllRooms());
        this.dijkstraEdges = mapEdges(databaseHandler.getAllEdges());
    }


    /**
     * Map the normal node objects from the Model to the custom DijkstraNode of the Dijkstra Algorithm.
     *
     * @param rooms a list of Nodes
     * @return a list of DijkstraNodes
     */
    public List<DijkstraNode> mapNodes(List<Room> rooms) {
        List<DijkstraNode> dijkstraNodes = new ArrayList<>(rooms.size());
        for (Room room : rooms) {
            dijkstraNodes.add(new DijkstraNode(room));
        }
        return dijkstraNodes;
    }


    /**
     * Map the normal Edge objects from the Model to the custom DijkstraEdge of the Dijkstra
     * algorithm. Since the graph is bidirectional, it maps 2 DijkstraEdges: one for nodeA->nodeB, one for nodeB->nodeA.
     *
     * @param edges a list of edges
     * @return a list of DijkstraEdges
     */
    public List<DijkstraEdge> mapEdges(List<Edge> edges) {
        List<DijkstraEdge> dijkstraEdges = new ArrayList<>();

        for (Edge edge : edges) {

            // Because it is an undirected graph, add both directions
            DijkstraNode source = new DijkstraNode(edge.getNodeA());
            DijkstraNode destination = new DijkstraNode(edge.getNodeB());

            // If accessible search wanted
            if (accessible) {
                if (edge.getAccessibility()) {
                    DijkstraEdge sourceToDestination = new DijkstraEdge(source, destination, edge.getWeight());
                    DijkstraEdge destinationToSource = new DijkstraEdge(destination, source, edge.getWeight());
                    dijkstraEdges.add(sourceToDestination);
                    dijkstraEdges.add(destinationToSource);
                }
                // If accessibility doesn't play a role
            } else {
                DijkstraEdge sourceToDestination = new DijkstraEdge(source, destination, edge.getWeight());
                DijkstraEdge destinationToSource = new DijkstraEdge(destination, source, edge.getWeight());
                dijkstraEdges.add(sourceToDestination);
                dijkstraEdges.add(destinationToSource);
            }
        }
        return dijkstraEdges;
    }


    /**
     * Executes all the calculations and the shortest paths from the specified source node.
     *
     * @param sourceNodeId the startnode for the calculations
     * @throws IllegalArgumentException if the source node does not exist
     */
    public void execute(String sourceNodeId) throws IllegalArgumentException {
        final Room source = databaseHandler.getRoom(sourceNodeId);

        if (source == null) {
            throw new IllegalArgumentException("Source Node Id is invalid! Given was:" + sourceNodeId);
        }
        final DijkstraNode sourceNode = new DijkstraNode(databaseHandler.getRoom(sourceNodeId));
        settledNodes = new HashSet<>();
        unSettledNodes = new HashSet<>();
        distance = new HashMap<>();
        predecessors = new HashMap<>();
        distance.put(sourceNode, 0.0);
        unSettledNodes.add(sourceNode);
        while (!unSettledNodes.isEmpty()) {
            DijkstraNode node = getMinimumDistance(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);

            Log.d("execute", "node=" + node.getId());
            findMinimalDistances(node);
        }
    }


    /**
     * Find the minimal distances from a DijkstraNode to all other DijkstraNodes
     *
     * @param node the startnode
     */
    public void findMinimalDistances(DijkstraNode node) {
        List<DijkstraNode> adjacentNodes = getNeighbors(node);
        for (DijkstraNode target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node) + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }
    }


    /**
     * Get the distance between the two DijkstraNodes
     *
     * @param node   the start-node
     * @param target the target-node
     * @return the weight (distance) between start-node and target-node
     */
    public double getDistance(DijkstraNode node, DijkstraNode target) {

        for (DijkstraEdge dijkstraEdge : dijkstraEdges) {
            if (dijkstraEdge.getSource().getId().equals(node.getId()) && dijkstraEdge.getDestination().getId().equals(target.getId())) {
                return dijkstraEdge.getWeight();
            }
        }
        throw new RuntimeException("Should not happen");
    }


    /**
     * Get all the neighbours (directly connected DijkstraNodes) for the specified node.
     *
     * @param node the specified node
     * @return list of neighbor nodes
     */
    public List<DijkstraNode> getNeighbors(DijkstraNode node) {
        List<DijkstraNode> neighbors = new ArrayList<>();
        for (DijkstraEdge dijkstraEdge : dijkstraEdges) {
            if (dijkstraEdge.getSource().equals(node) && !isSettled(dijkstraEdge.getDestination())) {
                neighbors.add(dijkstraEdge.getDestination());
            }
        }
        Log.d("getNeighbors", neighbors.toString());
        return neighbors;
    }


    /**
     * Get the minimum shortest distance of all the DijkstraNodes.
     *
     * @param dijkstraNodes a set of DijkstraNodes
     * @return the nearest DijkstraNode
     */
    public DijkstraNode getMinimumDistance(Set<DijkstraNode> dijkstraNodes) {
        DijkstraNode nearestDijkstraNode = null;
        for (DijkstraNode dijkstraNode : dijkstraNodes) {
            if (nearestDijkstraNode == null) {
                nearestDijkstraNode = dijkstraNode;
            } else {
                if (getShortestDistance(dijkstraNode) < getShortestDistance(nearestDijkstraNode)) {
                    nearestDijkstraNode = dijkstraNode;
                }
            }
        }
        return nearestDijkstraNode;
    }


    /**
     * @param dijkstraNode the input DijkstraNode
     * @return true if the DijkstraNode was already settled
     */
    public boolean isSettled(DijkstraNode dijkstraNode) {
        return settledNodes.contains(dijkstraNode);
    }


    /**
     * Gets the shortest distance to the destination, from the calculated start-node (called through
     * the method execute).
     *
     * @param destinationNode the destination-node
     * @return the shortest distance
     */
    public double getShortestDistance(DijkstraNode destinationNode) {
        Double d = distance.get(destinationNode);
        if (d == null) {
            return Double.MAX_VALUE;
        } else {
            return d;
        }
    }


    /**
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     *
     * @param targetNodeID the ID of the target Node
     */
    public LinkedList<String> getPath(String targetNodeID) {
        LinkedList<String> path = new LinkedList<>();
        DijkstraNode step = new DijkstraNode(databaseHandler.getRoom(targetNodeID));

        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step.getId());
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step.getId());
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }
}