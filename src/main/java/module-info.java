module org.example.techno_mvp {
    requires javafx.controls;
    requires javafx.fxml;

    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens MVP to javafx.fxml;
    exports MVP;
    exports Contoller;
    opens Contoller to javafx.fxml;
}