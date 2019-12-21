import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class CommandInput implements Runnable {
    private final GameState gameState;
    private final ReceiveTool receiveTool;
    private final MasterPlayer masterPlayer;
    private final GameConfiguration gameConfiguration;

    CommandInput(GameState gameState, ReceiveTool receiveTool, MasterPlayer masterPlayer, GameConfiguration gameConfiguration) {
        this.gameState = gameState;
        this.receiveTool = receiveTool;
        this.masterPlayer = masterPlayer;
        this.gameConfiguration = gameConfiguration;
    }

    @Override
    public void run() {
        Scanner commands = new Scanner(System.in);
        FieldShower fieldShower = new FieldShower();

        while (true) {
            String command = commands.next();

            Snake snake;
            ArrayList<Coordinate> snakePoints;

            switch (command) {
                case "p":
                    ArrayList<GamePlayer> tempPlayers;
                    synchronized (receiveTool.getGameState().getPlayers()) {
                        tempPlayers = receiveTool.getGameState().getPlayers();
                    }
                    if (tempPlayers != null) {
                        fieldShower.showPlayers(tempPlayers);
                    }
                    break;
                case "w":
                    synchronized (gameState.getSnakes()) {
                        snake = gameState.getSnakes().get(0);
                        snakePoints = snake.getPoints();
                        snakePoints.get(0).setNewType(Type.STEER_UP);
                    }
                    break;
                case "a":
                    synchronized (gameState.getSnakes()) {
                        snake = gameState.getSnakes().get(0);
                        snakePoints = snake.getPoints();
                        snakePoints.get(0).setNewType(Type.STEER_LEFT);
                    }
                    break;
                case "d":
                    synchronized (gameState.getSnakes()) {
                        snake = gameState.getSnakes().get(0);
                        snakePoints = snake.getPoints();
                        snakePoints.get(0).setNewType(Type.STEER_RIGHT);
                    }
                    break;
                case "s":
                    synchronized (gameState.getSnakes()) {
                        snake = gameState.getSnakes().get(0);
                        snakePoints = snake.getPoints();
                        snakePoints.get(0).setNewType(Type.STEER_DOWN);
                    }
                    break;
                case "c":
                    GamePlayer newMasterPlayer = new GamePlayer(masterPlayer.getName(), GamePlayer.uniqueId, masterPlayer.getIp(), masterPlayer.getPort());
                    GamePlayer.uniqueId++;

                    Snake newMasterSnake = new Snake(newMasterPlayer.getId(), gameConfiguration.getWidth(), gameConfiguration.getHeight());
                    synchronized (gameState) {
                        gameState.setMasterId(newMasterPlayer.getId());
                        gameState.getPlayers().add(newMasterPlayer);
                        gameState.getSnakes().add(newMasterSnake);
                    }
                    break;
                default:
                    break;
                case "q":
                    synchronized (gameState) {
                        ArrayList<Snake> gameSnakes = gameState.getSnakes();
                        for (int i = 0; i < gameSnakes.size(); i++) {
                            if (gameSnakes.get(i).getPlayerId() == gameState.getMasterId()) {
                                gameSnakes.remove(i);
                                break;
                            }
                        }
                        ArrayList<GamePlayer> gamePlayers = gameState.getPlayers();
                        for (int i = 0; i < gamePlayers.size(); i++) {
                            if (gamePlayers.get(i).getId() == gameState.getMasterId()) {
                                gamePlayers.remove(i);
                                break;
                            }
                        }
                    }
                    break;
            }
        }
    }
}
