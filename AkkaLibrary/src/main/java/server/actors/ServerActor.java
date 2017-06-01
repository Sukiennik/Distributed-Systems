package server.actors;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import client.model.Message;
import scala.concurrent.duration.Duration;
import server.model.Response;

import java.io.FileNotFoundException;
import java.io.IOException;

import static akka.actor.SupervisorStrategy.*;

/**
 * Created 23.05.17.
 */
public class ServerActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Message.class, m -> {
                    switch (m.getType()) {
                        case SEARCH:
                            context().child("searchExec").get().tell(m, getSelf());
                            break;
                        case ORDER:
                            context().child("orderExec").get().tell(m, getSelf());
                            break;
                        case STREAM:
                            context().child("streamExec").get().tell(m, getSender());
                            break;
                        default:
                            break;
                    }
                })
                .match(Response.class, r -> {
                    r.getDestination().tell(r, getSelf());
                    log.info("::Sending RESPONSE to CLIENT.");
                })
                .matchAny(o -> log.info("::Received unknown message."))
                .build();
    }



    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(ExecSearchBookActor.class), "searchExec");
        context().actorOf(Props.create(ExecOrderBookActor.class), "orderExec");
        context().actorOf(Props.create(ExecStreamBookActor.class), "streamExec");
    }

    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder.
                    match(FileNotFoundException.class, e -> resume())
                    .match(NumberFormatException.class, e -> resume())
                    .match(IOException.class, e -> restart())
                    .matchAny(o -> escalate()).
                    build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }


}

