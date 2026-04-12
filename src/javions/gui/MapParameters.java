package javions.gui;

import javions.Math2;
import javions.Preconditions;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * represents the parameters of the visible portion of the map in the graphic interface
 */
public final class MapParameters {
    private static final int MIN_ZOOM = 6;
    private static final int MAX_ZOOM = 19;
    /**
     * represents the zoom level
     */
    private final IntegerProperty zoomProperty;
    /**
     * represents the x coordinate of the top-left corner of the visible part of the map
     */
    private final DoubleProperty minXProperty;
    /**
     * represents the y coordinate of the top-left corner of the visible part of the map
     */
    private final DoubleProperty minYProperty;

    /**
     * @param zoom the initial value of the zoom level
     * @param minX the initial value of the x coordinate
     * @param minY the initial value of the y coordinate
     */
    public MapParameters(int zoom, int minX, int minY){
        Preconditions.checkArgument((MIN_ZOOM<=zoom) && (zoom<=MAX_ZOOM));
        this.zoomProperty = new SimpleIntegerProperty(zoom);
        this.minXProperty = new SimpleDoubleProperty(minX);
        this.minYProperty = new SimpleDoubleProperty(minY);
    }

    /**
     * @return the property of the zoom level
     */
    public IntegerProperty zoomProperty(){
        return zoomProperty;
    }

    /**
     * @return the value of the zoom level
     */
    public int getZoom(){
        return zoomProperty.getValue();
    }

    /**
     * @return the property of the x coordinate
     */
    public DoubleProperty minXProperty(){
        return minXProperty;
    }

    /**
     * @return the value of the x coordinate
     */
    public double getMinX(){
        return minXProperty.getValue();
    }

    /**
     * @return the property of the y coordinate
     */
    public DoubleProperty minYProperty(){
        return minYProperty;
    }

    /**
     * @return the value of the y coordinate
     */
    public double getMinY(){
        return minYProperty.getValue();
    }

    /**
     * translates the top-left corner of the part of the map by the given vector
     * @param x the x coordinate of the given vector
     * @param y the y coordinate of the given vector
     */
    public void scroll(double x, double y){
        minXProperty.setValue(minXProperty.getValue() + x);
        minYProperty.setValue(minYProperty.getValue() + y);
    }

    /**
     * adds a given difference of zoom level to the current zoom level
     * @param diff the given difference of zoom level
     */
    public void changeZoomLevel(int diff){
        int newZoom = zoomProperty.getValue() + diff;
        zoomProperty.setValue(Math2.clamp(MIN_ZOOM, newZoom, MAX_ZOOM));
        if(diff<0 && MIN_ZOOM<= newZoom && newZoom<=MAX_ZOOM){
            minXProperty.setValue(minXProperty.getValue() / (1<< -diff));
            minYProperty.setValue(minYProperty.getValue() / (1<< -diff));
        }
        if (diff>0 && MIN_ZOOM<= newZoom && newZoom<=MAX_ZOOM){
            minXProperty.setValue(minXProperty.getValue() * (1 << diff));
            minYProperty.setValue(minYProperty.getValue() * (1 << diff));
        }
    }
}

