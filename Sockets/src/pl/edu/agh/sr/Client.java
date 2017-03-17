package pl.edu.agh.sr;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created 14.03.17.
 */
public class Client {

    public static void main(String[] args) throws InterruptedException {
        String name = args[0];

        try(Socket tcpClientSocket = new Socket("127.0.0.1", 4242);
            DatagramSocket udpClientSocket = new DatagramSocket();
            MulticastSocket mcClientSocket = new MulticastSocket(4246)
        ) {

            ExecutorService threadPool = Executors.newFixedThreadPool(6);

            tcpClientWriter tcpWriter = new tcpClientWriter(tcpClientSocket, name);
            tcpClientReader tcpRead = new tcpClientReader(tcpClientSocket, name);

            udpClientWriter udpWriter = new udpClientWriter(udpClientSocket);
            udpWriter.setPriority(Thread.MAX_PRIORITY);
            udpClientReader udpReader = new udpClientReader(udpClientSocket, name);
            udpReader.setPriority(Thread.MAX_PRIORITY);

            mcClientWriter mcWriter = new mcClientWriter(mcClientSocket);
            mcWriter.setPriority(Thread.MAX_PRIORITY);
            mcClientReader mcReader = new mcClientReader(mcClientSocket, name);
            mcReader.setPriority(Thread.MAX_PRIORITY);

            while(tcpClientWriter.isChatting()){
                threadPool.execute(tcpWriter);
                Thread.sleep(200);
                threadPool.execute(tcpRead);
                Thread.sleep(200);
                threadPool.execute(udpWriter);
                Thread.sleep(200);
                threadPool.execute(udpReader);
                Thread.sleep(200);
                threadPool.execute(mcWriter);
                Thread.sleep(200);
                threadPool.execute(mcReader);
            }
            threadPool.shutdownNow();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
