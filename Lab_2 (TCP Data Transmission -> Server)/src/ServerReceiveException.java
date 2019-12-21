import java.io.IOException;

class ServerReceiveException extends IOException {
    ServerReceiveException(String exactError){
        super(exactError);
    }
}
