import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

class JsonManager {
    private static Gson gson = new Gson();

    static List<OtherUser> getUsersFromJson(String json) {
        return gson.fromJson(json, new TypeToken<List<OtherUser>>() {}.getType());
    }

    static User getUserFromJson(String json) {
        return gson.fromJson(json, User.class);
    }

    static String toJsonUsername(String username) {
        return gson.toJson(new UserName(username));
    }

    static String toJsonMessage(String message) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(new Message(message));
    }

    static List<ChatMessage> getMessagesFromJson(String json) {
        return gson.fromJson(json, new TypeToken<List<ChatMessage>>() {}.getType());
    }
}
