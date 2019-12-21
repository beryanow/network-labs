import java.util.List;

public class Chat {
    private List<ChatMessage> chat;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ChatMessage c : chat) {
            result.append(c).append("\n");
        }
        result.deleteCharAt(result.lastIndexOf("\n"));
        return result.toString();
    }

    int size() {
        return chat.size();
    }

    Chat(List<ChatMessage> chat) {
        this.chat = chat;
    }
}
