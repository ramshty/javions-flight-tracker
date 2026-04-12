package javions.adsb;

import javions.GeoPos;
import javions.Units;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {
/*
    @Test
    void decodePosition() {
        GeoPos position = CprDecoder.decodePosition(Math.scalb(111600,-17),Math.scalb(94445,-17),Math.scalb(108865,-17),Math.scalb(77558,-17),0);
        System.out.println(Units.convert(position.longitude(),Units.Angle.RADIAN,Units.Angle.DEGREE));
        System.out.println(Units.convert(position.latitude(),Units.Angle.RADIAN,Units.Angle.DEGREE));

        //assertEquals(7.476062,Units.convert(position.longitude(),Units.Angle.RADIAN,Units.Angle.DEGREE),1e-9);
        assertEquals(46.323349,Units.convert(position.latitude(),Units.Angle.RADIAN,Units.Angle.DEGREE),1e-9);

    }

 */

        private final static double DIVIDER = Math.scalb(1, 17);
        @Test
        void testDecodePosition() {
            GeoPos pos1 = CprDecoder.decodePosition(0.62,0.42,0.6200000000000000001,0.4200000000000000001,0);
            System.out.println(pos1);
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
            assertThrows(IllegalArgumentException.class, () -> {
                CprDecoder.decodePosition(x0, y0, x1, y1, finalMostRecent);
            });
        }

        @Test
        public void testImpairLongitudeZones() {
            double lat = 37.0;
            double lon = -120.0;
            GeoPos cprResult = CprDecoder.decodePosition(lat, lon,lat,lat,1);
            assertNull(cprResult);
        }
    }