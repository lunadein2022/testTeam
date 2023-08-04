package org.sp.tproject.main.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class TodoList extends JPanel {
    DefaultListModel<TodoItem> model;
    JList<TodoItem> list;
    JTextField inputField;
    JButton addButton;
    DatabaseHandler databaseHandler;
    JButton deleteButton;
    JButton clearButton;
    JScrollPane scrollPane;

    public TodoList() {
        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);
        setPreferredSize(new Dimension(370, 350));
        
        try {
            databaseHandler = new DatabaseHandler();
            model = new DefaultListModel<>();
            ResultSet rs = databaseHandler.getItems();
            while(rs.next()) {
                TodoItem item = new TodoItem(rs.getString("task"), rs.getInt("complete") == 1);
                model.addElement(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the database");
        }
        
        list = new JList<>(model);
        list.setCellRenderer(new TodoListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TodoItem item = list.getSelectedValue();
                item.setComplete(!item.isComplete());
                list.repaint();
                try {
                    databaseHandler.updateItem(item);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        scrollPane = new JScrollPane(list);
        scrollPane.setMinimumSize(new Dimension(370, 350));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);  // Adding the already created scrollPane
        
        inputField = new JTextField();
        addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String task = inputField.getText();
                if(!task.isEmpty()){
                    TodoItem item = new TodoItem(task, false);
                    model.addElement(item);
                    try {
                        databaseHandler.addItem(item);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    inputField.setText("");
                }
            }
        });
        
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TodoItem item = list.getSelectedValue();
                if (item != null) {
                    model.removeElement(item);
                    try {
                        databaseHandler.deleteItem(item);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.clear();
                try {
                    databaseHandler.clearItems();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        add(buttonPanel, BorderLayout.NORTH);
    }
}
