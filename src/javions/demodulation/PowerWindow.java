package javions.demodulation;

import javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * represents a window of fixed size over a sequence of power samples produced by a power calculator. Since the window has a
 * size that is less than or equal to one batch, it is possible for it to overlap two batches, but not more. Therefore,
 * PowerWindow must always manage two arrays each containing a batch, stored in private attributes of the class. The first of
 * these tables is intended to contain the batches of even index, the other those of odd index.
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class PowerWindow {

    /**
     * the table that contains the batches of odd index
     */
    private int[] secondTableOfPower;
    /**
     * the array that contains the batches of even index
     */
    private int[] firstTableOfPower;
    /**
     * the size of the window
     */
    private final int windowSize;
    /**
     * the calculator of power
     */
    private final PowerComputer powerComputer;
    /**
     * the position of the window on all the batches (number of time we advance the window)
     */
    private long position;
    /**
     * the samples that we have access to (since the window can only advance and can't move back hence we loose a sample each
     * time the window advance)
     */
    private int samplesWeHaveAccessTo;
    /**
     * the position of the window in one batch, when the window is completely contained in a new batch this variable is
     * reinitialized to 0
     */
    private int headPos;
    /**
     * constant that represents the size of the power batch
     */
    private static final int POWER_BATCH_SIZE = 1 << 16;

    /**
     * returns a window of given size over the sequence of power samples calculated from the bytes provided by the given input
     * stream
     *
     * @param stream     stream of bytes coming from the AirSpy
     * @param windowSize the size of the window
     * @throws IOException in case of input/output error
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(windowSize > 0 && windowSize <= POWER_BATCH_SIZE);
        this.powerComputer = new PowerComputer(stream, POWER_BATCH_SIZE);// j'oublie pas de changer
        this.windowSize = windowSize;
        this.secondTableOfPower = new int[POWER_BATCH_SIZE];
        this.firstTableOfPower = new int[POWER_BATCH_SIZE];
        this.samplesWeHaveAccessTo = powerComputer.readBatch(firstTableOfPower);
        this.headPos = 0;
        this.position = 0;
    }

    /**
     * return the size of the window
     *
     * @return the size of the window
     */
    public int size() {
        return windowSize;
    }

    /**
     * return the position of the window
     *
     * @return the position of the window
     */
    public long position() {
        return position;
    }

    /**
     * returns the power sample at the given index of the window
     *
     * @param i the index of the window
     * @return the power sample at the given index of the window
     * @throws IndexOutOfBoundsException if this index is not between 0 (included) and window size (excluded)
     */
    public int get(int i) {
        Objects.checkIndex(i, windowSize);
        if (headPos + i >= POWER_BATCH_SIZE) return secondTableOfPower[headPos + i - POWER_BATCH_SIZE];
        return firstTableOfPower[headPos + i];
    }

    /**
     * returns true iff the window is full, i.e. that it contains as many samples as its size; this is always true,
     * except when the end of the sample stream has been reached, and the window passes it
     *
     * @return true iff the window is full
     */
    public boolean isFull() {
        return samplesWeHaveAccessTo >= windowSize;
    }

    /**
     * advance the window by one sample by adding one to the position and subtracting one from the samples we have access to,
     * adding one to the headPos and resetting it to 0 if the headPos is equal to the power batch size
     *
     * @throws IOException in case of input/output error
     */
    public void advance() throws IOException {
        --samplesWeHaveAccessTo;
        if (++headPos == POWER_BATCH_SIZE) {
            headPos = 0;
            int[] temp = firstTableOfPower;
            firstTableOfPower = secondTableOfPower;
            secondTableOfPower = temp;
        }
        if (headPos + windowSize - 1 == POWER_BATCH_SIZE) {
            samplesWeHaveAccessTo += powerComputer.readBatch(secondTableOfPower);
        }
        ++position;

    }

    /**
     * which advances the window by the given number of samples, as if the advance method had been called the given number of
     * times
     *
     * @param offset the number of times we call the method advance
     * @throws IOException              in case of input/output error
     * @throws IllegalArgumentException if this number is not positive or zero
     */
    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for (int i = 1; i <= offset; i++) {
            advance();
        }
    }
}
