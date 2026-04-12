package javions.adsb;

import javions.ByteString;
import javions.GeoPos;
import javions.Units;
import javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class AirbornePositionMessageTest {

    @Test
    void of()throws IOException{
            try (InputStream s = new FileInputStream("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\samples_20230304_1442.bin")) {
                AdsbDemodulator d = new AdsbDemodulator(s);
                RawMessage m;
                byte[] message = {(byte) 0x8D,0x39,0x20,0x35,0x59, (byte) 0xB2,0x25, (byte) 0xF0,0x75,0x50, (byte) 0xAD, (byte) 0xBE,0x32, (byte) 0x8F};
                byte[] message2 = {(byte) 0x8D, (byte) 0xAE,0x02, (byte) 0xC8,0x58,0x64, (byte) 0xA5, (byte) 0xF5, (byte) 0xDD,0x49,0x75, (byte) 0xA1, (byte) 0xA3, (byte) 0xF5};
                RawMessage m1 = new RawMessage(0,new ByteString(message));
                RawMessage m2 = new RawMessage(0,new ByteString(message2));
                System.out.println(AirbornePositionMessage.of(m1));
                System.out.println(AirbornePositionMessage.of(m2));
                int a = 0;
                while ((m = d.nextMessage()) != null){
                    m = d.nextMessage();
                    if(AirbornePositionMessage.of(m)!=null) {
                        System.out.println(AirbornePositionMessage.of(m));
                        ++a;
                    }
                }
                System.out.println(a);
            }
        }
        @Test
    void nextMessageReturnsCorrectMessage() throws IOException {
        int i = 0;
            byte[] message = {(byte) 0x8D,0x39,0x20,0x35,0x59, (byte) 0xB2,0x25, (byte) 0xF0,0x75,0x50, (byte) 0xAD, (byte) 0xBE,0x32, (byte) 0x8F};
            byte[] message2 = {(byte) 0x8D, (byte) 0xAE,0x02, (byte) 0xC8,0x58,0x64, (byte) 0xA5, (byte) 0xF5, (byte) 0xDD,0x49,0x75, (byte) 0xA1, (byte) 0xA3, (byte) 0xF5};
            RawMessage m1 = new RawMessage(0,new ByteString(message));
            RawMessage m2 = new RawMessage(0,new ByteString(message2));
            System.out.println(AirbornePositionMessage.of(m1));
            System.out.println(AirbornePositionMessage.of(m2));
        try (InputStream s = new FileInputStream("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\samples_20230304_1442.bin")) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null){
                if ((m.typeCode() >= 9 && m.typeCode() <= 18) || (m.typeCode() >= 20 && m.typeCode() <= 22)){
                if(AirbornePositionMessage.of(m)!=null) {
                    System.out.println(AirbornePositionMessage.of(m));
                    ++i;
                }
                //System.out.println(m.icaoAddress());
            }
            }
        }
        System.out.println(i);
    }
    @Test
    void testDecodePosition() {
        GeoPos pos1 = CprDecoder.decodePosition(0.62,0.42,0.6200000000000000001,0.4200000000000000001,0);
        System.out.println(pos1);

        final double DIVIDER = Math.pow(2, 17);
        double x0 = 111600 / DIVIDER;
        double y0 = 94445 / DIVIDER;
        double x1 = 108865 / DIVIDER;
        double y1 = 77558 / DIVIDER;
        double expectedLat0 = Units.convertFrom((1.0 / 60) * (7 + y0), Units.Angle.TURN);
        double expectedLat1 = Units.convertFrom((1.0 / 59) * (7 + y1), Units.Angle.TURN);
        double expectedLon0 = Units.convertFrom((1.0 / 41) * x0, Units.Angle.TURN);
        double expectedLon1 = Units.convertFrom((1.0 / 40) * x1, Units.Angle.TURN);
        int mostRecent = 0;

        GeoPos pos = CprDecoder.decodePosition(x0, y0, x1, y1, mostRecent);

        assertNotNull(pos);
        assertEquals(expectedLat0, pos.latitude(), 1e-9);
        assertEquals(expectedLon0, pos.longitude(), 1e-9);

        mostRecent = 1;

        pos = CprDecoder.decodePosition(x0, y0, x1, y1, mostRecent);

        assertNotNull(pos);
        assertEquals(expectedLat1, pos.latitude(), 1e-9);
        assertEquals(expectedLon1, pos.longitude(), 1e-9);

        mostRecent = 2;

        int finalMostRecent = mostRecent;
        assertThrows(IllegalArgumentException.class, () -> {            CprDecoder.decodePosition(x0, y0, x1, y1, finalMostRecent);
        });
    }

    @Test
    public void testImpairLongitudeZones() {
        double lat = 37.0;
        double lon = -120.0;
        GeoPos cprResult = CprDecoder.decodePosition(lat, lon,lat,lat,1);
        assertNull(cprResult);
    }

    @Test
    void decodePositionWorksOnTheGivenExample(){
        double A = Math.acos(5);
        System.out.println(A);
        double x0 = Math.scalb(111600d, -17);
        double y0 = Math.scalb(94445d, -17);
        double x1 = Math.scalb(108865d, -17);
        double y1 = Math.scalb(77558d, -17);
        int mostRecent = 0;
        GeoPos actual = CprDecoder.decodePosition(x0,y0,x1,y1,mostRecent);
        GeoPos expected = new GeoPos(89192898,552659081);
        assertEquals(552659081 , actual.latitudeT32());
        assertEquals(89192898 , actual.longitudeT32());
        assertEquals(expected,actual);
    }

    @Test
    void updateBug(){
        double x0 = 0.64007568359375;
        double y0 = 0.6192169189453125;
        double x1 = 0.6243515014648438;
        double y1 = 0.4921417236328125;
        int mostRecent = 0;
        GeoPos actual = CprDecoder.decodePosition(x0,y0,x1,y1,mostRecent);
        System.out.println(actual);
    }
    }
