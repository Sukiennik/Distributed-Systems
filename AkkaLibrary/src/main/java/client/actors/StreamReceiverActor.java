package client.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Created 24.05.17.
 */
public class StreamReceiverActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, System.out::println)
                .matchAny(o -> {
                    log.info("Received unknown message.");
                })
                .build();
    }


}


