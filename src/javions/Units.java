package javions;

/**
 * contains the definition of SI prefixes useful to the project, nested classes containing the definitions of the
 * different units, as well as conversion methods.
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class Units {
    /**
     * The purpose of this private constructor is to make it impossible to create instances of the class
     */
    private Units() {
    }

    /**
     * prefix SI CENTI equal to 10^-2
     */
    public static double CENTI = 1e-2;
    /**
     * prefix SI KILO equal to 10^3
     */
    public static double KILO = 1e3;

    /**
     * @param value    the input value
     * @param fromUnit the unit of the input value
     * @param toUnit   the unit of conversion
     * @return the conversion of the input value ,which is expressed in unit fromUnit, in unit toUnit
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return value * (fromUnit / toUnit);
    }

    /**
     * equivalent to convert but the arrived unit is the basic unit equal to 1
     *
     * @param value    the input value
     * @param fromUnit the unit of the input value
     * @return the conversion of the input value from the unit fromUnit to the basic unit
     */
    public static double convertFrom(double value, double fromUnit) {
        return value * (fromUnit);
    }

    /**
     * equivalent to convert but the departure unit (fromUnit) is the basic unit equal to 1
     *
     * @param value  the input value
     * @param toUnit the arrived unit
     * @return the conversion of the input value from the basic unit to toUnit
     */
    public static double convertTo(double value, double toUnit) {
        return value * 1 / toUnit;
    }

    /**
     * contains definitions of angle's units
     */

    public static class Angle {
        /**
         * The purpose of this private constructor is to make it impossible to create instances of the class
         */
        private Angle() {
        }

        /**
         * the radian is the basic unit of the angle
         */
        public static double RADIAN = 1;
        /**
         * turn equal to 2π rad
         */
        public static double TURN = 2 * Math.PI * RADIAN;
        /**
         * degree which is equal to TURN/360
         */
        public static double DEGREE = TURN / 360;
        /**
         * T32 which is equal to TURN * 2^-32
         */
        public static double T32 = Math.scalb(TURN, -32);
    }

    /**
     * contains definitions of length's units
     */
    public static class Length {
        /**
         * The purpose of this private constructor is to make it impossible to create instances of the class
         */
        private Length() {
        }

        /**
         * the meter is the basic unit of the length
         */
        public static double METER = 1;
        /**
         * centimeter which is equal to 10^-2 * METER
         */
        public static double CENTIMETER = METER * CENTI;
        /**
         * kilometer which is equal to METER * 10^3
         */
        public static double KILOMETER = METER * KILO;
        /**
         * inch which is equal to 2.54 * 10^-2
         */
        public static double INCH = 2.54 * CENTI;
        /**
         * foot which is equal to 12 * INCH
         */
        public static double FOOT = 12 * INCH;
        /**
         * nautical mile which is equal to 1852 * METER
         */
        public static double NAUTICAL_MILE = 1852 * METER;
    }

    /**
     * contains definitions of time's units
     */

    public static class Time {
        /**
         * The purpose of this private constructor is to make it impossible to create instances of the class
         */
        private Time() {
        }

        /**
         * the second is the basic unit of the time
         */
        public static double SECOND = 1;
        /**
         * minute which is equal to 60 * SECOND
         */
        public static double MINUTE = 60 * SECOND;
        /**
         * hour which is  equal to 60 * MINUTE
         */
        public static double HOUR = 60 * MINUTE;
    }

    /**
     * contains definition of speed's units
     */

    public static class Speed {
        /**
         * The purpose of this private constructor is to make it impossible to create instances of the class
         */
        private Speed() {
        }

        /**
         * kilometer per hour which is equal KILOMETER/HOUR
         */
        public static double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
        /**
         * KNOT which is equal to NAUTICAL MILE / HOUR
         */
        public static double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
    }
}
