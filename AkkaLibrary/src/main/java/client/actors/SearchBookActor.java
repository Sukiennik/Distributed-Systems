package client.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import client.model.Message;

/**
 * Created 23.05.17.
 */
public class SearchBookActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private int counter;

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    counter++;
                    Message result = parse(s);
                    getSender().tell(result, getSelf());
                })
                .matchAny(o -> log.info("Received unknown message."))
                .build();

    }

    public int getCounter() {
        return counter;
    }

    private Message parse(String s){
        String[] split = s.split(",");
        String name = split[0].replace("search ", "");
        System.out.println(name);
        return new Message(Message.TYPE.SEARCH, name, context().parent());
    }

}
