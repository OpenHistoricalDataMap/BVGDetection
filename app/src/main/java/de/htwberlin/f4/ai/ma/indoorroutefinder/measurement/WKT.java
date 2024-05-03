package de.htwberlin.f4.ai.ma.indoorroutefinder.measurement;

/**
 * WKT Class
 * <p>
 * Used to create / transform coordinates <-> WKT String
 */
public class WKT {

    public static final String POINT_Z = "POINT Z";


    /**
     * convert coordinates to wkt string.
     * expects 3 coordinate values
     *
     * @param coordinates coordinates
     * @return wkt string with coordinates
     */
    public static String coordToStr(float[] coordinates) {
        String result = null;

        if (coordinates.length == 3) {
            result = POINT_Z + "(" + coordinates[0] + " " + coordinates[1] + " " + coordinates[2] + ")";
        }

        return result;
    }


    /**
     * Convert wkt string to float[]
     * <p>
     * expects a string containing 3 coordinate values
     * for example: POINT Z(1.0 2.0 3.0)
     *
     * @param coordinatesStr wkt string with coordinates
     * @return coordinates in float[]
     */
    public static float[] strToCoord(String coordinatesStr) {
        float[] coordinates = null;
        if (!coordinatesStr.contains(POINT_Z)) {
            return null;
        }

        String subStr = coordinatesStr.substring(coordinatesStr.indexOf("(") + 1, coordinatesStr.indexOf(")"));
        String[] splitted = subStr.split(" ");

        if (splitted.length == 3) {
            coordinates = new float[3];
            coordinates[0] = Float.parseFloat(splitted[0]);
            coordinates[1] = Float.parseFloat(splitted[1]);
            coordinates[2] = Float.parseFloat(splitted[2]);
        }

        return coordinates;
    }
}
