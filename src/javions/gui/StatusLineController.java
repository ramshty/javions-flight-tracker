package javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 * this class manages the status line
 *  @author Ramy Hatimy (356771)
 *  @author Nada Eddahir (356653)
 */
public class StatusLineController {

    private final IntegerProperty aircraftCount = new SimpleIntegerProperty();
    private final LongProperty messageCount = new SimpleLongProperty();
    private final BorderPane pane;

    /**
     * creates the scene graph
     */
    public StatusLineController() {
        Text leftContent = new Text();
        leftContent.textProperty().bind(Bindings.format("Visible aircraft: %d", aircraftCount));
        Text rightContent = new Text();
        rightContent.textProperty().bind(Bindings.format("Messages processed: %d", messageCount));
        this.pane = new BorderPane(null, null, rightContent, null, leftContent);
        this.pane.getStylesheets().add("status.css");

    }

    /**
     * @return the pane that contains the state line
     */
    public BorderPane pane(){
        return pane;
    }

    /**
     * @return the modifiable property containing the number of aircraft that are visible
     */
    public IntegerProperty aircraftCountProperty() {
        return aircraftCount;
    }

    /**
     * @return the modifiable property containing the number of messages received since the beginning of the
     * program's launching
     */
    public LongProperty messageCountProperty(){
        return messageCount;
    }
}
