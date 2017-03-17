package pl.edu.agh.sr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * Created 14.03.17.
 */
public class tcpClientWriter extends Thread {

    private Socket socket;
    private String name;
    private static boolean chatting = true;

    public tcpClientWriter(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public static boolean isChatting() {
        return chatting;
    }

    public static void setChatting(boolean b) {
        chatting = b;
    }

    public synchronized void run() {
        try(BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream())
        ) {
                os.writeObject(new Message(name, null, new Date()));
                os.flush();
                String msg;
                while (chatting) {
                    msg = input.readLine();
                    os.writeObject(new Message(name, msg, new Date()));
                    os.flush();
                    if (msg.equals("quit")) chatting = false;
                    if (msg.startsWith("-M")) {
                        udpClientWriter.setSendingImage(true);
                        Thread.sleep(500);
                    }
                    if (msg.startsWith("-N"))  {
                        mcClientWriter.setSendingImage(true);
                        Thread.sleep(500);
                    }

                }
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
