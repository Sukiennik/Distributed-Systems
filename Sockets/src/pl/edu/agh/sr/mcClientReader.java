package pl.edu.agh.sr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created 16.03.17.
 */
public class mcClientReader extends Thread {


    private DatagramSocket mcClientSocket;
    private String name;
    private static boolean readingImage = false;

    public mcClientReader(MulticastSocket mcClientSocket, String name) throws IOException {
        this.mcClientSocket = mcClientSocket;
        this.name = name;
        InetAddress group = InetAddress.getByName("230.0.0.1");
        mcClientSocket.joinGroup(group);
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
            if (mcClientReader.isReadingImage()) {
                synchronized (this) {
                    try {
                        System.out.println("mcread");
                        byte[] buffer = new byte[2048];
                        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                        mcClientSocket.receive(receivedPacket);
                        byte[] imgBuffer = receivedPacket.getData();
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBuffer));
                        File output = new File("image_test_mc_" + name);
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
