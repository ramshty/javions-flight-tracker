package javions.adsb;

import javions.Preconditions;

import java.util.regex.Pattern;

/**
 * represents the indicator "Call sign" of an aircraft
 *
 * @param string the string containing the text representation of the call sign
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public record CallSign(String string) {
    /**
     * The Pattern matching the call sign given to the record
     */
    static Pattern address = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * checks if the string given either matches the regular expression or is empty, and throws an
     * IllegalArgumentException otherwise
     *
     * @throws IllegalArgumentException if the string doesn't match the Pattern while also not being empty
     */
    public CallSign {
        Preconditions.checkArgument(address.matcher(string).matches() || string.isEmpty());
    }
}
