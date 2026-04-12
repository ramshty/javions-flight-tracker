package javions.demodulation;

import javions.Preconditions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class PowerComputerTest {
    @Test
    void PowerComputerReturnCorrectValue() throws IOException {
        int batchSize = 1208;
        InputStream samples = new FileInputStream("samples.bin");
        PowerComputer powerComputer = new PowerComputer(samples, batchSize);
        int[] normalPower = {73,292,65,745,98,4226,12244,25722,36818,23825};
        int[] batch = new int[batchSize];
        int readBatch = powerComputer.readBatch(batch);
        for (int i = 0; i < 10; i++) {
            assertEquals(normalPower[i],batch[i]);
        }
        for (int i = 0; i < 1208; i++) {
            System.out.println(batch[i]);
        }
        assertEquals(1201,readBatch);
        samples.close();
    }

}