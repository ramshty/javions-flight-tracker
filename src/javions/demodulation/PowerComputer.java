package javions.demodulation;

import javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * represents a "power calculator", i.e. an object capable of calculating the signal power samples from the signed samples
 * produced by a sample decoder. Like SamplesDecoder, PowerComputer works in batches, so the batch size is passed to its only
 * public constructor.
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class PowerComputer {
    /**
     * this constant depicts the factor 2 by which the power samples batch size is multiplied
     */
    private final static int FACTOR_2_SAMPLE_BATCH = 2;
    /**
     * the size of the sample's batch
     */
    private final int batchSize;
    /**
     * the array that contains the samples that will be converted into power samples
     */
    private final short[] sampleTable;
    /**
     * calculator of samples
     */
    private final SamplesDecoder samplesDecoder;

    /**
     * a constant that depicts the number of samples processed to calculate the power
     */
    private final static int NUMBER_OF_SAMPLES_PROCESSED = Byte.SIZE;//changer

    /**
     * the array that will contain the necessary samples to calculate the power
     */
    private final short[] samplesForComputePower = new short[NUMBER_OF_SAMPLES_PROCESSED];

    /**
     * returns a power calculator using the given input stream to get the AirSpy radio bytes and outputting power samples in
     * batches of given size
     *
     * @param stream    stream of bytes coming from the AirSpy
     * @param batchSize size of the power samples batch
     * @throws IllegalArgumentException if the batch size is not a multiple of 8 — the number of samples used for the moving
     *                                  average — strictly positive
     */
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize % NUMBER_OF_SAMPLES_PROCESSED == 0 && batchSize > 0);
        this.batchSize = batchSize;
        this.samplesDecoder = new SamplesDecoder(stream, FACTOR_2_SAMPLE_BATCH * batchSize);
        this.sampleTable = new short[FACTOR_2_SAMPLE_BATCH * batchSize];
    }

    /**
     * which reads from the sample decoder the number of samples needed to calculate a batch of power samples, then calculates
     * them using the formula given and places them in the array passed as an argument ; returns the number of power
     * samples placed in the array
     *
     * @param batch the array that will contain the power samples
     * @return the number of power samples placed in the array
     * @throws IOException              in case of input/output error
     * @throws IllegalArgumentException if the size of the array passed as an argument is not equal to the size of a batch
     */

    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int readSamplesBatch = samplesDecoder.readBatch(sampleTable);
        for (int i = 0; i < readSamplesBatch; i += FACTOR_2_SAMPLE_BATCH) {
            samplesForComputePower[i % Byte.SIZE] = sampleTable[i];
            samplesForComputePower[(i + 1) % Byte.SIZE] = sampleTable[i + 1];
            batch[i / FACTOR_2_SAMPLE_BATCH] = (int) (Math.pow(samplesForComputePower[2] - samplesForComputePower[4]
                    + samplesForComputePower[6] - samplesForComputePower[0], 2) + Math.pow(samplesForComputePower[3] -
                    samplesForComputePower[5]
                    + samplesForComputePower[7] - samplesForComputePower[1], 2));
        }
        return readSamplesBatch / FACTOR_2_SAMPLE_BATCH;
    }
}
