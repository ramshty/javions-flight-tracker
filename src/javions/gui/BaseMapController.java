package javions.gui;

import javions.GeoPos;
import javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * manages the display and interaction with the basemap
 */
public final class BaseMapController {
    private static final int TILE_SIDE_LENGTH = 1 << 8;
    private static final int ADDER_CURRENT_TIME = 200;
    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private final Canvas canvas;
    private final Pane pane;
    private GraphicsContext canvasGC;
    private boolean redrawNeeded = true;
    private ObjectProperty<Point2D> startDrag;
    private ObjectProperty<Point2D> currentDrag;

    /**
     * construct a base map controller which manages the display and interaction with the basemap
     * @param tileManager the tile manager for getting the tile
     * @param mapParameters the parameters of the visible portion of the map
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters){
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);
        initialize();
    }

    /**
     * returns the JavaFX panel displaying the basemap
     * @return the JavaFX panel displaying the basemap
     */
    public Pane pane() {
        return pane;
    }

    /**
     * moves the visible portion of the map so that it is centered at this point
     * @param center the point on which the map is centered
     */
    public void centerOn(GeoPos center){
        double centerX = WebMercator.x( mapParameters.getZoom(),center.longitude());
        double centerY = WebMercator.y( mapParameters.getZoom(),center.latitude());
        double positionCenterX = mapParameters.getMinX()+ (canvas.getWidth()/2);
        double positionCenterY = mapParameters.getMinY() + (canvas.getHeight()/2);
        double vectorScrollX = centerX - positionCenterX;
        double vectorScrollY = centerY - positionCenterY;
        mapParameters.scroll(vectorScrollX,vectorScrollY);
    }

    private void redrawIfNeeded(){
        if(!redrawNeeded) return;
        redrawNeeded = false;
        drawImage();
    }


    private void redrawOnNextPulse(){
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void drawImage() {
        int leftUpperTileX = (int) Math.floor(mapParameters.getMinX()/TILE_SIDE_LENGTH);
        int leftUpperTileY = (int) Math.floor(mapParameters.getMinY()/TILE_SIDE_LENGTH);
        double coordsDrawingX = (leftUpperTileX * TILE_SIDE_LENGTH) - mapParameters.getMinX();
        double coordsDrawingY = (leftUpperTileY * TILE_SIDE_LENGTH) - mapParameters.getMinY();
        canvasGC.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        for (int tileX = leftUpperTileX;
             coordsDrawingX + (tileX - leftUpperTileX) * TILE_SIDE_LENGTH < canvas.getWidth() + TILE_SIDE_LENGTH;
             tileX++) {
            double x = coordsDrawingX + (tileX - leftUpperTileX) * TILE_SIDE_LENGTH;
            for (int tileY = leftUpperTileY;
                 coordsDrawingY + (tileY - leftUpperTileY) * TILE_SIDE_LENGTH < canvas.getHeight() + TILE_SIDE_LENGTH;
                 tileY++) {
                double y = coordsDrawingY + (tileY - leftUpperTileY) * TILE_SIDE_LENGTH;
                if(TileManager.TileId.isValid(mapParameters.getZoom(), tileX, tileY)) {
                    TileManager.TileId newTile = new TileManager.TileId(mapParameters.getZoom(), tileX, tileY);
                    try {
                        canvasGC.drawImage(tileManager.imageForTileAt(newTile), x, y);
                    } catch (IOException ignored) {

                    }
                }
            }
        }
    }

    private void initialize(){
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        canvasGC = canvas.getGraphicsContext2D();
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        this.mapParameters.minYProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) redrawOnNextPulse();
        });
        this.mapParameters.minXProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) redrawOnNextPulse();
        });
        this.mapParameters.zoomProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) redrawOnNextPulse();
        });
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) redrawOnNextPulse();
        });
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) redrawOnNextPulse();
        });

        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + ADDER_CURRENT_TIME);

            if(e.getDeltaY()>0){
                mapParameters.scroll(e.getX(),e.getY());
                mapParameters.changeZoomLevel(-1);
                mapParameters.scroll(-e.getX(),-e.getY());
            }
            if(e.getDeltaY()<0) {
                mapParameters.scroll(e.getX(),e.getY());
                mapParameters.changeZoomLevel(1);
                mapParameters.scroll(-e.getX(),-e.getY());
            }
        });

        pane.setOnMousePressed(e -> {
            startDrag = new SimpleObjectProperty<>();
            startDrag.set(new Point2D(e.getX(),e.getY()));
        });

        currentDrag = new SimpleObjectProperty<>();
        pane.setOnMouseDragged(e -> {
            if(startDrag!=null) {
                currentDrag.set(startDrag.get().subtract(e.getX(), e.getY()));
                mapParameters.scroll(currentDrag.get().getX(), currentDrag.get().getY());
                startDrag.set(new Point2D(e.getX(), e.getY()));
            }
        });

        pane.onMouseDraggedProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) redrawOnNextPulse();
        });

        pane.setOnMouseReleased(e -> startDrag = null);
    }
}
