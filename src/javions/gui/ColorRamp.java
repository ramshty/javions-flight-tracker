package javions.gui;

import javions.Preconditions;
import javafx.scene.paint.Color;



/**
 * represents a color gradient
 */
public final class ColorRamp {

    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));
    private static final int PERCENTAGE_FACTOR = 100;
    private final Color[] functionColor;
    private final double delta;

    /**
     * construct a gradient color
     * @param colors all the colors from which are created the gradient
     */
    public ColorRamp(Color... colors) {
        Preconditions.checkArgument(colors.length>= 2);
        delta = 1d / (colors.length-1);
        functionColor = colors.clone();
    }

    /**
     * return the corresponding color to the value given
     * @param value the value given that corresponds to a color
     * @return the corresponding color to the value given
     */
    public Color at(double value){
        int lowerBound = (int) (value / delta);
        double percentage = ((value % delta) /delta) * PERCENTAGE_FACTOR;
        return (0<= value && value<=1) ? functionColor[lowerBound].interpolate(functionColor[lowerBound + 1],percentage) :
                (value < 0) ? functionColor[0] : functionColor[functionColor.length-1];
    }

}
