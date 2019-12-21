import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class FieldUpdater {
    private GameState gameState;
    private GameConfiguration gameConfiguration;
    private MulticastStructure multicastStructure;
    private MulticastPublisher multicastPublisher;
    private SendTool sendTool;
    private ReceiveTool receiveTool;
    private HashMap<InetSocketAddress, Snake> nodesSnakes;
    private ArrayList<Integer> playersToRemove;

    FieldUpdater(GameState gameState, GameConfiguration gameConfiguration, MulticastStructure multicastStructure, SendTool sendTool, HashMap<InetSocketAddress, Snake> nodesSnakes, ReceiveTool receiveTool) {
        this.gameConfiguration = gameConfiguration;
        this.gameState = gameState;
        this.multicastStructure = multicastStructure;
        multicastPublisher = new MulticastPublisher();
        this.sendTool = sendTool;
        this.nodesSnakes = nodesSnakes;
        this.receiveTool = receiveTool;
        playersToRemove = new ArrayList<>();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new DelayChecker(), gameConfiguration.getDelay(), gameConfiguration.getDelay());
    }

    boolean isFree(int x, int y) {
        synchronized (gameState.getSnakes()) {
            for (Snake snake : gameState.getSnakes()) {
                for (Coordinate point : snake.getPoints()) {
                    if (point.getX() == x && point.getY() == y) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private class DelayChecker extends TimerTask {
        private void updateField() throws IOException {
            synchronized (gameState.getSnakes()) {
                for (Snake snake : gameState.getSnakes()) {
                    int headX = snake.getPoints().get(0).getX();
                    int headY = snake.getPoints().get(0).getY();

                    Coordinate point = null;
                    Coordinate neighbourPoint = null;

                    if (snake.getMore() > 0) {
                        point = snake.getPoints().get(0);
                        if (point.getNewType() != Type.STEER_UNKNOWN) {
                            switch (point.getNewType()) {
                                case STEER_RIGHT:
                                        if (point.getY() + 1 == gameConfiguration.getHeight()) {
                                            playersToRemove.add(snake.getPlayerId());
                                        } else {
                                            if (isFree(point.getX(), point.getY() + 1)) {
                                                point.setY(point.getY() + 1);
                                            } else {
                                                playersToRemove.add(snake.getPlayerId());
                                            }
                                        }
                                    break;
                                case STEER_UP:
                                    if (point.getX() - 1 == -1) {
                                        playersToRemove.add(snake.getPlayerId());
                                    } else {
                                        if (isFree(point.getX() - 1, point.getY())) {
                                            point.setX(point.getX() - 1);
                                        } else {
                                            playersToRemove.add(snake.getPlayerId());
                                        }
                                    }
                                    break;
                                case STEER_DOWN:
                                    if (point.getX() + 1 == gameConfiguration.getWidth()) {
                                        playersToRemove.add(snake.getPlayerId());
                                    } else {
                                        if (isFree(point.getX() + 1, point.getY())) {
                                            point.setX(point.getX() + 1);
                                        } else {
                                            playersToRemove.add(snake.getPlayerId());
                                        }
                                    }
                                    break;
                                case STEER_LEFT:
                                    if (point.getY() - 1 == -1) {
                                        playersToRemove.add(snake.getPlayerId());
                                    } else {
                                        if (isFree(point.getX(), point.getY() - 1)) {
                                            point.setY(point.getY() - 1);
                                        } else {
                                            playersToRemove.add(snake.getPlayerId());
                                        }
                                    }
                                    break;
                            }
                            snake.getPoints().add(1, new Coordinate(headX, headY, Type.STEER_UNKNOWN, point.getOldType()));
                            snake.decMore();
                            point.setOldType(point.getNewType());
                            point.setNewType(Type.STEER_UNKNOWN);
                        } else {
                            switch (point.getOldType()) {
                                case STEER_RIGHT:
                                    if (point.getY() + 1 == gameConfiguration.getHeight()) {
                                        playersToRemove.add(snake.getPlayerId());
                                    } else {
                                        if (isFree(point.getX(), point.getY() + 1)) {
                                            point.setY(point.getY() + 1);
                                        } else {
                                            playersToRemove.add(snake.getPlayerId());
                                        }
                                    }
                                    break;
                                case STEER_UP:
                                    if (point.getX() - 1 == -1) {
                                        playersToRemove.add(snake.getPlayerId());
                                    } else {
                                        if (isFree(point.getX() - 1, point.getY())) {
                                            point.setX(point.getX() - 1);
                                        } else {
                                            playersToRemove.add(snake.getPlayerId());
                                        }
                                    }
                                    break;
                                case STEER_DOWN:
                                    if (point.getX() + 1 == gameConfiguration.getWidth()) {
                                        playersToRemove.add(snake.getPlayerId());
                                    } else {
                                        if (isFree(point.getX() + 1, point.getY())) {
                                            point.setX(point.getX() + 1);
                                        } else {
                                            playersToRemove.add(snake.getPlayerId());
                                        }
                                    }
                                    break;
                                case STEER_LEFT:
                                    if (point.getY() - 1 == -1) {
                                        playersToRemove.add(snake.getPlayerId());
                                    } else {
                                        if (isFree(point.getX(), point.getY() - 1)) {
                                            point.setY(point.getY() - 1);
                                        } else {
                                            playersToRemove.add(snake.getPlayerId());
                                        }
                                    }
                                    break;
                            }
                            snake.getPoints().add(1, new Coordinate(headX, headY, Type.STEER_UNKNOWN, point.getOldType()));
                            snake.decMore();
                        }
                    } else {
                        int snakeSize = snake.getPoints().size();
                        for (int i = 0; i < snakeSize; i++) {
                            point = snake.getPoints().get(i);

                            if (point.getNewType() != Type.STEER_UNKNOWN) {
                                if (i != snakeSize - 1) {
                                    neighbourPoint = snake.getPoints().get(i + 1);
                                }

                                switch (point.getNewType()) {
                                    case STEER_RIGHT:
                                        if (i != snakeSize - 1) {
                                            if (neighbourPoint.getOldType() == Type.STEER_LEFT || neighbourPoint.getOldType() == Type.STEER_RIGHT) {
                                                neighbourPoint.setNewType(point.getOldType());
                                            }
                                        }
                                        if (i == 0) {
                                            if (point.getY() + 1 == gameConfiguration.getHeight()) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                if (isFree(point.getX(), point.getY() + 1)) {
                                                    point.setY(point.getY() + 1);
                                                } else {
                                                    playersToRemove.add(snake.getPlayerId());
                                                }
                                            }

                                        } else {
                                            if (point.getY() + 1 == gameConfiguration.getHeight()) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                point.setY(point.getY() + 1);
                                            }
                                        }
                                        break;
                                    case STEER_UP:
                                        if (i != snakeSize - 1) {
                                            if (neighbourPoint.getOldType() == Type.STEER_DOWN || neighbourPoint.getOldType() == Type.STEER_UP) {
                                                neighbourPoint.setNewType(point.getOldType());
                                            }
                                        }
                                        if (i == 0) {
                                            if (point.getX() - 1 == -1) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                if (isFree(point.getX() - 1, point.getY())) {
                                                    point.setX(point.getX() - 1);
                                                } else {
                                                    playersToRemove.add(snake.getPlayerId());
                                                }
                                            }
                                        } else {
                                            if (point.getX() - 1 == -1) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                point.setX(point.getX() - 1);
                                            }
                                        }
                                        break;
                                    case STEER_DOWN:
                                        if (i != snakeSize - 1) {
                                            if (neighbourPoint.getOldType() == Type.STEER_UP || neighbourPoint.getOldType() == Type.STEER_DOWN) {
                                                neighbourPoint.setNewType(point.getOldType());
                                            }
                                        }
                                        if (i == 0) {
                                            if (point.getX() + 1 == gameConfiguration.getWidth()) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                if (isFree(point.getX() + 1, point.getY())) {
                                                    point.setX(point.getX() + 1);
                                                } else {
                                                    playersToRemove.add(snake.getPlayerId());
                                                }
                                            }
                                        } else {
                                            if (point.getX() + 1 == gameConfiguration.getWidth()) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                point.setX(point.getX() + 1);
                                            }
                                        }
                                        break;
                                    case STEER_LEFT:
                                        if (i != snakeSize - 1) {
                                            if (neighbourPoint.getOldType() == Type.STEER_RIGHT || neighbourPoint.getOldType() == Type.STEER_LEFT) {
                                                neighbourPoint.setNewType(point.getOldType());
                                            }
                                        }
                                        if (i == 0) {
                                            if (point.getY() - 1 == -1) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                if (isFree(point.getX(), point.getY() - 1)) {
                                                    point.setY(point.getY() - 1);
                                                } else {
                                                    playersToRemove.add(snake.getPlayerId());
                                                }
                                            }
                                        } else {
                                            if (point.getY() - 1 == -1) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                point.setY(point.getY() - 1);
                                            }
                                        }
                                        break;
                                }
                                point.setOldType(point.getNewType());
                                point.setNewType(Type.STEER_UNKNOWN);
                            } else {
                                if (i != snakeSize - 1) {
                                    neighbourPoint = snake.getPoints().get(i + 1);
                                }
                                switch (point.getOldType()) {
                                    case STEER_RIGHT:
                                        if (i == 0) {
                                            if (point.getY() + 1 == gameConfiguration.getHeight()) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                if (isFree(point.getX(), point.getY() + 1)) {
                                                    point.setY(point.getY() + 1);
                                                } else {
                                                    playersToRemove.add(snake.getPlayerId());
                                                }
                                            }
                                        } else {
                                            if (point.getY() + 1 == gameConfiguration.getHeight()) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                point.setY(point.getY() + 1);
                                            }
                                        }
                                        break;
                                    case STEER_UP:
                                        if (i == 0) {
                                            if (point.getX() - 1 == -1) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                if (isFree(point.getX() - 1, point.getY())) {
                                                    point.setX(point.getX() - 1);
                                                } else {
                                                    playersToRemove.add(snake.getPlayerId());
                                                }
                                            }
                                        } else {
                                            if (point.getX() - 1 == -1) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                point.setX(point.getX() - 1);
                                            }
                                        }
                                        break;
                                    case STEER_DOWN:
                                        if (i == 0) {
                                            if (point.getX() + 1 == gameConfiguration.getWidth()) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                if (isFree(point.getX() + 1, point.getY())) {
                                                    point.setX(point.getX() + 1);
                                                } else {
                                                    playersToRemove.add(snake.getPlayerId());
                                                }
                                            }
                                        } else {
                                            if (point.getX() + 1 == gameConfiguration.getWidth()) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                point.setX(point.getX() + 1);
                                            }
                                        }
                                        break;
                                    case STEER_LEFT:
                                        if (i == 0) {
                                            if (point.getY() - 1 == -1) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                if (isFree(point.getX(), point.getY() - 1)) {
                                                    point.setY(point.getY() - 1);
                                                } else {
                                                    playersToRemove.add(snake.getPlayerId());
                                                }
                                            }
                                        } else {
                                            if (point.getY() - 1 == -1) {
                                                playersToRemove.add(snake.getPlayerId());
                                            } else {
                                                point.setY(point.getY() - 1);
                                            }
                                        }
                                        break;
                                }
                                if (i != snakeSize - 1) {
                                    if (point.getOldType() != neighbourPoint.getOldType()) {
                                        neighbourPoint.setNewType(point.getOldType());
                                    }
                                }
                            }
                        }
                    }

                    ArrayList<Coordinate> toRemove = new ArrayList<>();
                    synchronized (gameState.getFoods()) {
                        for (Coordinate food : gameState.getFoods()) {
                            if (snake.getPoints().get(0).getX() == food.getX() && snake.getPoints().get(0).getY() == food.getY()) {
                                snake.incMore();
                                toRemove.add(food);
                            }
                        }
                    }
                    for (Coordinate food : toRemove) {
                        synchronized (gameState.getFoods()) {
                            gameState.getFoods().remove(food);
                        }
                    }
                }

                ArrayList<ResendMessage> resendMessages = receiveTool.getResendMessages();

                for (Integer playerId : playersToRemove) {
                    GamePlayer playerToDelete = null;
                    for (GamePlayer tempGamePlayer : receiveTool.getNodesGamePlayer().keySet()) {
                        if (tempGamePlayer.getId() == playerId) {
                            playerToDelete = tempGamePlayer;
                            sendTool.sendDefiniteMessage("", Type.OVER, receiveTool.getNodesGamePlayer().get(tempGamePlayer));
                            for (int i = 0; i < resendMessages.size(); i++) {
                                if (resendMessages.get(i).getSocket() == receiveTool.getNodesGamePlayer().get(tempGamePlayer)) {
                                    resendMessages.remove(i);
                                    break;
                                }
                            }
                            receiveTool.getNodes().remove(receiveTool.getNodesGamePlayer().get(tempGamePlayer));
                            break;
                        }
                    }
                    if (playerToDelete != null) {
                        receiveTool.getNodesGamePlayer().remove(playerToDelete);
                    }

                    for (int i = 0; i < gameState.getSnakes().size(); i++) {
                        Snake currentSnake = gameState.getSnakes().get(i);
                        if (currentSnake.getPlayerId() == playerId) {
                            gameState.getSnakes().remove(currentSnake);
                            break;
                        }
                    }

                    for (int i = 0; i < gameState.getPlayers().size(); i++) {
                        GamePlayer currentPlayer = gameState.getPlayers().get(i);
                        if (currentPlayer.getId() == playerId) {
                            if (playerId == gameState.getMasterId()) {
                                System.out.println("THE GAME IS OVER!");
                            }
                            gameState.getPlayers().remove(currentPlayer);
                            break;
                        }
                    }
                }

                for (int i = 0; i < resendMessages.size(); i++) {
                    sendTool.sendDefiniteMessage(resendMessages.get(i).getMessage(), resendMessages.get(i).getType(), resendMessages.get(i).getSocket());
                }

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String snakesJson = gson.toJson(gameState.getSnakes());
                String foodsJson = gson.toJson(gameState.getFoods());

                //System.out.println(snakesJson);
                //System.out.println("-----");
                //System.out.println(foodsJson);

                gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
                String gameConfig;
                gameConfig = gson.toJson(gameConfiguration);
                //System.out.println(gameConfig);

                gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
                String gamePlayersJson;
                synchronized (gameState.getPlayers()) {
                    gamePlayersJson = gson.toJson(gameState.getPlayers());
                }

                if (gameConfiguration.getMulticastNeeded()) {
                    multicastPublisher.send(gameConfig, Type.CONFIG, multicastStructure.getPort(), multicastStructure.getGroup(), multicastStructure.getSocket());
                    multicastPublisher.send(snakesJson, Type.SNAKES, multicastStructure.getPort(), multicastStructure.getGroup(), multicastStructure.getSocket());
                    multicastPublisher.send(foodsJson, Type.FOODS, multicastStructure.getPort(), multicastStructure.getGroup(), multicastStructure.getSocket());
                    multicastPublisher.send(gamePlayersJson, Type.PLAYERS, multicastStructure.getPort(), multicastStructure.getGroup(), multicastStructure.getSocket());
                }
                sendTool.sendEverybodyMessage(snakesJson, Type.SNAKES);
                sendTool.sendEverybodyMessage(foodsJson, Type.FOODS);
                sendTool.sendEverybodyMessage(gamePlayersJson, Type.PLAYERS);
            }
        }

        private void makeFood() {
            int foodX = (int) (Math.random() * gameConfiguration.getWidth());
            int foodY = (int) (Math.random() * gameConfiguration.getHeight());

            for (Snake snake : gameState.getSnakes()) {
                for (Coordinate point : snake.getPoints()) {
                    if ((point.getX() != foodX) || (point.getY() != foodY)) {
                        if (Math.random() <= gameConfiguration.getDeadFoodProbability()) {
                            if (gameState.getFoods().size() < gameConfiguration.getFoodStaticAmount()) {
                                gameState.getFoods().add(new Coordinate(foodX, foodY, Type.STEER_UNKNOWN, Type.STEER_UNKNOWN));
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            makeFood();
            try {
                updateField();
            } catch (IOException e) {
                e.printStackTrace();
            }
            gameState.showInfo(gameConfiguration.getWidth(), gameConfiguration.getHeight());
        }
    }
}
