import com.google.gson.annotations.Expose;

enum Type {       /* Тип сообщения */
    PING,          // Ничего не меняем, просто говорим что мы всё ещё онлайн
    STEER_UNKNOWN, // Неопределённый поворот головы
    STEER_UP,      // Повернуть голову вверх
    STEER_DOWN,    // Повернуть голову вниз
    STEER_LEFT,    // Повернуть голову влево
    STEER_RIGHT,   // Повернуть голову вправо
    ACK_JOIN_PLAY, // Подтверждение сообщения о подключении
    CONFIG,        // Конфигурация игры
    SNAKES,        // Список змей
    FOODS,         // Список еды
    PLAYERS,       // Список игроков
    JOIN_PLAY,     // Присоединиться к игре в режиме активной игры
    MOVE,          // Описание поворота головы
    JOIN_WATCH,    // Присоединиться к игре в режиме наблюдения
    JOIN_FAIL,     // Отказ в присоединении к игре (нет места на поле)
    QUIT,          // Выйти из игры
    OVER,          // Конец игры
    I_AM_MASTER    // Сообщение от заместителя другим игрокам о том, что пора начинать считать его главным
}

public class GameMessage {
    private static int uniqueSeq = 0;
    @Expose
    private Type type;         // Тип сообщения
    @Expose
    private int messageSeq;    // Порядковый номер сообщения, уникален для отправителя в пределах игры, монотонно возрастает
    private GameState state;   // Состояние игрового поля, если type = state

    Type getType() {
        return type;
    }

    static int getUniqueSeq() {
        return uniqueSeq;
    }

    static void incUniqueSeq(){
        uniqueSeq++;
    }

    GameMessage(Type type, int messageSeq) {
        this.type = type;
        this.messageSeq = messageSeq;
    }
}
