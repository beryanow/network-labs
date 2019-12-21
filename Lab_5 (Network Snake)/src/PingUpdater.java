import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PingUpdater {
    private SendTool sendTool;

    PingUpdater(SendTool sendTool) {
        this.sendTool = sendTool;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new DelayChecker(), 2000, 2000);
    }

    private class DelayChecker extends TimerTask {
        void sendPing() throws IOException {
            if (sendTool.getSendPing()) {
                sendTool.sendMessage("", Type.PING);
            }
        }

        @Override
        public void run() {
            try {
                sendPing();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
