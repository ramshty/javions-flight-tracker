package javions.adsb;

import javions.Bits;
import javions.Preconditions;
import javions.Units;
import javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * represents a flight velocity message
 *
 * @param timeStampNs    the timestamp of the message in nanoseconds
 * @param icaoAddress    the icao address of the message sender
 * @param speed          the aircraft's speed in m/s
 * @param trackOrHeading the direction of the aircraft in radians
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed,
                                      double trackOrHeading) implements Message {
    private static final int DEW_START = 21;
    private static final int DEW_SIZE = 1;
    private static final int DNS_START = 10;
    private static final int DNS_SIZE = 1;
    private static final int VEW_START = 11;
    private static final int VEW_SIZE = 10;
    private static final int VNS_START = 0;
    private static final int VNS_SIZE = 10;
    private static final int HEADING_START = 11;
    private static final int HEADING_SIZE = 10;
    private static final int SH_BIT_START = 21;
    private static final int SH_BIT_SIZE = 1;
    private static final int AIR_SPEED_START = 0;
    private static final int AIR_SPEED_SIZE = 10;
    private final static int ME_FIELD_STARTING_BYTE = 4;
    private final static int ME_FIELD_ENDING_BYTE = 11;
    private final static int SUB_TYPE_START = 48;
    private final static int SUB_TYPE_SIZE = 3;
    private final static int CONTENT_OF_THE_22_BITS_START = 21;
    private final static int CONTENT_OF_THE_22_BITS_SIZE = 22;
    private final static int FACTOR_OF_FOUR_TO_CONVERT_TO_4_KNOT = 4;
    private static final double DIVISION_VALUE_TO_GET_THE_HEADING = Math.scalb(1, -10);

    /**
     * return the time stamp of the message in nanoseconds
     *
     * @return the time stamp of the message in nanoseconds
     */
    @Override
    public long timeStampNs() {
        return timeStampNs;
    }

    /**
     * return the icao address of the aircraft that sends the message
     *
     * @return the icao address of the aircraft that sends the message
     */
    @Override
    public IcaoAddress icaoAddress() {
        return icaoAddress;
    }

    /**
     * @param timeStampNs    the time stamp of the message in nanoseconds
     * @param icaoAddress    the icao address of the aircraft that sends the message
     * @param speed          speed of the aircraft
     * @param trackOrHeading the direction of the aircraft
     * @throws IllegalArgumentException if time stamp is negative or speed is negative or trackOrHeading is negative
     * @throws NullPointerException     if the ICAO address is null
     */
    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0 && speed >= 0 && trackOrHeading >= 0);
    }

    /**
     * returns the airspeed message corresponding to the given raw message, or null if the subtype is invalid, or if the speed
     * or direction of travel cannot be determined
     *
     * @param rawMessage the message given with the corresponding type code
     * @return the airspeed message corresponding to the given raw message, or null if the subtype is invalid, or if the speed
     * or direction of travel cannot be determined
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        long velocityMessage = rawMessage.bytes().bytesInRange(ME_FIELD_STARTING_BYTE, ME_FIELD_ENDING_BYTE);
        int subType = Bits.extractUInt(velocityMessage, SUB_TYPE_START, SUB_TYPE_SIZE);
        long contentOfThe22Bits = Bits.extractUInt(velocityMessage, CONTENT_OF_THE_22_BITS_START, CONTENT_OF_THE_22_BITS_SIZE);
        if (isSubType1Or2(subType)) {

            int dew = Bits.extractUInt(contentOfThe22Bits, DEW_START, DEW_SIZE);
            int dns = Bits.extractUInt(contentOfThe22Bits, DNS_START, DNS_SIZE);
            int vew = Bits.extractUInt(contentOfThe22Bits, VEW_START, VEW_SIZE);
            int vns = Bits.extractUInt(contentOfThe22Bits, VNS_START, VNS_SIZE);

            if (isTheVelocityInvalid(vns, vew)) return null;

            double speed = computeTheSpeed(Math.hypot(vew - 1, vns - 1));
            double speed4 = computeTheSpeed(Math.hypot(vew - 1, vns - 1) * FACTOR_OF_FOUR_TO_CONVERT_TO_4_KNOT);
            double track = computeTheTrack(vew, dew, vns, dns);

            if (track < 0) track += Units.Angle.TURN;

            return (subType == 1) ? airborneVelocityMessage(rawMessage, speed, track) : airborneVelocityMessage(rawMessage, speed4, track);

        } else if (isSubType3Or4(subType)) {
            if (isTheBitSHEqualToOne(contentOfThe22Bits)) {

                double airSpeed = Bits.extractUInt(contentOfThe22Bits, AIR_SPEED_START, AIR_SPEED_SIZE);

                if (isTheAirSpeedInvalid(airSpeed)) return null;

                double speed = computeTheSpeed(airSpeed -1);
                double speed4 = computeTheSpeed((airSpeed - 1) * FACTOR_OF_FOUR_TO_CONVERT_TO_4_KNOT);
                double heading = computeHeading(contentOfThe22Bits);

                return (subType == 3) ? airborneVelocityMessage(rawMessage, speed, heading) :
                        airborneVelocityMessage(rawMessage, speed4, heading);

            } else return null;

        } else return null;
    }

    private static AirborneVelocityMessage airborneVelocityMessage(RawMessage rawMessage, double speed, double angle) {
        // Creates and returns a new AirborneVelocityMessage object with the specified timeStampNs(),
        // icaoAddress(), speed, and angle values.
        return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), speed, angle);
    }

    private static double computeHeading(long content22Bits) {
        // Extracts the heading value from the given content22Bits, converts it to degrees, and returns the result.
        return Units.convertFrom(Bits.extractUInt(content22Bits, HEADING_START, HEADING_SIZE)
                * DIVISION_VALUE_TO_GET_THE_HEADING, Units.Angle.TURN);
    }

    private static boolean isSubType3Or4(int subType) {
        // Checks whether the given subType value is equal to either 3 or 4, and returns true if it is,
        // or false otherwise.
        return subType == 3 || subType == 4;
    }

    private static boolean isSubType1Or2(int subType) {
        // Checks whether the given subType value is equal to either 1 or 2, and returns true if it is,
        // or false otherwise.
        return subType == 1 || subType == 2;
    }

    private static boolean isTheVelocityInvalid(int vns, int vew) {
        // Checks whether either the vns or vew values are equal to 0, and returns true if either of them is,
        // or false otherwise.
        return vns == 0 || vew == 0;
    }

    private static boolean isTheAirSpeedInvalid(double airspeed) {
        // Checks whether the given airspeed value is equal to 0, and returns true if it is, or false otherwise.
        return airspeed == 0;
    }

    private static boolean isTheBitSHEqualToOne(long contentOfThe22Bits) {
        // Checks whether the value of the SH bit (specified by SH_BIT_START and SH_BIT_SIZE)
        // in the given contentOfThe22Bits is equal to 1, and returns true if it is, or false otherwise.
        int bitSH = Bits.extractUInt(contentOfThe22Bits, SH_BIT_START, SH_BIT_SIZE);
        return bitSH == 1;
    }

    private static double computeTheTrack(int vew, int dew, int vns, int dns) {
        // The power of -1 is used here to get the sign of the velocity component because when the result of the power is
        // one it means that the dew and the dns value is equal to zero and hence that the aircraft is heading towards the
        // north and the east the opposite if dns or dew is equal to 0.
        return Math.atan2(Math.pow(-1, dew) * (vew - 1), Math.pow(-1, dns) * (vns - 1));
    }

    private static double computeTheSpeed(double airSpeed) {
        // Computes and returns the airspeed (in knots) based on the given airSpeed value.
        return Units.convertFrom(airSpeed, Units.Speed.KNOT);
    }
}
