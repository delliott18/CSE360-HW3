import java.util.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public class MemoryStorage {
    private static final Map<String, String> users = new HashMap<>();  // username -> password
    private static final Map<String, String> sessions = new HashMap<>();  // token -> username
    private static final Map<String, Integer> loginAttempts = new HashMap<>();  // username -> attempts
    private static final Map<String, LocalDateTime> lockouts = new HashMap<>();  // username -> lockout time

    private static final List<Email> emails = new ArrayList<>();
    private static final List<Question> questions = new ArrayList<>();
    private static final List<Answer> answers = new ArrayList<>();
    private static int nextEmailId = 1;
    private static int nextQuestionId = 1;
    private static int nextAnswerId = 1;

    // User Management
    public static void addUser(String username, String password) {
        users.put(username, password);
        addSampleEmails(username);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }

    public static String getPassword(String username) {
        return users.get(username);
    }

    // Session Management
    public static String createSession(String username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);
        return token;
    }

    public static void logout(String token) {
        sessions.remove(token);
    }

    public static String getUserFromSession(String token) {
        return sessions.get(token);
    }

    // Login Attempt Management
    public static void incrementLoginAttempts(String username) {
        loginAttempts.put(username, loginAttempts.getOrDefault(username, 0) + 1);
        if (loginAttempts.get(username) >= 5) {
            lockouts.put(username, LocalDateTime.now().plusMinutes(15));
        }
    }

    public static void resetLoginAttempts(String username) {
        loginAttempts.remove(username);
        lockouts.remove(username);
    }

    public static boolean isLockedOut(String username) {
        LocalDateTime lockoutTime = lockouts.get(username);
        return lockoutTime != null && lockoutTime.isAfter(LocalDateTime.now());
    }

    public static long getLockoutTimeRemaining(String username) {
        LocalDateTime lockoutTime = lockouts.get(username);
        if (lockoutTime == null || lockoutTime.isBefore(LocalDateTime.now())) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), lockoutTime);
    }

    // Email Management
    public static void addSampleEmails(String username) {
        addEmail("system@forum.com", username, "Welcome to the Forum", 
            "Welcome to our forum system! Feel free to ask questions and help others.");
    }

    public static int addEmail(String fromUser, String toUser, String subject, String content) {
        Email email = new Email();
        email.setId(nextEmailId++);
        email.setFromUser(fromUser);
        email.setToUser(toUser);
        email.setSubject(subject);
        email.setContent(content);
        email.setSentDate(LocalDateTime.now());
        email.setStatus(Email.EmailStatus.SENT);
        email.setRead(false);
        emails.add(email);
        return email.getId();
    }

    public static void saveDraft(String fromUser, String toUser, String subject, String content) {
        Email email = new Email();
        email.setId(nextEmailId++);
        email.setFromUser(fromUser);
        email.setToUser(toUser != null ? toUser : "");
        email.setSubject(subject != null ? subject : "");
        email.setContent(content != null ? content : "");
        email.setSentDate(LocalDateTime.now());
        email.setStatus(Email.EmailStatus.DRAFT);
        email.setRead(false);
        emails.add(email);
    }

    public static void sendDraft(int emailId) {
        emails.stream()
            .filter(e -> e.getId() == emailId && e.getStatus() == Email.EmailStatus.DRAFT)
            .findFirst()
            .ifPresent(e -> {
                // Only send if recipient and subject are specified
                if (!e.getToUser().trim().isEmpty() && !e.getSubject().trim().isEmpty()) {
                    e.setStatus(Email.EmailStatus.SENT);
                    e.setSentDate(LocalDateTime.now());  // Update sent date to now
                }
            });
    }

    public static List<Email> getInboxEmails(String username) {
        return emails.stream()
            .filter(e -> e.getToUser().equals(username) && 
                        e.getStatus() != Email.EmailStatus.DELETED)
            .sorted((e1, e2) -> e2.getSentDate().compareTo(e1.getSentDate()))
            .collect(Collectors.toList());
    }

    public static List<Email> getSentEmails(String username) {
        return emails.stream()
            .filter(e -> e.getFromUser().equals(username) && 
                        e.getStatus() == Email.EmailStatus.SENT)
            .sorted((e1, e2) -> e2.getSentDate().compareTo(e1.getSentDate()))
            .collect(Collectors.toList());
    }

    public static List<Email> getDrafts(String username) {
        return emails.stream()
            .filter(e -> e.getFromUser().equals(username) && 
                        e.getStatus() == Email.EmailStatus.DRAFT)
            .sorted((e1, e2) -> e2.getSentDate().compareTo(e1.getSentDate()))
            .collect(Collectors.toList());
    }

    public static void markAsRead(int emailId, String username) {
        emails.stream()
            .filter(e -> e.getId() == emailId && e.getToUser().equals(username))
            .findFirst()
            .ifPresent(e -> e.setRead(true));
    }

    public static void deleteEmail(int emailId, String username) {
        emails.stream()
            .filter(e -> e.getId() == emailId && 
                        (e.getFromUser().equals(username) || e.getToUser().equals(username)))
            .findFirst()
            .ifPresent(e -> e.setStatus(Email.EmailStatus.DELETED));
    }

    // Question Management
    public static int addQuestion(String title, String content, String author) {
        Question question = new Question(
            nextQuestionId++,
            title,
            content,
            author,
            LocalDateTime.now(),
            "OPEN"
        );
        questions.add(question);
        return question.getId();
    }

    public static List<Question> getQuestions() {
        return new ArrayList<>(questions);
    }

    public static Optional<Question> getQuestion(int questionId) {
        return questions.stream()
            .filter(q -> q.getId() == questionId)
            .findFirst();
    }

    public static void updateQuestion(int questionId, String title, String content) {
        questions.stream()
            .filter(q -> q.getId() == questionId)
            .findFirst()
            .ifPresent(q -> {
                q.setTitle(title);
                q.setContent(content);
            });
    }

    public static void deleteQuestion(int questionId) {
        questions.removeIf(q -> q.getId() == questionId);
        // Also remove all associated answers
        answers.removeIf(a -> a.getQuestionId() == questionId);
    }

    public static List<Question> searchQuestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getQuestions();
        }

        String searchQuery = query.toLowerCase().trim();
        return questions.stream()
            .filter(q -> q.getTitle().toLowerCase().contains(searchQuery) || 
                        q.getContent().toLowerCase().contains(searchQuery) ||
                        q.getAuthor().toLowerCase().contains(searchQuery))
            .collect(Collectors.toList());
    }

    // Answer Management
    public static int addAnswer(int questionId, String content, String author) {
        Answer answer = new Answer(
            nextAnswerId++,
            questionId,
            content,
            author,
            LocalDateTime.now(),
            false
        );
        answers.add(answer);

        // Update question status when first answer is added
        updateQuestionStatus(questionId);

        return answer.getId();
    }

    public static List<Answer> getAnswersForQuestion(int questionId) {
        return answers.stream()
            .filter(a -> a.getQuestionId() == questionId)
            .sorted((a1, a2) -> {
                // Show accepted answer first, then sort by date
                if (a1.isAccepted() && !a2.isAccepted()) return -1;
                if (!a1.isAccepted() && a2.isAccepted()) return 1;
                return a2.getCreatedAt().compareTo(a1.getCreatedAt());
            })
            .collect(Collectors.toList());
    }

    public static void updateAnswer(int answerId, String content) {
        answers.stream()
            .filter(a -> a.getId() == answerId)
            .findFirst()
            .ifPresent(a -> a.setContent(content));
    }

    public static void deleteAnswer(int answerId) {
        // First find the associated question
        Optional<Answer> answer = answers.stream()
            .filter(a -> a.getId() == answerId)
            .findFirst();

        if (answer.isPresent()) {
            int questionId = answer.get().getQuestionId();
            boolean wasAccepted = answer.get().isAccepted();

            // Remove the answer
            answers.removeIf(a -> a.getId() == answerId);

            // If this was the accepted answer or the last answer, update question status
            List<Answer> remainingAnswers = getAnswersForQuestion(questionId);
            if (remainingAnswers.isEmpty() || wasAccepted) {
                questions.stream()
                    .filter(q -> q.getId() == questionId)
                    .findFirst()
                    .ifPresent(q -> q.setStatus("OPEN"));
            }
        }
    }

    public static void acceptAnswer(int answerId, int questionId) {
        // First, unaccept any previously accepted answers
        answers.stream()
            .filter(a -> a.getQuestionId() == questionId)
            .forEach(a -> a.setAccepted(false));

        // Then accept the new answer
        answers.stream()
            .filter(a -> a.getId() == answerId)
            .findFirst()
            .ifPresent(a -> {
                a.setAccepted(true);
                // Update question status
                questions.stream()
                    .filter(q -> q.getId() == questionId)
                    .findFirst()
                    .ifPresent(q -> q.setStatus("ANSWERED"));
            });
    }

    private static void updateQuestionStatus(int questionId) {
        List<Answer> questionAnswers = getAnswersForQuestion(questionId);
        questions.stream()
            .filter(q -> q.getId() == questionId)
            .findFirst()
            .ifPresent(q -> {
                if (questionAnswers.isEmpty()) {
                    q.setStatus("OPEN");
                } else if (questionAnswers.stream().anyMatch(Answer::isAccepted)) {
                    q.setStatus("ANSWERED");
                } else {
                    q.setStatus("IN_PROGRESS");
                }
            });
    }
}