import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;

public class FileDownloader implements Runnable {

    private BlockingQueue<String> urlToDownloadQueue;

    private BlockingQueue<Pair<String, String>> fileToAnalyzeQueue;

    public FileDownloader(BlockingQueue<String> downloadQueue, BlockingQueue<Pair<String, String>> fileAnalyzeQueue) {
        urlToDownloadQueue = downloadQueue;
        fileToAnalyzeQueue = fileAnalyzeQueue;
    }

    public void run() {
        while (!Thread.interrupted()) {

            try {
                String url = urlToDownloadQueue.take();
                System.out.println("Downloading "+url);
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
            HttpURLConnection connection = null;
            InputStream inStream = null;
            try {
                URL url;
                byte[] buf;
                int byteRead, byteWritten = 0;

                url = new URL(fAddress);

                outStream = new BufferedOutputStream(new FileOutputStream(destinationDir + "/" + localFileName));
                connection = (HttpURLConnection) url.openConnection();

                if (connection.getResponseCode() == 200) {
                    inStream = connection.getInputStream();

                    buf = new byte[512];
                    while ((byteRead = inStream.read(buf)) != -1) {
                        outStream.write(buf, 0, byteRead);
                        byteWritten += byteRead;
                    }

                    String fileType = Files.probeContentType(FileSystems.getDefault().getPath(destinationDir, localFileName));
                    if (fileType.equals("text/html")) {
                        fileToAnalyzeQueue.put(new Pair<String, String>(destinationDir + "/" + localFileName, fAddress));
                    }
                }

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
