package pt.ist.sec.g27.hds_client.model;

public class Value {

    private int timestamp;
    private Body value;

    public Value(int timestamp, Body value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public Body getValue() {
        return value;
    }
}
