package client.model;

import akka.actor.AbstractActor;

/**
 * Created 24.05.17.
 */
public class streamActor extends AbstractActor {

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, m -> {
                    System.out.println(m+" in actor");
                })
                .matchAny(o -> System.out.println("unknown"))
                .build();
    }
}
