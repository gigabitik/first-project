package com.example.university;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Objects;

public class UniversityAppUI {
    public static void changeCSS(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(UniversityAppUI.class.getResource("/style/style.css")).toExternalForm());
    }

    public static void changeCSS(GridPane gridPane) {
        gridPane.getStylesheets().add(Objects.requireNonNull(UniversityAppUI.class.getResource("/style/style.css")).toExternalForm());
    }


    public static void setupScene(Stage mainStage, GridPane grid) {
        mainStage.setTitle("Университеты");
        mainStage.getIcons().add(new Image("file:img/logotip.png"));

        Scene scene = new Scene(grid, 600, 450);
        changeCSS(scene);

        mainStage.setScene(scene);
    }

    public static void setupTableScene(Stage mainStage, GridPane tableScene) {
        Scene scene = new Scene(tableScene, 600, 450);
        changeCSS(scene);

        mainStage.setScene(scene);
    }

}
