import javax.swing.*;
import java.awt.*;

public class EmailLoginUI extends JFrame {
    private final UserManager userManager;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton registerButton;
    private final JLabel statusLabel;

    public EmailLoginUI() {
        this.userManager = new UserManager();

        // Set up UI
        setTitle("Login System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Create panels
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        gbc.gridy = 3;
        mainPanel.add(statusLabel, gbc);

        add(mainPanel);

        // Add button listeners
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegister());
        passwordField.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Please enter both username and password");
            return;
        }

        UserManager.AuthResult result = userManager.login(username, password);

        if (result.success) {
            statusLabel.setForeground(Color.GREEN);
            statusLabel.setText("Login successful!");

            SwingUtilities.invokeLater(() -> {
                MainNavigationUI mainUI = new MainNavigationUI(username, result.sessionToken);
                mainUI.setVisible(true);
                this.dispose();
            });
        } else {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText(result.message);
            passwordField.setText("");
        }
    }

    private void handleRegister() {
        JDialog registerDialog = new JDialog(this, "Register New Account", true);
        registerDialog.setSize(450, 300);  // Increased size
        registerDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Increased padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        JTextField regUsernameField = new JTextField(20);
        panel.add(regUsernameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField regPasswordField = new JPasswordField(20);
        panel.add(regPasswordField, gbc);

        // Confirm password field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);

        // Register button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);  // Extra top padding for button
        JButton submitButton = new JButton("Register");
        panel.add(submitButton, gbc);

        // Status label
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 10, 10);
        JLabel regStatusLabel = new JLabel(" ");
        regStatusLabel.setPreferredSize(new Dimension(380, 40));  // Fixed size for status
        panel.add(regStatusLabel, gbc);

        submitButton.addActionListener(e -> {
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword());
            String confirmPass = new String(confirmPasswordField.getPassword());

            if (username.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                regStatusLabel.setForeground(Color.RED);
                regStatusLabel.setText("All fields are required");
                return;
            }

            if (!password.equals(confirmPass)) {
                regStatusLabel.setForeground(Color.RED);
                regStatusLabel.setText("Passwords do not match!");
                return;
            }

            UserManager.AuthResult result = userManager.registerUser(username, password);

            if (result.success) {
                regStatusLabel.setForeground(Color.GREEN);
                regStatusLabel.setText("Registration successful!");

                // Close dialog after successful registration
                Timer timer = new Timer(1500, evt -> {
                    registerDialog.dispose();
                    // Auto-fill the login form
                    usernameField.setText(username);
                    passwordField.setText(password);
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                regStatusLabel.setForeground(Color.RED);
                regStatusLabel.setText(result.message);
            }
        });

        // Add panel to dialog with some padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(panel, BorderLayout.CENTER);
        registerDialog.add(mainPanel);

        // Ensure dialog is properly sized
        registerDialog.setMinimumSize(new Dimension(450, 300));
        registerDialog.pack();
        registerDialog.setResizable(false);
        registerDialog.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            EmailLoginUI ui = new EmailLoginUI();
            ui.setVisible(true);
        });
    }
}