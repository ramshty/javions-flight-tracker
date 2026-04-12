package javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class SamplesDecoderTest {
    @Test
    void SamplesDecoderTest() throws IOException {
        InputStream samples = new FileInputStream("samples.bin");
        SamplesDecoder decoder = new SamplesDecoder(samples, 10);
        short[] normalSamples = {-3,8,-9,-8,-5,-8,-12,-16,-23,-9};
        short[] batch = new short[10];
        int readBatch = decoder.readBatch(batch);
        for (int i = 0; i < 10; i++) {
            assertEquals(normalSamples[i],batch[i]);
        }
        samples.close();
        }

        @Test
    void detectAnArrayFullOfZero() throws IOException{
            byte[] bytes = new byte[200];
            for (int i = 0; i < 200; i+=2) {
                bytes[i]= 0;
                bytes[i+1]= 8;
            }
        InputStream stream = new ByteArrayInputStream(bytes);
            SamplesDecoder samplesDecoder = new SamplesDecoder(stream,100);
            short[] samples = new short[100];
            short[] normalSamples = new short[100];
            for (int i = 0; i < 100; i++) {
                normalSamples[i]=0;
            }
            int readBatch = samplesDecoder.readBatch(samples);
            for (int i = 0; i < 100; i++) {
                assertEquals(normalSamples[i],samples[i]);
            }
        }
}