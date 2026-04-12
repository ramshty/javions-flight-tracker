package javions.adsb;

import javions.Bits;
import javions.ByteString;
import javions.Preconditions;
import javions.Units;
import javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * represents an ADS-B in-flight positioning message
 *
 * @param timeStampNs the timestamps of the message, in nanoseconds
 * @param icaoAddress the ICAO Address of the message sender
 * @param altitude    the latitude of the aircraft
 * @param parity      the parity of the message
 * @param x           the longitude of the aircraft
 * @param y           the latitude of the aircraft
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x,
                                      double y) implements Message {
    private final static int EVEN_PARITY = 0;
    private final static int ODD_PARITY = 1;
    // Constant to normalize the longitude and the latitude to put their values between 0 and 1
    private final static double TO_NORMALIZE = Math.scalb(1, -17);
    private final static int Q_IS_ONE = 1;
    private final static int Q_BIT_START = 4;
    private final static int Q_BIT_SIZE = 1;
    private final static int ME_FIELD_STARTING_BYTE = 4;
    private final static int ME_FIELD_ENDING_BYTE = 11;
    private final static int PARITY_FIELD_START = 34;
    private final static int PARITY_FIELD_SIZE = 1;
    private final static int LONGITUDE_CPR_FIELD_START = 0;
    private final static int LONGITUDE_CPR_FIELD_SIZE = 17;
    private final static int LATITUDE_CPR_FIELD_START = 17;
    private final static int LATITUDE_CPR_FIELD_SIZE = 17;
    private final static int ALTITUDE_FIELD_START = 36;
    private final static int ALTITUDE_FIELD_SIZE = 12;
    private final static int MSB_OF_ALT_FIELD_START = 5;
    private final static int MSB_OF_ALT_FIELD_SIZE = 7;
    private final static int LSB_OF_ALT_FIELD_START = 0;
    private final static int LSB_OF_ALT_FIELD_SIZE = 4;
    private final static int MULTIPLE_OF_25_FOOT = 25;
    private final static int BASE_ALTITUDE_Q_ONE = -1000;
    private final static int LEFT_SHIFT_EXTRACT_11_BITS = 4;
    private final static int MASK_EXTRACT_LSB = ((1 << 3) - 1);
    private final static int RIGHT_SHIFT_EXTRACT_MSB = 3;
    private final static int LENGTH_OF_LSB_OF_SORTED_FIELD = 3;
    private final static int LENGTH_OF_MSB_OF_SORTED_FIELD = 9;
    private final static int FIRST_VALUE_OF_INVALIDITY = 0;
    private final static int SECOND_VALUE_OF_INVALIDITY = 5;
    private final static int THIRD_VALUE_OF_INVALIDITY = 6;
    private final static int BASE_ALTITUDE_Q_ZERO = -1300;
    private final static int MULTIPLE_OF_500_FOOT = 500;
    private final static int MULTIPLE_OF_100_FOOT = 100;
    private final static int REPLACEMENT_VALUE = 5;

    @Override
    public long timeStampNs() {
        return timeStampNs;
    }

    @Override
    public IcaoAddress icaoAddress() {
        return icaoAddress;
    }

    /**
     * represents an ADS-B in-flight positioning message
     *
     * @param timeStampNs the timestamps of the message, in nanoseconds
     * @param icaoAddress the ICAO Address of the message sender
     * @param altitude    the latitude of the aircraft
     * @param parity      the parity of the message
     * @param x           the longitude of the aircraft
     * @param y           the latitude of the aircraft
     * @throws IllegalArgumentException if timeStamp is strictly less than 0, or parity is different from 0 or 1, or x or y
     *                                  are not included between 0 (included) and 1 (excluded).
     * @throws NullPointerException     if ICAO address is null
     */
    public AirbornePositionMessage {
        Preconditions.checkArgument(timeStampNs >= 0 &&
                (parity == ODD_PARITY || parity == EVEN_PARITY) && x >= 0 && x < 1 && y >= 0 && y < 1);
        Objects.requireNonNull(icaoAddress);
    }

    /**
     * which returns the in-flight positioning message corresponding to the given raw message, or null if the altitude it
     * contains is invalid
     *
     * @param rawMessage the message that is decrypted
     * @return the in-flight positioning message corresponding to the given raw message, or null if the altitude it
     * contains is invalid
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {
        ByteString byteMessage = rawMessage.bytes();

        long positioningMessage = byteMessage.bytesInRange(ME_FIELD_STARTING_BYTE, ME_FIELD_ENDING_BYTE);
        long fieldAlt = Bits.extractUInt(positioningMessage, ALTITUDE_FIELD_START, ALTITUDE_FIELD_SIZE);

        double altitudeFoot;

        if (isTheBitQEqualToOne(fieldAlt)) {

            int extractedElevenBits = extractTheElevenBits(fieldAlt);

            altitudeFoot = extractedElevenBits * MULTIPLE_OF_25_FOOT + BASE_ALTITUDE_Q_ONE;

        } else {
            int sortedField = sortBits(fieldAlt);

            int LSB = sortedField & MASK_EXTRACT_LSB;
            int MSB = sortedField >>> RIGHT_SHIFT_EXTRACT_MSB;
            int grayLSB = computeGrayValue(LSB, LENGTH_OF_LSB_OF_SORTED_FIELD);
            int grayMSB = computeGrayValue(MSB, LENGTH_OF_MSB_OF_SORTED_FIELD);

            if (isTheAltitudeInvalid(grayLSB)) {
                return null;
            }
            if (isTheGrayLSBEqualToSeven(grayLSB)) {
                grayLSB = REPLACEMENT_VALUE;
            }
            if (isTheGrayMSBOdd(grayMSB)) {
                grayLSB = computeTheGrayLSB(grayLSB);
            }
            altitudeFoot = computeTheAltitudeInFoot(grayLSB, grayMSB);
        }
        double altitude = Units.convert(altitudeFoot, Units.Length.FOOT, Units.Length.METER);
        // Extract all remaining fields of AirbornePositionMessage from ME field (parity, longitude, latitude)
        return airbornePositionMessage(rawMessage, altitude, positioningMessage);
    }

    // Sort the bits to be able to compute the gray value
    private static int sortBits(long fieldAlt) {
        int result = 0;
        // Define the bit positions to extract from the fieldAlt
        int[] fieldBitPositions = {11, 9, 7, 10, 8, 6, 5, 3, 1, 4, 2, 0};
        // Define the corresponding bit positions in the result
        int[] resultBitPositions = {2, 1, 0, 8, 7, 6, 5, 4, 3, 11, 10, 9};
        // Iterate through each bit position
        for (int i = 0; i < fieldBitPositions.length; i++) {
            // Extract the bit at the specified position from fieldAlt
            int bit = (int) ((fieldAlt >>> fieldBitPositions[i]) & 1);
            // Shift the extracted bit to its corresponding position in the result
            int shiftedBit = bit << resultBitPositions[i];
            // Combine the shifted bit with the current result using the OR operation
            result |= shiftedBit;
        }
        return result;
    }

    // Compute the gray value
    private static int computeGrayValue(int MSBOrLSB, int length) {
        int grayMSBOrLSB = MSBOrLSB;
        for (int i = 1; i < length; i++) {
            grayMSBOrLSB ^= (MSBOrLSB >> i);
        }
        return grayMSBOrLSB;
    }

    // Verify if the bit Q is equal to one
    private static boolean isTheBitQEqualToOne(long fieldAlt) {
        return Bits.extractUInt(fieldAlt, Q_BIT_START, Q_BIT_SIZE) == Q_IS_ONE;
    }

    // extract the eleven bits by removing the bit Q from the 12 bits of the altitude field
    private static int extractTheElevenBits(long fieldAlt) {
        return (Bits.extractUInt(fieldAlt, MSB_OF_ALT_FIELD_START, MSB_OF_ALT_FIELD_SIZE) << LEFT_SHIFT_EXTRACT_11_BITS)
                | Bits.extractUInt(fieldAlt, LSB_OF_ALT_FIELD_START, LSB_OF_ALT_FIELD_SIZE);
    }

    // Verify whether yes or no the 3 LSB of the gray value of the eleven bits is equal to 0, 5 or 6
    private static boolean isTheAltitudeInvalid(int grayLSB) {
        return grayLSB == FIRST_VALUE_OF_INVALIDITY || grayLSB == SECOND_VALUE_OF_INVALIDITY
                || grayLSB == THIRD_VALUE_OF_INVALIDITY;
    }

    // Verify if the value of the 9 MSB of the gray value is odd
    private static boolean isTheGrayMSBOdd(int grayMSB) {
        return grayMSB % 2 == ODD_PARITY;
    }

    // verify if the value of 3 LSB of the gray value is equal to seven
    private static boolean isTheGrayLSBEqualToSeven(int grayLSB) {
        return grayLSB == 7;
    }

    // Compute the altitude in foot
    private static double computeTheAltitudeInFoot(int grayLSB, int grayMSB) {
        return grayLSB * MULTIPLE_OF_100_FOOT + grayMSB * MULTIPLE_OF_500_FOOT + BASE_ALTITUDE_Q_ZERO;
    }

    // Compute the new value of the value given by the 3 LSB of the gray value by subtracting from 6 the last gray value
    private static int computeTheGrayLSB(int grayLSB) {
        return 6 - grayLSB;
    }

    // Create an AirbornePositionMessage by extracting the parity, the longitude and the latitude and by normalizing them
    private static AirbornePositionMessage airbornePositionMessage(RawMessage rawMessage, double altitude, long positioningMessage) {
        int parity = Bits.extractUInt(positioningMessage, PARITY_FIELD_START, PARITY_FIELD_SIZE);
        double longitudeX = Bits.extractUInt(positioningMessage, LONGITUDE_CPR_FIELD_START, LONGITUDE_CPR_FIELD_SIZE) * TO_NORMALIZE;
        double latitudeY = Bits.extractUInt(positioningMessage, LATITUDE_CPR_FIELD_START, LATITUDE_CPR_FIELD_SIZE) * TO_NORMALIZE;
        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), altitude, parity, longitudeX, latitudeY);
    }
}
