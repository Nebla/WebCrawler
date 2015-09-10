import Crawler.*;
import Monitor.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Created by adrian on 05/09/15.
 */
public class Launcher {

    public static void main(String[] args) {

        Properties prop = new Properties();
        InputStream input = null;

        try {
            String configFile = "Config/Config.properties";
            File f = new File(configFile);
            if(!f.exists()) {
                System.err.println("Config file " + configFile + " missing");
                System.err.println("Format:\n" +
                        "####Config file start####\n" +
                        "fileAnalyzer=#\n" +
                        "fileDownloader=#\n" +
                        "urlAnalyzer=#\n" +
                        "queueSize=#\n" +
                        "####Config file end####");

                System.exit(-1);
            }
            input = new FileInputStream(configFile);

            prop.load(input);

            int queueSize =  Integer.parseInt(prop.getProperty("queueSize"));

            // Monitor
            MonitorStats stats = new MonitorStats();
            stats.initMonitorStats();
            BlockingQueue<MonitorMessage> monitorQueue = new ArrayBlockingQueue<MonitorMessage>(queueSize);
            MonitorWriter writer = new MonitorWriter(stats);
            writer.initializeMonitor(prop.getProperty("logFile"), Integer.parseInt(prop.getProperty("logInterval")),Integer.parseInt(prop.getProperty("flushInterval")));
            (new Thread(writer)).start();
            MonitorFetcher fetcher = new MonitorFetcher(stats, monitorQueue);
            (new Thread(fetcher)).start();

            // Url Logger
            BlockingQueue<String> urlLoggerQueue = new ArrayBlockingQueue<String>(queueSize);
            UrlLogger urlLogger = new UrlLogger(urlLoggerQueue);
            urlLogger.initializeLogger(prop.getProperty("urlLogFile"));
            new Thread(urlLogger).start();

            // Crawler Queues
            BlockingQueue<UrlMessage> urlToAnalyzeQueue = new ArrayBlockingQueue<UrlMessage>(queueSize);
            BlockingQueue<Pair<String, UrlMessage>> fileToAnalyzedQueue = new ArrayBlockingQueue<Pair<String, UrlMessage>>(queueSize);
            BlockingQueue<UrlMessage> urlToDownloadQueue = new ArrayBlockingQueue<UrlMessage>(queueSize);

            ConcurrentHashMap<String,Boolean> hashMap = new ConcurrentHashMap<String, Boolean>();

            // Crawler worers
            int numberOfFileAnalyzer = Integer.parseInt(prop.getProperty("fileAnalyzer"));
            for (int i = 0; i < numberOfFileAnalyzer; ++i) {
                (new Thread(new FileAnalyzer(i, fileToAnalyzedQueue, urlToAnalyzeQueue, monitorQueue))).start();
            }

            int maxIterations = Integer.parseInt(prop.getProperty("iterations"));
            int numberOfUrlAnalyzer = Integer.parseInt(prop.getProperty("fileDownloader"));
            for (int i = 0; i < numberOfUrlAnalyzer; ++i) {
                (new Thread(new FileDownloader(i, maxIterations, urlToDownloadQueue,fileToAnalyzedQueue, monitorQueue))).start();
            }

            int numberOfFileDownloader = Integer.parseInt(prop.getProperty("urlAnalyzer"));
            for (int i = 0; i < numberOfFileDownloader; ++i) {
                (new Thread(new UrlAnalyzer(i, urlToAnalyzeQueue,urlToDownloadQueue,hashMap, monitorQueue, urlLoggerQueue))).start();
            }


            while(! Thread.interrupted()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Enter Url:");
                String src = br.readLine();

                try {
                    URI u = new URI(src);
                    if (u.isAbsolute()) {
                        urlToAnalyzeQueue.put(new UrlMessage(0,src));
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            }

            System.out.println("Finishing web crawler");

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
