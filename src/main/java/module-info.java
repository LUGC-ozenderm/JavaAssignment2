module com.example.javaassignment2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;


    opens com.example.javaassignment2 to javafx.fxml;
    exports com.example.javaassignment2;
    exports com.example.javaassignment2.Controller;
    opens com.example.javaassignment2.Controller to javafx.fxml;
    exports com.example.javaassignment2.Model;
    opens com.example.javaassignment2.Model to javafx.fxml;
}