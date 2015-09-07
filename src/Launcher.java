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

            BlockingQueue<String> urlToAnalyzeQueue = new ArrayBlockingQueue<String>(queueSize);
            BlockingQueue<Pair<String, String>> fileToAnalyzedQueue = new ArrayBlockingQueue<Pair<String, String>>(queueSize);
            BlockingQueue<String> urlToDownloadQueue = new ArrayBlockingQueue<String>(queueSize);

            ConcurrentHashMap<String,Boolean> hashMap = new ConcurrentHashMap<String, Boolean>();


            int numberOfFileAnalyzer = Integer.parseInt(prop.getProperty("fileAnalyzer"));
            ExecutorService executor = Executors.newFixedThreadPool(numberOfFileAnalyzer);
            for (int i = 0; i < numberOfFileAnalyzer; ++i) {
                Runnable worker = new FileAnalyzer(fileToAnalyzedQueue, urlToAnalyzeQueue);
                executor.execute(worker);
            }
            executor.shutdown();

            int numberOfUrlAnalyzer = Integer.parseInt(prop.getProperty("urlAnalyzer"));
            executor = Executors.newFixedThreadPool(numberOfUrlAnalyzer);
            for (int i = 0; i < numberOfUrlAnalyzer; ++i) {
                Runnable worker = new UrlAnalyzer(urlToAnalyzeQueue,urlToDownloadQueue,hashMap);
                executor.execute(worker);
            }
            executor.shutdown();

            int numberOfFileDownloader = Integer.parseInt(prop.getProperty("fileDownloader"));
            executor = Executors.newFixedThreadPool(numberOfFileDownloader);
            for (int i = 0; i < numberOfFileDownloader; ++i) {
                Runnable worker = new FileDownloader(urlToDownloadQueue,fileToAnalyzedQueue);
                executor.execute(worker);
            }
            executor.shutdown();


            //(new Thread(new FileAnalyzer(fileToAnalyzedQueue, urlToAnalyzeQueue))).start();
            //(new Thread(new FileDownloader(urlToDownloadQueue,fileToAnalyzedQueue))).start();
            //(new Thread(new UrlAnalyzer(urlToAnalyzeQueue,urlToDownloadQueue,hashMap))).start();

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
