class Special {
    static String host;
    static String login = "/login";
    static String logout = "/logout";
    static String users = "/users";
    static String messages = "/messages";

    static void setHost(String ip, String port) {
        host = "http://" + ip + ":" + port;
    }

    static void printInstructions() {
        System.out.println("Enter correct ip and port as arguments!");
    }
}
