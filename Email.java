import java.time.LocalDateTime;

public class Email {
    private int id;
    private String fromUser;
    private String toUser;
    private String subject;
    private String content;
    private LocalDateTime sentDate;
    private EmailStatus status;
    private boolean isRead;

    public enum EmailStatus {
        SENT,
        DRAFT,
        DELETED
    }

    public Email() {
        this.sentDate = LocalDateTime.now();
        this.status = EmailStatus.DRAFT;
        this.isRead = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }

    public EmailStatus getStatus() { return status; }
    public void setStatus(EmailStatus status) { this.status = status; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isValid() {
        return fromUser != null && !fromUser.trim().isEmpty() &&
               toUser != null && !toUser.trim().isEmpty() &&
               subject != null && !subject.trim().isEmpty() &&
               content != null;
    }
}