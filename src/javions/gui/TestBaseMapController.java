package javions.gui;

import javions.GeoPos;
import javions.Units;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Path;

public final class TestBaseMapController extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        Path tileCache = Path.of("tile-cache");
        TileManager tm =
                new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp =
                new MapParameters(17, 17_389_327, 11_867_430);
        BaseMapController bmc = new BaseMapController(tm, mp);
        BorderPane root = new BorderPane(bmc.pane());
        Scene scene = new Scene(root);
        scene.setOnKeyTyped(e -> bmc.centerOn(new GeoPos((int) Units.convertTo( -0.1341, Units.Angle.T32), (int) Units.convertTo( 0.5853 , Units.Angle.T32))));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
