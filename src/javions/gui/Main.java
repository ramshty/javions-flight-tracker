package javions.gui;

import javions.ByteString;
import javions.adsb.Message;
import javions.adsb.MessageParser;
import javions.adsb.RawMessage;
import javions.aircraft.AircraftDatabase;
import javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class Main extends Application {

    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    private static final int ZOOM_START = 8;
    private static final int MIN_X_START = 33530;
    private static final int MIN_Y_START = 23070;
    private static final String AIRCRAFT_DATABASE_RESOURCE = "/aircraft.zip";
    private static final String DEFAULT_SAMPLE_RESOURCE = "/messages_20230520_1725.bin";
    private static final String APPLICATION_TITLE = "Javions Flight Tracker";
    private static final Duration PURGE_INTERVAL = Duration.ofSeconds(1);

    /**
     * launch the
     * @param args contains the files to launch
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * starts the application by building the scene graph corresponding to the GUI, starting the execution thread
     * responsible for getting the messages, and finally starting the "animation timer" responsible for updating the
     * aircraft states depending on the messages received. The timer is also responsible for purging, once per second,
     * aircraft from which no message has been received for one minute.
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws Exception if there is input or output error or  if any thread has interrupted the current thread.
     * The interrupted status of the current thread is cleared when this exception is thrown or if we reached the end
     * of the file containing message.
     * @throws UncheckedIOException there is an invalid output/input
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Queue<RawMessage> queue = new ConcurrentLinkedQueue<>();
        final List<String> args = this.getParameters().getRaw();
        final Supplier<RawMessage> messageSupplier = createMessageSupplier(args);

        Thread newThread = new Thread(() -> {
            RawMessage m;
            while ((m = messageSupplier.get()) != null) {
                    queue.add(m);
            }
        }, "javions-input");

        newThread.setDaemon(true);
        newThread.start();

        Path tileCache = Path.of("tile-cache");
        TileManager tm =
                new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp =
                new MapParameters(ZOOM_START, MIN_X_START, MIN_Y_START);
        BaseMapController bmc = new BaseMapController(tm, mp);

        Path p = extractResourceToTempFile(AIRCRAFT_DATABASE_RESOURCE, "javions-aircraft", ".zip");
        AircraftDatabase db = new AircraftDatabase(p.toString());

        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap =
                new SimpleObjectProperty<>(null);
        AircraftController ac =
                new AircraftController(mp, asm.states(), sap);
        var aircraftView = new StackPane(bmc.pane(), ac.pane());

        Consumer<ObservableAircraftState> consumer = aircraftState -> bmc.centerOn(aircraftState.getPosition());
        AircraftTableController at =
                new AircraftTableController(asm.states(),sap);
        at.setOnDoubleClick(consumer);
        StatusLineController slc =
                new StatusLineController();
        slc.aircraftCountProperty().bind(Bindings.createIntegerBinding(() -> asm.states().size(),asm.states()));
        var tableAircraft = new BorderPane(at.pane(),slc.pane(),null, null,null);

        var javion = new SplitPane(aircraftView,tableAircraft);
        javion.setOrientation(Orientation.VERTICAL);
        primaryStage.setScene(new Scene(javion));
        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.show();

        new AnimationTimer() {
            private long lastPurgeTimeNs = Long.MIN_VALUE;
            private long messageCount = 0;
            @Override
            public void handle(long now) {
                try {
                        RawMessage m;
                        while ((m = queue.poll()) != null){
                            Message m1 = MessageParser.parse(m);
                            if(m1 != null) {
                                asm.updateWithMessage(m1);
                                messageCount++;
                            }
                        }
                        slc.messageCountProperty().set(messageCount);
                        if (lastPurgeTimeNs == Long.MIN_VALUE
                                || now - lastPurgeTimeNs >= PURGE_INTERVAL.toNanos()) {
                            lastPurgeTimeNs = now;
                            asm.purge();
                        }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }

    private Supplier<RawMessage> createMessageSupplier(List<String> args) throws IOException {
        if (args.isEmpty() || (args.size() == 1 && args.get(0).equals("--sample"))) {
            return supplierResource(DEFAULT_SAMPLE_RESOURCE);
        }
        if (args.size() == 1 && args.get(0).equals("--stdin")) {
            return supplierStandard();
        }
        if (args.size() == 2 && args.get(0).equals("--file")) {
            return supplierFile(args.get(1));
        }
        if (args.size() == 1 && !args.get(0).startsWith("--")) {
            return supplierFile(args.get(0));
        }
        throw new IllegalArgumentException(
                "Usage: Javions [--sample|--stdin|--file <messages.bin>|<messages.bin>]");
    }

    private Supplier<RawMessage> supplierFile(String fileName) throws IOException {
        return timedReplaySupplier(readAllMessages(fileName));
    }

    private Supplier<RawMessage> supplierResource(String resourceName) throws IOException {
        try (InputStream inputStream = Objects.requireNonNull(
                getClass().getResourceAsStream(resourceName),
                () -> "Missing bundled resource: " + resourceName)) {
            return timedReplaySupplier(readAllMessages(inputStream));
        }
    }

    private Supplier<RawMessage> timedReplaySupplier(ArrayDeque<RawMessage> messages) {
        final long replayStartMillis = System.currentTimeMillis();
        return () ->{
            try {
                RawMessage m = messages.poll();
                if (m != null) {
                    long messageTimeMillis = Duration.ofNanos(m.timeStampNs()).toMillis();
                    long elapsedMillis = System.currentTimeMillis() - replayStartMillis;
                    if (elapsedMillis < messageTimeMillis) {
                        long sleepingTime = messageTimeMillis - elapsedMillis;
                        if (sleepingTime > 0) {
                            Thread.sleep(sleepingTime);
                        }
                    }
                }
                return m;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };
    }

    private Supplier<RawMessage> supplierStandard() throws IOException {
        AdsbDemodulator adsb = new AdsbDemodulator(System.in);
        return () ->{
            try {
                return adsb.nextMessage();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }


    static ArrayDeque<RawMessage> readAllMessages(String fileName) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(fileName))) {
            return readAllMessages(inputStream);
        }
    }

    static ArrayDeque<RawMessage> readAllMessages(InputStream inputStream) throws IOException {
        ArrayDeque<RawMessage> queue = new ArrayDeque<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(inputStream))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                queue.add(rawMessage);
            }
        } catch (EOFException e) { /* nothing to do */ }
        return queue;
    }

    private static Path extractResourceToTempFile(String resourceName, String prefix, String suffix) throws IOException {
        try (InputStream inputStream = Objects.requireNonNull(
                Main.class.getResourceAsStream(resourceName),
                () -> "Missing bundled resource: " + resourceName)) {
            Path tempFile = Files.createTempFile(prefix, suffix);
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return tempFile;
        }
    }

}
