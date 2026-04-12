package javions.aircraft;

import javions.Preconditions;

import java.util.regex.Pattern;

/**
 * represents the type designator of an aircraft
 *
 * @param string the string containing the textual representation of the type designator
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public record AircraftTypeDesignator(String string) {
    /**
     * represents the Pattern, as a regular expression, of the type designator
     */
    public static Pattern address = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * checks if the string matches the Pattern or is empty and throws an IllegalArgumentException otherwise
     *
     * @throws IllegalArgumentException if the string doesn't match the Pattern while also not being empty
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(address.matcher(string).matches() || string.isEmpty());
    }
}
