
public class MasterPlayer {
    private String name;    // Имя игрока (для отображения в интерфейсе)
    private String ip;      // IPv4 или IPv6 адрес игрока в виде строки
    private int port;       // Порт UDP-сокета игрока

    MasterPlayer(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    String getName() {
        return name;
    }

    String getIp() {
        return ip;
    }

    int getPort() {
        return port;
    }
}
