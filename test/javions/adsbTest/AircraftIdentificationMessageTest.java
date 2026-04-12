package javions.adsbTest;

import javions.ByteString;
import javions.Crc24;
import javions.adsb.AircraftIdentificationMessage;
import javions.adsb.CallSign;
import javions.adsb.RawMessage;
import javions.aircraft.IcaoAddress;
import javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftIdentificationMessageTest {
    @Test
    void constructorThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(200, null,0xA3, new CallSign("12345678")));
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(200, new IcaoAddress("FFFF01"),0xA3,null));
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(200, null,0xA3, null));
    }


    @Test
    void constructorThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftIdentificationMessage(-6,  new IcaoAddress("FFFF01"),0xA3, new CallSign("12345678")));
    }

    @Test
    void ofReturnsNull() {
        byte[] RawMessage = HexFormat.of().parseHex("8D392AE89B00009570AC00DDDBE5");
        byte[] invalidMessage = new byte[RawMessage.length];
        System.arraycopy(RawMessage, 0, invalidMessage, 0, 4);
        for(int i =0; i<7; ++i){
            invalidMessage[i+4] = 0;
        }
        if (invalidMessage.length - 11 >= 0)
            System.arraycopy(RawMessage, 11, invalidMessage, 11, invalidMessage.length - 11);

        RawMessage expected = new RawMessage(200, new ByteString(invalidMessage));
        assertNull(AircraftIdentificationMessage.of(expected));
    }

    @Test
    void ofReturnsCorrectRawMessage() {
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        byte[] mAndC = HexFormat.of().parseHex("8D392AE499107FB5C00439035DB8");
        System.out.println(crc24.crc(mAndC));
    }

    // other people's tests
    @Test
    public void testAircraftIdentificationMessage() throws IOException {

        //String stream2 = Objects.requireNonNull(getClass().getResource("/samples_20230304_1442.bin")).getFile();
        //stream2 = URLDecoder.decode(stream2, StandardCharsets.UTF_8);
        try (InputStream s = new FileInputStream("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\samples_20230304_1442.bin")) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int count0 = 0;
            int count1 = 0;
            while ((m = d.nextMessage()) != null) {
                if (AircraftIdentificationMessage.of(m) != null) {
                    count0++;
                    if (m.typeCode() == 1 || m.typeCode() == 2 || m.typeCode() == 3 || m.typeCode() == 4) {
                        count1++;
                        System.out.println(AircraftIdentificationMessage.of(m));
                    }
                }
            }
            //number of messages before typeCode checking
            assertEquals(29, count0);
            //number of messages after typeCode checking
            assertEquals(14, count1);
        }
    }

    @Test
    void aircraftIdentificationMessageCallSignWorksForKnownMessages() {
        record MessageAndCallSign(String message, String callSign) {
        }

        var messagesAndCallSigns = List.of(
                new MessageAndCallSign("8D3991E1230464B1CD4320FCD23B", "AFR13TL"),
                new MessageAndCallSign("8F394C09200464B3D546A059C5C9", "AFR35TZ"),
                new MessageAndCallSign("8D406CA3230570F1DF1820BADDB9", "AWC171"),
                new MessageAndCallSign("8D44CE73230853324C38207EDEE4", "BEL2SC"),
                new MessageAndCallSign("8D4B1931220C16B9C3182021D5D1", "CAZ901"),
                new MessageAndCallSign("8D503E6A230C3373D41520E93419", "CCM35AT"),
                new MessageAndCallSign("8DA4C70325101336DA0820B4B812", "DAL66"),
                new MessageAndCallSign("8D44083C2314A573CB6060F4E8C4", "EJU326A"),
                new MessageAndCallSign("8D4400902314A579D504609ABEB8", "EJU95PQ"),
                new MessageAndCallSign("8E383F3C1C18A096620820918150", "FJBVX"),
                new MessageAndCallSign("8D461981211931B5CB3660E09D00", "FSF523Y"),
                new MessageAndCallSign("8D4D00C622307338CB5D201112C1", "LGL8254"),
                new MessageAndCallSign("8D06004025341575C31D203EBA81", "MAU5014"),
                new MessageAndCallSign("8D4CA805234994B81588206F63A2", "RYR8EX"),
                new MessageAndCallSign("8D4CAF7E234C14F2D78DE075A8A2", "SAS2587"),
                new MessageAndCallSign("8D3C0CA323515271D37820FC5632", "TUI147"));

        for (var messageAndCallSign : messagesAndCallSigns) {
            var hf = HexFormat.of();
            var message = hf.parseHex(messageAndCallSign.message());
            var callSign = messageAndCallSign.callSign();
            var rawMessage = RawMessage.of(100, message);
            assertNotNull(rawMessage);
            var aircraftIdentificationMessage = AircraftIdentificationMessage.of(rawMessage);
            assertNotNull(aircraftIdentificationMessage);
            assertEquals(callSign, aircraftIdentificationMessage.callSign().string());
        }
    }

    @Test
    public void constructorThrowsOnNullICAO() {
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(1499146900, null, 163, new CallSign("RYR7JD")));
    }

    @Test
    public void constructorThrowsOnNullCallSign() {
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(1499146900, new IcaoAddress("4D2228"), 163, null));
    }

    @Test
    public void constructorThrowsOnNegativeTimeStamp() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftIdentificationMessage(-1, new IcaoAddress("4D2228"), 163, new CallSign("RYR7JD")));
    }
}
