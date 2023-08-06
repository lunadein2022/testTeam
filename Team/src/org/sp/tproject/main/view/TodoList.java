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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class TodoList extends JPanel {
    DefaultListModel<TodoItem> model;
    JList<TodoItem> list;
    JTextField inputField1;
    JButton addButton1;
    DatabaseHandler databaseHandler;
    JButton deleteButton;
    JButton clearButton;
    JLabel countLabel;
    JLabel progressLabel1;
    JScrollPane scrollPane;

    public TodoList() {
        setLayout(new BorderLayout());
        setBackground(Color.PINK);
        setPreferredSize(new Dimension(300, 350));
        
        try {
            databaseHandler = new DatabaseHandler();
            model = new DefaultListModel<>();
            ResultSet rs = databaseHandler.getItems();
            while (rs.next()) {
                // Adjusting the instantiation of TodoItem with additional fields
                TodoItem item = new TodoItem(
                    rs.getInt("yy"),
                    rs.getInt("mm"),
                    rs.getInt("dd"),
                    rs.getString("task"),
                    rs.getInt("complete") == 1,
                    rs.getString("client")
                );
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
                updateCountAndProgress(); // Update count and progress after item is clicked
            }
        });

        countLabel = new JLabel();
        progressLabel1 = new JLabel();
        
        scrollPane = new JScrollPane(list);
        scrollPane.setMinimumSize(new Dimension(250, 350));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);  // Adding the already created scrollPane
        
        inputField1 = new JTextField();
        addButton1 = new JButton("Add");
        addButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String task = inputField1.getText();
                if (!task.isEmpty()) {
                    // Here you need to gather yy, mm, dd, client information to instantiate the TodoItem
                    // As an example, I'll use placeholders (you need to replace these with appropriate values)
                    int yy = 2023; // Placeholder for year
                    int mm = 8; // Placeholder for month
                    int dd = 4; // Placeholder for day
                    String client = "clientID"; // Placeholder for client ID

                    TodoItem item = new TodoItem(yy, mm, dd, task, false, client);
                    model.addElement(item);
                    try {
                        databaseHandler.addItem(item);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    inputField1.setText("");
                    updateCountAndProgress(); // Update count and progress after item is added
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
                    updateCountAndProgress(); // Update count and progress after item is deleted
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
                updateCountAndProgress(); // Update count and progress after items are cleared
            }
        });
        
        JPanel addPanel = new JPanel(new BorderLayout());
        addPanel.add(inputField1, BorderLayout.CENTER);
        addPanel.add(addButton1, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(deleteButton, BorderLayout.CENTER);
        buttonPanel.add(clearButton, BorderLayout.EAST);
        
        JPanel countPanel = new JPanel(new BorderLayout());
        countPanel.add(countLabel, BorderLayout.CENTER);
        countPanel.add(progressLabel1, BorderLayout.EAST);

        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(addPanel, BorderLayout.CENTER);
        southPanel.add(countPanel, BorderLayout.SOUTH);

        
        add(southPanel, BorderLayout.SOUTH);

        updateCountAndProgress();
    }



    private void updateCountAndProgress() {
        int complete = 0, total = model.getSize();
        for (int i = 0; i < total; i++) {
            if (model.get(i).isComplete()) complete++;
        }
        countLabel.setText(complete + " / " + total);
        int progress = total == 0 ? 0 : (complete * 100) / total;
        progressLabel1.setText(progress + "%");
    }
}
