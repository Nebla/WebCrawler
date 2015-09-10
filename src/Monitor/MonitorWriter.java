package Monitor;

import Crawler.Pair;

import java.util.concurrent.BlockingQueue;

/**
 * Created by fdv on 09/09/15.
 */
public class MonitorWriter implements Runnable {

    private MonitorStats stats;

    public MonitorWriter(MonitorStats stats) {
        this.stats = stats;
    }

    
    @Override
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
