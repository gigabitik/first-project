module com.example.university {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.university to javafx.fxml;
    exports com.example.university;
}