package MVP.Doctor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * This class is used to run the Validation interface independently.
 */
public class Validation extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Points to the Validation.fxml in your MVP package
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Validation.fxml"));

        // Setting the scene to match your signup dimensions
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);

        stage.setTitle("MEDMORY - Doctor Verification");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}