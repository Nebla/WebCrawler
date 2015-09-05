import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by adrian on 04/09/15.
 */
public class FileAnalyzer implements Runnable {

    private BlockingQueue<String> analizedUrlQueue;

    public FileAnalyzer(BlockingQueue<String> urlQueue) {
        analizedUrlQueue = urlQueue;
    }

    public void run() {
        System.out.println("Hello from a thread!");
        try {
            analyzeFile("Downloads/Clarín.html", "http://www.clarin.com");
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        //analyzeFile("Downloads/www.clarin.com","www.clarin.com");
    }

    /*public static void main(String args[]) {
        (new Thread(new FileAnalyzer())).start();
    }*/

    /*public static void main(String[] args) {

        System.out.println("Hello World!");

        analyzeFile("Downloads/www.clarin.com","www.clarin.com");
    }*/


    public void analyzeFile (String fileName, String url) throws InterruptedException {

        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);


        HTMLEditorKit htmlKit = new HTMLEditorKit();
        HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
        HTMLEditorKit.Parser parser = new ParserDelegator();
        HTMLEditorKit.ParserCallback callback = htmlDoc.getReader(0);

        try {
            parser.parse(br, callback, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (HTMLDocument.Iterator iterator = htmlDoc.getIterator(HTML.Tag.IMG); iterator.isValid(); iterator.next()) {
            AttributeSet attributes = iterator.getAttributes();
            String imgSrc = (String) attributes.getAttribute(HTML.Attribute.SRC);

            if (imgSrc != null && (imgSrc.endsWith(".jpg") || (imgSrc.endsWith(".png")) || (imgSrc.endsWith(".jpeg")) || (imgSrc.endsWith(".bmp")) || (imgSrc.endsWith(".ico")))) {

                URI u = null;
                try {
                    u = new URI(imgSrc);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                String finalImgUrl = null;
                if(u.isAbsolute()) {
                    finalImgUrl = imgSrc;
                }
                else {
                    finalImgUrl = url+ "/" + u.normalize().toString();
                }

                System.out.println(finalImgUrl);
                analizedUrlQueue.put(finalImgUrl);
            }
        }

        for (HTMLDocument.Iterator iterator = htmlDoc.getIterator(HTML.Tag.A); iterator.isValid(); iterator.next()) {
            AttributeSet attributes = iterator.getAttributes();
            String linkSrc = (String) attributes.getAttribute(HTML.Attribute.HREF);

            if (linkSrc != null) {
                // Check if it starts with a / is a relative address

                URI u = null;
                try {
                    u = new URI(linkSrc);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                String finalLinkUrl = null;
                if(u.isAbsolute()) {
                    finalLinkUrl = linkSrc;
                }
                else {
                    finalLinkUrl = url+ "/" + u.normalize().toString();
                }

                System.out.println(finalLinkUrl);
                analizedUrlQueue.put(finalLinkUrl);
            }
        }
    }
}
