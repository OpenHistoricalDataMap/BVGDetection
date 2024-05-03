package de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra;

/**
 * Created by Johann Winter
 */
class DijkstraEdge {

    private final DijkstraNode source;
    private final DijkstraNode destination;
    private final double weight;

    DijkstraEdge(DijkstraNode source, DijkstraNode destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    /**
     * Getter for the destination DijkstraNode
     *
     * @return the destination DijkstraNode
     */
    DijkstraNode getDestination() {
        return destination;
    }

    /**
     * Getter for the source DijkstraNode
     *
     * @return the source DijkstraNode
     */
    DijkstraNode getSource() {
        return source;
    }

    /**
     * Getter for the weight (length) of the DijkstraEdge
     *
     * @return the weight (length) of the DijkstraEdge
     */
    double getWeight() {
        return weight;
    }

    /**
     * Overrides standard toString() method
     *
     * @return the source and destination DijkstraNode
     */
    @Override
    public String toString() {
        return source + " " + destination;
    }

}