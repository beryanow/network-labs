import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class Client {
    private ConnectManager connectManager = new ConnectManager(Special.host);
    private User user;
    private int offset = 0;
    private Timer timer = new Timer();

    ConnectManager getConnectManager() {
        return connectManager;
    }

    User getUser() {
        return user;
    }

    void setUser(User user) {
        this.user = user;
    }

    void startTimer() {
        timer.scheduleAtFixedRate(new MessagesUpdater(), 1000, 1000);
    }

    void stopTimer() {
        timer.cancel();
    }

    private class MessagesUpdater extends TimerTask {
        @Override
        public void run() {
            List<ChatMessage> chatMessages = connectManager.getMessages(user, offset, 100);
            if (chatMessages != null) {
                Chat chat = new Chat(chatMessages);
                if (chat.size() != 0) {
                    offset += chat.size();
                    System.out.println(chat);
                }
            }
        }
    }
}
