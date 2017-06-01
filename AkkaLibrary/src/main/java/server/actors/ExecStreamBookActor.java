package server.actors;

import akka.Done;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import client.model.Message;
import scala.concurrent.duration.Duration;
import server.model.Response;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Created 23.05.17.
 */
public class ExecStreamBookActor extends AbstractActor {

    final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static int counter = 0;
    private String title;
    private String returnComment;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.class, m -> {
                    log.info("::Received stream task...");
                    counter++;
                    title = m.getTitle();

                    final Materializer materializer = ActorMaterializer.create(context());
                    final Path file = Paths.get("src/main/resources/content/"+title+".txt");
                    ActorRef sender = getSender();
                    ActorRef self = getSelf();

                    Sink<ByteString, CompletionStage<Done>> printlnSink =
                            Sink.foreach(chunk -> sender.tell(chunk.utf8String(), self));

                    CompletionStage<IOResult> ioResult =
                            FileIO.fromPath(file)
                                    .via(Framing.delimiter(ByteString.fromString(System.lineSeparator()), 1000, FramingTruncation.ALLOW))
                                    .throttle(1, Duration.create(1, TimeUnit.SECONDS), 1, ThrottleMode.shaping())
                                    .to(printlnSink)
                                    .run(materializer);

                    returnComment = "Book \"" + title + "\" is being streamed:";
                    log.info("::_::STREAM TASK ("+counter+") EXECUTED");
                    Response result = parse(m);
                    context().parent().tell(result, getSelf());
                })
                .matchAny(o -> log.info("::Received unknown message."))
                .build();

    }

    public int getCounter() {
        return counter;
    }

    private Response parse(Message m){
        return new Response(Response.TYPE.STREAM, 0, returnComment, m.getSender());
    }
}
