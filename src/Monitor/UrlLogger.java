package Monitor;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by adrian on 10/09/15.
 */
public class UrlLogger implements Runnable {

    private BlockingQueue<String> urlQueue;
    private PrintWriter writer;

    public UrlLogger(BlockingQueue<String> urlQueue) {
        this.urlQueue = urlQueue;
    }

    public void initializeLogger (String fileName) {
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
            try {
                String url = urlQueue.take();
                writer.println(url);
                writer.flush();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }
}
