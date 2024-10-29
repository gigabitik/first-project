package com.example.university;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataBaseUtils {

    public static List<String> getTableNames(Connection connection) {
        List<String> tableNames = new ArrayList<>();

        try (ResultSet resultSet = connection.getMetaData().getTables(null, "dbo", "%", new String[]{"TABLE"})) {
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }

        return tableNames;
    }

    public static List<List<String>> getTableData(Connection connection, String tableName) {
        List<List<String>> tableData = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName)) {
            int columnCount = resultSet.getMetaData().getColumnCount();

            while (resultSet.next()) {
                List<String> rowData = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.add(resultSet.getString(i));
                }
                tableData.add(rowData);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }

        return tableData;
    }

    public static String getFirstColumnName(Connection connection, String tableName) {
        try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, tableName, null)) {
            if (resultSet.next()) {
                return resultSet.getString("COLUMN_NAME");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return null;
    }

    public static List<String> getColumns(Connection connection, String tableName) {
        List<String> columnNames = new ArrayList<>();

        try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, tableName, null)) {
            while (resultSet.next()) {
                columnNames.add(resultSet.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }

        return columnNames;
    }

    public static void updateTableData(Connection connection, String tableName, List<List<String>> newData) {
        try {
            List<String> columnNames = getColumns(connection, tableName);

            for (List<String> rowData : newData) {
                String originalId = rowData.get(0);
                String updateQuery = buildUpdateQuery(connection, tableName, columnNames, rowData, originalId);

                executeUpdateQuery(connection, updateQuery);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void executeUpdateQuery(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        }
    }

    private static String buildUpdateQuery(Connection connection, String tableName, List<String> columnNames, List<String> rowData, String originalId) {
        List<String> columnValuePairs = new ArrayList<>();

        for (int i = 1; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            String columnValue = rowData.get(i);

            if (isDateTimeColumn(connection, tableName, columnName)) {
                String[] dateFormats = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};

                Date parsedDate = null;
                for (String dateFormat : dateFormats) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                        parsedDate = sdf.parse(columnValue);
                        if (parsedDate != null) {
                            break;
                        }
                    } catch (ParseException e) {
                        //
                    }
                }

                if (parsedDate != null) {
                    columnValue = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(parsedDate);
                } else {
                    System.out.println("Error: " + columnValue);
                }
            }

            columnValuePairs.add(columnName + " = N'" + columnValue + "'");
        }

        String idCol = getFirstColumnName(connection, tableName);

        return "UPDATE " + tableName + " SET " +
                String.join(", ", columnValuePairs) +
                " WHERE " + idCol + " = '" + originalId + "'";
    }




    public static void removeRows(Connection connection, String tableName, List<List<String>> rowsToRemove) {
        try {
            for (List<String> rowData : rowsToRemove) {
                String originalId = rowData.get(0);
                String idCol = getFirstColumnName(connection, tableName);
                String deleteQuery = "DELETE FROM " + tableName + " WHERE " + idCol + "= '" + originalId + "'";

                executeUpdateQuery(connection, deleteQuery);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public static void insertRows(Connection connection, String tableName, List<List<String>> newData) {
        try {
            List<String> columnNames = getColumns(connection, tableName);

            for (List<String> rowData : newData) {
                String insertQuery = buildInsertQuery(tableName, columnNames, rowData);

                executeUpdateQuery(connection, insertQuery);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static String buildInsertQuery(String tableName, List<String> columnNames, List<String> rowData) {
        String columns = String.join(", ", columnNames.subList(1, columnNames.size()));
        String values = String.join(", ", rowData.subList(1, rowData.size()).stream()
                .map(val -> "N'" + val + "'")
                .toArray(String[]::new));

        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
    }

    private static boolean isDateTimeColumn(Connection connection, String tableName, String columnName) {
        try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
            if (resultSet.next()) {
                int dataType = resultSet.getInt("DATA_TYPE");
                return dataType == java.sql.Types.TIMESTAMP || dataType == java.sql.Types.DATE;
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    private static void handleSQLException(SQLException e) {
        e.printStackTrace();
    }
}