package javions;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class Crc24Test {

    @Test
    void crc24TestBitWise(){
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        String mS = "8D392AE499107FB5C00439";
        String cS = "035DB8";
        int c = Integer.parseInt(cS, 16); // == 0x035DB8

        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        assertEquals(0, crc24.crc(mAndC));

        byte[] mOnly = HexFormat.of().parseHex(mS);
        assertEquals(c, crc24.crc(mOnly));
    }
}