package javions.adsb;

import javions.Bits;
import javions.Preconditions;
import javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * represents an ADS-B identification and category message
 *
 * @param timeStampNs the timestamp of the message, in nanoseconds
 * @param icaoAddress the ICAO address of the message sender
 * @param category    the sender's aircraft category
 * @param callSign    the sender's indicator
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category,
                                            CallSign callSign) implements Message {
    private static final int PARTIAL_CATEGORY_INDEX = 48;
    private static final int PARTIAL_CATEGORY_SIZE = 3;
    private static final int CALL_SIGN_CHARACTER_SIZE = 6;
    private static final int FIRST_CALL_SIGN_CHAR = 42;

    private static boolean invalidCharacter = false;

    /**
     * @return the timestamp of the message in nanoseconds
     */
    @Override
    public long timeStampNs() {
        return timeStampNs;
    }

    /**
     * @return return the icao address of the message
     */
    @Override
    public IcaoAddress icaoAddress() {
        return icaoAddress;
    }

    /**
     * @throws NullPointerException     if the icao Address or the call Sign are null
     * @throws IllegalArgumentException if timestamps is strictly lower than zero
     */
    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * @param rawMessage the rawMessage whose Identification message we are looking for
     * @return the identification message of the raw message given or null if one of
     * the characters of the indicator  is invalid
     */

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        invalidCharacter = false;
        int partialCategory = Bits.extractUInt(rawMessage.payload(), PARTIAL_CATEGORY_INDEX, PARTIAL_CATEGORY_SIZE);
        int category = ((14 - rawMessage.typeCode()) << 4) | partialCategory;
        CallSign decodedCallSign = CallSignDecoder(rawMessage.payload());
        return (invalidCharacter) ? null :
                new AircraftIdentificationMessage(rawMessage.timeStampNs(),
                        rawMessage.icaoAddress(),
                        category,
                        decodedCallSign);
    }

    private static CallSign CallSignDecoder(long payload) {
        String[] decoder = {null,
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                null, null, null, null, null,
                "",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                null, null, null, null, null, null
        };
        StringBuilder callSign = new StringBuilder();
        for (int i = FIRST_CALL_SIGN_CHAR; i >= 0; i -= CALL_SIGN_CHARACTER_SIZE) {
            if ((decoder[Bits.extractUInt(payload, i, CALL_SIGN_CHARACTER_SIZE)] == null)) {
                invalidCharacter = true;
                return null;
            }
            String decoded = decoder[Bits.extractUInt(payload, i, CALL_SIGN_CHARACTER_SIZE)];
            callSign.append(decoded);
        }
        return new CallSign(callSign.toString());
    }
}
