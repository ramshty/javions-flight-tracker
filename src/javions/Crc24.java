package javions;

/**
 * represents a 24-bit CRC calculator
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class Crc24 {

    private final static int NUMBER_XOR_OPERATION = 3;
    private final static int EXTRACTED_BIT_SIZE = 1;
    private final static int LAST_CRC_BIT_SIZE = 1;
    private final static int LAST_CRC_BIT_START = 23;
    private final static int SIZE_GENERATOR_TABLE = 256;
    private final static int LAST_CRC_BYTE_START = 16;
    private final static int CRC_SIZE = 24;
    private final static int CRC_START = 0;
    /**
     * contains the 24 least significant bits of the generator used to calculate the CRC24 of ADS-B messages,
     * which is 0xFFF409
     */
    public final static int GENERATOR = 0xFFF409;
    /**
     * the table of 256 inputs of all the possible bytes corresponding to a generator
     */
    private final int[] table;

    /**
     * returns a CRC24 calculator using the generator whose least significant 24 bits are those of generator
     *
     * @param generator the generator used to compute the crc
     */

    public Crc24(int generator) {
        this.table = buildTable(Bits.extractUInt(generator, 0, 24));
    }

    /**
     * returns the CRC24 of the given array which works byte per byte
     *
     * @param bytes the array of bytes whose crc 24 is calculated
     * @return the CRC24 of the given array
     */
    public int crc(byte[] bytes) {
        int crc = 0;
        for (byte aByte : bytes) {
            int lastCrcBit = Bits.extractUInt(crc, LAST_CRC_BYTE_START, Byte.SIZE);
            crc = ((crc << Byte.SIZE) | (Byte.toUnsignedInt(aByte))) ^ table[lastCrcBit];
        }
        for (int i = 0; i < NUMBER_XOR_OPERATION; ++i) {
            crc = (crc << Byte.SIZE) ^ table[Bits.extractUInt(crc, LAST_CRC_BYTE_START, Byte.SIZE)];
        }
        return Bits.extractUInt(crc, CRC_START, CRC_SIZE);
    }

    /**
     * returns the crc24 of the given array working bit per bit
     *
     * @param generator the generator that will be used for computing the crc24 of the given array
     * @param bytes     the array of bytes whose crc 24 is calculated
     * @return the crc24 of the given array
     */
    private static int crc_bitwise(int generator, byte[] bytes) {
        int[] table = new int[]{0, generator};
        int crc = 0;
        for (byte aByte : bytes) {
            for (int j = Byte.SIZE-1; j >= 0; --j) {
                int extractedBit = Bits.extractUInt(aByte, j, EXTRACTED_BIT_SIZE);
                int lastCrcBit = Bits.extractUInt(crc, LAST_CRC_BIT_START, LAST_CRC_BIT_SIZE);
                crc = ((crc << 1) | (extractedBit)) ^ table[lastCrcBit];
            }
        }
        for (int i = 0; i < CRC_SIZE; ++i) {
            crc = (crc << 1) ^ table[Bits.extractUInt(crc, LAST_CRC_BIT_START, LAST_CRC_BIT_SIZE)];
        }
        return Bits.extractUInt(crc, CRC_START, CRC_SIZE);
    }

    /**
     * constructing the table of 256 entries corresponding to a generator by using crc_bitwise
     *
     * @param generator the generator that will be used for computing the crc24 of the 256 bytes
     * @return a generator that will be used in for computing the crc24 of a bytes array byte per byte
     */
    private static int[] buildTable(int generator) {
        int[] table = new int[SIZE_GENERATOR_TABLE];
        for (int i = 0; i < SIZE_GENERATOR_TABLE; ++i) {
            table[i] = crc_bitwise(generator, new byte[]{(byte) i});
        }
        return table;
    }
}

