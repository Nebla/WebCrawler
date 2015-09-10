package Monitor;

/**
 * Created by adrian on 09/09/15.
 */
public class FileTypeUpdateMessage implements MonitorMessage {

    private String fileType;

    public FileTypeUpdateMessage(String type) {
        this.fileType = type;
    }

    public void updateMonitor(MonitorStats monitor) {
        monitor.increaseFileType(this.fileType);
    }
}
