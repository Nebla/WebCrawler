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
    private Integer iterations;
    private BlockingQueue<MonitorMessage> monitorQueue;
    private BlockingQueue<UrlMessage> urlToDownloadQueue;
    private BlockingQueue<Pair<String, UrlMessage>> fileToAnalyzeQueue;

    public FileDownloader(Integer threadId, Integer iterations, BlockingQueue<UrlMessage> downloadQueue, BlockingQueue<Pair<String, UrlMessage>> fileAnalyzeQueue, BlockingQueue<MonitorMessage> monitorQueue) {
        this.threadId = threadId;
        this.iterations = iterations;
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
                UrlMessage url = urlToDownloadQueue.take();
                this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.FILE_DOWNLOADER, this.threadId, ThreadState.Status.WORKING)));
                downloadFile(url, "Downloads");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadFile(UrlMessage message, String destinationDir) throws InterruptedException {
        String localFileName;
        int slashIndex = message.getUrl().lastIndexOf('/');
        // We should remode tha last / from the url because we use the string next to the slash to use as file name
        if (slashIndex == message.getUrl().length() - 1) {
            String noLastSlashUrl = message.getUrl().substring(0,message.getUrl().length() - 1);
            slashIndex = noLastSlashUrl.lastIndexOf('/');
            localFileName = noLastSlashUrl.substring(slashIndex + 1);
        } else {
            localFileName = message.getUrl().substring(slashIndex + 1);
        }


        if (localFileName.length() > 0) {
            File dir = new File(destinationDir);

            // We create the download directory if it doesn't exists
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream outStream = null;
            URLConnection connection;
            InputStream inStream = null;
            try {
                URL url;
                byte[] buf;
                int byteRead;
                url = new URL(message.getUrl());
                connection = url.openConnection();

                outStream = new BufferedOutputStream(new FileOutputStream(destinationDir + "/" + localFileName));
                inStream = connection.getInputStream();

                buf = new byte[1024];
                while ((byteRead = inStream.read(buf)) != -1) {
                    outStream.write(buf, 0, byteRead);
                }

                String fileType = Files.probeContentType(FileSystems.getDefault().getPath(destinationDir, localFileName));
                if ((message.getIteration() < this.iterations-1) && fileType.equals("text/html")) {
                    fileToAnalyzeQueue.put(new Pair<String, UrlMessage>(destinationDir + "/" + localFileName, new UrlMessage(message.getIteration()+1,message.getUrl()) ));
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
