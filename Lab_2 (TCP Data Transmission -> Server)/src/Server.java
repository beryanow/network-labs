import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class Server {
    private ServerSocket socket;
    private Logger logger;

    Server(int port) throws IOException {
        socket = new ServerSocket(port);
        logger = new Logger(3000);
    }

    void listenClients() {
        ArrayBlockingQueue<Runnable> dataProcessingTasks = new ArrayBlockingQueue<>(5);

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, dataProcessingTasks);
        threadPool.execute(logger);

        while (true) {
            try {
                Socket clientSocket = socket.accept();

                DataProcessing dataProcessingTask = new DataProcessing(clientSocket);
                logger.addClientData(dataProcessingTask);

                threadPool.execute(dataProcessingTask);

            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }
}
