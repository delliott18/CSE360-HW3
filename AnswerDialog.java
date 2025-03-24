import javax.swing.*;
import java.awt.*;

public class AnswerDialog extends JDialog {
    private final Answer answer;
    private final String currentUser;
    private final Question question;
    private final Runnable onUpdate;

    public AnswerDialog(Frame owner, Answer answer, Question question, String currentUser, Runnable onUpdate) {
        super(owner, "Edit Answer", true);
        this.answer = answer;
        this.question = question;
        this.currentUser = currentUser;
        this.onUpdate = onUpdate;

        setupUI();
    }

    private void setupUI() {
        setMinimumSize(new Dimension(500, 300));
        setPreferredSize(new Dimension(500, 300));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Original question display
        JTextArea questionArea = new JTextArea(question.getContent());
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setBackground(new Color(240, 240, 240));
        JScrollPane questionScroll = new JScrollPane(questionArea);
        questionScroll.setBorder(BorderFactory.createTitledBorder("Original Question"));

        // Answer content area
        JTextArea contentArea = new JTextArea(answer.getContent(), 10, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane answerScroll = new JScrollPane(contentArea);
        answerScroll.setBorder(BorderFactory.createTitledBorder("Your Answer"));

        // Create panel for text areas
        JPanel textPanel = new JPanel(new BorderLayout(0, 10));
        textPanel.add(questionScroll, BorderLayout.NORTH);
        textPanel.add(answerScroll, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        updateButton.addActionListener(e -> {
            if (contentArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Answer cannot be empty");
                return;
            }

            MemoryStorage.updateAnswer(answer.getId(), contentArea.getText().trim());
            onUpdate.run();
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        mainPanel.add(textPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(getOwner());
    }

    public static void showEditDialog(Frame owner, Answer answer, Question question, String currentUser, Runnable onUpdate) {
        if (!answer.getAuthor().equals(currentUser)) {
            JOptionPane.showMessageDialog(owner,
                "You can only edit your own answers.",
                "Edit Not Allowed",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        AnswerDialog dialog = new AnswerDialog(owner, answer, question, currentUser, onUpdate);
        dialog.setVisible(true);
    }

    public static void handleDelete(Component parent, Answer answer, String currentUser, Runnable onDelete) {
        if (!answer.getAuthor().equals(currentUser)) {
            JOptionPane.showMessageDialog(parent,
                "You can only delete your own answers.",
                "Delete Not Allowed",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(parent,
            "Are you sure you want to delete this answer?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            MemoryStorage.deleteAnswer(answer.getId());
            onDelete.run();
        }
    }

    public static void acceptAnswer(Component parent, Answer answer, Question question, String currentUser, Runnable onAccept) {
        if (!question.getAuthor().equals(currentUser)) {
            JOptionPane.showMessageDialog(parent,
                "Only the question author can accept answers.",
                "Accept Not Allowed",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        MemoryStorage.acceptAnswer(answer.getId(), question.getId());
        onAccept.run();
    }
}