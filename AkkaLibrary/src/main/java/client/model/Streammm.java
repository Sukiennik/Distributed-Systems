package client.model;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import scala.concurrent.duration.Duration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Created 24.05.17.
 */
public class Streammm {

    public static void main(String[] argv) throws Exception {


        List<String> chunks = new ArrayList<>();
        final ActorSystem system = ActorSystem.create("stream_system");
        final Materializer materializer = ActorMaterializer.create(system);
        final ActorRef actor = system.actorOf(Props.create(streamActor.class), "sink");

        final Path file = Paths.get("src/main/resources/content/Book.txt");

        Sink<ByteString, CompletionStage<Done>> printlnSink =
                Sink.<ByteString> foreach(chunk -> {
                    chunks.add(chunk.utf8String());
                    actor.tell(chunk.utf8String(), null);
                });
                //Sink.<ByteString> foreach(chunk -> System.out.println(chunk.utf8String()));

        CompletionStage<IOResult> ioResult =
                FileIO.fromPath(file)
                        .via(Framing.delimiter(ByteString.fromString(System.lineSeparator()), 1000, FramingTruncation.ALLOW))
                        .throttle(1, Duration.create(1, TimeUnit.SECONDS), 1, ThrottleMode.shaping())
                        .to(printlnSink)
                        .run(materializer);



        final Source<Integer, NotUsed> source = Source.range(1, 10);
        //final Flow oldflow = Flow.of(Integer.class).map(val -> val * 2);
        final Flow flow = Flow.of(Integer.class).scan(1, (acc, next) -> next * acc);

        //final Sink<Integer, CompletionStage<Done>> sinkPrint = Sink.foreach(i -> System.out.println(i));
        //final Sink<Integer, CompletionStage<Done>> oldsinkCombined = flow.toMat(sinkPrint, Keep.right());

        final Sink<Integer, NotUsed> sinkSends = Sink.actorRef(actor, "complete");
        //final Sink<Integer, CompletionStage<Done>> sinkSend = Sink.foreach(i -> actor.tell(i, null));
        final Sink<Integer, NotUsed> sinkCombined = flow.toMat(sinkSends, Keep.right());

        //final NotUsed done = source.runWith(sinkCombined, materializer);

    }

}
