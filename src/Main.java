import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Main Application
 * Kicks off the program and initialises the FXML display;
 * as static, to be accessed in the Controller as well.
 */
public class Main extends Application {

    // Static and accessible in Controller.
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
