package pl.edu.agh.sr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created 16.03.17.
 */
public class udpClientWriter extends Thread {

    private DatagramSocket udpClientSocket;
    private static boolean sendingImage = false;

    public udpClientWriter(DatagramSocket udpClientSocket) {
        this.udpClientSocket = udpClientSocket;
    }

    public static void setSendingImage(boolean b) { sendingImage = b; }

    public static boolean isSendingImage(){ return sendingImage; }

    public void run(){
        DatagramPacket sendPacket;
        try {
            byte buffer[] = "".getBytes();
            sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), 4242);
            udpClientSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true){
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (udpClientWriter.sendingImage) {
                synchronized (this) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        sleep(1000);
                        BufferedImage img = ImageIO.read(new File("src/image_test.jpg"));
                        ImageIO.write(img, "jpg", baos);
                        baos.flush();
                        byte[] buffer = baos.toByteArray();
                        sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), 4242);
                        udpClientSocket.send(sendPacket);
                        sleep(2000);
                        sendingImage = false;
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
