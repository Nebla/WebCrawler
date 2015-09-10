package Crawler;

import Monitor.FileTypeUpdateMessage;
import Monitor.MonitorMessage;
import Monitor.ThreadUpdateMessage;

import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;

public class FileDownloader implements Runnable {

    private Integer threadId;

    private BlockingQueue<MonitorMessage> monitorQueue;

    private BlockingQueue<String> urlToDownloadQueue;

    private BlockingQueue<Pair<String, String>> fileToAnalyzeQueue;

    public FileDownloader(Integer threadId, BlockingQueue<String> downloadQueue, BlockingQueue<Pair<String, String>> fileAnalyzeQueue, BlockingQueue<MonitorMessage> monitorQueue) {
        this.threadId = threadId;
        this.monitorQueue = monitorQueue;
        this.urlToDownloadQueue = downloadQueue;
        this.fileToAnalyzeQueue = fileAnalyzeQueue;
    }

    public void run() {

        try {
            this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.FILE_DOWNLOADER, this.threadId, ThreadState.Status.STARTING)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!Thread.interrupted()) {

            try {
                this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.FILE_DOWNLOADER, this.threadId, ThreadState.Status.BLOCKED)));
                String url = urlToDownloadQueue.take();
                this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.FILE_DOWNLOADER, this.threadId, ThreadState.Status.WORKING)));
                downloadFile(url, "Downloads");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadFile(String fAddress, String destinationDir) throws InterruptedException {
        int slashIndex = fAddress.lastIndexOf('/');

        String localFileName = fAddress.substring(slashIndex + 1);

        if (localFileName.length() > 0) {
            File dir = new File(destinationDir);

            // We create the download directory if it doesn't exists
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream outStream = null;
            HttpURLConnection connection;
            InputStream inStream = null;
            try {
                URL url;
                byte[] buf;
                int byteRead = 0;
                url = new URL(fAddress);
                connection = (HttpURLConnection) url.openConnection();

                outStream = new BufferedOutputStream(new FileOutputStream(destinationDir + "/" + localFileName));
                inStream = connection.getInputStream();

                buf = new byte[512];
                while ((byteRead = inStream.read(buf)) != -1) {
                    outStream.write(buf, 0, byteRead);
                }

                String fileType = Files.probeContentType(FileSystems.getDefault().getPath(destinationDir, localFileName));
                if (fileType.equals("text/html")) {
                    fileToAnalyzeQueue.put(new Pair<String, String>(destinationDir + "/" + localFileName, fAddress));
                }

                this.monitorQueue.put(new FileTypeUpdateMessage(fileType));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
