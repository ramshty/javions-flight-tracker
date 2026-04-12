package javions.gui;

import javions.Units;
import javions.adsb.CallSign;
import javions.aircraft.IcaoAddress;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * the class manages the table of aircraft
 */
public final class AircraftTableController {
    private static final int WIDTH_NUMERIC_COLUMNS = 85;
    private static final int WIDTH_DESCRIPTION = 70;
    private static final int WIDTH_TYPE = 50;
    private static final int WIDTH_MODEL = 230;
    private static final int WIDTH_REGISTRATION = 90;
    private static final int WIDTH_CALLSIGN = 70;
    private static final int WIDTH_ICAO = 60;
    private static final int FRACTION_DIGITS_LONG_FORMAT = 4;
    private static final int DEFAULT_FRACTION_DIGITS = 0;
    private final TableView<ObservableAircraftState> tableView = new TableView<>();
    private final ObjectProperty<ObservableAircraftState> observableAircraftState;
    private Consumer<ObservableAircraftState> consumer;
    private final ObservableSet<ObservableAircraftState> stateSet;
    private NumberFormat LongLatFormat;
    private NumberFormat defaultFormat;

    /**
     * @param stateSet the set of aircraft states that need to appear on the view
     * @param observableAircraftState a JavaFX property containing the state of the selected aircraft
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> stateSet, ObjectProperty<ObservableAircraftState> observableAircraftState){
        this.observableAircraftState = observableAircraftState;
        this.stateSet = stateSet;
        setTableView();
        TextColumnsAdder();
        NumericColumnsAdder();
        NumberFormatCreator();
        bindingsAndListeners();
    }

    /**
     * @return the node at the root of the scene graph
     */
    public TableView<ObservableAircraftState> pane(){
        return tableView;
    }

    /**
     *
     * @param consumer the used consumer
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer){
        this.consumer = consumer;

    }

    private void setTableView(){
        tableView.getStylesheets().add("table.css");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);
        tableView.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)
                    && observableAircraftState.get() != null){
                setOnDoubleClick(consumer);
                consumer.accept(observableAircraftState.get());
            }
        });
    }

    private void TextColumnsAdder(){
        TableColumn<ObservableAircraftState, String> oaci = TextColumnsCreator("ICAO",
                f -> new ReadOnlyObjectWrapper<>(f.address()).map(IcaoAddress::string),
                WIDTH_ICAO,
                false);

        TableColumn<ObservableAircraftState, String> callSign = TextColumnsCreator("Callsign",
                f -> f.callSignProperty().map(CallSign::string),
                WIDTH_CALLSIGN,
                false);

        TableColumn<ObservableAircraftState, String> registration = TextColumnsCreator("Registration",
                f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()).map(
                        e -> {
                            if(e!= null)
                                return e.registration().string();
                            else return "";
                        }
                ),
                WIDTH_REGISTRATION,
                false);

        TableColumn<ObservableAircraftState, String> model = TextColumnsCreator("Model",
                f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()).map(
                        e -> {
                            if(e!= null)
                                return e.model();
                            else return "";
                        }
                ),
                WIDTH_MODEL,
                false);

        TableColumn<ObservableAircraftState, String> type = TextColumnsCreator("Type",
                f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()).map(
                        e -> {
                            if(e!= null)
                                return e.typeDesignator().string();
                            else return "";
                        }
                ),
                WIDTH_TYPE,
                false);

        TableColumn<ObservableAircraftState, String> description = TextColumnsCreator("Description",
                f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()).map(
                        e -> {
                            if(e!= null)
                                return e.description().string();
                            else return "";
                        }
                ),
                WIDTH_DESCRIPTION,
                false);

        tableView.getColumns().addAll(oaci, callSign, registration, model, type, description);

    }

    private TableColumn<ObservableAircraftState, String> TextColumnsCreator(String title, Function<ObservableAircraftState,
            ObservableValue<String>> function, double width, boolean numeric){
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.setCellValueFactory(e -> function.apply(e.getValue()));
        column.setPrefWidth(width);
        if(numeric)
            column.getStyleClass().add("numeric");
        return column;
    }

    private void NumericColumnsAdder(){
        TableColumn<ObservableAircraftState, String> longitude = TextColumnsCreator("Longitude (°)",
                f -> f.positionProperty().map(
                        e -> LongLatFormat.format(Units.convertTo(e.longitude(), Units.Angle.DEGREE))),
                WIDTH_NUMERIC_COLUMNS,
                true);
        longitude.setCellValueFactory(
                f -> f.getValue().positionProperty().map(e -> LongLatFormat.format(Units.convertTo(e.longitude(),
                        Units.Angle.DEGREE)))
        );
        comparatorSetter(longitude, true);

        TableColumn<ObservableAircraftState, String> latitude = TextColumnsCreator("Latitude (°)",
                f -> f.positionProperty().map(e -> LongLatFormat.format(Units.convertTo(e.latitude(), Units.Angle.DEGREE))),
                WIDTH_NUMERIC_COLUMNS,
                true);
        comparatorSetter(latitude, true);

        TableColumn<ObservableAircraftState, String> altitude = TextColumnsCreator("Altitude (m)",
                f -> f.altitudeProperty().map(e -> {
                    if(Double.isNaN(e.doubleValue()))
                        return "";
                    else return defaultFormat.format(e.doubleValue());
                }),
                WIDTH_NUMERIC_COLUMNS,
                true);
        comparatorSetter(altitude, false);

        TableColumn<ObservableAircraftState, String> velocity = TextColumnsCreator("Speed (km/h)",
                f -> f.velocityProperty().map(e -> {
                    if(Double.isNaN(e.doubleValue()))
                        return "";
                    else return defaultFormat.format(Units.convertTo(e.doubleValue(), Units.Speed.KILOMETER_PER_HOUR));
                }),
                WIDTH_NUMERIC_COLUMNS,
                true);
        comparatorSetter(velocity, false);

        tableView.getColumns().addAll(longitude, latitude, altitude, velocity);

    }

    private void NumberFormatCreator(){
        LongLatFormat = NumberFormat.getInstance();
        LongLatFormat.setMinimumFractionDigits(FRACTION_DIGITS_LONG_FORMAT);
        LongLatFormat.setMaximumFractionDigits(FRACTION_DIGITS_LONG_FORMAT);

        defaultFormat = NumberFormat.getInstance();
        defaultFormat.setMinimumFractionDigits(DEFAULT_FRACTION_DIGITS);
        defaultFormat.setMaximumFractionDigits(DEFAULT_FRACTION_DIGITS);
    }

    private void comparatorSetter(TableColumn<ObservableAircraftState, String> column, boolean LongOrLat){
        column.setComparator(
                (s1, s2) -> {
                    try {
                        if (s1 == null || s2 == null)
                            return s1.compareTo(s2);
                        else {
                            if (LongOrLat) {
                                return Double.compare(LongLatFormat.parse(s1).doubleValue(),
                                        LongLatFormat.parse(s2).doubleValue());
                            } else
                                return Double.compare(defaultFormat.parse(s1).doubleValue(),
                                        defaultFormat.parse(s2).doubleValue());
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void bindingsAndListeners(){
        stateSet.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                    if(change.wasAdded()){
                        tableView.getItems().add(change.getElementAdded());
                        tableView.sort();
                    }
                    if(change.wasRemoved()){
                        tableView.getItems().remove(change.getElementRemoved());
                    }

                });

        observableAircraftState.addListener((observable, oldValue, newValue) -> {
            if(!Objects.equals(tableView.getSelectionModel().getSelectedItem(), newValue))
                tableView.scrollTo(newValue);
            tableView.getSelectionModel().select(newValue);
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                observableAircraftState.setValue(newValue)
        );
    }
}
