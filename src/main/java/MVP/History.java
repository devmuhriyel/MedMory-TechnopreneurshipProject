package MVP;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class History extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource("History.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1366, 768);
        stage.setTitle("MEDMORY - Your Medical History");
        stage.setScene(scene);
        stage.show();
    }

    public void handleDateClick(ActionEvent actionEvent) {
    }

    public void handleNavigation(ActionEvent actionEvent) {
    }
}
