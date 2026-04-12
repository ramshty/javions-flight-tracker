package javions.adsb;

import javions.GeoPos;
import javions.Preconditions;
import javions.Units;

import static javions.Units.Angle.TURN;

/**
 * represents a CPR position decoder
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class CprDecoder {

    private static final int EVEN_PARITY = 0;
    private static final int ODD_PARITY = 1;
    private static final double LAT_ODD_ZONE = 59;
    private static final double LAT_EVEN_ZONE = 60;
    private static final double WIDTH_LAT_ODD_ZONE = 1d / LAT_ODD_ZONE;
    private static final double WIDTH_LAT_EVEN_ZONE = 1d / LAT_EVEN_ZONE;

    /**
     * returns the geographical position corresponding to the given normalized local positions—x0 and
     * y0 being the local longitude and latitude of an even message, x1 and y1 those of an odd
     * message—knowing that the most recent positions are those of index
     *
     * @param x0         the even longitude normalized
     * @param y0         the even latitude normalized
     * @param x1         the odd longitude normalized
     * @param y1         the odd longitude normalized
     * @param mostRecent the most recent message received
     * @return returns the geographical position corresponding to the given normalized local positions—x0
     * and y0 being the local longitude and latitude of an even message, x1 and y1 those of an odd message—knowing
     * that the most recent positions are those of index
     * @throws IllegalArgumentException if mostRecent is not 0 or 1
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == ODD_PARITY || mostRecent == EVEN_PARITY);
        // The component z sub phi which is given to the even and odd latitude zone if its value
        // is greater or equal to one
        double actualLatitudeZone = Math.rint(y0 * LAT_ODD_ZONE - y1 * LAT_EVEN_ZONE);
        double actualOddLatitudeZone = computeActualZone(LAT_ODD_ZONE, actualLatitudeZone);
        double actualEvenLatitudeZone = computeActualZone(LAT_EVEN_ZONE, actualLatitudeZone);
        double evenLatitude = convertToNegativeEquivalent(WIDTH_LAT_EVEN_ZONE * (actualEvenLatitudeZone + y0));
        double oddLatitude = convertToNegativeEquivalent(WIDTH_LAT_ODD_ZONE * (actualOddLatitudeZone + y1));

        if (verifyIfLatitudeIsInTheBound(latitudeInDegree(evenLatitude), latitudeInDegree(oddLatitude))) return null;

        // The variables without the 1 are the areas calculated with the even value of the latitude,
        // those with the suffix 1 are those calculated with the odd value of the latitude
        double longEvenZone, longEvenZone1, longOddZone, longOddZone1;
        double valueAForLongZoneEven = valueAForLongZone(evenLatitude);
        double valueAForLongZoneOdd = valueAForLongZone(oddLatitude);
        // Check if value A which is used to compute the number of longitude zones is greater than 1 and hence a NaN
        if (isAGreaterThan1(valueAForLongZoneEven, valueAForLongZoneOdd,mostRecent)) {
            if (mostRecent == EVEN_PARITY) return position(x0, evenLatitude);
            else return position(x1, oddLatitude);
        } else {
            // We compute the even and the odd longitude zone with the odd and the even latitude value
            longEvenZone1 = Math.floor((TURN) / valueAForLongZoneOdd);
            longEvenZone = Math.floor((TURN) / valueAForLongZoneEven);
            longOddZone = longEvenZone - 1;
            longOddZone1 = longEvenZone1 - 1;

            if ((longEvenZone != longEvenZone1) || (longOddZone1 != longOddZone)) return null;
        }
        // actualLongitudeZone is the component z sub phi which is given to the even and odd longitude zone
        // if its value is greater or equal to one.
        // actualLongitudeZoneMostRecent is the actual longitude zone using all the components that have for parity the
        // most recent parity
        double longitude, actualLongitudeZone, actualLongitudeZoneMostRecent;

        if (mostRecent == EVEN_PARITY) {

            actualLongitudeZone = Math.rint(x0 * longOddZone - x1 * longEvenZone);
            actualLongitudeZoneMostRecent = computeActualZone(longEvenZone, actualLongitudeZone);
            longitude = inverseOf(longEvenZone) * (actualLongitudeZoneMostRecent + x0);

            return position(longitude, evenLatitude);
        } else {

            actualLongitudeZone = Math.rint(x0 * longOddZone1 - x1 * longEvenZone1);
            actualLongitudeZoneMostRecent = computeActualZone(longOddZone1, actualLongitudeZone);
            longitude = inverseOf(longOddZone1) * (actualLongitudeZoneMostRecent + x1);

            return position(longitude, oddLatitude);
        }
    }

    private static double computeActualZone(double zone, double toComputeActualZone) {
        // Computes and returns the actual zone based on the given `zone` and `toComputeActualZone` values.
        double actualZone;
        if (toComputeActualZone < 0) actualZone = toComputeActualZone + zone;
        else actualZone = toComputeActualZone;
        return actualZone;
    }

    private static boolean verifyIfLatitudeIsInTheBound(double evenLatitude, double oddLatitude) {
        // Verifies whether the given evenLatitude and oddLatitude values are within the bounds of -90 and 90 degrees.
        // Returns true if either value is less than -90 or greater than 90; false otherwise.
        return evenLatitude < -90 || evenLatitude > 90 || oddLatitude < -90 || oddLatitude > 90;
    }

    private static double latitudeInDegree(double latitude) {
        // Converts the given `latitude` value (in turns) to degrees and returns the result.
        return Units.convert(latitude, TURN, Units.Angle.DEGREE);
    }

    private static int latitudeOrLongitudeInT32(double latitudeOrLongitude) {
        // Converts the given `latitudeOrLongitude` value (in turns) to T.32 format (32-bit integer)
        // and returns the result.
        return (int) Math.rint(Units.convert(latitudeOrLongitude, TURN, Units.Angle.T32));
    }

    private static double valueAForLongZone(double latitude) {
        // Computes and returns the value of `a` for the given `latitude` value using the WGS 84 reference ellipsoid.
        return Math.acos(1 - ((1 - Math.cos(2 * Math.PI * WIDTH_LAT_EVEN_ZONE))
                / Math.pow(Math.cos(Units.convertFrom(latitude, TURN)), 2)));
    }

    private static boolean isAGreaterThan1(double valueOfAEven, double valueOfAOdd,int mostRecent) {
        // Checks if either `valueOfAEven` or `valueOfAOdd` is NaN.
        // Returns true if either value is NaN; false otherwise.
        return (Double.isNaN(valueOfAEven) && mostRecent==EVEN_PARITY) || (Double.isNaN(valueOfAOdd) && mostRecent== ODD_PARITY);
    }

    private static double convertToNegativeEquivalent(double angle) {
        // Converts the given `angle` value to its negative equivalent
        // (i.e., adds -1 to it if it's greater than or equal to 0.5).
        if (angle >= 0.5) {
            angle = angle - 1;
        }
        return angle;
    }

    private static GeoPos position(double longitude, double latitude) {
        // Creates and returns a new GeoPos object with the given `longitude` and `latitude`
        // values converted to T.32 format.
        return new GeoPos(latitudeOrLongitudeInT32(convertToNegativeEquivalent(longitude)),
                latitudeOrLongitudeInT32(latitude));
    }

    private static double inverseOf(double longitude) {
        // Computes and returns the inverse of the given `longitude` value.
        return 1d / longitude;
    }

}
