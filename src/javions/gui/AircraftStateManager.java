package javions.gui;

import javions.adsb.*;
import javions.aircraft.AircraftData;
import javions.aircraft.AircraftDatabase;
import javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;

/**
 * this class is meant to keep the states of an aircraft set updated with the messages received from them
 */
public final class AircraftStateManager {
    private static final double ONE_MINUTE = 6e10;
    /**
     * represents a map that associates an aircraft state accumulator to the icao address of every aircraft
     * from whom a message has been received recently
     */
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> map = new HashMap<>();
    /**
     * represents an observable set of the states of the aircraft whose position is known
     */
    private final ObservableSet<ObservableAircraftState> knownState = FXCollections.observableSet();
    private final ObservableSet<ObservableAircraftState> statesCopy = FXCollections.unmodifiableObservableSet(knownState);
    private long lastMessageTimeStamps = 0;
    private final AircraftDatabase aircraftDatabase;

    /**
     * @param aircraftDatabase the database that contains fixed characteristics of the aircraft
     */
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftDatabase = aircraftDatabase;
    }

    /**
     * @return an observable but not modifiable set of observable aircraft states whose position is known
     */
    public ObservableSet<ObservableAircraftState> states() {
        return statesCopy;
    }

    /**
     * uses a received message to update the state of the aircraft that sent it
     * @param message represents a message that has been received
     * @throws IOException if input and output error
     */
    public void updateWithMessage(Message message) throws IOException {
        ObservableAircraftState state;
        IcaoAddress newIcaoAddress = message.icaoAddress();
        if (map.containsKey(newIcaoAddress)) {
            map.get(newIcaoAddress).update(message);
            state = map.get(newIcaoAddress).stateSetter();
        }
        else {
            AircraftData newAircraftData = aircraftDatabase.get(newIcaoAddress);
            state = new ObservableAircraftState(newIcaoAddress, newAircraftData);
            AircraftStateAccumulator<ObservableAircraftState> newStateAccumulator = new AircraftStateAccumulator<>(state);
            newStateAccumulator.update(message);
            map.put(newIcaoAddress, newStateAccumulator);
        }
        if (state.getPosition() != null) {
            knownState.add(state);
        }
        lastMessageTimeStamps = message.timeStampNs();
    }

    /**
     * deletes from the set of states all those from aircraft from which no message has been sent in the minute
     * preceding the reception of the last message given to updateWithMessage
     */
    public void purge() {
        knownState.removeIf(o -> lastMessageTimeStamps - o.getLastMessageTimeStampNs() > ONE_MINUTE);
        map.keySet().removeIf(o -> lastMessageTimeStamps - map.get(o).stateSetter().getLastMessageTimeStampNs() > ONE_MINUTE);
    }


}
