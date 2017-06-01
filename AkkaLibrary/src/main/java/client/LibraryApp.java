package client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import client.actors.ClientActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Created 23.05.17.
 */
public class LibraryApp {



    public static void main(String[] args) throws Exception {

        // config
        File configFile = new File("client2_app.conf");
        Config config = ConfigFactory.parseFile(configFile);

        // create actor system & actors
        final ActorSystem system = ActorSystem.create("client2_system", config);
        final ActorRef local = system.actorOf(Props.create(ClientActor.class), "client2");
        System.out.println(local.toString());


        // interaction
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.equals("exit")) {
                break;
            }
            local.tell(line, null);
        }

        system.terminate();
    }

}
