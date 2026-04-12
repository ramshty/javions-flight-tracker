package javions.adsb;


import java.util.Objects;

/**
 * this class transforms ads-b messages into one of the three types : identification, position or velocity message
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class MessageParser {
    private static final int VELOCITY_MESSAGE_TYPE_CODE = 19;
    private static final int POSITION_MESSAGE_START_TYPE_CODE_FIRST_RANGE = 9;
    private static final int POSITION_MESSAGE_END_TYPE_CODE_FIRST_RANGE = 18;
    private static final int POSITION_MESSAGE_START_TYPE_CODE_SECOND_RANGE = 20;
    private static final int POSITION_MESSAGE_END_TYPE_CODE_SECOND_RANGE = 22;
    private static final int IDENTIFICATION_MESSAGE_START_TYPE_CODE_RANGE = 1;
    private static final int IDENTIFICATION_MESSAGE_END_TYPE_CODE_RANGE = 4;

    /**
     * returns the instance of AircraftIdentificationMessage, AirbornePositionMessage or AirborneVelocityMessage
     * corresponding to the given raw message, or null if the type code of the latter does
     * not correspond to any of these three message types, or if it is invalid
     *
     * @param rawMessage the message parsed
     * @return the instance of AircraftIdentificationMessage, AirbornePositionMessage or AirborneVelocityMessage
     * corresponding to the given raw message, or null if the type code of the latter does
     * not correspond to any of these three message types, or if it is invalid
     */
    public static Message parse(RawMessage rawMessage) {
        if (isAVelocityMessage(rawMessage)) {

            if (Objects.isNull(AirborneVelocityMessage.of(rawMessage))) return null;
            else return AirborneVelocityMessage.of(rawMessage);

        } else if (isAnIdentificationMessage(rawMessage)) {

            if (Objects.isNull(AircraftIdentificationMessage.of(rawMessage))) return null;
            else return AircraftIdentificationMessage.of(rawMessage);

        } else if (isAPositionMessage(rawMessage)) {

            if (Objects.isNull(AirbornePositionMessage.of(rawMessage))) return null;
            else return AirbornePositionMessage.of(rawMessage);

        }
        return null;
    }

    private static boolean isAPositionMessage(RawMessage rawMessage) {
        // Checks if the given `rawMessage` is a position message.
        // Returns true if the type code of `rawMessage` falls within either
        // the first or second range of position message type codes; false otherwise.
        return (rawMessage.typeCode() >= POSITION_MESSAGE_START_TYPE_CODE_FIRST_RANGE
                && rawMessage.typeCode() <= POSITION_MESSAGE_END_TYPE_CODE_FIRST_RANGE)
                || (rawMessage.typeCode() >= POSITION_MESSAGE_START_TYPE_CODE_SECOND_RANGE
                && rawMessage.typeCode() <= POSITION_MESSAGE_END_TYPE_CODE_SECOND_RANGE);
    }

    private static boolean isAnIdentificationMessage(RawMessage rawMessage) {
        // Checks if the given `rawMessage` is an identification message.
        // Returns true if the type code of `rawMessage` falls within the
        // identification message type code range; false otherwise.
        return rawMessage.typeCode() >= IDENTIFICATION_MESSAGE_START_TYPE_CODE_RANGE
                && rawMessage.typeCode() <= IDENTIFICATION_MESSAGE_END_TYPE_CODE_RANGE;
    }

    private static boolean isAVelocityMessage(RawMessage rawMessage) {
        // Checks if the given `rawMessage` is a velocity message.
        // Returns true if the type code of `rawMessage` is equal to the velocity message type code; false otherwise.
        return rawMessage.typeCode() == VELOCITY_MESSAGE_TYPE_CODE;
    }

}
