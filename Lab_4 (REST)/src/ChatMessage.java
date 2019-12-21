public class ChatMessage {
    private int id;
    private String message;
    private String author;

    public ChatMessage(int id, String message, String author) {
        this.id = id;
        this.message = message;
        this.author = author;
    }

    @Override
    public String toString() {
        return author + ": " + message;
    }
}
