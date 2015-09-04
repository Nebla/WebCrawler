import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Main {

    public static void main(String[] args) {

        System.out.println("Hello World!");

        fileDownload("http://docs.oracle.com/javase/7/docs/api/java/io/File.html#mkdirs()", "Downloads");
    }

    public static void fileUrl(String fAddress, String localFileName, String destinationDir) {

        File dir = new File(destinationDir);

        // We create the download directory if it doesn't exists
        if (!dir.exists()) {
            dir.mkdirs();
        }

        OutputStream outStream = null;
        URLConnection connection = null;
        InputStream inStream = null;
        try {
            URL url;
            byte[] buf;
            int byteRead, byteWritten = 0;
            url = new URL(fAddress);
            outStream = new BufferedOutputStream(new FileOutputStream(destinationDir + "/" + localFileName));

            connection = url.openConnection();
            inStream = connection.getInputStream();
            buf = new byte[512];
            while ((byteRead = inStream.read(buf)) != -1) {
                outStream.write(buf, 0, byteRead);
                byteWritten += byteRead;
            }
            System.out.println("Downloaded Successfully.");
            System.out.println("File name:\"" + localFileName + "\"\nNo ofbytes :" + byteWritten);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void fileDownload(String fAddress, String destinationDir) {
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
