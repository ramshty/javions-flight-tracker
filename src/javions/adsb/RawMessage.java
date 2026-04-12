package javions.adsb;

import javions.ByteString;
import javions.Crc24;
import javions.Preconditions;
import javions.aircraft.IcaoAddress;

/**
 * represents a "raw" ADS-B message, whose ME hasn't been analyzed yet
 *
 * @param timeStampNs the timestamps of the message, in nanoseconds
 * @param bytes       the bytes of the message
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public record RawMessage(long timeStampNs, ByteString bytes) {
    /**
     * the length, in bytes, of ADS-B messages
     */
    public static final int LENGTH = 14;

    /**
     * the constructor checks if the timestamps are valid and the byte string is of length 14
     *
     * @throws IllegalArgumentException if the timestamps are strictly negative or if the number of bytes in the byte string
     *                                  isn't the same as LENGTH (= 14)
     */
    public RawMessage {
        Preconditions.checkArgument((timeStampNs >= 0) && (bytes.size() == LENGTH));
    }

    /**
     * the method returns the RawMessage with the timestamps and bytes given
     *
     * @param timeStampNs the timeStamps of the message, in nanoseconds
     * @param bytes       the bytes of the message
     * @return the raw ADS-B message with the timestamps and bytes given in the parameters
     */
    public static RawMessage of(long timeStampNs, byte[] bytes) {
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        return (crc24.crc(bytes) != 0) ? null : new RawMessage(timeStampNs, new ByteString(bytes));
    }

    /**
     * the method returns the size of a message whose first byte is the one given as a parameter
     *
     * @param byte0 the first byte of the message whose size we're looking for
     * @return the size of a message as LENGTH if the DF is 17 or as 0 otherwise
     */
    public static int size(byte byte0) {
        byte Df = (byte) (byte0 >>> 3 & 0b11111);
        return (Df == 17) ? LENGTH : 0;
    }

    /**
     * the method returns the typeCode of the ME given as a parameter
     *
     * @param payload the ME of the message
     * @return the typeCode of the ME given as a parameter
     */
    public static int typeCode(long payload) {
        return (byte) (payload >>> 51 & 0b11111);
    }

    /**
     * the method returns the format of the message
     *
     * @return the DF stored in the first byte of the message
     */
    public int downLinkFormat() {
        return (byte) (this.bytes.byteAt(0) >>> 3 & 0b11111);
    }

    /**
     * @return the ICAO Address of the message sender
     */
    public IcaoAddress icaoAddress() {
        String icaoString = bytes.toString().substring(2, 8);
        return new IcaoAddress(icaoString);
    }

    /**
     * @return the ME of the message
     */
    public long payload() {
        return bytes.bytesInRange(4, 11);
    }

    /**
     * @return the typeCode of the message
     */
    public int typeCode() {
        return typeCode(payload());
    }
}
