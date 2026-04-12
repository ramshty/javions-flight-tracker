package javions.aircraft;

import java.util.Objects;

/**
 * collects fix data of an aircraft
 *
 * @param registration           represents the registration of the aircraft
 * @param typeDesignator         represents the indicator of the type of the aircraft
 * @param model                  represents the model of the aircraft
 * @param description            represents the description of the aircraft
 * @param wakeTurbulenceCategory represents the turbulence category of an aircraft
 * @author Ramy Hatimy (356771)
 * @author Nada Eddahir (356653)
 */
public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator,
                           String model, AircraftDescription description,
                           WakeTurbulenceCategory wakeTurbulenceCategory) {
    /**
     * checks if neither of the arguments given are null and throws a NullPointerException otherwise
     *
     * @throws NullPointerException if one of more of the arguments are null
     */
    public AircraftData {
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }
}
