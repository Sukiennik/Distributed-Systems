package server.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import client.model.Message;
import server.database.FileParser;
import server.model.Response;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created 23.05.17.
 */
public class ExecSearchBookActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static int counter = 0;
    private int price;
    private String title;
    private String returnComment;


    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Message.class, m -> {
                    log.info("::Received search task...");
                    counter++;
                    title = m.getTitle();

                    FileParser parser = new FileParser();
                    ExecutorService pool = Executors.newFixedThreadPool(2);
                    Future<Boolean> resultFromFirst = pool.submit(() -> parser.searchForTitle(title, FileParser.DBnumber.FIRST));
                    Future<Boolean> resultFromSecond = pool.submit(() -> parser.searchForTitle(title, FileParser.DBnumber.SECOND));

                    if(resultFromFirst.get() || resultFromSecond.get()) {
                        price = parser.getFoundPrice();
                        returnComment = "Book \"" + title + "\" successfully found. PRICE :: ";
                    }
                    else returnComment = "No such book in database. Search failed.";



                    log.info("::_::SEARCH TASK ("+counter+") EXECUTED");
                    Response result = parse(m);
                    getSender().tell(result, getSelf());
                })
                .matchAny(o -> log.info("::Received unknown message."))
                .build();

    }

    public int getCounter() {
        return counter;
    }

    private Response parse(Message m){
        return new Response(Response.TYPE.SEARCH, price, returnComment, m.getSender());
    }
}
