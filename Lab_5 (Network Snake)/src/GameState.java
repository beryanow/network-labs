import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class GameState {
    private int stateOrder;                // Порядковый номер состояния, уникален в пределах игры, монотонно возрастает
    private final ArrayList<Snake> snakes; // Список змей
    private final ArrayList<Coordinate> foods;   // Список клеток с едой
    private final ArrayList<GamePlayer> players; // Актуальнейший список игроков
    private int masterId;                  // Идентификатор хозяина доски

    void setMasterId(int masterId) {
        this.masterId = masterId;
    }

    int getMasterId() {
        return masterId;
    }

    GameState() {
        this.stateOrder = 0;
        snakes = new ArrayList<>();
        foods = new ArrayList<>();
        players = new ArrayList<>();
    }

    void incStateOrder() {
        stateOrder++;
    }

    ArrayList<Snake> getSnakes() {
        return snakes;
    }

    ArrayList<GamePlayer> getPlayers() {
        return players;
    }

    ArrayList<Coordinate> getFoods() {
        return foods;
    }

    void showInfo(int width, int height) {
        System.out.print("\033[H\033[2J");
        char[][] table = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                table[i][j] = '.';
            }
        }
        synchronized (snakes) {
            for (Snake snake : snakes) {
                for (Coordinate point : snake.getPoints()) {
                    if (snake.getPlayerId() == masterId) {
                        table[point.getX()][point.getY()] = 'Ø';
                    } else {
                        table[point.getX()][point.getY()] = 'O';
                    }
                }
            }
        }

        synchronized (foods) {
            for (Coordinate food : foods) {
                table[food.getX()][food.getY()] = 'S';
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
}
