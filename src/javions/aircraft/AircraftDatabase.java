package javions.aircraft;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * represents the mictronics database of the aircraft
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class AircraftDatabase {
    private final static int REGISTRATION_INDEX = 1;
    private final static int DESIGNATOR_INDEX = 2;
    private final static int MODEL_INDEX = 3;
    private final static int DESCRIPTION_INDEX = 4;
    private final static int WAKE_TURBULENCE_INDEX = 5;

    /**
     * the string representing the name of the file containing the database
     */
    private final String filename;

    /**
     * checks if the string given is not null and stores it as a parameter of the class or throws a NullPointerException
     * otherwise
     *
     * @throws NullPointerException if the string is null
     */
    public AircraftDatabase(String filename) {
        Objects.requireNonNull(filename);
        this.filename = filename;
    }

    /**
     * returns the data, as an AircraftData, of the aircraft whose IcaoAddress is given as a parameter and throws
     * an IOException in case of an input/output error
     *
     * @param address the IcaoAddress of the aircraft whose data we are looking for
     * @return the data of the aircraft whose IcaoAddress is matching the one given as a parameter
     * @throws IOException in case of an input/output error
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        String icaoAddress = address.string();
        String icaoAddressSub = icaoAddress.substring(icaoAddress.length() - 2);
        try (ZipFile aircraft = new ZipFile(
                java.net.URLDecoder.decode(filename, UTF_8));
             InputStream stream = aircraft.getInputStream(aircraft.getEntry(icaoAddressSub.concat(".csv")));
             Reader reader = new InputStreamReader(stream, UTF_8);
             BufferedReader finalReader = new BufferedReader(reader)) {
            String l = "";
            while ((l = finalReader.readLine()) != null) {
                if (l.compareTo(icaoAddress) > 0) {
                    if (l.startsWith(icaoAddress)) {
                        String[] data = l.split(",", -1);
                        return new AircraftData(new AircraftRegistration(data[REGISTRATION_INDEX]),
                                new AircraftTypeDesignator(data[DESIGNATOR_INDEX]),
                                data[MODEL_INDEX],
                                new AircraftDescription(data[DESCRIPTION_INDEX]),
                                WakeTurbulenceCategory.of(data[WAKE_TURBULENCE_INDEX]));
                    }
                    break;
                }
            }
        }
        return null;
    }
}
