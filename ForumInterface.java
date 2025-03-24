import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ForumInterface extends JPanel {
    private final String currentUser;
    private final String sessionToken;
    private JTable questionsTable;
    private DefaultTableModel tableModel;
    private JButton answerButton;
    private JButton editQuestionButton;
    private JButton deleteQuestionButton;
    private JSplitPane splitPane;

    public ForumInterface(String username, String sessionToken) {
        this.currentUser = username;
        this.sessionToken = sessionToken;

        setLayout(new BorderLayout());
        setupUI();
        loadQuestions();
    }

    private void setupUI() {
        // Create toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton askButton = new JButton("Ask Question");
        JButton refreshButton = new JButton("Refresh");
        answerButton = new JButton("Answer");
        editQuestionButton = new JButton("Edit");
        deleteQuestionButton = new JButton("Delete");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        toolbar.add(askButton);
        toolbar.addSeparator();
        toolbar.add(answerButton);
        toolbar.addSeparator();
        toolbar.add(editQuestionButton);
        toolbar.addSeparator();
        toolbar.add(deleteQuestionButton);
        toolbar.addSeparator();
        toolbar.add(searchField);
        toolbar.add(searchButton);
        toolbar.add(refreshButton);

        // Initially disable buttons
        answerButton.setEnabled(false);
        editQuestionButton.setEnabled(false);
        deleteQuestionButton.setEnabled(false);

        // Create questions table
        String[] columns = {"Title", "Author", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        questionsTable = new JTable(tableModel);
        questionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create split pane
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(new JScrollPane(questionsTable));
        splitPane.setDividerLocation(300);

        // Add components
        add(toolbar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Add listeners
        askButton.addActionListener(e -> showAskQuestionDialog());
        refreshButton.addActionListener(e -> refreshDisplay());
        searchButton.addActionListener(e -> searchQuestions(searchField.getText()));
        answerButton.addActionListener(e -> showAnswerDialog());
        editQuestionButton.addActionListener(e -> editSelectedQuestion());
        deleteQuestionButton.addActionListener(e -> deleteSelectedQuestion());

        questionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = questionsTable.getSelectedRow();
                boolean hasSelection = selectedRow != -1;

                if (hasSelection) {
                    Question question = MemoryStorage.getQuestions().get(selectedRow);
                    boolean isAuthor = question.getAuthor().equals(currentUser);
                    editQuestionButton.setEnabled(isAuthor);
                    deleteQuestionButton.setEnabled(isAuthor);
                    answerButton.setEnabled(true);
                    displaySelectedQuestion();
                } else {
                    editQuestionButton.setEnabled(false);
                    deleteQuestionButton.setEnabled(false);
                    answerButton.setEnabled(false);
                }
            }
        });
    }

    private void showAskQuestionDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Ask Question", true);
        dialog.setMinimumSize(new Dimension(500, 400));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        JTextField titleField = new JTextField(30);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);

        // Question content
        JTextArea contentArea = new JTextArea(15, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Question Details"));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton postButton = new JButton("Post Question");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(postButton);
        buttonPanel.add(cancelButton);

        postButton.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a title");
                return;
            }
            if (contentArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter your question");
                return;
            }

            MemoryStorage.addQuestion(titleField.getText().trim(), 
                                    contentArea.getText().trim(), 
                                    currentUser);
            dialog.dispose();
            refreshDisplay();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAnswerDialog() {
        int selectedRow = questionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Question> questions = MemoryStorage.getQuestions();
            Question question = questions.get(selectedRow);

            JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Answer Question", true);
            dialog.setMinimumSize(new Dimension(500, 400));

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Question display
            JTextArea questionDisplay = new JTextArea(question.getContent());
            questionDisplay.setEditable(false);
            questionDisplay.setLineWrap(true);
            questionDisplay.setWrapStyleWord(true);
            questionDisplay.setBackground(new Color(240, 240, 240));
            JScrollPane questionScroll = new JScrollPane(questionDisplay);
            questionScroll.setBorder(BorderFactory.createTitledBorder("Question: " + question.getTitle()));

            // Answer area
            JTextArea answerArea = new JTextArea(10, 30);
            answerArea.setLineWrap(true);
            answerArea.setWrapStyleWord(true);
            JScrollPane answerScroll = new JScrollPane(answerArea);
            answerScroll.setBorder(BorderFactory.createTitledBorder("Your Answer"));

            // Create a panel for both text areas
            JPanel textPanel = new JPanel(new BorderLayout(0, 10));
            textPanel.add(questionScroll, BorderLayout.NORTH);
            textPanel.add(answerScroll, BorderLayout.CENTER);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton postButton = new JButton("Post Answer");
            JButton cancelButton = new JButton("Cancel");
            buttonPanel.add(postButton);
            buttonPanel.add(cancelButton);

            postButton.addActionListener(e -> {
                if (answerArea.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter your answer");
                    return;
                }

                MemoryStorage.addAnswer(question.getId(), 
                                      answerArea.getText().trim(), 
                                      currentUser);
                dialog.dispose();
                refreshDisplay();
            });

            cancelButton.addActionListener(e -> dialog.dispose());

            panel.add(textPanel, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
    }

    private void showEditAnswerDialog(Answer answer, Question question) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Answer", true);
        dialog.setMinimumSize(new Dimension(500, 400));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Question display
        JPanel questionPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Original Question: " + question.getTitle());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JTextArea questionDisplay = new JTextArea(question.getContent());
        questionDisplay.setEditable(false);
        questionDisplay.setLineWrap(true);
        questionDisplay.setWrapStyleWord(true);
        questionDisplay.setBackground(new Color(240, 240, 240));

        questionPanel.add(titleLabel, BorderLayout.NORTH);
        questionPanel.add(new JScrollPane(questionDisplay), BorderLayout.CENTER);

        // Answer area
        JTextArea answerArea = new JTextArea(answer.getContent(), 10, 30);
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        JScrollPane answerScroll = new JScrollPane(answerArea);
        answerScroll.setBorder(BorderFactory.createTitledBorder("Your Answer"));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        updateButton.addActionListener(e -> {
            if (answerArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Answer cannot be empty");
                return;
            }

            MemoryStorage.updateAnswer(answer.getId(), answerArea.getText().trim());
            dialog.dispose();
            refreshDisplay();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // Layout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.add(questionPanel, BorderLayout.NORTH);
        mainPanel.add(answerScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainPanel);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editSelectedQuestion() {
        int selectedRow = questionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Question> questions = MemoryStorage.getQuestions();
            Question question = questions.get(selectedRow);

            if (!question.getAuthor().equals(currentUser)) {
                JOptionPane.showMessageDialog(this, 
                    "You can only edit your own questions.", 
                    "Edit Not Allowed", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Question", true);
            dialog.setMinimumSize(new Dimension(500, 400));

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Create form panel with current values
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            // Title field with current title
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Title:"), gbc);
            JTextField titleField = new JTextField(question.getTitle(), 30);
            gbc.gridx = 1;
            formPanel.add(titleField, gbc);

            // Question content with current content
            JTextArea contentArea = new JTextArea(question.getContent(), 15, 30);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(contentArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Question Details"));

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton updateButton = new JButton("Update");
            JButton cancelButton = new JButton("Cancel");
            buttonPanel.add(updateButton);
            buttonPanel.add(cancelButton);

            updateButton.addActionListener(e -> {
                if (titleField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a title");
                    return;
                }
                if (contentArea.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter your question");
                    return;
                }

                MemoryStorage.updateQuestion(question.getId(), 
                                           titleField.getText().trim(), 
                                           contentArea.getText().trim());
                dialog.dispose();
                refreshDisplay();
            });

            cancelButton.addActionListener(e -> dialog.dispose());

            panel.add(formPanel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
    }

    private void deleteSelectedQuestion() {
        int selectedRow = questionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Question> questions = MemoryStorage.getQuestions();
            Question question = questions.get(selectedRow);

            if (!question.getAuthor().equals(currentUser)) {
                JOptionPane.showMessageDialog(this, 
                    "You can only delete your own questions.", 
                                        "Delete Not Allowed", 
                                        JOptionPane.WARNING_MESSAGE);
                                    return;
                                }

                                int confirm = JOptionPane.showConfirmDialog(this,
                                    "Are you sure you want to delete this question?\n" +
                                    "This will also delete all associated answers.",
                                    "Confirm Delete",
                                    JOptionPane.YES_NO_OPTION);

                                if (confirm == JOptionPane.YES_OPTION) {
                                    MemoryStorage.deleteQuestion(question.getId());
                                    refreshDisplay();
                                }
                            }
                        }

                        private void loadQuestions() {
                            int selectedRow = questionsTable.getSelectedRow();
                            tableModel.setRowCount(0);
                            List<Question> questions = MemoryStorage.getQuestions();

                            for (Question question : questions) {
                                Object[] row = {
                                    question.getTitle(),
                                    question.getAuthor(),
                                    question.getCreatedAt(),
                                    question.getStatus()
                                };
                                tableModel.addRow(row);
                            }

                            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                                questionsTable.setRowSelectionInterval(selectedRow, selectedRow);
                            }
                        }

                        private void displaySelectedQuestion() {
                            int selectedRow = questionsTable.getSelectedRow();
                            if (selectedRow >= 0) {
                                List<Question> questions = MemoryStorage.getQuestions();
                                Question question = questions.get(selectedRow);
                                List<Answer> answers = MemoryStorage.getAnswersForQuestion(question.getId());

                                JPanel contentPanel = new JPanel();
                                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

                                // Question section
                                JPanel questionPanel = new JPanel(new BorderLayout());
                                questionPanel.setBorder(BorderFactory.createTitledBorder("Question"));

                                // Question header
                                JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                                headerPanel.add(new JLabel("Title: " + question.getTitle()));
                                headerPanel.add(new JLabel(" | Author: " + question.getAuthor()));
                                headerPanel.add(new JLabel(" | Posted: " + question.getCreatedAt()));

                                // Question content
                                JTextArea questionContent = new JTextArea(question.getContent());
                                questionContent.setEditable(false);
                                questionContent.setLineWrap(true);
                                questionContent.setWrapStyleWord(true);
                                questionContent.setBackground(new Color(240, 240, 240));

                                questionPanel.add(headerPanel, BorderLayout.NORTH);
                                questionPanel.add(new JScrollPane(questionContent), BorderLayout.CENTER);
                                contentPanel.add(questionPanel);

                                // Answers section
                                JPanel answersPanel = new JPanel();
                                answersPanel.setLayout(new BoxLayout(answersPanel, BoxLayout.Y_AXIS));
                                answersPanel.setBorder(BorderFactory.createTitledBorder("Answers"));

                                if (answers.isEmpty()) {
                                    JLabel noAnswersLabel = new JLabel("No answers yet. Be the first to answer!");
                                    noAnswersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                                    answersPanel.add(noAnswersLabel);
                                } else {
                                    for (Answer answer : answers) {
                                        JPanel answerPanel = new JPanel(new BorderLayout());
                                        answerPanel.setBorder(BorderFactory.createEtchedBorder());

                                        // Answer header with controls
                                        JPanel answerHeader = new JPanel(new BorderLayout());
                                        JPanel answerInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
                                        answerInfo.add(new JLabel("From: " + answer.getAuthor()));
                                        answerInfo.add(new JLabel(" | Posted: " + answer.getCreatedAt()));

                                        if (answer.isAccepted()) {
                                            JLabel acceptedLabel = new JLabel(" ✓ ACCEPTED");
                                            acceptedLabel.setForeground(new Color(0, 120, 0));
                                            answerInfo.add(acceptedLabel);
                                        }

                                        JPanel answerControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                                        if (answer.getAuthor().equals(currentUser)) {
                                            JButton editButton = new JButton("Edit");
                                            JButton deleteButton = new JButton("Delete");

                                            editButton.addActionListener(e -> showEditAnswerDialog(answer, question));
                                            deleteButton.addActionListener(e -> {
                                                int confirm = JOptionPane.showConfirmDialog(this,
                                                    "Are you sure you want to delete this answer?",
                                                    "Confirm Delete",
                                                    JOptionPane.YES_NO_OPTION);

                                                if (confirm == JOptionPane.YES_OPTION) {
                                                    MemoryStorage.deleteAnswer(answer.getId());
                                                    refreshDisplay();
                                                }
                                            });

                                            answerControls.add(editButton);
                                            answerControls.add(deleteButton);
                                        }

                                        if (question.getAuthor().equals(currentUser) && !answer.isAccepted()) {
                                            JButton acceptButton = new JButton("Accept Answer");
                                            acceptButton.addActionListener(e -> {
                                                MemoryStorage.acceptAnswer(answer.getId(), question.getId());
                                                refreshDisplay();
                                            });
                                            answerControls.add(acceptButton);
                                        }

                                        answerHeader.add(answerInfo, BorderLayout.WEST);
                                        answerHeader.add(answerControls, BorderLayout.EAST);

                                        // Answer content
                                        JTextArea answerContent = new JTextArea(answer.getContent());
                                        answerContent.setEditable(false);
                                        answerContent.setLineWrap(true);
                                        answerContent.setWrapStyleWord(true);
                                        answerContent.setBackground(new Color(250, 250, 250));

                                        answerPanel.add(answerHeader, BorderLayout.NORTH);
                                        answerPanel.add(new JScrollPane(answerContent), BorderLayout.CENTER);

                                        answersPanel.add(answerPanel);
                                        answersPanel.add(Box.createVerticalStrut(10)); // Add spacing
                                    }
                                }

                                contentPanel.add(answersPanel);

                                // Update the display
                                JScrollPane scrollPane = new JScrollPane(contentPanel);
                                scrollPane.getVerticalScrollBar().setUnitIncrement(16);

                                splitPane.setBottomComponent(scrollPane);
                            }
                        }

                        private void refreshDisplay() {
                            loadQuestions();
                            displaySelectedQuestion();
                        }

                        private void searchQuestions(String query) {
                            if (query.trim().isEmpty()) {
                                loadQuestions();
                                return;
                            }

                            tableModel.setRowCount(0);
                            List<Question> questions = MemoryStorage.searchQuestions(query);

                            for (Question question : questions) {
                                Object[] row = {
                                    question.getTitle(),
                                    question.getAuthor(),
                                    question.getCreatedAt(),
                                    question.getStatus()
                                };
                                tableModel.addRow(row);
                            }
                        }
                    }