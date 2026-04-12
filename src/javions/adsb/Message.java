package javions.adsb;

import javions.aircraft.IcaoAddress;

/**
 * is implemented by all the classes representing analyzed ADS-B messages
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public interface Message {
    /**
     * @return the timestamp of the message, in nanoseconds
     */
    long timeStampNs();

    /**
     * @return the icao address of the message sender
     */
    IcaoAddress icaoAddress();
}
