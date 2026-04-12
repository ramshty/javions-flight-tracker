package javions.aircraft;

import javions.Preconditions;

import java.util.regex.Pattern;

/**
 * represents the description of the aircraft
 *
 * @param string the string containing the textual representation of the aircraft's description
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public record AircraftDescription(String string) {
    /**
     * represents the Pattern, as a regular expression, of the aircraft's description
     */
    static Pattern address = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * checks if the string given either matches the Pattern or is empty and throws an IllegalArgumentException if it's not
     *
     * @throws IllegalArgumentException if the string doesn't match the Pattern while also not being empty
     */
    public AircraftDescription {
        Preconditions.checkArgument(address.matcher(string).matches() || string.isEmpty());
    }
}
