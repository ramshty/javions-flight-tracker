package javions.aircraft;

import javions.Preconditions;

import java.util.regex.Pattern;

/**
 * represents an ICAO address
 *
 * @param string the string containing the text representation of the OACI address
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public record IcaoAddress(String string) {
    /**
     * The Pattern matching the ICAO address given to the record
     */
    public static Pattern address = Pattern.compile("[0-9A-F]{6}");

    /**
     * checks if the string given is valid and throws an IllegalArgumentException otherwise
     *
     * @throws IllegalArgumentException if the string is invalid
     */
    public IcaoAddress {
        Preconditions.checkArgument(address.matcher(string).matches());
    }
}
