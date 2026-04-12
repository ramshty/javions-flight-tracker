package javions.gui;

import javions.Preconditions;
import javions.Units;
import javions.WebMercator;
import javions.adsb.CallSign;
import javions.aircraft.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;

import javafx.scene.layout.Pane;

import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.List;


import static javions.gui.ColorRamp.PLASMA;
import static javafx.scene.paint.CycleMethod.NO_CYCLE;

/**
 * manage the aircraft view
 */
public final class AircraftController {

    private static final double COLOR_DIVIDER = 12000d;
    private static final double CONTAINER_OPACITY = 0.5;
    private static final int ADDER_FOR_BOUNDS = 4;
    private static final int MAX_ZOOM_LABEL = 11;
    private static final int FACTOR_COLOR = 12000;
    private final Pane pane;
    private final ObjectProperty<ObservableAircraftState> observableAircraftState;

    /**
     * construct an aircraft controller
     * @param mapParameters the parameters of the visible portion of the map
     * @param stateSet the (observable but not modifiable) set of aircraft states that should appear on the view
     * @param observableAircraftState a JavaFX property containing the state of the selected aircraft, whose content can be null when no aircraft is selected.
     */
    public AircraftController(MapParameters mapParameters, ObservableSet<ObservableAircraftState> stateSet,
                              ObjectProperty<ObservableAircraftState> observableAircraftState){
        pane = new Pane();
        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");
        this.observableAircraftState = observableAircraftState;
        Preconditions.checkArgument(stateSet.isEmpty());
        stateSet.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
            if(change.wasAdded()){
                initialize(mapParameters,change.getElementAdded());
            }
            if(change.wasRemoved()){
                pane.getChildren().removeIf(e -> e.getId().equals(change.getElementRemoved().address().string()));
            }

        });

    }

    /**
     * method that provides the JavaFX panel on which the aircraft are displayed
     * @return the JavaFX panel on which the aircraft are displayed
     */
    public Pane pane() {
        return pane;
    }
    private void initialize(MapParameters mapParameters, ObservableAircraftState newState){
        Group labelAndIconGroup = labelAndIcon(mapParameters, newState);
        Group aircraftGroup = new Group(trajectory(newState,mapParameters),labelAndIconGroup);
        aircraftGroup.setId(newState.address().string());
        aircraftGroup.viewOrderProperty().bind(newState.altitudeProperty().negate());
        pane.getChildren().add(aircraftGroup);
    }

    private Group trajectory(ObservableAircraftState newState, MapParameters mapParameters){
            Group trajectory = new Group();
            trajectory.getStyleClass().add("trajectory");

            trajectory.layoutXProperty().bind(Bindings.createDoubleBinding(mapParameters::getMinX
                    ,mapParameters.minXProperty()).negate());
            trajectory.layoutYProperty().bind(Bindings.createDoubleBinding(mapParameters::getMinY
                    ,mapParameters.minYProperty()).negate());

            trajectory.visibleProperty().addListener((p,oldV, newV) -> {
                if (newV) buildTrajectory(mapParameters, newState, trajectory);
                else trajectory.getChildren().clear();
            });
            trajectory.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                    newState.equals(observableAircraftState.get()),observableAircraftState));

            newState.trajectory().addListener((ListChangeListener<ObservableAircraftState.AirbornePos>) change ->{
                if(trajectory.visibleProperty().get()) buildTrajectory(mapParameters,newState,trajectory);
                    });

            mapParameters.zoomProperty().addListener(c -> {
                if(trajectory.visibleProperty().get()) buildTrajectory(mapParameters,newState,trajectory);
            });

            return trajectory;
    }


    private Group labelAndIcon(MapParameters mapParameters, ObservableAircraftState newState){
        Group labelAndIcon = new Group(label(mapParameters,newState),icon(newState));

        labelAndIcon.layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    double x = WebMercator.x( mapParameters.getZoom(), newState.getPosition().longitude());
                    return x - (mapParameters.getMinX());
                }
        , mapParameters.minXProperty(),mapParameters.zoomProperty(),newState.positionProperty()));

        labelAndIcon.layoutYProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    double y = WebMercator.y( mapParameters.getZoom(), newState.getPosition().latitude());
                    return y - (mapParameters.getMinY());
                }
        , mapParameters.minYProperty(),mapParameters.zoomProperty(),newState.positionProperty()));

        return labelAndIcon;
    }

    // returns icons
    private SVGPath icon(ObservableAircraftState newState){
        SVGPath svgPath = new SVGPath();
        svgPath.getStyleClass().add("aircraft");
        ObjectProperty<AircraftIcon> aircraftIconProperty = new SimpleObjectProperty<>();

        aircraftIconProperty.bind(newState.categoryProperty().map( category -> {
            AircraftIcon aircraftIcon;
            if(newState.getAircraftData() != null) {
                aircraftIcon = AircraftIcon.iconFor(newState.getAircraftData().typeDesignator(),
                        newState.getAircraftData().description()
                 , category.intValue() , newState.getAircraftData().wakeTurbulenceCategory());
            } else {
                aircraftIcon = AircraftIcon.iconFor(new AircraftTypeDesignator(""),new AircraftDescription("")
                        , category.intValue() , WakeTurbulenceCategory.UNKNOWN);
            }
            return aircraftIcon;
        }));

        svgPath.contentProperty().bind(aircraftIconProperty.map(AircraftIcon::svgPath));

        svgPath.rotateProperty().bind(Bindings.createDoubleBinding(() ->
                aircraftIconProperty.get().canRotate() ?
                        Units.convertTo(newState.getTrackOrHeading(), Units.Angle.DEGREE)
                        : 0, newState.trackOrHeadingProperty())
        );

        svgPath.setOnMouseClicked(event -> observableAircraftState.set(newState));
        svgPath.fillProperty().bind(Bindings.createObjectBinding(() ->
                PLASMA.at(Math.cbrt(newState.getAltitude() / COLOR_DIVIDER)),newState.altitudeProperty()));
        return svgPath;
    }

    //returns label
    private Group label(MapParameters mapParameters, ObservableAircraftState newState){
        Text content = new Text();
        StringProperty id = buildId(newState);
        StringProperty velocity = buildVelocity(newState);
        StringProperty altitude = buildAltitude(newState);
        content.textProperty().bind(Bindings.format("%s%n%s\u2002%s", id,velocity, altitude));

        Rectangle container = new Rectangle();
        container.setOpacity(CONTAINER_OPACITY);

        container.widthProperty().bind(content.layoutBoundsProperty().map(b -> b.getWidth() + ADDER_FOR_BOUNDS));
        container.heightProperty().bind(content.layoutBoundsProperty().map(b -> b.getHeight() + ADDER_FOR_BOUNDS));

        Group label = new Group(container, content);
        label.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                mapParameters.getZoom() >= MAX_ZOOM_LABEL
                        || newState.equals(observableAircraftState.get()), mapParameters.zoomProperty()
                ,observableAircraftState));
        label.getStyleClass().add("label");
        return label;
    }

    //returns id
    private StringProperty buildId(ObservableAircraftState newState){
        StringProperty id = new SimpleStringProperty();
            if (newState.getAircraftData() != null)
                id.set(newState.getRegistration().string());
            else id.bind(Bindings.when(newState.callSignProperty().isNotNull())
                                 .then(Bindings.convert(newState.callSignProperty().map(CallSign::string)))
                                 .otherwise(newState.address().string()));
        return id;
    }

    private StringProperty buildVelocity(ObservableAircraftState newState){
        StringProperty velocity = new SimpleStringProperty();
        velocity.bind(Bindings.createStringBinding(() -> {
            int actualVelocity = (int) Math.rint(Units.convertTo(newState.getVelocity(),Units.Speed.KILOMETER_PER_HOUR));
            return Double.isNaN(newState.getVelocity()) ? "? km/h" : actualVelocity + " km/h";
        }
        ,newState.velocityProperty()));

        return velocity;
    }

    private StringProperty buildAltitude (ObservableAircraftState newState){
        StringProperty altitude = new SimpleStringProperty();
        altitude.bind(Bindings.createStringBinding(() ->
                        Double.isNaN(newState.getAltitude()) ? "? m" : (int) Math.rint(newState.getAltitude())+" m"
        ,newState.altitudeProperty()));
        return altitude;
    }

    private void buildTrajectory(MapParameters mapParameters, ObservableAircraftState newState, Group trajectory){
        trajectory.getChildren().clear();
        List<ObservableAircraftState.AirbornePos> trajectoryList = newState.trajectory();
        for (int i = 0; i < trajectoryList.size()-1; i++) {
            if(trajectoryList.get(i).position()!=null) {
                double startX = WebMercator.x(mapParameters.getZoom(), trajectoryList.get(i).position().longitude());
                double startY = WebMercator.y(mapParameters.getZoom(), trajectoryList.get(i).position().latitude());
                double endX = WebMercator.x(mapParameters.getZoom(), trajectoryList.get(i + 1).position().longitude());
                double endY = WebMercator.y(mapParameters.getZoom(), trajectoryList.get(i + 1).position().latitude());
                Line actualTrajectory = new Line(startX,startY,endX,endY);
                if (trajectoryList.get(i).altitude() == trajectoryList.get(i+1).altitude()) {
                    actualTrajectory.setStroke(colorCompute(trajectoryList.get(i).altitude()));
                } else {
                    Stop s1 = new Stop(0, colorCompute(trajectoryList.get(i).altitude()));
                    Stop s2 = new Stop(1, colorCompute(trajectoryList.get(i+1).altitude()));
                    actualTrajectory.setStroke(
                            new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s1, s2));
                }
                trajectory.getChildren().add(actualTrajectory);

            }
        }
    }

    private static Color colorCompute(double altitude){
        return PLASMA.at(Math.cbrt(altitude/ FACTOR_COLOR));
    }
}
