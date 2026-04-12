package javions.adsbTest;

import javions.ByteString;
import javions.adsb.RawMessage;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

public class RawMessageTest {

    byte[] byteTest = new byte[]{4, 5, 6, 1, 2, 3, 4, 5, 8, 7, 3, 4, 6, 4};
    byte[] wrongByteTest = new byte[]{4};
    @Test
    void constructorThrowsIllegalArgumentTimeStamps() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(-2, new ByteString(byteTest)));
    }

    @Test
    void constructorThrowsIllegalArgumentLength() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(2, new ByteString(wrongByteTest)));
    }

    @Test
    void ofReturnsNull() {
        assertNull(RawMessage.of(2, byteTest));
    }

    @Test
    void ofReturnsRightRawMessage() {
        String mS = "8D392AE499107FB5C00439";
        String cS = "035DB8";
        int c = Integer.parseInt(cS, 16); // == 0x035DB8

        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        RawMessage expected = new RawMessage(2, new ByteString(mAndC));
        assertEquals(expected, RawMessage.of(2, mAndC));

    }

    @Test
    void sizeReturnsLengthWithValidDF() {
        byte testing = (byte) (17 << 3);
        assertEquals(14, RawMessage.size(testing));
    }

    // check later all the remaining tests
    @Test
    void sizeReturns0WithInvalidDF() {
        assertEquals(0, RawMessage.size((byte) 58));
    }

    @Test
    void typeCodeReturnsCorrectType() {

    }

    @Test
    void downLinkFormatReturnsCorrectFormat() {

    }

    @Test
    void icaoAddressReturnsCorrectIcao() {

    }

    @Test
    void payloadReturnsCorrectME() {

    }
}

