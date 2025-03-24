import javax.swing.*;
import java.awt.*;

public class MainNavigationUI extends JFrame {
    private final String currentUser;
    private final String sessionToken;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    public MainNavigationUI(String username, String sessionToken) {
        this.currentUser = username;
        this.sessionToken = sessionToken;
        setTitle("Email and Forum System - " + username);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create top panel for navigation and user info
        JPanel topPanel = new JPanel(new BorderLayout());

        // Create navigation toolbar
        JToolBar navBar = new JToolBar();
        navBar.setFloatable(false);
        JButton emailButton = new JButton("Email");
        JButton forumButton = new JButton("Forum");
        navBar.add(emailButton);
        navBar.addSeparator();
        navBar.add(forumButton);

        // Create user panel with logout button
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel userLabel = new JLabel("Logged in as: " + username);
        JButton logoutButton = new JButton("Logout");
        userPanel.add(userLabel);
        userPanel.add(logoutButton);

        topPanel.add(navBar, BorderLayout.WEST);
        topPanel.add(userPanel, BorderLayout.EAST);

        // Main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize interfaces
        EmailInterface emailInterface = new EmailInterface(username, sessionToken);
        ForumInterface forumInterface = new ForumInterface(username, sessionToken);

        // Add interfaces to card layout
        mainPanel.add(emailInterface, "EMAIL");
        mainPanel.add(forumInterface, "FORUM");

        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Add button listeners
        emailButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "EMAIL");
            setTitle("Email System - " + username);
        });

        forumButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "FORUM");
            setTitle("Forum System - " + username);
        });

        logoutButton.addActionListener(e -> {
            // Handle logout
            MemoryStorage.logout(sessionToken);
            dispose(); // Close current window

            // Show login screen
            SwingUtilities.invokeLater(() -> {
                EmailLoginUI loginUI = new EmailLoginUI();
                loginUI.setVisible(true);
            });
        });

        // Show email interface by default
        cardLayout.show(mainPanel, "EMAIL");
    }
}