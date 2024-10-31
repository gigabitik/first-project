package com.example.university;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Table {
    public static GridPane createTable(String tableName, List<String> columnNames, List<List<String>> tableData,
                                       Runnable backButtonAction) {
        TableView<ObservableList<String>> tableView = createTableView(columnNames);
        configureTableColumns(tableView.getColumns());
        ObservableList<ObservableList<String>> data = createTableData(tableData);
        tableView.setItems(data);

        GridPane buttonPane = createButtonPane(backButtonAction, tableName, tableView);
        GridPane gridPane = createGridPane(tableView, buttonPane);

        return gridPane;
    }

    private static TableView<ObservableList<String>> createTableView(List<String> columnNames) {
        TableView<ObservableList<String>> tableView = new TableView<>();
        tableView.getStyleClass().add("table");

        for (int i = 0; i < columnNames.size(); i++) {
            final int columnIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnNames.get(i));
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(columnIndex)));
            tableView.getColumns().add(column);
        }

        return tableView;
    }

    private static void configureTableColumns(List<TableColumn<ObservableList<String>, ?>> columns) {
        for (TableColumn<ObservableList<String>, ?> column : columns) {
            TableColumn<ObservableList<String>, String> stringColumn = (TableColumn<ObservableList<String>, String>) column;
            stringColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            stringColumn.setOnEditCommit(t -> {
                ObservableList<String> row = t.getTableView().getItems().get(t.getTablePosition().getRow());
                row.set(t.getTablePosition().getColumn(), t.getNewValue());
            });
        }
    }

    private static ObservableList<ObservableList<String>> createTableData(List<List<String>> tableData) {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (List<String> rowData : tableData) {
            data.add(FXCollections.observableArrayList(rowData));
        }
        return data;
    }

    private static GridPane createButtonPane(Runnable backButtonAction, String tableName, TableView<ObservableList<String>> tableView) {
        GridPane buttonPane = new GridPane();
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setPadding(new Insets(10, 10, 10, 10));

        Button backButton = createButton("К списку", e -> backButtonAction.run());
        Button editButton = createButton("Изменить", e -> tableView.setEditable(true));
        Button saveButton = createButton("Сохранить", e -> handleSave(tableView, tableName));
        Button deleteButton = createButton("Удалить", e -> handleDeleteRow(tableView, tableName));
        Button addButton = createButton("Добавить", e -> handleAddRow(tableView));

        GridPane.setConstraints(backButton, 0, 0);
        GridPane.setConstraints(editButton, 1, 0);
        GridPane.setConstraints(saveButton, 2, 0);
        GridPane.setConstraints(deleteButton, 3, 0);
        GridPane.setConstraints(addButton, 4, 0);

        buttonPane.getChildren().addAll(backButton, editButton, saveButton, deleteButton, addButton);

        return buttonPane;
    }

    private static Button createButton(String text, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setOnAction(action);
        button.getStyleClass().add("button");
        return button;
    }


    private static void handleSave(TableView<ObservableList<String>> tableView, String tableName) {
        List<List<String>> updatedData = new ArrayList<>();
        List<List<String>> newData = new ArrayList<>();

        for (ObservableList<String> rowData : tableView.getItems()) {
            if (rowData.contains("")) {
                newData.add(new ArrayList<>(rowData));
            } else {
                updatedData.add(new ArrayList<>(rowData));
            }
        }

        try (Connection connection = DataBaseConnection.connect()) {
            if (!newData.isEmpty()) {
                DataBaseUtils.insertRows(connection, tableName, newData);
            }

            if (!updatedData.isEmpty()) {
                DataBaseUtils.updateTableData(connection, tableName, updatedData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static GridPane createGridPane(TableView<ObservableList<String>> tableView, GridPane buttonPane) {
        GridPane gridPane = new GridPane();

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(column1);

        RowConstraints row1 = new RowConstraints();
        row1.setMaxHeight(Control.USE_COMPUTED_SIZE);
        row1.setMinHeight(Control.USE_COMPUTED_SIZE);
        row1.setVgrow(Priority.NEVER);

        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);

        gridPane.getRowConstraints().addAll(row1, row2);

        gridPane.add(buttonPane, 0, 0);
        gridPane.add(tableView, 0, 1);

        return gridPane;
    }

    private static void handleDeleteRow(TableView<ObservableList<String>> tableView, String tableName) {
        ObservableList<ObservableList<String>> selectedRows = tableView.getSelectionModel().getSelectedItems();
        List<List<String>> rowsToRemove = new ArrayList<>();

        for (ObservableList<String> selectedRow : selectedRows) {
            rowsToRemove.add(new ArrayList<>(selectedRow));
        }

        try (Connection connection = DataBaseConnection.connect()) {
            DataBaseUtils.removeRows(connection, tableName, rowsToRemove);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableView.getItems().removeAll(selectedRows);
    }

    private static void handleAddRow(TableView<ObservableList<String>> tableView) {
        ObservableList<String> newRow = FXCollections.observableArrayList();

        for (int i = 0; i < tableView.getColumns().size(); i++) {
            newRow.add("");
        }

        tableView.getItems().add(newRow);
    }

}
