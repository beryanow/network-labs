import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, NoRoomForSnakeException {

        /* node info */
        String playerName;
        String nodeIP;
        int nodePort;
        String parentIP;
        int parentPort;
        Node node;
        MulticastStructure multicastStructure;
        MulticastReceiver multicastReceiver;

        /* game info */
        int width;
        int height;
        int foodStaticAmount;
        double foodPlayerAmount;
        int delay;
        double deadFoodProbability;

        switch (args.length) {
            case 6:
                playerName = args[0];
                nodeIP = args[1];
                nodePort = Integer.parseInt(args[2]);
                parentIP = args[3];
                parentPort = Integer.parseInt(args[4]);
                multicastStructure = new MulticastStructure(args[5]);
                multicastReceiver = new MulticastReceiver();
                multicastReceiver.setSocket(multicastStructure.getSocket());

                node = new Node(nodeIP, nodePort, parentIP, parentPort);
                GamePlayer newPlayer = new GamePlayer(playerName, 0, nodeIP, nodePort);
                Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
                String newPlayerJson = gson.toJson(newPlayer);
                //System.out.println(newPlayerJson);

                Thread commandInputPlayerThread = new Thread(new CommandInputPlayer(node.getSendTool(), node, newPlayerJson, multicastStructure, node.getReceiveTool()));
                commandInputPlayerThread.start();
                break;
            case 10:
                playerName = args[0];
                nodeIP = args[1];
                nodePort = Integer.parseInt(args[2]);
                width = Integer.parseInt(args[3]);
                height = Integer.parseInt(args[4]);
                foodStaticAmount = Integer.parseInt(args[5]);
                foodPlayerAmount = Double.parseDouble(args[6]);
                delay = Integer.parseInt(args[7]);
                deadFoodProbability = Double.parseDouble(args[8]);
                multicastStructure = new MulticastStructure(args[9]);
                node = new Node(nodeIP, nodePort);

                GameConfiguration gameConfiguration = new GameConfiguration(width, height, foodStaticAmount, foodPlayerAmount, delay, deadFoodProbability);

                GamePlayer masterPlayer = new GamePlayer(playerName, GamePlayer.uniqueId, nodeIP, nodePort);
                GamePlayer.uniqueId++;

                MasterPlayer masterPlayerOverall = new MasterPlayer(masterPlayer.getName(), masterPlayer.getIp(), masterPlayer.getPort());

                Snake masterSnake = new Snake(masterPlayer.getId(), gameConfiguration.getWidth(), gameConfiguration.getHeight());

                GameState gameState = new GameState();
                gameState.setMasterId(masterPlayer.getId());

                gameState.getPlayers().add(masterPlayer);
                gameState.getSnakes().add(masterSnake);

                node.getReceiveTool().setGameState(gameState);
                node.getReceiveTool().setGameConfiguration(gameConfiguration);

                Thread commandInputThread = new Thread(new CommandInput(gameState, node.getReceiveTool(), masterPlayerOverall, gameConfiguration));
                commandInputThread.start();

                gameState.showInfo(gameConfiguration.getWidth(), gameConfiguration.getHeight());
                FieldUpdater fieldUpdater = new FieldUpdater(gameState, gameConfiguration, multicastStructure, node.getSendTool(), node.getReceiveTool().getNodesSnakes(), node.getReceiveTool());

//                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                String json = gson.toJson(gameState);
//                CommandInput st = gson.fromJson(json, CommandInput.class);
//                System.out.println(json.length());
                break;
        }

    }
}
