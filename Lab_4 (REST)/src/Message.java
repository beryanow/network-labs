import com.google.gson.annotations.Expose;

class Message {
    @Expose
    private String message;
    private int id;

    Message(String message) {
        this.message = message;
    }
}
