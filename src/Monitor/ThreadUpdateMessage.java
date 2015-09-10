package Monitor;

import Crawler.ThreadState;

/**
 * Created by adrian on 09/09/15.
 */
public class ThreadUpdateMessage implements MonitorMessage {

    private ThreadState state;

    public ThreadUpdateMessage (ThreadState state) {
        this.state = state;
    }

    public void updateMonitor(Monitor.MonitorStats monitor) {
        monitor.setStatus(this.state);
    }
}
