package server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import server.actors.ServerActor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Created 23.05.17.
 */
public class ServerApp {

    public static void main(String[] args) throws Exception {

        // config
        File configFile = new File("server_app.conf");
        Config config = ConfigFactory.parseFile(configFile);

        // create actor system & actors
        final ActorSystem system = ActorSystem.create("server_system", config);
        final ActorRef remote = system.actorOf(Props.create(ServerActor.class), "server");
        System.out.println(remote.toString());


        // interaction
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.equals("exit")) {
                break;
            }
            remote.tell(line, null);
        }

        system.terminate();
    }
}
