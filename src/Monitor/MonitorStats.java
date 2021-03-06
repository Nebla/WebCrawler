package Monitor;

import Crawler.ThreadState;

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

    public synchronized void initMonitorStats (Integer urlAnalyzers, Integer fileAnalyzers, Integer fileDownloaders) {
        // I synchronize this block to be consistent, in that every public method should be atomic, but it's not necessary
        this.initializeStats(fileAnalyzerState, fileAnalyzers);
        this.initializeStats(fileDownloaderState, fileDownloaders);
        this.initializeStats(urlAnalyzerState, urlAnalyzers);

        urlsCount = 0;
    }

    private void initializeStats(HashMap<Integer, ThreadState.Status> map, Integer amount) {
        for (Integer i = 0; i < amount; ++i) {
            map.put(i, ThreadState.Status.UNKNOWN);
        }
    }

    public synchronized void setStatus(ThreadState state) {
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

    public synchronized void increaseAnalyzedUrl() {
        urlsCount++;
    }

    public synchronized void increaseFileType(String fileType) {
        if (fileTypes.containsKey(fileType)) {
            fileTypes.put(fileType, fileTypes.get(fileType) + 1);
        } else {
            fileTypes.put(fileType, 1);
        }
    }

    public synchronized String getFormattedStats () {
        String result;
            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String stringDate = formatter.format(new Date());

            result = stringDate + " - Analyzed URLS: " + urlsCount + "\n";
            result = result + "Downloaded files by type: \n" + this.getFileTypesStats() + "\n";
            result = result + this.getThreadStats("Analyzing URL",urlAnalyzerState);
            result = result + this.getThreadStats("Downloading File", fileDownloaderState);
            result = result + this.getThreadStats("Analyzing File",fileAnalyzerState);

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
        String threadStats;
        Integer working = 0;
        Integer blocked = 0;
        Integer unknown = 0;
        Integer starting = 0;

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
            }
        }

        threadStats = "\t" + threadName + "\n\t\t";
        threadStats = threadStats + "Unknown: " + unknown;
        threadStats = threadStats + " Starting: " + starting;
        threadStats = threadStats + " Blocked: " + blocked;
        threadStats = threadStats + " Working: " + working + "\n";
        return threadStats;
    }
}
