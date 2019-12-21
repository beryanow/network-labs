import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

class Client {
    private Socket socket;
    private File file;

    Client(String filePath) {
        Path fileDirectory = Paths.get(filePath);
        file = new File(fileDirectory.toUri());
        socket = new Socket();
    }

    private void sendFileToServer() throws IOException {
        BufferedOutputStream socketOutputStream = new BufferedOutputStream(socket.getOutputStream());

        byte[] fileNameBuffer = file.getName().getBytes("UTF-8");
        int fileNameLength = fileNameBuffer.length;
        byte[] fileNameLengthBuffer = ByteBuffer.allocate(4).putInt(fileNameLength).array();
        socketOutputStream.write(fileNameLengthBuffer);
        socketOutputStream.write(fileNameBuffer);

        byte[] partFileData = new byte[256];
        FileInputStream fileInputStream = new FileInputStream(file);
        int readAmount;
        while ((readAmount = fileInputStream.read(partFileData, 0, 256)) != -1) {
            byte[] partFileDataLength = ByteBuffer.allocate(4).putInt(readAmount).array();
            socketOutputStream.write(partFileDataLength);
            socketOutputStream.write(partFileData, 0, readAmount);
        }
        socketOutputStream.flush();
        fileInputStream.close();
        socket.shutdownOutput();
    }

    private void receiveServerResponse() throws IOException, ClientReadingException {
        BufferedInputStream socketInputStream = new BufferedInputStream(socket.getInputStream());
        byte[] serverResponseLengthBuffer = new byte[4];
        if (socketInputStream.read(serverResponseLengthBuffer, 0, 4) != 4) {
            throw new ClientReadingException("Failed to receive server response length!");
        }
        int serverResponseLength = ByteBuffer.wrap(serverResponseLengthBuffer).getInt();
        byte[] serverResponseBuffer = new byte[serverResponseLength];
        if (socketInputStream.read(serverResponseBuffer, 0, serverResponseLength) != serverResponseLength) {
            throw new ClientReadingException("Failed to receive server response!");
        }
        System.out.println(new String(serverResponseBuffer, "UTF-8"));
    }

    void connectToServer(String serverIP, int serverPort) {
        try {
            socket.connect(new InetSocketAddress(serverIP, serverPort));
            sendFileToServer();
            receiveServerResponse();
        } catch (ClientReadingException e) {
            System.out.println(e.getMessage());
        } catch (ConnectException e) {
            System.out.println("Unable to find the server!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Failed to close the socket!");
            }
        }
    }
}
