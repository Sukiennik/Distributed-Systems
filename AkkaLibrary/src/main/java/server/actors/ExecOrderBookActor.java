package server.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import client.model.Message;
import server.database.FileParser;
import server.model.Response;

/**
 * Created 23.05.17.
 */
public class ExecOrderBookActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static int counter = 0;
    private String title;
    private String returnComment;


    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Message.class, m -> {
                    log.info("::Received order task...");
                    counter++;
                    title = m.getTitle();
                    if(FileParser.orderBook(title)) {
                        returnComment = "Book \"" + title + "\" successfully ordered";
                    }
                    else returnComment = "No such book in database. Order cancelled";

                    log.info("::_::ORDER TASK ("+counter+") EXECUTED");
                    Response result = parse(m);
                    getSender().tell(result, getSelf());
                })
                .matchAny(o -> log.info("::Received unknown message"))
                .build();

    }

    public int getCounter() {
        return counter;
    }

    private Response parse(Message m){
        return new Response(Response.TYPE.ORDER, 0, returnComment, m.getSender());
    }

}
