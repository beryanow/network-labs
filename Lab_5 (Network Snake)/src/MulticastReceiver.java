import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MulticastReceiver implements Runnable {
    private MulticastSocket socket;
    private GameConfiguration gameConfiguration;
    private ArrayList<GamePlayer> tempPlayers;
    private boolean showField = true;

    boolean getShowField() {
        return showField;
    }

    void setShowFieldFalse() {
        showField = false;
    }

    void setShowFieldTrue() {
        showField = true;
    }

    void setSocket(MulticastSocket socket) {
        this.socket = socket;
    }

    ArrayList<GamePlayer> getTempPlayers() {
        return tempPlayers;
    }

    @Override
    public void run() {
        ArrayList<Snake> tempSnakes = null;

        ArrayList<Coordinate> tempFoods = null;
        FieldShower fieldShower = new FieldShower();

        while (true) {
            byte[] buf = new byte[32768];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String data = new String(buf, 0, packet.getLength(), StandardCharsets.UTF_8);
            String[] messageParts = data.split("-", 2);

            //System.out.println(data);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            GameMessage gameMessage = gson.fromJson(messageParts[0], GameMessage.class);

            if (gameMessage.getType() == Type.SNAKES) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                tempSnakes = gson.fromJson(messageParts[1], new TypeToken<ArrayList<Snake>>() {}.getType());
            } else if (gameMessage.getType() == Type.FOODS) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                tempFoods = gson.fromJson(messageParts[1], new TypeToken<ArrayList<Coordinate>>() {}.getType());
                if (showField) {
                    fieldShower.showInfo(gameConfiguration.getWidth(), gameConfiguration.getHeight(), tempSnakes, tempFoods, null);
                }
            } else if (gameMessage.getType() == Type.CONFIG) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                gameConfiguration = gson.fromJson(messageParts[1], GameConfiguration.class);
            } else if (gameMessage.getType() == Type.PLAYERS) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                tempPlayers = gson.fromJson(messageParts[1], new TypeToken<ArrayList<GamePlayer>>() {}.getType());
            }
        }
    }
}
