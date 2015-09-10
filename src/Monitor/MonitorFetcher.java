package Monitor;

import java.util.concurrent.BlockingQueue;

/**
 * Created by adrian on 09/09/15.
 */
public class MonitorFetcher implements Runnable {

    private MonitorStats stats;
    private BlockingQueue<MonitorMessage> monitorQueue;

    public MonitorFetcher(MonitorStats stats, BlockingQueue<MonitorMessage> monitorQueue) {
        this.stats = stats;
        this.monitorQueue = monitorQueue;
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                MonitorMessage message = monitorQueue.take();
                message.updateMonitor(stats);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
