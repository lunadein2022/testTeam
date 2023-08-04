package org.sp.tproject.main.view;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
    private static final String DATABASE_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DATABASE_USERNAME = "pomodoro";
    private static final String DATABASE_PASSWORD = "1234";
    private Connection connection;

    public DatabaseHandler() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        this.connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
    }

    public ResultSet getItems() throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery("SELECT * FROM todolist");
    }

    public void addItem(TodoItem item) throws SQLException {
        String sql = "INSERT INTO todolist (task_idx, todo, complete) VALUES (task_idx.nextval, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, item.getTask());
        statement.setInt(2, item.isComplete() ? 1 : 0);
        statement.executeUpdate();
    }


    public void updateItem(TodoItem item) throws SQLException {
        String sql = "UPDATE todolist SET complete = ? WHERE todo = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, item.isComplete() ? 1 : 0);
        statement.setString(2, item.getTask());
        statement.executeUpdate();
    }
    
    public void deleteItem(TodoItem item) throws SQLException {
        String query = "DELETE FROM todolist WHERE todo = ?"; // Adjust this based on how you're identifying tasks
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, item.getTask());
            pstmt.executeUpdate();
        }
    }

    public void clearItems() throws SQLException {
        String query = "DELETE FROM todolist";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.executeUpdate();
        }
    }
    
}
