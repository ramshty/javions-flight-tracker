package javions;

import javions.Math2;
import javions.Units;

/**
 * contains (static) methods to project geographical coordinates according to the WebMercator projection
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class WebMercator {
    /**
     * The purpose of this private constructor is to make it impossible to create instances of the class
     */
    private WebMercator() {
    }

    /**
     * @param zoomLevel the given zoom level
     * @param longitude the given longitude
     * @return returns the x coordinate corresponding to the given longitude (in radians) at the given zoom level
     */

    public static double x(int zoomLevel, double longitude) {
        return ((1 << (8 + zoomLevel)) * (Units.convertTo(longitude, Units.Angle.TURN) + 1 / 2.0));
    }

    /**
     * @param zoomLevel the given zoom level
     * @param latitude  the given latitude
     * @return returns the y coordinate corresponding to the given latitude (in radians) at the given zoom level
     */
    public static double y(int zoomLevel, double latitude) {
        return ((1 << (8 + zoomLevel)) * ((Units.convertTo(-Math2.asinh(Math.tan(latitude)), Units.Angle.TURN)) + 1 / 2.0));
    }
}
