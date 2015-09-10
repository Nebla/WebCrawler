package Crawler;

import Monitor.MonitorMessage;
import Monitor.ThreadUpdateMessage;
import Monitor.UrlIncreaseMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by adrian on 05/09/15.
 */

public class UrlAnalyzer implements Runnable {

    private Integer threadId;
    private BlockingQueue<MonitorMessage> monitorQueue;

    private BlockingQueue<UrlMessage> urlToAnalyzeQueue;
    private BlockingQueue<UrlMessage> urlToDownloadQueue;

    private ConcurrentHashMap<String,Boolean> analyzedUrls;

    public UrlAnalyzer(Integer threadId, BlockingQueue<UrlMessage> analyzeQueue, BlockingQueue<UrlMessage> downloadQueue, ConcurrentHashMap<String,Boolean> map, BlockingQueue<MonitorMessage> monitorQueue) {
        this.threadId = threadId;
        this.monitorQueue = monitorQueue;
        this.urlToAnalyzeQueue = analyzeQueue;
        this.urlToDownloadQueue = downloadQueue;
        this.analyzedUrls = map;
    }

    public void run() {

        try {
            this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.URL_ANALYZER, this.threadId, ThreadState.Status.STARTING)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!Thread.interrupted()) {
            try {
                this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.URL_ANALYZER, this.threadId, ThreadState.Status.BLOCKED)));
                UrlMessage url = urlToAnalyzeQueue.take();
                this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.URL_ANALYZER, this.threadId, ThreadState.Status.WORKING)));

                System.out.println("Analyzing url: "+url.getUrl());
                    if (analyzedUrls.putIfAbsent(url.getUrl(), true) == null) {
                        System.out.println("Url NOT downloaded " + url.getUrl());
                        urlToDownloadQueue.put(url);
                    } else {
                        // url already downloaded
                        System.out.println("Url already downloaded " + url.getUrl());
                    }


                this.monitorQueue.put(new UrlIncreaseMessage());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
