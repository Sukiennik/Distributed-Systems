package pl.edu.agh.sr;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created 14.03.17.
 */
public class Message implements Serializable {

    private String name;
    private String msg;
    private String date;
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public Message(String name, String msg, Date date) {
        this.name = name;
        this.msg = msg;
        this.date = dateFormat.format(date);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDate() {
        return date;
    }
}
