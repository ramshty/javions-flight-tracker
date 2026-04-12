package javions.gui;

import javions.GeoPos;
import javions.adsb.AircraftStateSetter;
import javions.adsb.CallSign;
import javions.aircraft.AircraftData;
import javions.aircraft.AircraftRegistration;
import javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

import static java.lang.Double.NaN;

/**
 * represents the state of an aircraft (javaFX bean)
 */
public final class ObservableAircraftState implements AircraftStateSetter {
    private final AircraftData aircraftData;
    private final IcaoAddress icaoAddress;
    private long lastTrajectoryAddedTimeStampNs;
    private final LongProperty lastMessageTimeStampNs;
    private final IntegerProperty category;
    private final ObjectProperty<CallSign> callSignProperty;
    private final ObjectProperty<GeoPos> positionProperty;
    private final ObservableList<AirbornePos> trajectoryModifiable;
    private final ObservableList<AirbornePos> trajectoryUnmodifiable;
    private final DoubleProperty altitude;
    private final DoubleProperty velocity;
    private final DoubleProperty trackOrHeading;

    /**
     * construct the current state of an aircraft
     * @param icaoAddress the ICAO adress of the aircraft
     * @param aircraftData the fixed characteristics of this aircraft
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {
        this.aircraftData = aircraftData;
        this.icaoAddress = Objects.requireNonNull(icaoAddress);
        this.lastMessageTimeStampNs = new SimpleLongProperty();
        this.category = new SimpleIntegerProperty(-1);
        this.callSignProperty = new SimpleObjectProperty<>();
        this.positionProperty = new SimpleObjectProperty<>();
        this.trajectoryModifiable = FXCollections.observableArrayList();
        this.trajectoryUnmodifiable = FXCollections.unmodifiableObservableList(trajectoryModifiable);
        this.altitude = new SimpleDoubleProperty(NaN); // Nan a garder ?
        this.velocity = new SimpleDoubleProperty(Double.NaN);
        this.trackOrHeading = new SimpleDoubleProperty(0);
        this.lastTrajectoryAddedTimeStampNs = -1L;// est-ce que pour l'initialisation de cet attribut on met n'importe quel valeur ?
    }

    /**
     * time stamp property access method
     * @return time stamp property
     */
    public ReadOnlyLongProperty lastMessageTimeStampNsProperty(){
        return lastMessageTimeStampNs;
    }

    /**
     * method for accessing the value contained in the time stamp property
     * @return lastMessageTimeStampNs
     */
    public long getLastMessageTimeStampNs(){
        return lastMessageTimeStampNs.getValue();
    }

    /**
     * method for modifying the value contained in the time stamp property
     * @param lastMessageTimeStampNs the new timestamp of the message
     */
    public void setLastMessageTimeStampNs(long lastMessageTimeStampNs){
        this.lastMessageTimeStampNs.set(lastMessageTimeStampNs);
    }

    /**
     *category property access method
     * @return category property
     */
    public ReadOnlyIntegerProperty categoryProperty(){
        return category;
    }

    /**
     * method for accessing the value contained in the category property
     * @return the category of the aircraft
     */
    public int getCategory(){
        return category.get();
    }

    /**
     * method for modifying the value contained in the category property
     * @param category the new category of the aircraft
     */
    public void setCategory(int category){
        this.category.set(category);
    }

    /**
     *call sign property access method
     * @return call sign property
     */

    public ReadOnlyObjectProperty<CallSign> callSignProperty(){
        return callSignProperty;
    }

    /**
     * method for accessing the value contained in the call sign property
     * @return callSign property
     */
    public CallSign getCallSign(){
        return callSignProperty.get();
    }

    /**
     * method for modifying the value contained in the call sign property
     * @param callSignProperty the new CallSign of the aircraft
     */
    public void setCallSign(CallSign callSignProperty){
        this.callSignProperty.set(callSignProperty);
    }

    /**
     * position property access method
     * @return position property
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty(){
        return positionProperty;
    }

    /**
     * method for accessing the value contained in the position property
     * @return position property
     */
    public GeoPos getPosition(){
        return positionProperty.get();
    }

    /**
     * method for modifying the value contained in the position property
     * @param positionProperty the new position of the aircraft
     */
    @Override
    public void setPosition(GeoPos positionProperty){
        this.positionProperty.set(positionProperty);
        if(!Double.isNaN(getAltitude())) {
            trajectoryModifiable.add(new AirbornePos(getAltitude(), getPosition()));
            lastTrajectoryAddedTimeStampNs = getLastMessageTimeStampNs();
        }
    }

    /**
     * trajectory access method
     * @return an unmodifiable list of trajectories
     */
    public ObservableList<AirbornePos> trajectory(){
        return trajectoryUnmodifiable;
    }

    /**
     * altitude property access method
     * @return altitude property
     */
    public ReadOnlyDoubleProperty altitudeProperty(){
        return altitude;
    }

    /**
     * method for accessing the value contained in the altitude property
     * @return altitude
     */
    public double getAltitude(){
        return altitude.get();
    }

    /**
     * method for modifying the value contained in the altitude property
     * @param altitude the new altitude of the aircraft
     */
    @Override
    public void setAltitude(double altitude){
        this.altitude.set(altitude);
        if(trajectoryModifiable.isEmpty()){
            trajectoryModifiable.add(new AirbornePos(getAltitude(), getPosition()));
            lastTrajectoryAddedTimeStampNs=getLastMessageTimeStampNs();
        } else if (lastTrajectoryAddedTimeStampNs==getLastMessageTimeStampNs()) {
            trajectoryModifiable.set(trajectoryModifiable.size()-1,new AirbornePos(getAltitude(), getPosition()));
        }
    }

    /**
     * velocity property access method
     * @return velocity property
     */
    public ReadOnlyDoubleProperty velocityProperty(){
        return velocity;
    }

    /**
     * method for accessing the value contained in the velocity property
     * @return velocity
     */
    public double getVelocity(){
        return velocity.get();
    }

    /**
     * method for modifying the value contained in the velocity property
     * @param velocity the new velocity of the aircraft
     */
    public void setVelocity(double velocity){
        this.velocity.set(velocity);
    }

    /**
     * track or heading property access method
     * @return track or heading property
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty(){
        return trackOrHeading;
    }

    /**
     * method for accessing the value contained in the track or heading property
     * @return track or heading
     */
    public double getTrackOrHeading(){
        return trackOrHeading.get();
    }

    /**
     * method for modifying the value contained in the track or heading property
     * @param trackOrHeading the new direction of the aircraft
     */
    public void setTrackOrHeading(double trackOrHeading){
        this.trackOrHeading.set(trackOrHeading);
    }

    /**
     * icao address access method
     * @return the ICAO address of the aircraft
     */
    public IcaoAddress address(){
        return icaoAddress;
    }

    /**
     * aircraft data access method
     * @return the fixed features of the aircraft
     */
    public AircraftData getAircraftData() {
        return aircraftData;
    }

    /**
     * registration access method
     * @return the registration of the aircraft
     */
    public AircraftRegistration getRegistration(){
        return aircraftData.registration();
    }

    /**
     * depicts the trajectory by pairing the altitude and the position
     * @param altitude altitude of the aircraft
     * @param position position of the aircraft
     */
    public record AirbornePos(double altitude, GeoPos position){

    }


}
