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
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class TestAircraftController extends Application {
        public static void main(String[] args) { launch(args); }
        static List<RawMessage> readAllMessages() throws IOException {
            List<RawMessage> list = new ArrayList<>();
            try (DataInputStream s = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("C:\\Users\\nedda\\Desktop\\PPOO javions\\ppoo-project-2.0\\Javions\\messages_20230318_0915.bin")))){
                byte[] bytes = new byte[RawMessage.LENGTH];
                while (true) {
                    long timeStampNs = s.readLong();
                    int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                    assert bytesRead == RawMessage.LENGTH;
                    ByteString message = new ByteString(bytes);
                    RawMessage rawMessage = new RawMessage(timeStampNs, message);
                    list.add(rawMessage);
                }
            } catch (EOFException e) { /* nothing to do */ }
            return list;
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            // … à compléter (voir TestBaseMapController)
            Path tileCache = Path.of("tile-cache");
            TileManager tm =
                    new TileManager(tileCache, "tile.openstreetmap.org");
            MapParameters mp =
                    new MapParameters(17, 17_389_327, 11_867_430);
            BaseMapController bmc = new BaseMapController(tm, mp);

            // Création de la base de données
            URL dbUrl = getClass().getResource("/aircraft.zip");
            assert dbUrl != null;
            String f = Path.of(dbUrl.toURI()).toString();
            var db = new AircraftDatabase(f);

            AircraftStateManager asm = new AircraftStateManager(db);
            ObjectProperty<ObservableAircraftState> sap =
                    new SimpleObjectProperty<>();
            AircraftController ac =
                    new AircraftController(mp, asm.states(), sap);
            var root = new StackPane(bmc.pane(), ac.pane());
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

            var mi = readAllMessages()
                    .iterator();

            // Animation des aéronefs
            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    try {
                        for (int i = 0; i < 10; i += 1) {
                            Message m = MessageParser.parse(mi.next());
                            if (m != null) asm.updateWithMessage(m);
                            asm.purge();
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }.start();
        }
    }

