import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class CommandInputPlayer implements Runnable {
    private SendTool sendTool;
    private Node node;
    private String newPlayerJson;
    private MulticastStructure multicastStructure;
    private ReceiveTool receiveTool;

    CommandInputPlayer(SendTool sendTool, Node node, String newPlayerJson, MulticastStructure multicastStructure, ReceiveTool receiveTool) {
        this.sendTool = sendTool;
        this.node = node;
        this.newPlayerJson = newPlayerJson;
        this.multicastStructure = multicastStructure;
        this.receiveTool = receiveTool;
    }

    @Override
    public void run() {
        Scanner commands = new Scanner(System.in);
        MulticastReceiver multicastReceiver = null;
        FieldShower fieldShower = new FieldShower();

        PingUpdater pingUpdater = new PingUpdater(sendTool);
        Thread multicastReceiverThread;
        while (true) {
            String command = commands.next();

            Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

            GameMessage gameMessage;
            String newGameMoveJson;
            
            switch (command) {
                case "i":
                    System.out.println("WATCHING THE GAME...");
                    if (multicastReceiver == null) {
                        multicastReceiver = new MulticastReceiver();
                        multicastReceiver.setSocket(multicastStructure.getSocket());
                        multicastReceiverThread = new Thread(multicastReceiver);
                        multicastReceiverThread.start();
                    } else {
                        multicastReceiver.setShowFieldTrue();
                    }

                    try {
                        node.getSendTool().sendMessage(newPlayerJson, Type.JOIN_WATCH);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "p":
                    ArrayList<GamePlayer> tempPlayers = null;
                    if (multicastReceiver != null) {
                        tempPlayers = multicastReceiver.getTempPlayers();
                    }
                    if (tempPlayers != null) {
                        fieldShower.showPlayers(tempPlayers);
                    } else {
                        tempPlayers = receiveTool.getTempPlayers();
                        if (tempPlayers != null) {
                            fieldShower.showPlayers(tempPlayers);
                        }
                    }
                    break;
                case "c":
                    if (multicastReceiver != null) {
                        multicastReceiver.setShowFieldFalse();
                    }
                    while (receiveTool.getNoAckJoin()) {
                        System.out.println("TRYING TO CONNECT TO THE GAME...");
                        try {
                            node.getSendTool().sendMessage(newPlayerJson, Type.JOIN_PLAY);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    receiveTool.setNoAckJoinTrue();
                    break;
                case "w":
                    gameMessage = new GameMessage(Type.STEER_UP, GameMessage.getUniqueSeq());
                    GameMessage.incUniqueSeq();
                    newGameMoveJson = gson.toJson(gameMessage);
                    //System.out.println(gameMessage);
                    try {
                        sendTool.sendMessage(newGameMoveJson, Type.MOVE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "a":
                    gameMessage = new GameMessage(Type.STEER_LEFT, GameMessage.getUniqueSeq());
                    GameMessage.incUniqueSeq();
                    newGameMoveJson = gson.toJson(gameMessage);
                    //System.out.println(gameMessage);
                    try {
                        sendTool.sendMessage(newGameMoveJson, Type.MOVE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "d":
                    gameMessage = new GameMessage(Type.STEER_RIGHT, GameMessage.getUniqueSeq());
                    GameMessage.incUniqueSeq();
                    newGameMoveJson = gson.toJson(gameMessage);
                    //System.out.println(gameMessage);
                    try {
                        sendTool.sendMessage(newGameMoveJson, Type.MOVE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "s":
                    gameMessage = new GameMessage(Type.STEER_DOWN, GameMessage.getUniqueSeq());
                    GameMessage.incUniqueSeq();
                    newGameMoveJson = gson.toJson(gameMessage);
                    //System.out.println(gameMessage);
                    try {
                        sendTool.sendMessage(newGameMoveJson, Type.MOVE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "q":
                    sendTool.setSendPingFalse();
                    try {
                        sendTool.sendMessage("", Type.QUIT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
