package com.example.university;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UniversityApp {

    private TextField username;
    private PasswordField password;
    private Stage mainStage;
    private Connection connection;

    public void start(Stage mainStage) {
        this.mainStage = mainStage;
        GridPane grid = createGridPane();
        UniversityAppUI.setupScene(mainStage, grid);
        mainStage.show();
    }

    private GridPane createGridPane() {
        Image image = new Image("file:img/login.png");

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);

        GridPane.setConstraints(imageView, 0, 0, 2, 1);
        GridPane.setHalignment(imageView, HPos.CENTER);

        // Other components
        Label usernameLabel = new Label("Логин");
        GridPane.setConstraints(usernameLabel, 0, 2);
        username = new TextField();
        GridPane.setConstraints(username, 1, 2);

        Label passwordLabel = new Label("Пароль");
        GridPane.setConstraints(passwordLabel, 0, 3);
        password = new PasswordField();
        GridPane.setConstraints(password, 1, 3);

        Button loginButton = new Button("Войти");
        GridPane.setConstraints(loginButton, 1, 4);
        GridPane.setHalignment(loginButton, javafx.geometry.HPos.RIGHT);
        loginButton.setOnAction(e -> handleLogin());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(12);
        grid.getStyleClass().add("login");

        grid.getChildren().addAll(imageView, usernameLabel, username, passwordLabel, password, loginButton);

        return grid;
    }

    private void handleLogin() {
        String username = this.username.getText();
        String password = this.password.getText();

        try {
            DataBaseConnection.username = username;
            DataBaseConnection.password = password;
            connection = DataBaseConnection.connect();

            if (connection.isValid(3)) {
                System.out.println("Подключился к базе данных");
                showTables();

            } else {
                System.out.println("Не смог подключиться к базе данных");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Button> generateTableButtons(List<String> tableNames) {
        List<Button> buttons = new ArrayList<>();

        for (String tableName : tableNames) {
            Button btn = new Button(tableName);
            btn.setOnAction(e -> handleTableButtonClick(tableName));
            buttons.add(btn);
        }

        return buttons;
    }


    private void handleTableButtonClick(String tableName) {
        try (Connection connection = DataBaseConnection.connect()) {
            List<String> columnNames = DataBaseUtils.getColumns(connection, tableName);
            List<List<String>> tableData = DataBaseUtils.getTableData(connection, tableName);

            GridPane tableScene = Table.createTable(tableName, columnNames, tableData, this::showTables);
            UniversityAppUI.setupTableScene(mainStage, tableScene);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showTables() {
        List<String> tableNames = DataBaseUtils.getTableNames(connection);
        List<Button> tableButtons = generateTableButtons(tableNames);
        Scene tableScene = createTableScene(tableButtons);
        UniversityAppUI.changeCSS(tableScene);

        mainStage.setScene(tableScene);
    }

    private Scene createTableScene(List<Button> tableButtons) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(7);
        grid.setHgap(9);

        for (int i = 0; i < tableButtons.size(); i++) {
            grid.add(tableButtons.get(i), 0, i);
        }

        Label label = new Label("Список доступных таблиц");
        label.getStyleClass().add("scene-label");
        label.setMaxHeight(30);
        label.setPadding(new Insets(15, 0, 0, 0));

        VBox layout = new VBox(7);
        layout.getChildren().addAll(label, grid);
        layout.setAlignment(Pos.TOP_CENTER);

        Scene tableScene = new Scene(layout, 600, 450);
        UniversityAppUI.changeCSS(tableScene);

        return tableScene;
    }
}
