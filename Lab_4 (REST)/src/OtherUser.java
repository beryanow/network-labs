public class OtherUser {
    private int id;
    private String username;
    private boolean online;

    @Override
    public String toString() {
        return "id: " + id + "\nusername: " + username + "\nonline: " + isOnline() + "\n";
    }

    int getId() {
        return id;
    }

    private boolean isOnline() {
        return online;
    }
}
