package de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

/**
 * Created by Johann Winter
 * <p>
 * Thanks to tognitos.
 * <p>
 * The Dijkstra algorithm maps the common node and edge objects to its own DijkstraNode and
 * DijkstraEdge objects in order to avoid loading the model objects to the algorithm's logic.
 * <p>
 * USE:
 * For regular Dijkstra calculations you only need to run two methods:
 * 1- execute() method by passing the ID (name) of the start-node of the wanted route as parameter
 * 2- after that, run getPath() by passing the ID (name) of the destination-node as parameter,
 * to get the route.
 */

public interface DijkstraAlgorithm {

    /**
     * Map the normal node objects from the model to the custom DijkstraNode of the Dijkstra Algorithm.
     *
     * @param nodes a list of nodes
     * @return a list of DijkstraNodes
     */
    List<DijkstraNode> mapNodes(List<Node> nodes);

    /**
     * Map the normal edge objects from the model to the custom DijkstraEdge of the Dijkstra Algorithm.
     * Since the graph is bidirectional, it maps 2 DijkstraEdges: one for nodeA->nodeB, one for nodeB->nodeA.
     *
     * @param edges a list of edges
     * @return a list of DijkstraEdges
     */
    List<DijkstraEdge> mapEdges(List<Edge> edges);

    /**
     * Executes all the calculations and calculates the shortest paths from the specified start node.
     *
     * @param sourceNodeId the start node for the calculations
     * @throws IllegalArgumentException if the source node does not exist
     */
    void execute(String sourceNodeId) throws IllegalArgumentException;

    /**
     * Find the minimal distances from a DijkstraNode to all other DijkstraNodes
     *
     * @param node the start node
     */
    void findMinimalDistances(DijkstraNode node);

    /**
     * Get the distance between the two DijkstraNodes
     *
     * @param node   the start-node
     * @param target the target-node
     * @return the weight (distance in meters) between start-node and target-node
     */
    double getDistance(DijkstraNode node, DijkstraNode target);

    /**
     * Get all the neighbours (directly connected DijkstraNodes) for the specified node.
     *
     * @param node the specified node
     * @return list of neighbor DijkstraNodes
     */
    List<DijkstraNode> getNeighbors(DijkstraNode node);

    /**
     * Get the nearest of all the DijkstraNodes for a specified DijkstraNode.
     *
     * @param dijkstraNodes a set of DijkstraNodes
     * @return the nearest DijkstraNode
     */
    DijkstraNode getMinimumDistance(Set<DijkstraNode> dijkstraNodes);

    /**
     * Check if the algorithm already ran through a specified DijkstraNode (is settled)
     *
     * @param dijkstraNode the specified DijkstraNode
     * @return true if the DijkstraNode was already settled
     */
    boolean isSettled(DijkstraNode dijkstraNode);

    /**
     * Gets the shortest distance to the destination, from the calculated start-node (called through
     * the method "execute").
     *
     * @param destinationNode the destination-DijkstraNode
     * @return the shortest distance (in meters)
     */
    double getShortestDistance(DijkstraNode destinationNode);

    /**
     * This method returns the path as a list of node IDs from the source node
     * (which was parameter of the method "execute") to the selected target.
     * Returns NULL if no path exists.
     *
     * @param targetNodeID the ID of the target Node
     */
    LinkedList<String> getPath(String targetNodeID);

}
