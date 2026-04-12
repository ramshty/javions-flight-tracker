package javions.demodulation;

import javions.adsb.RawMessage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AdsbDemodulatorTest {
    @Test
     void nextMessageReturnsCorrectMessage() throws IOException {
        int i = 0;
        try (InputStream s = new FileInputStream("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\samples_20230304_1442.bin")) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null){
                ++i;
                System.out.println(m);
            }
        }
        System.out.println(i);
    }
/*
    @Test
    void nextMessageReturnsNull1() throws IOException {
        String f = "sampleNull.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            assertNull(d.nextMessage());
        }
    }

 */

    @Test
    void nextMessageReturnsNull() throws IOException {
        byte[] f = new byte[0];
        try (InputStream s = new ByteArrayInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            assertNull(d.nextMessage());
        }
     }



}
