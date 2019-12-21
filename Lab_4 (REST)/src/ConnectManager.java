import okhttp3.*;

import java.io.IOException;
import java.util.List;

class ConnectManager {
    private OkHttpClient client = new OkHttpClient();
    private String host;

    ConnectManager(String host) {
        this.host = host;
    }

    List<OtherUser> listUsers(User user) {
        HttpUrl route = HttpUrl.parse(host + Special.users);
        Request request = getRequest(route, user.getToken());
        try (Response response = client.newCall(request).execute()) {
            return JsonManager.getUsersFromJson(response.body().string());
        } catch (IOException e) {
            return null;
        }
    }

    void sendMessage(User user, String message) {
        HttpUrl route = HttpUrl.parse(host + Special.messages);
        Request request = postRequest(route, user.getToken(), JsonManager.toJsonMessage(message));
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    User login(String username) {
        HttpUrl route = HttpUrl.parse(host + Special.login);
        Request request = new Request.Builder().url(route).post(RequestBody.create(MediaType.parse("application/json"), JsonManager.toJsonUsername(username))).build();
        try (Response response = client.newCall(request).execute()) {
            String payload = response.body().string();
            return JsonManager.getUserFromJson(payload);
        } catch (IOException e) {
            return null;
        }
    }

    void logout(User user) {
        HttpUrl route = HttpUrl.parse(host + Special.logout);
        Request request = postRequest(route, user.getToken(), "");
        try {
            client.newCall(request).execute();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    List<ChatMessage> getMessages(User user, int offset, int count) {
        HttpUrl route = HttpUrl.parse(host + Special.messages + "?offset=" + offset + "&count=" + count);
        Request request = getRequest(route, user.getToken());
        try (Response response = client.newCall(request).execute()) {
            String a = response.body().string();
            return JsonManager.getMessagesFromJson(a);
        } catch (IOException e) {
            return null;
        }
    }

    private Request getRequest(HttpUrl route, String token) {
        return new Request.Builder()
                .url(route)
                .get()
                .header("Authorization", "Token " + token)
                .build();
    }

    private Request postRequest(HttpUrl route, String token, String body) {
        return new Request.Builder()
                .url(route)
                .post(RequestBody.create(MediaType.parse("application/json"), body))
                .header("Authorization", "Token " + token)
                .build();
    }
}