package javions.aircraft;

/**
 * represents the wake turbulence category of an aircraft. It contains four values which are, in order: LIGHT, MEDIUM,
 * HEAVY, UNKNOWN.
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */

public enum WakeTurbulenceCategory {

    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN;

    /**
     * returns the wake turbulence category corresponding to the given string
     *
     * @param s the string given corresponding to turbulence category
     * @return the wake turbulence category corresponding to the given string or the default one if the string
     * doesn't match any of the cases
     */
    public static WakeTurbulenceCategory of(String s) {
        return switch (s) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            default -> UNKNOWN;
        };
    }

}
