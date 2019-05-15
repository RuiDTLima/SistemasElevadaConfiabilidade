package pt.ist.sec.g27.hds_notary.utils;

public class Timeouts {

    public static long start(long timeout) {
        return System.currentTimeMillis() + timeout;
    }

    public static long remaining(long target) {
        return target - System.currentTimeMillis();
    }

    public static boolean isTimeout(long remaining) {
        return remaining <= 0;
    }
}
