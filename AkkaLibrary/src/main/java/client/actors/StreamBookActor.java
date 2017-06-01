package client.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import client.model.Message;

/**
 * Created 23.05.17.
 */
public class StreamBookActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private int counter;

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    counter++;
                    Message result = parse(s);
                    getSender().tell(result, context().child("streamReceiver").get());
                })
                .matchAny(o -> log.info("Received unknown message."))
                .build();

    }

    public int getCounter() {
        return counter;
    }

    private Message parse(String s){
        String[] split = s.split(" ");
        return new Message(Message.TYPE.STREAM, split[1], context().parent());
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(StreamReceiverActor.class), "streamReceiver");
    }

}
