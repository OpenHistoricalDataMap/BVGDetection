package de.htwberlin.f4.ai.ma.indoorroutefinder.dijkstra;

import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;

/**
 * Created by Johann Winter
 */

class DijkstraNode {

    private final String id;

    DijkstraNode(Room room) {
        this.id = room.getRoomName();
    }

    /**
     * Getter for the ID
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }


    /**
     * Override hashCode method of the class "Object"
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }


    /**
     * @param object the input object
     * @return boolean if the object equals an other object
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;
        DijkstraNode other = (DijkstraNode) object;
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }


    /**
     * Overrides standard toString() method
     *
     * @return the id of the DijkstraNode
     */
    @Override
    public String toString() {
        return id;
    }

}