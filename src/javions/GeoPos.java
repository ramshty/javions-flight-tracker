package javions;

/**
 * represents geographic coordinates, i.e. a longitude/latitude couple. These coordinates are expressed in t32
 * and stored as 32-bit integers (type int).
 *
 * @param longitudeT32 longitude expressed in T32
 * @param latitudeT32  latitude expressed in T32
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */

public record GeoPos(int longitudeT32, int latitudeT32) {

    /**
     * the maximum latitude
     */
    private static final int MAX_LATITUDE = 1 << 30;

    /**
     * the minimum latitude
     */
    private static final int MIN_LATITUDE = -(1 << 30);

    /**
     * @param latitudeT32 the input value
     * @return returns true iff the passed value, interpreted as a latitude expressed in t32, is valid, ie. between -230
     * (inclusive, and which corresponds to -90°) and 230 (inclusive, and which corresponds to +90°).
     */

    public static boolean isValidLatitudeT32(int latitudeT32) {
        return (latitudeT32 >= MIN_LATITUDE) && (latitudeT32 <= MAX_LATITUDE);
    }

    /**
     * validates the received latitude and throws IllegalArgumentException if it is invalid
     *
     * @throws IllegalArgumentException if the received latitude is invalid
     */
    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * @return returns the longitude in radians
     */

    public double longitude() {
        return (Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.RADIAN));
    }

    /**
     * @return returns the longitude in radians
     */
    public double latitude() {
        return (Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.RADIAN));
    }

    /**
     * @return returns a textual representation of the position in which longitude and latitude are given in that
     * order, in degrees
     */
    public String toString() {
        double longitude = Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE);
        double latitude = Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE);
        return "(" + longitude + "°, " + latitude + "°)";
    }

}
