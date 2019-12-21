import java.util.HashMap;

class UserList {
    private HashMap<String, String> users;
    private HashMap<String, Long> userTime;

    UserList() {
        users = new HashMap<>();
        userTime = new HashMap<>();
    }

    HashMap<String, String> getUsers() {
        return users;
    }

    HashMap<String, Long> getUserTime() {
        return userTime;
    }

    void printNewUserPhrase(String user) {
        System.out.println('\n' + ">>>>> New user -> " + user.substring(1) + '\n');
    }

    void printUserTerminatedPhrase(String user) {
        System.out.println('\n' + ">>>>> Disconnected user -> " + user.substring(1) + '\n');
    }

    void printUserList() {
        System.out.println("Current users:" + '\n' + "--------------");
        users.forEach((ip, time) -> System.out.println("IP -> " + ip.substring(1) + " <=> time -> " + time));
        System.out.println("-----–-----------------------------------------" + '\n');
    }
}
