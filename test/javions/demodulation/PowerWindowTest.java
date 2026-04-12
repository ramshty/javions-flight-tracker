package javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PowerWindowTest {

    @Test
    void returnTheGoodWindowSize() throws IOException {
        InputStream stream = new FileInputStream("samples.bin");
        PowerWindow powerWindow = new PowerWindow(stream,5);
        assertEquals(5, powerWindow.size());
    }

    @Test
    void position() throws IOException {
        InputStream stream = new FileInputStream("samples.bin");
        PowerWindow powerWindow = new PowerWindow(stream,5);
        for (int i = 0; i < 100; i++) {
            assertEquals(i,powerWindow.position());
            powerWindow.advance();
        }
    }

    @Test
    void get() throws IOException {//je change les tableaux à une taille de 8
        InputStream stream = new FileInputStream("samples.bin");
        PowerWindow powerWindow = new PowerWindow(stream,3);
        powerWindow.advanceBy(6);
        assertEquals(36818,powerWindow.get(2));
    }

    @Test
    void getIfTheWindowIsOn2Batches() throws IOException {
        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < (Math.scalb(1,16) -1200) * 4; i+=2) {
            bytes.add((byte) 0);
            bytes.add((byte) 8);
        }
        InputStream stream = new FileInputStream("samples.bin");
        byte[] bytes1 = new byte[4804];

        int readBatch = stream.readNBytes(bytes1,0,bytes1.length);

        for (int i = 0; i < bytes1.length; i++) {
            bytes.add(bytes1[i]);
        }
        byte[] bytesFinal = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesFinal[i]=bytes.get(i);
        }
        InputStream stream2 = new ByteArrayInputStream(bytesFinal);
        PowerWindow powerWindow = new PowerWindow(stream2,5);
        for (int i = 0; i < Math.scalb(1,16) -4; i++) {
            powerWindow.advance();
        }
        int[] tabTest =  {585,740,360,1130,3370};
        assertEquals(tabTest[0],powerWindow.get(4));
        powerWindow.advance();
        assertEquals(tabTest[0],powerWindow.get(3));
        powerWindow.advance();
        assertEquals(tabTest[0],powerWindow.get(2));
        powerWindow.advance();
        assertEquals(tabTest[0],powerWindow.get(1));
        powerWindow.advance();
        assertEquals(tabTest[0],powerWindow.get(0));
        powerWindow.advance();
        assertEquals(0,powerWindow.get(0));
    }

    @Test
    void isFullIfTheWindowIsOn2Batches() throws IOException {//je change les tableaux à une taille de 800
        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < (Math.scalb(1,16) -600) * 4; i+=2) {
            bytes.add((byte) 0);
            bytes.add((byte) 8);
        }
        InputStream stream = new FileInputStream("samples.bin");
        byte[] bytes1 = new byte[4804];

        int readBatch = stream.readNBytes(bytes1,0,bytes1.length);

        for (int i = 0; i < bytes1.length; i++) {
            bytes.add(bytes1[i]);
        }
        byte[] bytesFinal = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesFinal[i]=bytes.get(i);
        }
        InputStream stream2 = new ByteArrayInputStream(bytesFinal);
        PowerWindow powerWindow = new PowerWindow(stream2,5);
        for (int i = 0; i < Math.scalb(1,16) +597; i++) {
            powerWindow.advance();
        }
        assertFalse(powerWindow.isFull());
    }

    @Test
    void isFullIfTheStreamFulfillsOnlyOneBatch() throws IOException {
        InputStream stream = new FileInputStream("samples.bin");
        PowerWindow powerWindow = new PowerWindow(stream,5);
        for (int i = 0; i < 1197; i++) {
            powerWindow.advance();
        }
        assertFalse(powerWindow.isFull());
    }
    @Test
    void isFull() throws IOException {//je change les tableaux à une taille de 800
        InputStream stream = new FileInputStream("samples.bin");
        PowerWindow powerWindow = new PowerWindow(stream,5);
        for (int i = 0; i < 1197; i++) {
            if(i==797){
                powerWindow.isFull();
            }
            powerWindow.advance();
        }
        assertFalse(powerWindow.isFull());
    }



    @Test
    void advance() {

    }

    @Test
    void advanceBy() {
    }
}