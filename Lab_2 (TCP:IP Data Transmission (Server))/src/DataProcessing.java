import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class DataProcessing implements Runnable {
    private Socket socket;
    private boolean over;
    private boolean valid;

    private Long overallDataAmount;
    private Long recentDataAmount;
    private Long startTime;

    DataProcessing(Socket socket) {
        this.socket = socket;
        valid = true;
        overallDataAmount = Long.valueOf(0);
        recentDataAmount = Long.valueOf(0);
        over = false;
        startTime = System.currentTimeMillis();
    }

    String getClientIP() {
        return socket.getInetAddress().toString();
    }

    void setRecentDataAmount(Long amount) {
        recentDataAmount = amount;
    }

    Long getOverallDataAmount() {
        return overallDataAmount;
    }

    Long getRecentDataAmount() {
        return recentDataAmount;
    }

    Long getStartTime() {
        return startTime;
    }

    boolean isOver() {
        return over;
    }

    private void receiveClientRequest() throws IOException {
        String name;
        BufferedInputStream in = new BufferedInputStream(socket.getInputStream());

        byte[] fileNameLengthBuffer = new byte[4];
        if (in.read(fileNameLengthBuffer, 0, 4) != 4) {
            throw new ServerReceiveException("Getting client's file length error!");
        }
        overallDataAmount += 4;
        Integer fileNameLength = ByteBuffer.wrap(fileNameLengthBuffer).getInt();

        byte[] fileNameBuffer = new byte[fileNameLength];
        if (in.read(fileNameBuffer, 0, fileNameLength) != fileNameLength) {
            throw new ServerReceiveException("Getting client's file name error!");
        }

        overallDataAmount += fileNameLength;
        name = new String(fileNameBuffer, "UTF-8");
        receiveClientData(name, in);

        try {
            socket.shutdownInput();
        } catch (IOException e) {
            System.out.println("Not able to close socket's input!");
        }
    }

    private void receiveClientData(String fileName, BufferedInputStream in) throws IOException {
        Path p = Paths.get("./uploads");
        File file;
        try {
            Files.createDirectories(p);
            file = new File("./uploads" + File.separator + fileName);
        } catch (IOException e) {
            System.out.println("Failed to create the directory!");
        }

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println("Failed to open file's output stream!");
        }

        int successReadAmount;
        byte[] partFile = new byte[256];
        byte[] partFileLengthBuffer = new byte[4];

        while ((successReadAmount = in.read(partFileLengthBuffer, 0, 4)) != -1) {
            if (successReadAmount != 4) {
                throw new ServerReceiveException("Reading part file length error!");
            }
            overallDataAmount += 4;
            Integer partFileLength = ByteBuffer.wrap(partFileLengthBuffer).getInt();
            successReadAmount = in.readNBytes(partFile, 0, partFileLength);
            if (successReadAmount != partFileLength) {
                throw new ServerReceiveException("Reading part file data error!");
            }
            overallDataAmount += successReadAmount;
            fileOutputStream.write(partFile, 0, successReadAmount);
        }

        if (fileOutputStream != null) {
            fileOutputStream.close();
        }
    }

    private void sendServerResponse() throws IOException {
        OutputStream socketOutputStream = null;
        try {
            socketOutputStream = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Failed to open socket's output stream!");
        }

        int responseLength;
        if (valid) {
            responseLength = "The file has been delivered without errors!".getBytes().length;
        } else {
            responseLength = "An error happened while delivering the file!".getBytes().length;
        }

        try {
            socketOutputStream.write(ByteBuffer.allocate(4).putInt(responseLength).array());
        } catch (IOException e) {
            System.out.println("Failed to create buffer for the socket's output stream!");
        }
        if (valid) {
            try {
                socketOutputStream.write("The file has been delivered without errors!".getBytes("UTF-8"));
            } catch (IOException e) {
                System.out.println("Failed to write message to the socket's output stream!");
            }
        } else {
            try {
                socketOutputStream.write("An error happened while delivering the file!".getBytes("UTF-8"));
            } catch (IOException e) {
                System.out.println("Failed to write message to the socket's output stream!");
            }
        }

        socketOutputStream.flush();
        socketOutputStream.close();
    }

    @Override
    public void run() {
        try {
            receiveClientRequest();
        } catch (ServerReceiveException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            valid = false;
            e.printStackTrace();
        }

        over = true;
        try {
            sendServerResponse();
        } catch (IOException e) {
            System.out.println("Failed to send response to the client!");
        }
    }
}
