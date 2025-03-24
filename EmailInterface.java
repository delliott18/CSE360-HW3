import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmailInterface extends JPanel {
    private final String currentUser;
    private final String sessionToken;
    private JTable emailTable;
    private DefaultTableModel tableModel;
    private JTextArea emailContent;
    private JButton replyButton;
    private JButton forwardButton;
    private JButton deleteButton;
    private JToolBar toolbar;
    private JButton sendDraftButton;

    public EmailInterface(String username, String sessionToken) {
        this.currentUser = username;
        this.sessionToken = sessionToken;

        setLayout(new BorderLayout());
        setupUI();
        loadEmails();
    }

    private void setupUI() {
        // Create toolbar
        toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton composeButton = new JButton("Compose");
        JButton refreshButton = new JButton("Refresh");
        replyButton = new JButton("Reply");
        forwardButton = new JButton("Forward");
        deleteButton = new JButton("Delete");
        sendDraftButton = new JButton("Send Draft");
        sendDraftButton.setVisible(false);

        toolbar.add(composeButton);
        toolbar.addSeparator();
        toolbar.add(sendDraftButton);
        toolbar.addSeparator();
        toolbar.add(replyButton);
        toolbar.add(forwardButton);
        toolbar.addSeparator();
        toolbar.add(deleteButton);
        toolbar.add(refreshButton);

        // Initially disable email-specific buttons
        replyButton.setEnabled(false);
        forwardButton.setEnabled(false);
        deleteButton.setEnabled(false);

        // Create email list
        String[] columns = {"From", "Subject", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        emailTable = new JTable(tableModel);

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(new JScrollPane(emailTable));

        // Create and add email content viewer
        emailContent = new JTextArea();
        emailContent.setEditable(false);
        emailContent.setLineWrap(true);
        emailContent.setWrapStyleWord(true);
        splitPane.setBottomComponent(new JScrollPane(emailContent));

        // Set divider location
        splitPane.setDividerLocation(300);

        // Add components to panel
        add(toolbar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Add listeners
        composeButton.addActionListener(e -> showEmailDialog("Compose Email", "", "", ""));
        refreshButton.addActionListener(e -> loadEmails());
        replyButton.addActionListener(e -> replyToEmail());
        forwardButton.addActionListener(e -> forwardEmail());
        deleteButton.addActionListener(e -> deleteSelectedEmail());
        sendDraftButton.addActionListener(e -> sendSelectedDraft());

        emailTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedEmail();
                boolean hasSelection = emailTable.getSelectedRow() != -1;
                updateButtonStates(hasSelection);
            }
        });
    }

    private void updateButtonStates(boolean hasSelection) {
        if (!hasSelection) {
            replyButton.setEnabled(false);
            forwardButton.setEnabled(false);
            deleteButton.setEnabled(false);
            sendDraftButton.setVisible(false);
            return;
        }

        List<Email> allEmails = getAllEmails();
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < allEmails.size()) {
            Email email = allEmails.get(selectedRow);
            boolean isDraft = email.getStatus() == Email.EmailStatus.DRAFT;
            boolean isAuthor = email.getFromUser().equals(currentUser);

            sendDraftButton.setVisible(isDraft && isAuthor);
            replyButton.setEnabled(!isDraft);
            forwardButton.setEnabled(!isDraft);
            deleteButton.setEnabled(true);
        }
    }

    private void loadEmails() {
        tableModel.setRowCount(0);
        List<Email> allEmails = getAllEmails();

        for (Email email : allEmails) {
            Object[] row = {
                email.getFromUser(),
                email.getSubject(),
                email.getSentDate(),
                email.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private List<Email> getAllEmails() {
        List<Email> allEmails = new ArrayList<>();
        allEmails.addAll(MemoryStorage.getInboxEmails(currentUser));
        allEmails.addAll(MemoryStorage.getSentEmails(currentUser));
        allEmails.addAll(MemoryStorage.getDrafts(currentUser));
        allEmails.sort((e1, e2) -> e2.getSentDate().compareTo(e1.getSentDate()));
        return allEmails;
    }

    private void showEmailDialog(String title, String to, String subject, String content) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // To field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("To:"), gbc);
        JTextField toField = new JTextField(to, 30);
        gbc.gridx = 1;
        formPanel.add(toField, gbc);

        // Subject field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Subject:"), gbc);
        JTextField subjectField = new JTextField(subject, 30);
        gbc.gridx = 1;
        formPanel.add(subjectField, gbc);

        // Content area
        JTextArea contentArea = new JTextArea(content, 15, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendButton = new JButton("Send");
        JButton saveButton = new JButton("Save Draft");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(sendButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add button listeners
        sendButton.addActionListener(e -> {
            if (toField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please specify a recipient");
                return;
            }
            if (subjectField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please specify a subject");
                return;
            }

            MemoryStorage.addEmail(currentUser, toField.getText(), 
                                 subjectField.getText(), contentArea.getText());
            dialog.dispose();
            loadEmails();
            JOptionPane.showMessageDialog(this, "Email sent successfully!");
        });

        saveButton.addActionListener(e -> {
            MemoryStorage.saveDraft(currentUser,
                                  toField.getText().trim(),
                                  subjectField.getText().trim(),
                                  contentArea.getText().trim());
            dialog.dispose();
            loadEmails();
            JOptionPane.showMessageDialog(this, "Draft saved successfully!");
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void sendSelectedDraft() {
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Email> allEmails = getAllEmails();
            Email email = allEmails.get(selectedRow);

            if (email.getStatus() == Email.EmailStatus.DRAFT) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to send this draft?",
                    "Send Draft",
                    JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    MemoryStorage.sendDraft(email.getId());
                    loadEmails();
                    JOptionPane.showMessageDialog(this, "Draft sent successfully!");
                }
            }
        }
    }

    private void replyToEmail() {
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Email> allEmails = getAllEmails();
            Email email = allEmails.get(selectedRow);
            String subject = "Re: " + email.getSubject();
            showEmailDialog("Reply", email.getFromUser(), subject, 
                "\n\n-------- Original Message --------\n" + email.getContent());
        }
    }

    private void forwardEmail() {
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Email> allEmails = getAllEmails();
            Email email = allEmails.get(selectedRow);
            String subject = "Fwd: " + email.getSubject();
            showEmailDialog("Forward", "", subject,
                "\n\n-------- Forwarded Message --------\n" +
                "From: " + email.getFromUser() + "\n" +
                "Date: " + email.getSentDate() + "\n" +
                "Subject: " + email.getSubject() + "\n\n" +
                email.getContent());
        }
    }

    private void deleteSelectedEmail() {
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Email> allEmails = getAllEmails();
            if (selectedRow < allEmails.size()) {
                Email email = allEmails.get(selectedRow);

                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this email?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    MemoryStorage.deleteEmail(email.getId(), currentUser);
                    loadEmails();
                    emailContent.setText("");
                }
            }
        }
    }

    private void displaySelectedEmail() {
        int selectedRow = emailTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Email> allEmails = getAllEmails();
            if (selectedRow < allEmails.size()) {
                Email email = allEmails.get(selectedRow);

                StringBuilder content = new StringBuilder();
                content.append("From: ").append(email.getFromUser()).append("\n");
                content.append("To: ").append(email.getToUser()).append("\n");
                content.append("Subject: ").append(email.getSubject()).append("\n");
                content.append("Date: ").append(email.getSentDate()).append("\n");
                content.append("Status: ").append(email.getStatus()).append("\n\n");
                content.append(email.getContent());

                emailContent.setText(content.toString());
                emailContent.setCaretPosition(0);

                if (!email.isRead() && email.getToUser().equals(currentUser)) {
                    MemoryStorage.markAsRead(email.getId(), currentUser);
                    tableModel.setValueAt("READ", selectedRow, 3);
                }
            }
        }
    }
}