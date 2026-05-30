module com.examplez.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.examplez.demo to javafx.fxml;
    exports com.examplez.demo;
}