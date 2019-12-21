import com.google.gson.annotations.Expose;

public class GamePlayer {
    @Expose
    private String name;    // Имя игрока (для отображения в интерфейсе)
    private int id;         // Уникальный идентификатор игрока в пределах игры
    @Expose
    private String ip;      // IPv4 или IPv6 адрес игрока в виде строки
    @Expose
    private int port;       // Порт UDP-сокета игрока
    public static int uniqueId = 0;

    GamePlayer(String name, int id, String ip, int port) {
        this.name = name;
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    GamePlayer() {}

    String getName() {
        return name;
    }

    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    String getIp() {
        return ip;
    }

    int getPort() {
        return port;
    }
}
