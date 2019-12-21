import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        try {
            Special.setHost(args[0], args[1]);
        }
        catch (Exception e) {
            Special.printInstructions();
            return;
        }
        Client client = new Client();
        Scanner in = new Scanner(System.in);
        do {
            System.out.println("Enter your login:");
            String login = in.nextLine();

            client.setUser(client.getConnectManager().login(login));
            if (client.getUser() == null) {
                System.out.println("You are not able to use that login!");
            }
        } while (client.getUser() == null);

        client.startTimer();
        String message = "";

        do {
            message = in.nextLine();
            String usersCheck = "";
            String id;
            try {
                usersCheck = message.substring(0, 6);
            } catch (Exception ignore) {}

            if (usersCheck.equals("/users") && (message.length() > "/users".length())) {
                id = message.substring(7);
                List<OtherUser> allUsers = client.getConnectManager().listUsers(client.getUser());
                String finalId = id;
                allUsers.forEach((t) -> {
                    if (t.getId() == Integer.parseInt(finalId)) {
                        System.out.println(t.toString());
                    }
                });
                continue;
            } else {
                if (message.equals("/users")) {
                    client.getConnectManager().listUsers(client.getUser()).forEach((t) -> System.out.println(t.toString()));
                }
            }

            if (!message.equals("/exit") && !message.equals("/users")) {
                String messageCheck = "";
                try {
                    messageCheck = message.substring(0, 9);
                } catch (Exception ignore) {}

                if (messageCheck.equals("/messages")) {
                    Pattern pat = Pattern.compile("[-]?[0-9]+(.[0-9]+)?");
                    Matcher matcher = pat.matcher(message.substring(10));

                    matcher.find();
                    int offset = Integer.parseInt(matcher.group());
                    matcher.find();
                    int count = Integer.parseInt(matcher.group());

                    List<ChatMessage> chatMessages = client.getConnectManager().getMessages(client.getUser(), offset, count);
                    chatMessages.forEach((t) -> {
                        System.out.println(t.toString());
                    });
                } else {
                    client.getConnectManager().sendMessage(client.getUser(), message);
                }
            }
        } while (!message.equals("/exit"));

        client.stopTimer();
        client.getConnectManager().logout(client.getUser());
    }
}
