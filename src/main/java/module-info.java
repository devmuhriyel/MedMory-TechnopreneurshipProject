module org.example.techno_mvp {
    requires javafx.controls;
    requires javafx.fxml;

    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens MVP to javafx.fxml;
    exports MVP;
    exports Controller;
    opens Controller to javafx.fxml;
    exports MVP.Doctor;
    opens MVP.Doctor to javafx.fxml;
    exports MVP.Admin;
    opens MVP.Admin to javafx.fxml;
    exports Controller.Doctor;
    opens Controller.Doctor to javafx.fxml;
}