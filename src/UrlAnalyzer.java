import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by adrian on 05/09/15.
 */

public class UrlAnalyzer implements Runnable {

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    private BlockingQueue<String> urlToAnalyzeQueue;
    private BlockingQueue<String> urlToDownloadQueue;
    private ConcurrentHashMap<String,Boolean> analyzedUrls;

    public UrlAnalyzer(BlockingQueue<String> analyzeQueue, BlockingQueue<String> downloadQueue, ConcurrentHashMap<String,Boolean> map) {
        urlToAnalyzeQueue = analyzeQueue;
        urlToDownloadQueue = downloadQueue;
        analyzedUrls = map;
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                String url = urlToAnalyzeQueue.take();
                System.out.println("Analyzing url: "+url);

                if (analyzedUrls.putIfAbsent(url, true) == null) {
                    System.out.println("Url NOT downloaded " + url);
                    urlToDownloadQueue.put(url);
                } else {
                    // url already downloaded
                    System.out.println("Url already downloaded " + url);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
