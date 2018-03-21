import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 */
public class Main extends Application {

    public static AnchorPane display;

    /**
     * Loads the FXML file for display
     * @param stage creates, loads and shows the initial stage.
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception{
        display = FXMLLoader.load(getClass().getResource("Display.fxml"));
        stage.setScene(new Scene(display));
        stage.show();
    }

    /**
     * Main Java run sequence initializer.
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
