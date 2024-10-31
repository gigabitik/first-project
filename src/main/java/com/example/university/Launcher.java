package com.example.university;

import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }











    @Override
    public void start(Stage mainStage) {
        UniversityApp universityApp = new UniversityApp();
        universityApp.start(mainStage);
    }
}