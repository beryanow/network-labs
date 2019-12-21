import java.io.IOException;

public class NoRoomForSnakeException extends IOException {
    NoRoomForSnakeException(){
        super("No room for snake!");
    }
}
