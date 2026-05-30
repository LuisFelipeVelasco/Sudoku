module com.examplez.demo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.examplez.demo.controllers to javafx.fxml;
    exports com.examplez.demo;
}