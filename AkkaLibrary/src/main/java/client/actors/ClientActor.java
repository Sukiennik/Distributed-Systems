package client.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import client.model.Message;
import scala.concurrent.duration.Duration;
import server.model.Response;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;

/**
 * Created 23.05.17.
 */
public class ClientActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorSelection selection = getContext().actorSelection("akka.tcp://server_system@127.0.0.1:3559/user/server");


    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    if(s.startsWith("search")) {
                        context().child("searchTask").get().tell(s, getSelf());
                    } else if(s.startsWith("order")) {
                        context().child("orderTask").get().tell(s, getSelf());
                    } else if(s.startsWith("stream")) {
                        context().child("streamTask").get().tell(s, getSelf());
                    }})
                .match(Message.class, m -> {
                    selection.tell(m, getSender());
                    System.out.println("EXECUTING " + m.getType() + " TASK");
                })
                .match(Response.class, r -> {
                    switch (r.getType()) {
                        case SEARCH:
                            if(r.getPrice()==0)
                                System.out.println(r.getComment());
                            else System.out.println(r.getComment()+r.getPrice());

                            break;
                        case ORDER:
                            System.out.println(r.getComment());
                            break;
                        case STREAM:
                            System.out.println(r.getComment());
                            break;
                        default:
                            break;
                    }
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(SearchBookActor.class), "searchTask");
        context().actorOf(Props.create(OrderBookActor.class), "orderTask");
        context().actorOf(Props.create(StreamBookActor.class), "streamTask");
    }

    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder.
                    match(Exception.class, e -> restart()).
                    matchAny(o -> escalate()).
                    build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }



}
