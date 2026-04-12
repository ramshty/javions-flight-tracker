package javions;

/**
 * The Math2 class, from the main package, public, final and not instantiable, offers static methods for performing
 * certain mathematical calculations
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */

public final class Math2 {
    /**
     * The purpose of this private constructor is to make it impossible to create instances of the class
     */
    private Math2() {
    }

    /**
     * limits the value v to the interval from min to max
     *
     * @param min the minimum of the interval
     * @param v   the input value
     * @param max the maximum of the interval
     * @return returns min if v is less than min, max if v is greater than max, and v otherwise.
     * @throws IllegalArgumentException if min is (strictly) greater than max,
     */

    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(max >= min);
        return Math.max(min,Math.min(v,max));
    }

    /**
     * @param x the input value
     * @return returns the reciprocal hyperbolic sine of its argument x
     */

    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + x * x));
    }


}
