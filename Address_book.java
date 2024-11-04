import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.*;

public class AddressBook {
    private JFrame frame;
    private JPanel panel;
    private JTextField nameField, phoneField, emailField, searchField;
    private JTextArea contactsArea;
    private ArrayList<Contact> contacts;
    private Connection connection;

    public AddressBook() {
        contacts = new ArrayList<>();
        setupDatabaseConnection();
        setupUI();
    }

    private void setupDatabaseConnection() {
        try {
            String url = "jdbc:mysql://127.0.0.1:3306/sri"; // Your database URL
            String user = "root"; // Your database username
            String password = "shree2308"; // Your database password
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database connection error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupUI() {
        frame = new JFrame("Address Book");
        frame.setSize(500, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel();
        panel.setLayout(new GridLayout(10, 1));
        panel.setBackground(new Color(220, 240, 255));

        JLabel titleLabel = new JLabel("Address Book", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 120, 180));
        panel.add(titleLabel);

        nameField = new JTextField(20);
        nameField.setBorder(BorderFactory.createTitledBorder("Name"));
        panel.add(nameField);

        phoneField = new JTextField(20);
        phoneField.setBorder(BorderFactory.createTitledBorder("Phone"));
        panel.add(phoneField);

        emailField = new JTextField(20);
        emailField.setBorder(BorderFactory.createTitledBorder("Email"));
        panel.add(emailField);

        JButton addButton = new JButton("Add Contact");
        addButton.setBackground(new Color(100, 200, 100));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(new AddButtonListener());
        panel.add(addButton);

        searchField = new JTextField(20);
        searchField.setBorder(BorderFactory.createTitledBorder("Search by Name"));
        panel.add(searchField);

        JButton updateButton = new JButton("Update Contact");
        updateButton.setBackground(new Color(255, 180, 0));
        updateButton.setForeground(Color.WHITE);
        updateButton.addActionListener(new UpdateButtonListener());
        panel.add(updateButton);

        JButton deleteButton = new JButton("Delete Contact");
        deleteButton.setBackground(new Color(255, 80, 80));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(new DeleteButtonListener());
        panel.add(deleteButton);

        contactsArea = new JTextArea(10, 30);
        contactsArea.setBorder(BorderFactory.createTitledBorder("Contacts"));
        contactsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(contactsArea);
        panel.add(scrollPane);

        frame.add(panel);
        frame.setVisible(true);
    }

    private class AddButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();

            if (!name.isEmpty() && !phone.isEmpty() && !email.isEmpty()) {
                contacts.add(new Contact(name, phone, email));
                addContactToDatabase(name, phone, email);
                displayContacts();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(frame, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addContactToDatabase(String name, String phone, String email) {
        try {
            String query = "INSERT INTO contacts (name, phone, email) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, phone);
            preparedStatement.setString(3, email);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error adding contact: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private class UpdateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String oldName = searchField.getText().trim(); // Get the name to search
            Contact contactToUpdate = findContactByName(oldName);

            if (contactToUpdate != null) {
                // Capture updated values
                String newName = nameField.getText().trim(); // New name input
                String newPhone = phoneField.getText().trim(); // New phone input
                String newEmail = emailField.getText().trim(); // New email input

                // Update the contact's fields with new values
                if (!newName.isEmpty()) {
                    contactToUpdate.setName(newName);
                }
                if (!newPhone.isEmpty()) {
                    contactToUpdate.setPhone(newPhone);
                }
                if (!newEmail.isEmpty()) {
                    contactToUpdate.setEmail(newEmail);
                }

                // Update contact in the database
                updateContactInDatabase(contactToUpdate, oldName);

                // Refresh display and clear fields
                displayContacts();
                clearFields();
                JOptionPane.showMessageDialog(frame, "Contact updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Contact not found! Please check the name and try again.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateContactInDatabase(Contact contact, String oldName) {
        String query = "UPDATE contacts SET name = ?, phone = ?, email = ? WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, contact.getName());
            preparedStatement.setString(2, contact.getPhone());
            preparedStatement.setString(3, contact.getEmail());
            preparedStatement.setString(4, oldName); // Use the old name for the WHERE clause

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error updating contact: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private class DeleteButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String nameToSearch = searchField.getText();
            Contact contactToDelete = findContactByName(nameToSearch);

            if (contactToDelete != null) {
                contacts.remove(contactToDelete);
                deleteContactFromDatabase(contactToDelete);
                displayContacts();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(frame, "Contact not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteContactFromDatabase(Contact contact) {
        try {
            String query = "DELETE FROM contacts WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, contact.getName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error deleting contact: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Contact findContactByName(String name) {
        for (Contact contact : contacts) {
            if (contact.getName().equalsIgnoreCase(name)) {
                return contact;
            }
        }
        return null;
    }

    private void displayContacts() {
        contactsArea.setText("");
        for (Contact contact : contacts) {
            contactsArea.append(contact.toString() + "\n");
        }
    }

    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        searchField.setText("");
    }

    public static void main(String[] args) {
        new AddressBook();
    }
}
