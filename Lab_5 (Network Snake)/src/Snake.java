import java.util.ArrayList;
import java.util.LinkedList;

public class Snake {
    private int playerId; // Идентификатор игрока-владельца змеи, см. GamePlayer.id
    private ArrayList<Coordinate> points; /* Список "ключевых" точек змеи. Первая точка хранит координаты головы змеи.
     * Каждая следующая - смещение следующей "ключевой" точки относительно предыдущей.
     * Последняя точка соответствует хвосту змеи. */
    private int more = 0;

    Snake(int playerId, int width, int height, ArrayList<Snake> snakes) throws NoRoomForSnakeException {
        this.playerId = playerId;
        points = new ArrayList<>();
        int sum = 0;
        for (int i = 0; i < snakes.size(); i++) {
            for (int j = 0; j < snakes.get(i).getPoints().size(); j++) {
                sum++;
            }
        }

        if (sum > width * height) {
            throw new NoRoomForSnakeException();
        }
        Coordinate head = new Coordinate((int) (Math.random() * width), (int) (Math.random() * height), Type.STEER_UNKNOWN, Type.STEER_RIGHT);
        points.add(head);
    }

    Snake(int playerId, int width, int height) {
        this.playerId = playerId;
        points = new ArrayList<>();
        Coordinate head = new Coordinate((int) (Math.random() * width), (int) (Math.random() * height), Type.STEER_UNKNOWN, Type.STEER_RIGHT);
        points.add(head);
    }

    void incMore() {
        more++;
    }

    void decMore() {
        more--;
    }

    int getMore() {
        return more;
    }

    ArrayList<Coordinate> getPoints() {
        return points;
    }

    int getPlayerId() {
        return playerId;
    }
}