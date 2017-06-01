package server.model;

import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * Created 23.05.17.
 */
public final class Response implements Serializable {

    private final TYPE type;
    private final int price;
    private final String comment;
    private final ActorRef destination;

    public Response(TYPE type, int price, String comment, ActorRef destination) {
        this.type = type;
        this.price = price;
        this.comment = comment ;
        this.destination = destination;
    }

    public int getPrice() {
        return price;
    }

    public String getComment() {
        return comment;
    }

    public ActorRef getDestination() {
        return destination;
    }

    public TYPE getType() {
        return type;
    }

    public enum TYPE {
        NONE (0),
        SEARCH (1),
        ORDER (2),
        STREAM (3);

        private int type;

        TYPE(int i) {
            type = i;
        }
    }

}
