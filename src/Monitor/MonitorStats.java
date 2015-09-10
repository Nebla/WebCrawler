package Monitor;

import Crawler.ThreadState;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by adrian on 09/09/15.
 */
public class MonitorStats {

    private Integer urlsCount;

    private HashMap<String, Integer> fileTypes;

    private HashMap<Integer, ThreadState.Status> urlAnalyzerState;
    private HashMap<Integer, ThreadState.Status> fileAnalyzerState;
    private HashMap<Integer, ThreadState.Status> fileDownloaderState;

    public MonitorStats() {
        urlsCount = 0;
        fileTypes = new HashMap<String, Integer>();
        urlAnalyzerState = new HashMap<Integer, ThreadState.Status>();
        fileAnalyzerState = new HashMap<Integer, ThreadState.Status>();
        fileDownloaderState = new HashMap<Integer, ThreadState.Status>();
    }

    public void initMonitorStats () {
        synchronized (this) {
            // I synchronize this block to be consistent, in that every public method should be atomic, but it's not neccesary
            String configFile = "Config/Config.properties";
            File f = new File(configFile);
            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(configFile);
                prop.load(input);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.initializeStats(fileAnalyzerState, Integer.parseInt(prop.getProperty("fileAnalyzer")));
            this.initializeStats(fileDownloaderState, Integer.parseInt(prop.getProperty("fileDownloader")));
            this.initializeStats(urlAnalyzerState, Integer.parseInt(prop.getProperty("urlAnalyzer")));

            urlsCount = 0;
        }
    }

    private void initializeStats(HashMap<Integer, ThreadState.Status> map, Integer amount) {
        for (Integer i = 0; i < amount; ++i) {
            map.put(i, ThreadState.Status.UNKNOWN);
        }
    }

    public void setStatus(ThreadState state) {
        synchronized (this) {
            HashMap<Integer, ThreadState.Status> map = null;
            switch (state.getThreadName()) {
                case URL_ANALYZER:
                    map = urlAnalyzerState;
                    break;
                case HTML_ANALYZER:
                    map = fileAnalyzerState;
                    break;
                case FILE_DOWNLOADER:
                    map = fileDownloaderState;
                    break;
            }
            map.put(state.getThreadId(), state.getThreadStatus());
        }
    }

    public void increaseAnalyzedUrl() {
        synchronized (this) {
            urlsCount++;
        }
    }

    public void increaseFileType(String fileType) {
        synchronized (this) {
            if (fileTypes.containsKey(fileType)) {
                fileTypes.put(fileType, fileTypes.get(fileType) + 1);
            }
            else {
                fileTypes.put(fileType, 1);
            }
        }
    }

    public String getFormattedStats () {
        String result = "";
        synchronized (this) {
            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String stringDate = formatter.format(new Date());

            result = stringDate + " - Analyzed URLS: " + urlsCount + "\n";
            result = result + "Downloaded files bt type: \n" + this.getFileTypesStats();
            result = result + this.getThreadStats("Analyzing URL",urlAnalyzerState);
            result = result + this.getThreadStats("Downloading File", fileDownloaderState);
            result = result + this.getThreadStats("Analyzing File",fileAnalyzerState);
        }
        return result;
    }

    private String getFileTypesStats () {
        String filesStats = "";
        for (Map.Entry pair : fileTypes.entrySet()) {
            filesStats = filesStats + "\t" + pair.getKey() + ":" + pair.getValue();
        }
        return filesStats;
    }

    private String getThreadStats (String threadName, HashMap<Integer, ThreadState.Status> map) {
        String threadStats = "";
        Integer working = 0;
        Integer blocked = 0;
        Integer unknown = 0;
        Integer starting = 0;
        Integer finished = 0;

        for (Map.Entry pair : map.entrySet()) {
            ThreadState.Status status = (ThreadState.Status) pair.getValue();
            switch (status) {
                case UNKNOWN:
                    unknown++;
                    break;
                case STARTING:
                    starting++;
                    break;
                case BLOCKED:
                    blocked++;
                    break;
                case WORKING:
                    working++;
                    break;
                case FINISHED:
                    finished++;
                    break;
            }
        }

        threadStats = "\t" + threadName + "\n\t\t";
        threadStats = threadStats + "Unknown: " + unknown;
        threadStats = threadStats + " Starting: " + starting;
        threadStats = threadStats + " Blocked: " + blocked;
        threadStats = threadStats + " Working: " + working;
        threadStats = threadStats + " Finished: " + finished;
        return threadStats;
    }
}
