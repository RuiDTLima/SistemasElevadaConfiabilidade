package pt.ist.sec.g27.hds_client.model;

public class Value {

    private int timestamp;
    private Message value;

    public Value(int timestamp, Message value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public Message getValue() {
        return value;
    }
}
