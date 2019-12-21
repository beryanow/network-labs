import java.util.ArrayList;

class Logger implements Runnable {
    private final ArrayList<DataProcessing> clientsData;
    private int updateInterval;

    Logger(int time) {
        clientsData = new ArrayList<>();
        updateInterval = time;
    }

    void addClientData(DataProcessing data) {
        synchronized (clientsData) {
            clientsData.add(data);
        }
    }

    private void makeClientLog(DataProcessing clientData) {
        System.out.println("IP -> " + clientData.getClientIP().substring(1));
        System.out.println("Speed -> " + ((clientData.getOverallDataAmount() - clientData.getRecentDataAmount()) / (updateInterval / 1000) / 1024) + " Kb/sec");
        clientData.setRecentDataAmount(clientData.getOverallDataAmount());
        System.out.println("Average speed -> " + (clientData.getOverallDataAmount() / (System.currentTimeMillis() - clientData.getStartTime()) * 1000 / 1024) + " Kb/sec");
        System.out.println("Overall received data -> " + (clientData.getOverallDataAmount() / 1024) + " Kb");
        System.out.println("-----------------------------------");
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(updateInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int clientIndex;
            synchronized (clientsData) {
                if (clientsData.isEmpty()) {
                    System.out.println("The client list is empty!");
                    continue;
                }
                System.out.println("\n-----------------------------------");
                System.out.println(clientsData.size() == 1 ? "There is 1 client!" : "There are " + clientsData.size() + " clients!");
                System.out.println("-----------------------------------");
                for (clientIndex = 0; clientIndex < clientsData.size(); clientIndex++) {
                    makeClientLog(clientsData.get(clientIndex));
                    if (clientsData.get(clientIndex).isOver()) {
                        clientsData.remove(clientIndex);
                        --clientIndex;
                    }
                    if (clientIndex == clientsData.size() - 1) {
                        System.out.println();
                    }
                }
            }
        }
    }
}
