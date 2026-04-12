package javions.adsbTest;

import javions.Bits;
import javions.ByteString;
import javions.adsb.RawMessage;
import javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.BeforeEach;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class RawMessageTest3 {

    @org.junit.jupiter.api.Test
    void TestRawMessageEmpty(){
        assertThrows(IllegalArgumentException.class,() -> new RawMessage(2, new ByteString(new byte[1])));
        assertThrows(IllegalArgumentException.class,() -> new RawMessage(-155, new ByteString(new byte[14])));
    }

    @org.junit.jupiter.api.Test
    void of() {
        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        assertNull(RawMessage.of(0, new byte[]{(byte) 0b10001111}));
        long testVal = 5;
        assertEquals(new RawMessage(testVal, new ByteString(mAndC)), RawMessage.of(testVal, mAndC));
    }

    @org.junit.jupiter.api.Test
    void size() {
        assertEquals(14, RawMessage.size((byte) 0b10001000));
        assertEquals(14, RawMessage.size((byte) 0b10001110));
        assertEquals(0, RawMessage.size((byte) 0b10101000));
    }

    @org.junit.jupiter.api.Test
    void typeCode() {
        long expected = (long)(Math.pow(2, 55) + Math.pow(2, 53));
        //System.out.println(expected);
        //System.out.println(0b10100);
        //System.out.println(RawMessage.typeCode(expected));
        assertEquals(0b10100, RawMessage.typeCode(expected));
    }

    @org.junit.jupiter.api.Test
    void downLinkFormat() {
        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage testMsg = new RawMessage(5, new ByteString(mAndC));
        int expectedVal = 0x8D;
        expectedVal = Bits.extractUInt(expectedVal, 3, 5);
        assertEquals(expectedVal, testMsg.downLinkFormat());

    }

    @org.junit.jupiter.api.Test
    void icaoAddress() {

        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage testMsg = new RawMessage(5, new ByteString(mAndC));
        assertEquals(new IcaoAddress("4B18F4"), testMsg.icaoAddress());
    }

    @org.junit.jupiter.api.Test
    void payload() {
        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage testMsg = new RawMessage(5, new ByteString(mAndC));
        assertEquals("231445F2DB63A0", Long.toHexString(testMsg.payload()).toUpperCase());
    }

    @org.junit.jupiter.api.Test
    void testTypeCode() {
        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage testMsg = new RawMessage(5, new ByteString(mAndC));
        Long longy = 9873914844636064L;
        assertEquals(Bits.extractUInt(longy, 51, 5), testMsg.typeCode());
    }

    @org.junit.jupiter.api.Test
    void timeStampNs() {

        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);

        assertThrows(IllegalArgumentException.class, () -> new RawMessage(-2, new ByteString(mAndC)));
    }

    @org.junit.jupiter.api.Test
    void bytes() {

        String mS = "8D4B18F4231445F2DB63A0";
        String cS = "DEEB82";
        byte[] mAndC = HexFormat.of().parseHex(mS + cS);

        assertThrows(IllegalArgumentException.class, () -> new RawMessage(2, new ByteString(new byte[] {0b10001})));
    }

    private static final long timeStampNs = 1L;


    @org.junit.jupiter.api.Test
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(-1, new ByteString(new byte[14])));
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(0, new ByteString(new byte[10])));
    }


    @org.junit.jupiter.api.Test
    void testIcaoAddress() {
        var bytes1 = HexFormat.of().parseHex("8D4B17E5F8210002004BB8B1F1AC");
        var byteString1 = new ByteString(bytes1);
        var rawMessage1 = new RawMessage(timeStampNs,byteString1);
        assertEquals(new IcaoAddress("4B17E5"),rawMessage1.icaoAddress());

        var bytes2 = HexFormat.of().parseHex("8D49529958B302E6E15FA352306B");
        var byteString2 = new ByteString(bytes2);
        var rawMessage2 = new RawMessage(timeStampNs,byteString2);
        assertEquals(new IcaoAddress("495299"),rawMessage2.icaoAddress());
    }


    @org.junit.jupiter.api.Test
    void testDownLinkFormat() {
        var validBytes = HexFormat.of().parseHex("8D4B17E5F8210002004BB8B1F1AC");
        var validByeString = new ByteString(validBytes);
        var validRawMessage = new RawMessage(timeStampNs,validByeString);
        assertEquals(17,validRawMessage.downLinkFormat());

        var invalidBytes = HexFormat.of().parseHex("7A6E944F3AD0CC5A3101CC7299D8");
        var invalidString = new ByteString(invalidBytes);
        var invalidRawMessage = new RawMessage(timeStampNs,invalidString);
        assertEquals(15,invalidRawMessage.downLinkFormat());
    }


    @org.junit.jupiter.api.Test
    void testPayload() {
        var bytes = HexFormat.of().parseHex("8D4B17E5F8210002004BB8B1F1AC");
        var byteString = new ByteString(bytes);
        var RawMessage = new RawMessage(timeStampNs,byteString);
        assertEquals(69842078141533112L,RawMessage.payload());
    }


    @org.junit.jupiter.api.Test
    void testStaticTyeCode() {
        var payload = 0b11111000001000010000000000000010000000000100101110111000L;
        assertEquals(31, javions.adsb.RawMessage.typeCode(payload));
    }


    @org.junit.jupiter.api.Test
    void testTypeCode2() {
        var bytes = HexFormat.of().parseHex("8D4B17E5F8210002004BB8B1F1AC");
        var byteString = new ByteString(bytes);
        var RawMessage = new RawMessage(timeStampNs,byteString);
        assertEquals(31,RawMessage.typeCode());
    }

    public static final int RAW_MESSAGE_LENGTH = 14;
    public byte[] validBytes;
    public ByteString validByteString;
    public RawMessage rawMessage;
    public long validHorodatage;


    @BeforeEach
    void setUp() {
        validBytes = new byte[RAW_MESSAGE_LENGTH];
        validByteString = new ByteString( validBytes );
        validHorodatage = 100;

        rawMessage = new RawMessage( validHorodatage, validByteString );
    }


    @org.junit.jupiter.api.Test
    void testConstructorThrowsOnNegativeHorodatage() {
        long invalidHorodatage = -1;

        assertThrows( IllegalArgumentException.class, () -> new RawMessage( invalidHorodatage, validByteString ) );
    }


    @org.junit.jupiter.api.Test
    void testConstructorThrowsOnInvalidByteArrayLentgh() {
        byte[] invalidBytes = new byte[RAW_MESSAGE_LENGTH - 1];
        ByteString invalidByteString = new ByteString( invalidBytes );
        long validHorodatage = 10;

        assertThrows( IllegalArgumentException.class, () -> new RawMessage( validHorodatage, invalidByteString ) );
    }


    @org.junit.jupiter.api.Test
    void testConstructorDoenstThrowOnValidArguments() {

        assertDoesNotThrow( () -> new RawMessage( validHorodatage, validByteString ) );

        long veryBigHorodatage = 1L << 63 - 1;
        assertDoesNotThrow( () -> new RawMessage( veryBigHorodatage, validByteString ) );

        long nullHorodatage = 0;
        assertDoesNotThrow( () -> new RawMessage( nullHorodatage, validByteString ) );
    }


    @org.junit.jupiter.api.Test
    void testLengthVariableIsCorrect() {

        assertEquals( 14, RawMessage.LENGTH );
    }


    @org.junit.jupiter.api.Test
    void testOfReturnsTheCorrectMessageAndHorodatage() {
        byte[][] bytes = new byte[][]{
                {(byte)0x8D, (byte)0x39, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0xFC, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x18, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0x99, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7B, (byte)0xB4,
                        (byte)0xC0, (byte)0x04, (byte)0x29, (byte)0x03, (byte)0x5D, (byte)0x98},
                {(byte)0x8D, (byte)0xB1, (byte)0x2A, (byte)0x64, (byte)0x19, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5C, (byte)0xB8},
                {(byte)0x8D, (byte)0xBA, (byte)0x0A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xA5,
                        (byte)0xC0, (byte)0x84, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8}};

        ByteString[] expectedByteString = new ByteString[bytes.length];

        for ( int i = 0 ; i < expectedByteString.length ; i++ ) {
            expectedByteString[i] = new ByteString( bytes[i] );
        }

        for ( int i = 0 ; i < bytes.length ; i++ ) {
            RawMessage expectedRawMessage = new RawMessage( validHorodatage, expectedByteString[i] );
            RawMessage actualRawMessage = RawMessage.of( validHorodatage, bytes[i] );

            assertEquals( expectedRawMessage, actualRawMessage );
        }
    }


    @org.junit.jupiter.api.Test
    void testOfRetrunsNullForInvalidCrc() {
        byte[][] bytes = new byte[][]{
                {(byte)0x8D, (byte)0x56, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x0A, (byte)0x03, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0xFC, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x18, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x34, (byte)0x01, (byte)0x8D, (byte)0xB8},
                {(byte)0x8D, (byte)0x99, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7B, (byte)0xB4,
                        (byte)0xC0, (byte)0x04, (byte)0x29, (byte)0x03, (byte)0x6D, (byte)0x4E},
                {(byte)0x67, (byte)0xB1, (byte)0x3A, (byte)0x64, (byte)0x19, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5D, (byte)0x28},
                {(byte)0x8D, (byte)0xBB, (byte)0x0A, (byte)0xE4, (byte)0x49, (byte)0x10, (byte)0x7F, (byte)0xA5,
                        (byte)0xC0, (byte)0x84, (byte)0x79, (byte)0x03, (byte)0x5F, (byte)0xB8}};

        for ( int i = 0 ; i < bytes.length ; i++ ) {
            RawMessage actualRawMessage = RawMessage.of( validHorodatage, bytes[i] );

            assertEquals( null, actualRawMessage );
        }
    }


    @org.junit.jupiter.api.Test
    void testSizeReturnsZeroIfInvalidDf() {
        byte theByte = 0x00;
        for ( int i = 0 ; i < ( 2 * Byte.SIZE ) * ( 2 * Byte.SIZE ) ; i++ ) {
            if ( theByte != 0x11 ) {
                assertEquals( 0, rawMessage.size( theByte ) );
                theByte++;
            }
        }
    }


    @org.junit.jupiter.api.Test
    void testSizeReturnsCorrectLengthForValidDf() {
        int expectedValue = 17;
        byte theByte = 0x11;
        assertEquals( expectedValue, theByte );
    }


    @org.junit.jupiter.api.Test
    void testDownLinkFormatReturnsCorrectValue() {
        int expectedValue = 0b10001;
        byte[][] bytes = new byte[][]{
                {(byte)0x8D, (byte)0x39, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0xFC, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x18, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0x99, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7B, (byte)0xB4,
                        (byte)0xC0, (byte)0x04, (byte)0x29, (byte)0x03, (byte)0x5D, (byte)0x98},
                {(byte)0x8D, (byte)0xB1, (byte)0x2A, (byte)0x64, (byte)0x19, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5C, (byte)0xB8},
                {(byte)0x8D, (byte)0xBA, (byte)0x0A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xA5,
                        (byte)0xC0, (byte)0x84, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8}};

        for ( int i = 0 ; i < bytes.length ; i++ ) {
            ByteString byteString = new ByteString( bytes[i] );
            RawMessage rawMessage1 = new RawMessage( validHorodatage, byteString );

            int actualValue = rawMessage1.downLinkFormat();
            assertEquals( expectedValue, actualValue );
        }
    }


    @org.junit.jupiter.api.Test
    void testIcaoAddressReturnsCorrectValue() {

        byte[][] icaoAddresses = new byte[][]{{(byte)0x39, (byte)0x2A, (byte)0xE4},
                {(byte)0xFC, (byte)0x2A, (byte)0xE4},
                {(byte)0x99, (byte)0x2A, (byte)0xE4},
                {(byte)0xB1, (byte)0x2A, (byte)0x64},
                {(byte)0xBA, (byte)0x0A, (byte)0xE4}};
        byte[][] bytes = new byte[][]{
                {(byte)0x8D, (byte)0x39, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0xFC, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x18, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0x99, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7B, (byte)0xB4,(byte)0xC0, (byte)0x04, (byte)0x29, (byte)0x03, (byte)0x5D, (byte)0x98},
                {(byte)0x8D, (byte)0xB1, (byte)0x2A, (byte)0x64, (byte)0x19, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5C, (byte)0xB8},
                {(byte)0x8D, (byte)0xBA, (byte)0x0A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xA5,
                        (byte)0xC0, (byte)0x84, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8}};

        HexFormat hexFormat = HexFormat.of();

        for ( int i = 0 ; i < icaoAddresses.length ; i++ ) {
            ByteString expectedByteString = new ByteString( icaoAddresses[i] );
            int expectedNumberOfHex = 6;
            long expectedBytes = expectedByteString.bytesInRange( 0, expectedByteString.size() );
            IcaoAddress expectedIcaoAddress = new IcaoAddress( hexFormat.withUpperCase()
                    .toHexDigits( expectedBytes,
                            expectedNumberOfHex ) );

            ByteString actualByteString = new ByteString( bytes[i] );
            RawMessage actualRawMessage = new RawMessage( validHorodatage, actualByteString );
            IcaoAddress actualIcaoAddress = actualRawMessage.icaoAddress();

            assertEquals( expectedIcaoAddress, actualIcaoAddress );
        }
    }


    @org.junit.jupiter.api.Test
    void testPayloadReturnsCorrectValue() {
        byte[][] mes = new byte[][]{
                {(byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xB5, (byte)0xC0, (byte)0x04, (byte)0x39},
                {(byte)0x99, (byte)0x18, (byte)0x7F, (byte)0xB5, (byte)0xC0, (byte)0x04, (byte)0x39},
                {(byte)0x99, (byte)0x10, (byte)0x7B, (byte)0xB4, (byte)0xC0, (byte)0x04, (byte)0x29},
                {(byte)0x19, (byte)0x10, (byte)0x7F, (byte)0xB5, (byte)0xC0, (byte)0x04, (byte)0x39},
                {(byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xA5, (byte)0xC0, (byte)0x84, (byte)0x39}};
        byte[][] bytes = new byte[][]{
                {(byte)0x8D, (byte)0x39, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0xFC, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x18, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0x99, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7B, (byte)0xB4,
                        (byte)0xC0, (byte)0x04, (byte)0x29, (byte)0x03, (byte)0x5D, (byte)0x98},
                {(byte)0x8D, (byte)0xB1, (byte)0x2A, (byte)0x64, (byte)0x19, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5C, (byte)0xB8},
                {(byte)0x8D, (byte)0xBA, (byte)0x0A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xA5,
                        (byte)0xC0, (byte)0x84, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8}};

        for ( int i = 0 ; i < mes.length ; i++ ) {
            ByteString expectedByteString = new ByteString( mes[i] );
            long expectedPayload = expectedByteString.bytesInRange( 0, expectedByteString.size() );

            ByteString actualByteString = new ByteString( bytes[i] );
            RawMessage actualRawMessage = new RawMessage( validHorodatage, actualByteString );
            long actualPayload = actualRawMessage.payload();

            assertEquals( expectedPayload, actualPayload );
        }
    }


    @org.junit.jupiter.api.Test
    void testTypeCodeReturnsCorrectValue2() {
        byte[] typeCodes = new byte[]{(byte)0b10011, (byte)0b10011, (byte)0b10011, (byte)0b00011, (byte)0b10011};
        byte[][] bytes = new byte[][]{
                {(byte)0x8D, (byte)0x39, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8},
                {(byte)0x8D, (byte)0xFC, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x18, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5D, (byte)0xB8},{(byte)0x8D, (byte)0x99, (byte)0x2A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7B, (byte)0xB4,
                (byte)0xC0, (byte)0x04, (byte)0x29, (byte)0x03, (byte)0x5D, (byte)0x98},
                {(byte)0x8D, (byte)0xB1, (byte)0x2A, (byte)0x64, (byte)0x19, (byte)0x10, (byte)0x7F, (byte)0xB5,
                        (byte)0xC0, (byte)0x04, (byte)0x39, (byte)0x01, (byte)0x5C, (byte)0xB8},
                {(byte)0x8D, (byte)0xBA, (byte)0x0A, (byte)0xE4, (byte)0x99, (byte)0x10, (byte)0x7F, (byte)0xA5,
                        (byte)0xC0, (byte)0x84, (byte)0x39, (byte)0x03, (byte)0x5D, (byte)0xB8}};

        for ( int i = 0 ; i < typeCodes.length ; i++ ) {

            long expectedTypeCode = typeCodes[i];

            ByteString actualByteString = new ByteString( bytes[i] );
            RawMessage actualRawMessage = new RawMessage( validHorodatage, actualByteString );
            long actualTypeCode = actualRawMessage.typeCode();

            assertEquals( expectedTypeCode, actualTypeCode );
        }
    }
}