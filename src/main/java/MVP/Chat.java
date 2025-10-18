package MVP;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Chat extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Chat.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1366, 768);
        stage.setTitle("Chat");
        stage.setScene(scene);
        stage.show();
    }
    public void handleNavigation(ActionEvent actionEvent) {
    }

    public void handleNotificationClick(ActionEvent actionEvent) {
    }

    public void handleSendMessage(ActionEvent actionEvent) {
    }
}
