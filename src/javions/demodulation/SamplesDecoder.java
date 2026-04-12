package javions.demodulation;


import javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * represents a “sample decoder”, i.e. an object able of transforming bytes coming from
 * the AirSpy into signed 12-bit samples.
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class SamplesDecoder {
    private final static int FACTOR_2_BYTES_EXTRACTED = 2;
    /**
     * stream of bytes that contains the bytes coming from the AirSpy
     */
    private final InputStream stream;
    /**
     * the size of the sample's batch
     */
    private final int batchSize;
    /**
     * the array that contains the bytes extracted from the stream of bytes
     */
    private final byte[] bytesExtracted;
    /**
     * constant that we subtract from the samples to make them signed
     */
    private static final int UNSIGNED_TO_SIGNED = 1 << 11;

    /**
     * returns a sample decoder using the given input stream to get the bytes from the AirSpy radio and outputting the samples
     * in batches of given size
     *
     * @param stream    stream of bytes coming from the AirSpy
     * @param batchSize the size of the sample's batch
     * @throws IllegalArgumentException if the batch size isn't strictly positive
     * @throws NullPointerException     if the stream is null
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);
        this.batchSize = batchSize;
        this.stream = Objects.requireNonNull(stream);
        this.bytesExtracted = new byte[FACTOR_2_BYTES_EXTRACTED * batchSize];
    }

    /**
     * reads from the stream passed to the constructor the number of bytes corresponding to a batch, then converts these bytes
     * into signed samples, which are placed in the array passed as argument; the number of samples actually
     * converted is returned, and it is always equal to the batch size except when the end of the stream was reached before
     * it could read enough bytes, in which case it is equal to the number of bytes read divided by two, rounded down
     *
     * @param batch the array that will contain the samples signed
     * @return the number of samples actually converted
     * @throws IOException              in case of input/output error
     * @throws IllegalArgumentException if the size of the array passed as an argument is not equal to the size of a batch
     */
    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int bytesRead = stream.readNBytes(bytesExtracted, 0, bytesExtracted.length);
        int numberOfSamples = bytesRead / FACTOR_2_BYTES_EXTRACTED;
        for (int i = 0; i < bytesRead; i += FACTOR_2_BYTES_EXTRACTED) {
            int sampleUnsigned = (Byte.toUnsignedInt(bytesExtracted[i + 1]) << Byte.SIZE) | Byte.toUnsignedInt(bytesExtracted[i]);
            short sampleSigned = (short) (sampleUnsigned - UNSIGNED_TO_SIGNED);
            batch[i / FACTOR_2_BYTES_EXTRACTED] = sampleSigned;
        }
        return numberOfSamples;
    }
}
