package pl.edu.agh.sr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Created 14.03.17.
 */
public class tcpClientReader extends Thread {

    private final String name;
    private Socket socket;

    public tcpClientReader(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public synchronized void run() {
        try(ObjectInputStream is = new ObjectInputStream(this.socket.getInputStream())) {
                Message msg = (Message) is.readObject();
                if (msg.getName().equals("NAMETAKEN")) {
                    System.out.println("SERVER :: " + msg.getDate() + " :: " + msg.getMsg());
                    tcpClientWriter.setChatting(false);
                } else {
                    System.out.println(msg.getName() + " :: " + msg.getDate() + " :: " + msg.getMsg());
                }
                while (tcpClientWriter.isChatting()) {
                    msg = (Message) is.readObject();
                    System.out.println(msg.getName() + " :: " + msg.getDate() + " :: " + msg.getMsg());
                    if(msg.getMsg().contains("-M")) udpClientReader.setReadingImage(true);
                    if(msg.getMsg().contains("-N") && !msg.getMsg().contains(name + " ::") ) mcClientReader.setReadingImage(true);
                    Thread.sleep(500);
                }
        } catch(IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
