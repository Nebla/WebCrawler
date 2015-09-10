package Monitor;

/**
 * Created by adrian on 09/09/15.
 */
public class UrlIncreaseMessage implements MonitorMessage {
    public void updateMonitor(MonitorStats monitor) {
        monitor.increaseAnalyzedUrl();
    }
}
