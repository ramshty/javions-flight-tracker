package javions.adsb;

import javions.GeoPos;

/**
 * is implemented by all the classes that represent the state of an aircraft
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public interface AircraftStateSetter {
    /**
     * changes the timestamp of the last received message into a given value
     *
     * @param timeStampNs the new timestamp of the message
     */
    void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * changes the category of an aircraft into a given one
     *
     * @param category the new category of the aircraft
     */
    void setCategory(int category);

    /**
     * changes the CallSign of the aircraft into a given one
     *
     * @param callSignProperty the new CallSign of the aircraft
     */
    void setCallSign(CallSign callSignProperty);

    /**
     * changes the position of the aircraft into a given one
     *
     * @param positionProperty the new position of the aircraft
     */
    void setPosition(GeoPos positionProperty);

    /**
     * changes the altitude of the aircraft into a given one
     *
     * @param altitude the new altitude of the aircraft
     */
    void setAltitude(double altitude);

    /**
     * changes the velocity of the aircraft into a given one
     *
     * @param velocity the new velocity of the aircraft
     */
    void setVelocity(double velocity);

    /**
     * changes the direction of the aircraft into a given one
     *
     * @param trackOrHeading the new direction of the aircraft
     */
    void setTrackOrHeading(double trackOrHeading);
}
