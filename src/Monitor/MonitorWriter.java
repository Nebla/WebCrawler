package Monitor;

import java.io.*;
import java.util.Date;

/**
 * Created by adrian on 09/09/15.
 */
public class MonitorWriter implements Runnable {

    private MonitorStats stats;

    private PrintWriter writer;
    private Integer logInterval;

    private Date flushDate;
    private Integer flushInterval;

    public MonitorWriter(MonitorStats stats) {
        this.stats = stats;
    }

    public void initializeMonitor (String fileName, Integer logInterval, Integer flushInterval) {
        flushDate = new Date();
        this.logInterval = logInterval;
        this.flushInterval = flushInterval;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (!Thread.interrupted()) {
            String statsString = stats.getFormattedStats();
            writer.println(statsString);

            if (((new Date().getTime() - this.flushDate.getTime()) / 1000) > flushInterval) {
                writer.flush();
                this.flushDate = new Date();
            }

            try {
                Thread.sleep((long) logInterval * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
