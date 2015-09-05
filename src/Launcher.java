import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Created by adrian on 05/09/15.
 */
public class Launcher {

    /*public static void main(String[] args) throws Exception {
        WebCrawler app = new WebCrawler();
        Runtime.getRuntime().addShutdownHook(app);
        app.crawl();
    }*/

    public static void main(String[] args) {

        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("Config/Config.properties");

            prop.load(input);

            System.out.println(prop.getProperty("fileAnalyzer"));
            System.out.println(prop.getProperty("fileDownloader"));
            System.out.println(prop.getProperty("urlAnalyzer"));


            int queueSize =  Integer.parseInt(prop.getProperty("queueSize"));
            BlockingQueue<String> queue = new ArrayBlockingQueue<String>(queueSize);

            (new Thread(new FileAnalyzer(queue))).start();

            Thread thread  = (new Thread(new FileDownloader(queue)));
            thread.start();
            (new Thread(new FileDownloader(queue))).start();
            (new Thread(new FileDownloader(queue))).start();

            thread.join();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
