public class User {
    private int id;
    private String username;
    private Boolean online;
    private String token;

    public User(int id, String username, Boolean online, String token) {
        this.id = id;
        this.username = username;
        this.online = online;
        this.token = token;
    }

    String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username='" + username + '\'' + ", online=" + online + ", token='" + token + '\'' + '}';
    }
}
