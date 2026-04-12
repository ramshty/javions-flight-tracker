package javions.gui;
import javions.ByteString;
import javions.adsb.Message;
import javions.adsb.MessageParser;
import javions.adsb.RawMessage;
import javions.aircraft.AircraftData;
import javions.aircraft.AircraftDatabase;
import javions.aircraft.AircraftRegistration;
import javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Comparator;

public class AircraftStateManagerTest {


    @Test
    void findAName() {
        AircraftDatabase aircraftDatabase = new AircraftDatabase("aircraft.zip");
        AircraftStateManager aircraftStateManager = new AircraftStateManager(aircraftDatabase);

        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("messages_20230318_0915.bin")))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                Message analyzedMessage = MessageParser.parse(rawMessage);
                assert analyzedMessage != null;
                aircraftStateManager.updateWithMessage(analyzedMessage);
                IcaoAddress icaoAddress = analyzedMessage.icaoAddress();
                AircraftData aircraftData = aircraftDatabase.get(icaoAddress);
                AircraftRegistration aircraftRegistration = aircraftData.registration();
                String model = aircraftData.model();
                //System.out.printf("%13d: %s\n", timeStampNs, message);
            }
        } catch (IOException e) { /* nothing to do */ }

    }

    private static class AddressComparator
            implements Comparator<ObservableAircraftState> {
        @Override
        public int compare(ObservableAircraftState o1,
                           ObservableAircraftState o2) {
            String s1 = o1.address().string();
            String s2 = o2.address().string();
            return s1.compareTo(s2);
        }
    }

}
