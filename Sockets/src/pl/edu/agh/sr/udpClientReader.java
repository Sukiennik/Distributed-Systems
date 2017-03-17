package pl.edu.agh.sr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created 16.03.17.
 */
public class udpClientReader extends Thread {


    private DatagramSocket udpClientSocket;
    private String name;
    private static boolean readingImage = false;

    public udpClientReader(DatagramSocket udpClientSocket, String name) {
        this.udpClientSocket = udpClientSocket;
        this.name = name;
    }

    public static boolean isReadingImage() {
        return readingImage;
    }

    public static void setReadingImage(boolean b) {
        readingImage = b;
    }

    public void run(){
        while(true) {
            try {
                sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (udpClientReader.isReadingImage()) {
                synchronized (this) {
                    try {
                        byte[] buffer = new byte[2048];
                        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                        udpClientSocket.receive(receivedPacket);
                        byte[] imgBuffer = receivedPacket.getData();
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBuffer));
                        File output = new File("image_test_udp_" + name);
                        ImageIO.write(img, "jpg", output);
                        sleep(2000);
                        readingImage = false;
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
