import Crawler.FileAnalyzer;
import Crawler.FileDownloader;
import Crawler.Pair;
import Crawler.UrlAnalyzer;
import Monitor.MonitorMessage;

import java.io.*;
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

            System.out.println(prop.getProperty("fileAnalyzer"));
            System.out.println(prop.getProperty("fileDownloader"));
            System.out.println(prop.getProperty("urlAnalyzer"));

            int queueSize =  Integer.parseInt(prop.getProperty("queueSize"));

            BlockingQueue<MonitorMessage> monitorQueue = new ArrayBlockingQueue<MonitorMessage>(queueSize);




            BlockingQueue<String> urlToAnalyzeQueue = new ArrayBlockingQueue<String>(queueSize);
            BlockingQueue<Pair<String, String>> fileToAnalyzedQueue = new ArrayBlockingQueue<Pair<String, String>>(queueSize);
            BlockingQueue<String> urlToDownloadQueue = new ArrayBlockingQueue<String>(queueSize);

            ConcurrentHashMap<String,Boolean> hashMap = new ConcurrentHashMap<String, Boolean>();

            int numberOfFileAnalyzer = Integer.parseInt(prop.getProperty("fileAnalyzer"));
            for (int i = 0; i < numberOfFileAnalyzer; ++i) {
                (new Thread(new FileAnalyzer(i, fileToAnalyzedQueue, urlToAnalyzeQueue))).start();
            }

            int numberOfUrlAnalyzer = Integer.parseInt(prop.getProperty("urlAnalyzer"));
            for (int i = 0; i < numberOfUrlAnalyzer; ++i) {
                (new Thread(new FileDownloader(urlToDownloadQueue,fileToAnalyzedQueue))).start();
            }

            int numberOfFileDownloader = Integer.parseInt(prop.getProperty("fileDownloader"));
            for (int i = 0; i < numberOfFileDownloader; ++i) {
                (new Thread(new UrlAnalyzer(urlToAnalyzeQueue,urlToDownloadQueue,hashMap))).start();
            }

            //(new Thread(new Crawler.FileAnalyzer(fileToAnalyzedQueue, urlToAnalyzeQueue))).start();
            //(new Thread(new Crawler.FileDownloader(urlToDownloadQueue,fileToAnalyzedQueue))).start();
            //(new Thread(new Crawler.UrlAnalyzer(urlToAnalyzeQueue,urlToDownloadQueue,hashMap))).start();

            while(! Thread.interrupted()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Enter Url:");
                String s = br.readLine();
                urlToAnalyzeQueue.put(s);
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
