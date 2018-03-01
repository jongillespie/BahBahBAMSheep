import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static javafx.scene.paint.Color.TRANSPARENT;

public class Main extends Application {

    static AnchorPane display;

    @Override
    public void start(Stage stage) throws Exception{
       display = FXMLLoader.load(getClass().getResource("Display.fxml"));
       stage.setScene(new Scene(display));
       // Make the outer border invisible
       stage.initStyle(StageStyle.TRANSPARENT);
       //stage.initStyle(StageStyle.DECORATED);
       // Make the un used portions of the Image View disapear.
       display.getScene().setFill(TRANSPARENT);
       stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
