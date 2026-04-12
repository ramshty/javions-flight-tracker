package javions;

import java.util.Objects;

/**
 * this class contains methods that help to extract a subset of 64 bits from a long value
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class Bits {
    /**
     * The purpose of this private constructor is to make it impossible to create instances of the class
     */
    private Bits() {
    }

    /**
     * extracts from the 64-bit vector value the range of size bits starting at the index bit start, which it interprets
     * as an unsigned value,or raises
     *
     * @param value the input value
     * @param start the start index
     * @param size  the size of the bit string extracted
     * @return an int which is corresponding to the bit string extracted
     * @throws IllegalArgumentException  if the length isn't strictly superior to 0 or strictly lower than 32
     * @throws IndexOutOfBoundsException if the range described by start and size is not entirely between 0 (inclusive)
     *                                   and 64 (exclusive)
     */
    public static int extractUInt(long value, int start, int size) {
        Preconditions.checkArgument(size > 0 && size < Integer.SIZE);
        Objects.checkFromIndexSize(start, size, Long.SIZE);

        long tempValue = value >>> start;
        long tempValue2 = tempValue << (Long.SIZE - size);
        long tempValue3 = tempValue2 >>> (Long.SIZE - size);
        return (int) tempValue3;
    }

    /**
     * @param value the given value
     * @param index the index value
     * @return returns true iff the given index value bit is 1
     * @throws IndexOutOfBoundsException if it is not between 0 (included) and 64 (excluded).
     */

    public static boolean testBit(long value, int index) {
        Objects.checkIndex(index, Long.SIZE);
        long maskIndex = 1L << index;
        return (value & maskIndex) != 0;
    }
}

