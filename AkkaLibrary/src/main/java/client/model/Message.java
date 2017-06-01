package client.model;

import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * Created 23.05.17.
 */
public final class Message implements Serializable {

    private final TYPE type;
    private final String title;
    private final ActorRef sender;

    public Message(TYPE type, String title, ActorRef sender) {
        this.type = type;
        this.title = title;
        this.sender = sender;
    }

    public TYPE getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public ActorRef getSender() {
        return sender;
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
