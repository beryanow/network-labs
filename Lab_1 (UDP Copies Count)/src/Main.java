import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Enter the multicast IP as an argument!");
            return;
        }
        MulticastPublisher publisher = new MulticastPublisher();
        MulticastReceiver receiver = new MulticastReceiver();
        UserList list = new UserList();

        try (MulticastStructure structure = new MulticastStructure(args[0])) {
            structure.setRecentSendTimeLong(System.currentTimeMillis() - 2000);
            while (true) {
                if ((System.currentTimeMillis() - structure.getRecentSendTimeLong()) > 2000) {
                    publisher.send("Greetings, traveler!", structure.getPort(), structure.getGroup(), structure.getSocket());
                    structure.setRecentSendTimeLong(System.currentTimeMillis());
                }
                ArrayList<String> message = receiver.receive(structure.getSocket());

                structure.setRecentReceiveTime();

                if (message == null) continue;
                if (message.get(1).equals("Greetings, traveler!")) {
                    String ip = message.get(0);
                    if (list.getUsers().containsKey(ip)) {
                        list.getUsers().replace(ip, structure.getRecentReceiveTime());
                        list.printUserList();
                    } else {
                        list.getUsers().put(ip, structure.getRecentReceiveTime());
                        list.printNewUserPhrase(ip);
                        list.printUserList();
                    }

                    if (list.getUserTime().containsKey(ip)) {
                        list.getUserTime().replace(ip, System.currentTimeMillis());
                    } else {
                        list.getUserTime().put(ip, System.currentTimeMillis());
                    }
                }

                ArrayList<String> terminated = new ArrayList<>();
                for (String key : list.getUserTime().keySet()) {
                    if ((System.currentTimeMillis() - list.getUserTime().get(key)) > 5000) {
                        list.printUserTerminatedPhrase(key);
                        terminated.add(key);
                    }
                }

                for (int i = 0; i < terminated.size(); i++) {
                    list.getUserTime().remove(terminated.get(i));
                    list.getUsers().remove(terminated.get(i));
                }

                terminated.clear();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
