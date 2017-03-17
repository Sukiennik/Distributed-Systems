package pl.edu.agh.sr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ChatServer {

    private static final ConcurrentHashMap<String, ObjectOutputStream> tcpUsers = new ConcurrentHashMap<>();
    private static final Set<Integer> udpPorts = Collections.synchronizedSet(new HashSet<>());
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static boolean sendingImageByUdp = false;
    private static boolean sendingImageByMulticast = false;

    public static boolean isSendingImageByUdp() {
        return sendingImageByUdp;
    }

    public static void setSendingImageByUdp(boolean b) {
        sendingImageByUdp = b;
    }

    public static boolean isSendingImageByMulticast() {
        return sendingImageByMulticast;
    }

    public static void setSendingImageByMulticast(boolean sendingImageByMulticast) {
        ChatServer.sendingImageByMulticast = sendingImageByMulticast;
    }

    public static void main(String[] args) {

        Runnable tcpListener = () -> {
            try(ServerSocket tcpServerSocket = new ServerSocket(4242) ) {
                while (true) {
                    System.out.println("NEW TCP CREATION");
                    threadPool.execute(new TcpHandler(tcpServerSocket.accept()));
                    System.out.println("TCP CONNECTION CREATED");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        };

        Runnable udpListener = () -> {
            try(DatagramSocket udpSocket = new DatagramSocket(4242)) {
                while(true) {
                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    if (!udpPorts.contains(packet.getPort())) udpPorts.add(packet.getPort());
                    System.out.println("NEW UDP CREATION");
                    threadPool.execute(new UdpHandler(udpSocket));
                    System.out.println("UDP CONNECTION CREATED");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        };


        Runnable multicastListener = () -> {
            try(DatagramSocket mcSocket = new DatagramSocket(4240)) {
                while(true) {
                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    mcSocket.receive(packet);
                    //if (!udpPorts.contains(packet.getPort())) udpPorts.add(packet.getPort());
                    System.out.println("NEW MULTICAST CREATION");
                    threadPool.execute(new McHandler(mcSocket));
                    System.out.println("UDP MULTICAST CREATED");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        };

        new Thread(tcpListener).start();
        new Thread(udpListener).start();
        new Thread(multicastListener).start();
    }


    private static class McHandler extends Thread {

        private DatagramSocket clientSocket;
        private InetAddress group = InetAddress.getByName("230.0.0.1");

        public McHandler(DatagramSocket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                while(true) {
                    sleep(200);
                    if(ChatServer.isSendingImageByMulticast()) {
                        System.out.println("true");
                        synchronized (this) {
                            byte[] buffer = new byte[2048];
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            clientSocket.receive(packet);
                            //if (!udpPorts.contains(packet.getPort())) udpPorts.add(packet.getPort());
                            packet.setAddress(group);
                            packet.setPort(4246);
                            clientSocket.send(packet);
                            System.out.println("server udpppp");
                            sleep(2000);
                            ChatServer.setSendingImageByMulticast(false);
                        }
                    }
                    sleep(500);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class UdpHandler extends Thread {

        private DatagramSocket clientSocket;
        //private DatagramPacket packet;
        //private byte[] buffer;
        private Logger logger = Logger.getLogger("UdpHandler");

        public UdpHandler(DatagramSocket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            //this.packet = packet;
            //this.buffer = buffer;
        }

        public void run() {
            try {
                while(true) {
                    sleep(200);
                    if(ChatServer.isSendingImageByUdp()) {
                        synchronized (this) {
                            byte[] buffer = new byte[2048];
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            clientSocket.receive(packet);
                            if (!udpPorts.contains(packet.getPort())) udpPorts.add(packet.getPort());
                            udpSend(buffer, packet.getPort());
                            System.out.println("server udpppp");
                            sleep(2000);
                            ChatServer.setSendingImageByUdp(false);
                        }
                    }
                    sleep(500);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void udpSend(byte[] buffer, int excludePort) throws IOException {
            for (Integer port : udpPorts) {
                System.out.println(port);
                if(port == excludePort) continue;
                DatagramPacket packetToSend = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("127.0.0.1"), port);
                clientSocket.send(packetToSend);
            }
        }

    }

    private static class TcpHandler extends Thread {

        private Socket clientSocket;
        private Logger logger = Logger.getLogger("TcpHandler");

        public TcpHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }


        public void run() {
            logger.info("New user connecting...");
            try (
                    ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream())
            ) {
                    Message msg = (Message) is.readObject();
                    if (tcpUsers.containsKey(msg.getName())) {
                        logger.info("Name of new user is already taken");
                        msg.setName("NAMETAKEN");
                        msg.setMsg("Disconnected. Name already taken.");
                        os.writeObject(msg);
                        os.flush();
                        os.close();
                        is.close();
                        clientSocket.close();
                    } else {
                        tcpUsers.put(msg.getName(), os);
                        logger.info("User " + msg.getName() + " has been connected.");
                        String newUser = msg.getName();
                        os.writeObject(new Message("SERVER", "Connected", new Date()));
                        os.flush();
                        msg.setMsg(newUser + " has connected.");
                        tcpSend(msg);
                    }
                    while (!clientSocket.isClosed()) {
                        msg = (Message) is.readObject();
                        if (msg.getMsg().startsWith("-M")) {
                            ChatServer.setSendingImageByUdp(true);
                            msg.setMsg("I am sending image by UDP (-M).");
                        } else if (msg.getMsg().startsWith("-N")) {
                            ChatServer.setSendingImageByMulticast(true);
                            msg.setMsg("I am sending image by MULTICAST UDP (-N).");
                        } else if (msg.getMsg().equals("quit")) {
                            String quiter = msg.getName();
                            tcpUsers.remove(msg.getName());
                            msg.setName("SERVER");
                            msg.setMsg(quiter + " disconnected.");
                        }
                        tcpSend(msg);
                        Thread.sleep(1);
                    }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void tcpSend(Message msg) throws IOException{
            for(String name : tcpUsers.keySet()) {
                if(name.equals(msg.getName())) continue;
                ObjectOutputStream oos = tcpUsers.get(name);
                oos.writeObject(msg);
                oos.flush();
            }
        }

    }
}
