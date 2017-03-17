package pl.edu.agh.sr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created 16.03.17.
 */
public class mcClientWriter extends Thread {

    private MulticastSocket mcClientSocket;
    private static boolean sendingImage = false;

    public mcClientWriter(MulticastSocket mcClientSocket) throws IOException {
        this.mcClientSocket = mcClientSocket;
    }

    public static void setSendingImage(boolean b) { sendingImage = b; }

    public static boolean isSendingImage(){ return sendingImage; }

    @SuppressWarnings("duplicate")
    public void run() {
        DatagramPacket sendPacket;
        try {
            byte buffer[] = "".getBytes();
            sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), 4240);
            mcClientSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true){
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mcClientWriter.sendingImage) {
                synchronized (this) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        System.out.println("mcwrite");
                        sleep(1000);
                        BufferedImage img = ImageIO.read(new File("src/image_test.jpg"));
                        ImageIO.write(img, "jpg", baos);
                        baos.flush();
                        byte[] buffer = baos.toByteArray();
                        sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), 4240);
                        mcClientSocket.send(sendPacket);
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
