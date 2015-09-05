import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.BlockingQueue;

public class FileDownloader implements Runnable {

    private BlockingQueue<String> analizedUrlQueue;

    public FileDownloader(BlockingQueue<String> urlQueue) {
        analizedUrlQueue = urlQueue;
    }

    public void run() {

        while (true) {
            String url = null;
            try {
                url = analizedUrlQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (url != null) {
                System.out.println("Downloading "+url);
                fileDownload(url, "Downloads");
            }
            else {
                break;
            }
        }

    }

    /*public static void main(String[] args) {

        System.out.println("Hello World!");

        fileDownload("http://www.clarin.com", "Downloads");
    }*/

    private void fileUrl(String fAddress, String localFileName, String destinationDir) {

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
            connection = (HttpURLConnection)url.openConnection();

            if (connection.getResponseCode() == 200) {
                inStream = connection.getInputStream();

                buf = new byte[512];
                while ((byteRead = inStream.read(buf)) != -1) {
                    outStream.write(buf, 0, byteRead);
                    byteWritten += byteRead;
                }
                System.out.println("Downloaded Successfully.");
                System.out.println("File name:\"" + localFileName + "\"\nNo ofbytes :" + byteWritten);
            }
        } catch (Exception e) {
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

    private void fileDownload(String fAddress, String destinationDir) {
        int slashIndex = fAddress.lastIndexOf('/');
        int periodIndex = fAddress.lastIndexOf('.');

        String fileName = fAddress.substring(slashIndex + 1);

        if (periodIndex >= 1 && slashIndex >= 0 && slashIndex < fAddress.length() - 1) {
            fileUrl(fAddress, fileName, destinationDir);
        } else {
            System.err.println("path or file name.");
        }
    }
}
