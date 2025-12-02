module org.example.techno_mvp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;

    opens MVP to javafx.fxml;
    exports MVP;

    opens MVP.Doctor to javafx.fxml;
    exports MVP.Doctor;

    opens MVP.Admin to javafx.fxml;
    exports MVP.Admin;

    opens Controller to javafx.fxml;
    exports Controller;

    opens Controller.Doctor to javafx.fxml;
    exports Controller.Doctor;

    opens Controller.Client to javafx.fxml;
    exports Controller.Client;
}