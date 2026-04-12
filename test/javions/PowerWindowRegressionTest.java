package javions;

import javions.demodulation.PowerWindow;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class PowerWindowRegressionTest {



    @Test
    void checkIsFull() throws IOException {
        InputStream file = new FileInputStream("samples.bin");
        PowerWindow powerWindow = new PowerWindow(file, 2);
        powerWindow.advanceBy(1198);
        assertTrue(powerWindow.isFull());
        file.close();

    }

    @Test
    public void testGetValidIndex() throws IOException {
        // Advance far enough to exercise reads across buffer boundaries.
        int[] tab = new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722};
        int[] tab1 = new int[]{36818, 23825, 10730, 1657, 1285, 1280, 394, 521};
        int[] tab2 = new int[]{1370, 200, 292, 290, 106, 116, 194, 64};
        int[] tab3 = new int[]{37, 50, 149, 466, 482, 180, 148, 5576};
        InputStream stream = new FileInputStream("samples.bin");
        int windowSize = 5;
        PowerWindow window = new PowerWindow(stream, windowSize);

        assertEquals(tab[0], window.get(0));
        assertEquals(tab[1], window.get(1));
        assertEquals(tab[2], window.get(2));
        assertEquals(tab[3], window.get(3));
        assertEquals(tab[4], window.get(4));

        window.advanceBy(8);
        assertEquals(tab1[0], window.get(0));
        assertEquals(tab1[1], window.get(1));
        assertEquals(tab1[2], window.get(2));
        assertEquals(tab1[3], window.get(3));
        assertEquals(tab1[4], window.get(4));

        window.advanceBy(8);
        assertEquals(tab2[0], window.get(0));
        assertEquals(tab2[1], window.get(1));
        assertEquals(tab2[2], window.get(2));
        assertEquals(tab2[3], window.get(3));
        assertEquals(tab2[4], window.get(4));

        window.advanceBy(6);
        assertEquals(tab2[6], window.get(0));
        assertEquals(tab2[7], window.get(1));
        assertEquals(tab3[0], window.get(2));
        assertEquals(tab3[1], window.get(3));
        assertEquals(tab3[2], window.get(4));

    }
    @Test
    public void testIsFull() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[80]);
        // 80 bytes yield 40 decoded samples and 10 power samples.
        int windowSize = 5;
        PowerWindow window = new PowerWindow(stream, windowSize);
        window.advanceBy(8);
        assertTrue(window.isFull());

        window.advanceBy(8);
        assertFalse(window.isFull());

    }
    @Test
    void getWorksWithTableIntersection() throws IOException {
        InputStream file = new FileInputStream("samples.bin");
        PowerWindow powerWindow = new PowerWindow(file, 2);
        powerWindow.advanceBy(8);
        assertEquals(23825, powerWindow.get(1));
        file.close();
    }

    @Test
    void getWorksWithSingleTable() throws IOException {
        InputStream file = new FileInputStream("samples.bin");
        PowerWindow powerWindow = new PowerWindow(file, 2);
        powerWindow.advance();
        assertEquals(65, powerWindow.get(1));
        file.close();
    }

    @Test
    void isFullWorksWithLastValidElement() throws IOException {
        try (InputStream testFile = new FileInputStream("samples.bin");) {
            PowerWindow powerWindow = new PowerWindow(testFile, 2);
            powerWindow.advanceBy(1198);
            assertTrue(powerWindow.isFull());
        }
    }

    @Test
    void isFullWorksWithInvalidElement() throws IOException {
        try (InputStream file = new FileInputStream("samples.bin");) {
            PowerWindow powerWindow = new PowerWindow(file, 2);
            powerWindow.advanceBy(1201);
            assertFalse(powerWindow.isFull());
        }
    }

    @Test
    void constructorFailsWithInvalidWindowSize() throws IOException {
        try(InputStream stream = new FileInputStream("samples.bin")) {
            assertThrows(IllegalArgumentException.class, () -> new PowerWindow(stream, (int) Math.pow(2, 16) + 1));
        }
    }

    @Test
    void getFailsWithIllicitIndex() throws IOException{
        try(InputStream stream = new FileInputStream("samples.bin")) {
            PowerWindow powerWindow = new PowerWindow(stream, 2);
            assertThrows(IndexOutOfBoundsException.class, () -> powerWindow.get(3));
        }
    }
}
