package javions.demodulation;

import javions.ByteString;
import javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * represents an ADS-B message demodulator
 *
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public final class AdsbDemodulator {
    /**
     * represents the PowerWindow of size 1200 used to look for messages
     */
    private final PowerWindow powerWindow;
    /**
     * the pulse of a message or its preamble that are centered around multiples of 5 samples
     */
    private final static int PULSE = 5;
    /**
     * the number of samples a PowerWindow skips after finding a valid message since each message represents 1200 samples
     */
    private final static int MESSAGE_JUMP = 1200;

    /**
     * returns a demodulator getting the bytes of the samples' stream given as a parameter
     *
     * @param samplesStream the stream of power samples
     * @throws IOException in case of an error during the creation of a PowerWindow object
     */

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.powerWindow = new PowerWindow(samplesStream, 1200);
    }

    /**
     * @return the next ADS-B message of the samples' stream given to the constructor or null if no message is left, i.e.
     * if the end of the stream has been reached
     * @throws IOException in case of an input/output error
     */
    public RawMessage nextMessage() throws IOException {

        int sumOfPeaks, sumOfValleys, previousSum = 0, nextSum = 0;
        boolean sumCalculated = false;

        while (powerWindow.isFull()) {

            if (sumCalculated) {
                sumOfPeaks = nextSum;
                sumCalculated = false;
            } else sumOfPeaks = sumOfPeaks();

            sumOfValleys = sumOfValleys();
            byte[] bytes = new byte[RawMessage.LENGTH];

            if ((sumOfPeaks >= 2 * sumOfValleys)) {
                nextSum = nextSum();
                sumCalculated = true;

                if ((nextSum < sumOfPeaks) && (previousSum < sumOfPeaks)) {
                    long timeStampNs = powerWindow.position() * 100;

                    for (int j = 0; j < RawMessage.LENGTH; ++j) {
                        bytes[j] = byteTable(j * Byte.SIZE);
                    }

                    RawMessage rawMessage = new RawMessage(timeStampNs, new ByteString(bytes));

                    if (RawMessage.size(bytes[0]) == RawMessage.LENGTH && RawMessage.of(timeStampNs, bytes) != null) {
                        powerWindow.advanceBy(MESSAGE_JUMP);
                        return rawMessage;
                    }
                }
            }
            previousSum = sumOfPeaks;
            powerWindow.advance();
        }
        return null;
    }

    /**
     * @return the sum of "peaks" where the pulse should be
     */
    private int sumOfPeaks() {
        return (powerWindow.get(0) + powerWindow.get(PULSE * 2) + powerWindow.get(PULSE * 7) + powerWindow.get(PULSE * 9));
    }

    /**
     * @return the sum of "valleys" where nothing should be transmitted
     */
    private int sumOfValleys() {
        return (powerWindow.get(PULSE) + powerWindow.get(PULSE * 3) + powerWindow.get(PULSE * 4) +
                powerWindow.get(PULSE * 5) + powerWindow.get(PULSE * 6) + powerWindow.get(PULSE * 8));
    }

    /**
     * @return the sum of peaks but for one position further in the window
     */
    private int nextSum() {
        return (powerWindow.get(1) + powerWindow.get(11) + powerWindow.get(36) + powerWindow.get(46));
    }

    /**
     * decodes the bit at the given index according to the value of the power samples
     *
     * @param index the index of the bit whose value is determined
     * @return a 0 or a 1 for each bit
     */
    private byte demodulator(int index) {
        return (byte) ((powerWindow.get(80 + 10 * index) < powerWindow.get(85 + 10 * index)) ? 0 : 1);
    }

    /**
     * @param index the position of the next byte's first bit
     * @return the byte made of the last 8 decoded bits
     */
    private byte byteTable(int index) {
        byte returned = 0;
        for (int i = 0; i < Byte.SIZE; ++i) {
            returned = (byte) (returned | demodulator(index + i) << 7 - i);
        }
        return returned;
    }
}
