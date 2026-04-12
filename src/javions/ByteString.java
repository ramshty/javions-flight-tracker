package javions;

import java.util.Arrays;
import java.util.Objects;

import java.util.HexFormat;

/**
 * represents a string (sequence) of bytes
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class ByteString {
    /**
     * the byteTable that will be converted to a ByteString as we define it with this class
     */
    private final byte[] byteTable;
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    /**
     * returns a byte string whose content is that of the array passed as argument
     *
     * @param bytes the given table of bytes
     */
    public ByteString(byte[] bytes) {
        this.byteTable = bytes.clone();
    }

    /**
     * returns the byte string whose string passed as argument is the hexadecimal representation
     *
     * @param hexString the hexadecimal string given
     * @return the byte string whose string passed as argument is the hexadecimal representation
     * @throws IllegalArgumentException (or NumberFormatException, which is a subtype of it) if the given string is not
     *                                  of even length, or if it contains a character that is not a hexadecimal digit
     */
    public static ByteString ofHexadecimalString(String hexString) {
        return new ByteString(HEX_FORMAT.parseHex(hexString));
    }

    /**
     * returns the size of the string, i.e. the number of bytes it contains
     *
     * @return the size of the string, i.e. the number of bytes it contains
     */
    public int size() {
        return byteTable.length;
    }

    /**
     * returns the byte (interpreted as unsigned) at the given index, or throws IndexOutOfBoundsException if this is
     * invalid
     *
     * @param index the index of the byte that we want to interpret to an unsigned int
     * @return the byte (interpreted as unsigned) at the given index, or throws IndexOutOfBoundsException if this is
     * invalid
     * @throws IndexOutOfBoundsException if the index isn't between 0 and byte.length-1
     */
    public int byteAt(int index) {
        Objects.checkIndex(index, byteTable.length);
        return Byte.toUnsignedInt(byteTable[index]);
    }

    /**
     * returns the bytes between the indexes fromIndex (inclusive) and toIndex (excluded) as a long type value,
     * with index byte toIndex - 1 being the least significant byte of the result
     *
     * @param fromIndex the start index from where we extract the byte
     * @param toIndex   the end index to where we want to extract the byte
     * @return the bytes between the indexes fromIndex (inclusive) and toIndex (excluded) as a long type value, with
     * index byte toIndex - 1 being the least significant byte of the result
     * @throws IndexOutOfBoundsException if the range described by fromIndex and toIndex is not entirely between 0 and
     *                                   the size of the string
     * @throws IllegalArgumentException  if the difference between toIndex and fromIndex is not strictly less than the
     *                                   number of bytes contained in a long type value
     */
    public long bytesInRange(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, byteTable.length);
        Preconditions.checkArgument((toIndex - fromIndex) < (Long.BYTES));
        return byteTableToLong(fromIndex, toIndex);
    }

    /**
     * returns the bytes between the indexes fromIndex (inclusive) and toIndex (excluded) as a long type value,
     * with index byte toIndex - 1 being the least significant byte of the result
     *
     * @param fromIndex the start index from where we extract the byte
     * @param toIndex   the end index to where we want to extract the byte
     * @return the bytes between the indexes fromIndex (inclusive) and toIndex (excluded) as a long type value,
     * with index byte toIndex - 1 being the least significant byte of the result
     */
    private long byteTableToLong(int fromIndex, int toIndex) {
        long finalValue = 0;
        int indexDif = toIndex - fromIndex;
        for (int i = 0; i < indexDif; ++i) {
            finalValue = finalValue | Byte.toUnsignedInt(byteTable[fromIndex + i]);
            if (i != indexDif - 1) {
                finalValue = finalValue << Byte.SIZE;
            }
        }
        return finalValue;
    }


    /**
     * returns true if the value passed to it is also an instance of ByteString and its bytes are identical to those
     * of the receiver
     *
     * @param that0 the element that is compared
     * @return true if the value passed to it is also an instance of ByteString and its bytes are identical to those
     */
    public boolean equals(Object that0) {
        if (that0 instanceof ByteString that) {
            return Arrays.equals(that.byteTable, this.byteTable);
        } else if (that0 == null) {
            return false;
        } else {
            return false;
        }
    }

    /**
     * returns the value returned by the hashCode method of Arrays applied to the array containing the bytes
     *
     * @return the value returned by the hashCode method of Arrays applied to the array containing the bytes
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(byteTable);
    }


    /**
     * returns a representation of the bytes of the string in hexadecimal, with each byte occupying exactly two characters
     *
     * @return a representation of the bytes of the string in hexadecimal, with each byte occupying exactly two characters
     */
    @Override
    public String toString() {
        return HEX_FORMAT.formatHex(byteTable);
    }
}
