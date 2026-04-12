package javions.gui;

import javions.ByteString;
import javions.adsb.Message;
import javions.adsb.MessageParser;
import javions.adsb.RawMessage;
import javions.aircraft.AircraftDatabase;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class AircraftTableControllerTest extends Application {
    ObservableSet<ObservableAircraftState> st;
    public static void main(String[] args) {
        launch(args);
    }

    static List<RawMessage> readAllMessages(String fileName)
            throws IOException {
        List<RawMessage> finalList = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);

                finalList.add(new RawMessage(timeStampNs, message));
            }
        } catch (EOFException e) { return finalList; }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TileManager tileManager = new TileManager(Path.of("tile-cache"), "tile.openstreetmap.org");
        MapParameters mp = new MapParameters(17, 17_389_327, 11_867_430);

        BaseMapController bmc = new BaseMapController(tileManager, mp);

        // Création de la base de données
        URL dbUrl = getClass().getResource("/aircraft.zip");
        assert dbUrl != null;
        String f = Path.of((dbUrl).toURI()).toString();
        var db = new AircraftDatabase(f);

        Consumer<ObservableAircraftState> consumer = aircraftState -> System.out.println("Double Click");

        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap =
                new SimpleObjectProperty<>();
        st = asm.states();
        AircraftTableController ac =
                new AircraftTableController(st, sap);
        ac.setOnDoubleClick(consumer);
        var root = new StackPane(bmc.pane(), ac.pane());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        var mi = readAllMessages("/Users/a.hatimy/Desktop/WORKSPACE/PPOOPROJECT/Javions/messages_20230318_0915.bin")
                .iterator();

        // Animation des aéronefs
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    for (int i = 0; i < 10; i += 1) {
                        Message m = MessageParser.parse(mi.next());
                        if (m != null) {
                            asm.updateWithMessage(m);
                            asm.purge();
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }
}
