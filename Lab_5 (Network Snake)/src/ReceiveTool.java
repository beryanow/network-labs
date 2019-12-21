import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class ReceiveTool implements Runnable {
    private InetSocketAddress nodeReceiveAddress;
    private final ArrayList<InetSocketAddress> nodes;
    private HashMap<InetSocketAddress, Snake> nodesSnakes;
    private HashMap<GamePlayer, InetSocketAddress> nodesGamePlayer;
    private DatagramSocket socket;
    private GameState gameState;
    private GameConfiguration gameConfiguration;
    private SendTool sendTool;
    private ArrayList<GamePlayer> tempPlayers;
    private boolean noAckJoin = true;
    private ArrayList<ResendMessage> resendMessages;
    private Integer currentPlayerId;

    ReceiveTool(InetSocketAddress address, ArrayList<InetSocketAddress> nodes) throws SocketException {
        nodeReceiveAddress = address;
        this.nodes = nodes;
        socket = new DatagramSocket(nodeReceiveAddress);
        nodesSnakes = new HashMap<>();
        nodesGamePlayer = new HashMap<>();
        resendMessages = new ArrayList<>();
        currentPlayerId = null;
    }

    ArrayList<ResendMessage> getResendMessages() {
        return resendMessages;
    }

    boolean getNoAckJoin() {
        return noAckJoin;
    }

    void setNoAckJoinTrue() {
        noAckJoin = true;
    }

    ArrayList<InetSocketAddress> getNodes() {
        return nodes;
    }

    HashMap<GamePlayer, InetSocketAddress> getNodesGamePlayer() {
        return nodesGamePlayer;
    }

    void setSendTool(SendTool sendTool) {
        this.sendTool = sendTool;
    }

    ArrayList<GamePlayer> getTempPlayers() {
        return tempPlayers;
    }

    HashMap<InetSocketAddress, Snake> getNodesSnakes() {
        return nodesSnakes;
    }

    void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    DatagramSocket getSocket() {
        return socket;
    }

    GameState getGameState() {
        return gameState;
    }

    void setGameConfiguration(GameConfiguration gameConfiguration) {
        this.gameConfiguration = gameConfiguration;
    }

    @Override
    public void run() {
        byte[] buf = new byte[32768];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        ArrayList<Snake> tempSnakes = null;
        ArrayList<Coordinate> tempFoods = null;
        FieldShower fieldShower = new FieldShower();
        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

            String data = new String(buf, 0, packet.getLength(), StandardCharsets.UTF_8);
            String[] messageParts = data.split("-", 2);
            //System.out.println(data);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            GameMessage gameMessage = gson.fromJson(messageParts[0], GameMessage.class);

            if (gameMessage.getType() == Type.JOIN_PLAY) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                GamePlayer gamePlayer = gson.fromJson(messageParts[1], GamePlayer.class);

                gamePlayer.setId(GamePlayer.uniqueId);
                GamePlayer.uniqueId++;

                Snake playerSnake = null;
                InetSocketAddress senderAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
                synchronized (nodes) {
                    nodes.add(senderAddress);
                }
                try {
                    sendTool.sendRecentMessage(String.valueOf(gamePlayer.getId()), Type.ACK_JOIN_PLAY);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    playerSnake = new Snake(gamePlayer.getId(), gameConfiguration.getWidth(), gameConfiguration.getHeight(), gameState.getSnakes());
                    synchronized (gameState.getSnakes()) {
                        gameState.getSnakes().add(playerSnake);
                    }
                    synchronized (gameState.getPlayers()) {
                        gameState.getPlayers().add(gamePlayer);
                    }
                    nodesSnakes.put(senderAddress, playerSnake);
                    nodesGamePlayer.put(gamePlayer, senderAddress);
                } catch (NoRoomForSnakeException e1) {
                    try {
                        sendTool.sendRecentMessage("", Type.JOIN_FAIL);
                        synchronized (nodes) {
                            nodes.remove(senderAddress);
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }

                gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
                String gameConfig = gson.toJson(gameConfiguration);

                try {
                    sendTool.sendRecentMessage(gameConfig, Type.CONFIG);
                    resendMessages.add(new ResendMessage(gameConfig, Type.CONFIG, senderAddress));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (gameMessage.getType() == Type.MOVE) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                GameMessage moveMessage = gson.fromJson(messageParts[1], GameMessage.class);
                Type moveType = moveMessage.getType();
                InetSocketAddress senderAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());

                Snake moveSnake;
                synchronized (gameState.getSnakes()) {
                    moveSnake = nodesSnakes.get(senderAddress);
                }

                ArrayList<Coordinate> moveSnakePoints;
                switch (moveType) {
                    case STEER_UP:
                        synchronized (gameState.getSnakes()) {
                            moveSnakePoints = moveSnake.getPoints();
                            moveSnakePoints.get(0).setNewType(Type.STEER_UP);
                        }
                        break;
                    case STEER_LEFT:
                        synchronized (gameState.getSnakes()) {
                            moveSnakePoints = moveSnake.getPoints();
                            moveSnakePoints.get(0).setNewType(Type.STEER_LEFT);
                        }
                        break;
                    case STEER_RIGHT:
                        synchronized (gameState.getSnakes()) {
                            moveSnakePoints = moveSnake.getPoints();
                            moveSnakePoints.get(0).setNewType(Type.STEER_RIGHT);
                        }
                        break;
                    case STEER_DOWN:
                        synchronized (gameState.getSnakes()) {
                            moveSnakePoints = moveSnake.getPoints();
                            moveSnakePoints.get(0).setNewType(Type.STEER_DOWN);
                        }
                        break;
                    default:
                        break;
                }
            } else if (gameMessage.getType() == Type.SNAKES) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                tempSnakes = gson.fromJson(messageParts[1], new TypeToken<ArrayList<Snake>>() {}.getType());
            } else if (gameMessage.getType() == Type.FOODS) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                tempFoods = gson.fromJson(messageParts[1], new TypeToken<ArrayList<Coordinate>>() {}.getType());
                try {
                    fieldShower.showInfo(gameConfiguration.getWidth(), gameConfiguration.getHeight(), tempSnakes, tempFoods, currentPlayerId);
                } catch (Exception e) {
                    System.out.println("You are still in the game!");
                }
            } else if (gameMessage.getType() == Type.CONFIG) {
                sendTool.setSendPingTrue();
                gson = new GsonBuilder().setPrettyPrinting().create();
                gameConfiguration = gson.fromJson(messageParts[1], GameConfiguration.class);
            } else if (gameMessage.getType() == Type.PLAYERS) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                tempPlayers = gson.fromJson(messageParts[1], new TypeToken<ArrayList<GamePlayer>>() {}.getType());
            } else if (gameMessage.getType() == Type.JOIN_WATCH) {
                gameConfiguration.setMulticastNeededTrue();
            } else if (gameMessage.getType() == Type.JOIN_FAIL) {
                System.out.println("Failed to join the game because there is no room for your snake!");
            } else if (gameMessage.getType() == Type.QUIT) {
                InetSocketAddress senderAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
                synchronized (nodes) {
                    nodes.remove(senderAddress);
                }
                gameState.getSnakes().remove(nodesSnakes.get(senderAddress));

                for (int i = 0; i < resendMessages.size(); i++) {
                    if (resendMessages.get(i).getSocket().equals(senderAddress)) {
                        //System.out.println("REMOVED " + i + " " + resendMessages.get(i).getType());
                        resendMessages.remove(i);
                        break;
                    }
                }

                synchronized (gameState.getPlayers()) {
                    for (int i = 0; i < gameState.getPlayers().size(); i++) {
                        try {
                            if (gameState.getPlayers().get(i).getId() == nodesSnakes.get(senderAddress).getPlayerId()) {
                                gameState.getPlayers().remove(i);
                                break;
                            }
                        } catch (Exception ignore) {}
                    }
                }
                nodesSnakes.remove(senderAddress);
            } else if (gameMessage.getType() == Type.OVER) {
                sendTool.setSendPingFalse();
                System.out.println("THE GAME IS OVER!");
            } else if (gameMessage.getType() == Type.PING) {
                //System.out.println("The player is alive!");
            } else if (gameMessage.getType() == Type.ACK_JOIN_PLAY) {
                currentPlayerId = Integer.parseInt(messageParts[1]);
                noAckJoin = false;
            }
        }
    }
}
