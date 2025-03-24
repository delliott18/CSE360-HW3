import java.time.LocalDateTime;

public class Answer {
    private int id;
    private int questionId;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private boolean isAccepted;

    public Answer(int id, int questionId, String content, String author, 
                 LocalDateTime createdAt, boolean isAccepted) {
        this.id = id;
        this.questionId = questionId;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.isAccepted = isAccepted;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAccepted() { return isAccepted; }
    public void setAccepted(boolean accepted) { isAccepted = accepted; }
}