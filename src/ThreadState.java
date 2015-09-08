import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by adrian on 08/09/15.
 */




public class ThreadState {
    private Date localTime;
    private Type threadName;
    private int threadId;
    private Status threadStatus;

    public enum Status {
        BLOCKED,
        WORKING
    }

    public enum Type {
        URL_ANALYZER,
        HTML_ANALYZER,
        FILE_DOWNLOADER
    }

    public ThreadState(Date date, Type name, int id, Status status) {
        localTime = date;
        threadName = name;
        threadId = id;
        threadStatus = status;
    }

    public String getDescription () {
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String stringDate = formatter.format(localTime);
        String stringStatus = (threadStatus == Status.BLOCKED)?"Blocked":"Working";
        String name = "";
        switch (threadName) {
            case URL_ANALYZER:
                name = "URL Analyzer";
                break;
            case HTML_ANALYZER:
                name = "HTML Analyzer";
                break;
            case FILE_DOWNLOADER:
                name = "File Downloader";
                break;
        }

        return stringDate + " " + name + " " + threadId + ": " + stringStatus;
    }
}
