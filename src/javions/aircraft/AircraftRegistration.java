package javions.aircraft;

import javions.Preconditions;

import java.util.regex.Pattern;

/**
 * represents the registration of the aircraft
 *
 * @param string the string containing the text representation of the aircraft's registration
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public record AircraftRegistration(String string) {
    /**
     * represents the Pattern matching the registration given to the record
     */
    public static Pattern address = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * checks if the string given matches the Pattern of the registration and throws and IllegalArgumentException otherwise
     *
     * @throws IllegalArgumentException if the string doesn't match the regular expression
     */
    public AircraftRegistration {
        Preconditions.checkArgument(address.matcher(string).matches());
    }
}
