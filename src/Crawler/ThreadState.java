package Crawler;

/**
 * Created by adrian on 08/09/15.
 */

public class ThreadState {

    private Type threadName;
    private Integer threadId;
    private Status threadStatus;

    public enum Status {
        UNKNOWN,
        STARTING,
        BLOCKED,
        WORKING
    }

    public enum Type {
        URL_ANALYZER,
        HTML_ANALYZER,
        FILE_DOWNLOADER
    }

    public ThreadState(Type name, int id, Status status) {
        threadName = name;
        threadId = id;
        threadStatus = status;
    }

    public Type getThreadName() {
        return threadName;
    }

    public int getThreadId() {
        return threadId;
    }

    public Status getThreadStatus() {
        return threadStatus;
    }
}
