import java.util.ArrayList;

public class FieldShower {
    void showInfo(int width, int height, ArrayList<Snake> snakes, ArrayList<Coordinate> foods, Integer currentPlayerId) {
        System.out.print("\033[H\033[2J");
        char[][] table = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                table[i][j] = '.';
            }
        }

        for (Coordinate food: foods) {
            table[food.getX()][food.getY()] = 'S';
        }

        if (snakes != null) {
            for (Snake snake : snakes) {
                if (snake.getPoints() != null) {
                    for (Coordinate point : snake.getPoints()) {
                        if (currentPlayerId != null) {
                            if (snake.getPlayerId() == currentPlayerId) {
                                table[point.getX()][point.getY()] = 'Ø';
                            }
                            else {
                                table[point.getX()][point.getY()] = 'O';
                            }
                        } else {
                            table[point.getX()][point.getY()] = 'O';
                        }
                    }
                }
            }
        }

        System.out.println("\nCURRENT GAME FIELD:");
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                System.out.print(table[i][j] + " ");
            }
            System.out.println();
        }
    }

    void showPlayers(ArrayList<GamePlayer> players) {
        System.out.println("\nCURRENT PLAYERS:");
        if (players.size() == 0) {
            System.out.println("empty");
        }
        for (int i = 0; i < players.size(); i++) {
            System.out.println(players.get(i).getName() + " -> ip: " + players.get(i).getIp() + " -> port: " + players.get(i).getPort());
        }
        System.out.println();
    }
}
