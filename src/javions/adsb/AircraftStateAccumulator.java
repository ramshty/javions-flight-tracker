package javions.adsb;

import javions.GeoPos;

import java.util.Objects;

/**
 * represents an accumulator of aircraft state (i.e. an object that accumulates ads-b messages coming
 * from a single aircraft to determine his state as time goes on)
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */

public final class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private final static int EVEN_PARITY = 0;
    private final static int ODD_PARITY = 1;
    private final static double DELAY_OF_VALIDITY = 1e10;
    private AirbornePositionMessage lastEvenMessage;
    private AirbornePositionMessage lastOddMessage;
    private final T stateSetter;

    /**
     * returns an aircraft state accumulator associated with the given modifiable state,
     * or throws NullPointerException if this is null.
     *
     * @param stateSetter the given modifiable state
     */
    public AircraftStateAccumulator(T stateSetter) {
        this.stateSetter = Objects.requireNonNull(stateSetter);
    }

    /**
     * return the given modifiable state
     *
     * @return the given modifiable state
     */
    public T stateSetter() {
        return stateSetter;
    }

    /**
     * updates the editable state based on the given message, by calling, for all message types,
     * its setLastMessageTimeStampNs method, as well as:
     * if it is an identification and category message, setCategory and setCallSign,
     * if it is an in-flight positioning message, setAltitude and, if the position can be determined, setPosition,
     * if it is an in-flight velocity message, setVelocity and setTrackOrHeading.
     *
     * @param message message given for updating
     */
    public void update(Message message) {
        switch (message) {
            case AircraftIdentificationMessage aim -> {

                stateSetter.setLastMessageTimeStampNs(aim.timeStampNs());
                stateSetter.setCategory(aim.category());
                stateSetter.setCallSign(aim.callSign());

            }
            case AirborneVelocityMessage aim -> {

                stateSetter.setLastMessageTimeStampNs(aim.timeStampNs());
                stateSetter.setVelocity(aim.speed());
                stateSetter.setTrackOrHeading(aim.trackOrHeading());
                
            }
            case AirbornePositionMessage aim -> {

                stateSetter.setLastMessageTimeStampNs(aim.timeStampNs());

                if (canThePositionBeUpdatedForAnEvenMessage(aim.parity(), aim.timeStampNs())) {
                    if (!Objects.isNull(computeThePositionForAnEvenMessage(aim.x(), aim.y())))

                        stateSetter.setPosition(computeThePositionForAnEvenMessage(aim.x(), aim.y()));

                }
                if (canThePositionBeUpdatedForAnOddMessage(aim.parity(), aim.timeStampNs())) {
                    if (!Objects.isNull(computeThePositionForAnOddMessage(aim.x(), aim.y())))

                        stateSetter.setPosition(computeThePositionForAnOddMessage(aim.x(), aim.y()));

                }

                if (aim.parity() == EVEN_PARITY) lastEvenMessage = aim;
                else lastOddMessage = aim;

                stateSetter.setAltitude(aim.altitude());
            }
            default -> throw new Error("Impossible case");
        }
    }

    private boolean canThePositionBeUpdatedForAnEvenMessage(int parity, long timeStampNs) {
        // Checks whether the position can be updated for an even message based on the given parity value and timeStampNs.
        // Returns true if parity is EVEN_PARITY, lastOddMessage is not null,
        // and the time difference between timeStampNs and lastOddMessage.timeStampNs()
        // is less than or equal to DELAY_OF_VALIDITY; false otherwise.
        return parity == EVEN_PARITY && lastOddMessage != null
                && (timeStampNs - lastOddMessage.timeStampNs()) <= DELAY_OF_VALIDITY;
    }

    private boolean canThePositionBeUpdatedForAnOddMessage(int parity, long timeStampNs) {
        // Checks whether the position can be updated for an odd message based on the given parity value and timeStampNs.
        // Returns true if parity is ODD_PARITY, lastEvenMessage is not null,
        // and the time difference between timeStampNs and lastEvenMessage.timeStampNs() is less than or
        // equal to DELAY_OF_VALIDITY; false otherwise.
        return parity == ODD_PARITY && lastEvenMessage != null
                && (timeStampNs - lastEvenMessage.timeStampNs()) <= DELAY_OF_VALIDITY;
    }

    private GeoPos computeThePositionForAnOddMessage(double x, double y) {
        // Computes and returns the position (in latitude and longitude) for an odd message
        // using the lastEvenMessage's x and y values, and the given x and y values, and the ODD_PARITY.
        return CprDecoder.decodePosition(lastEvenMessage.x(), lastEvenMessage.y(), x, y, ODD_PARITY);
    }

    private GeoPos computeThePositionForAnEvenMessage(double x, double y) {
        // Computes and returns the position (in latitude and longitude) for an even message
        // using the given x and y values, and the lastOddMessage's x and y values, and the EVEN_PARITY.
        return CprDecoder.decodePosition(x, y, lastOddMessage.x(), lastOddMessage.y(), EVEN_PARITY);
    }

}
