package Crawler;

import Monitor.MonitorMessage;
import Monitor.ThreadUpdateMessage;

import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adrian on 04/09/15.
 */
public class FileAnalyzer implements Runnable {

    private Integer threadId;
    private BlockingQueue<MonitorMessage> monitorQueue;
    private BlockingQueue<String> urlToAnalyzeQueue;
    private BlockingQueue<Pair<String, String>> fileToAnalyzedQueue;

    public FileAnalyzer(Integer threadId, BlockingQueue<Pair<String, String>> fileQueue, BlockingQueue<String> urlQueue, BlockingQueue<MonitorMessage> monitorQueue) {
        this.threadId = threadId;
        this.monitorQueue = monitorQueue;
        this.urlToAnalyzeQueue = urlQueue;
        this.fileToAnalyzedQueue = fileQueue;
    }

    public void run() {
        try {
            this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.HTML_ANALYZER, this.threadId, ThreadState.Status.STARTING)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!Thread.interrupted()) {
            try {
                this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.HTML_ANALYZER, this.threadId, ThreadState.Status.BLOCKED)));
                Pair<String, String> file = fileToAnalyzedQueue.take();
                this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.HTML_ANALYZER, this.threadId, ThreadState.Status.WORKING)));
                //System.out.println("Analyzing url: "+file.getFirst());
                analyzeFile(file.getFirst(), file.getSecond());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

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

        // Images
        getResource(url, htmlDoc, HTML.Tag.IMG, HTML.Attribute.SRC);

        // Https
        getResource(url, htmlDoc, HTML.Tag.A, HTML.Attribute.HREF);

    }

    private void getResource(String url, HTMLDocument htmlDoc, HTML.Tag tag, HTML.Attribute attribute) throws InterruptedException {
        for (HTMLDocument.Iterator iterator = htmlDoc.getIterator(tag); iterator.isValid(); iterator.next()) {
            AttributeSet attributes = iterator.getAttributes();
            String src = (String) attributes.getAttribute(attribute);

            if (src != null) {

                URI u = null;
                try {
                    u = new URI(src);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                if (u != null) {
                    String finalUrl;
                    if (u.isAbsolute()) {
                        finalUrl = src;
                    } else {
                        finalUrl = url + "/" + u.normalize().toString();
                    }

                    urlToAnalyzeQueue.put(finalUrl);
                }
            }
        }
    }

    /*private void getImages(String url, HTMLDocument htmlDoc) {
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

                //System.out.println(finalImgUrl);
                urlToAnalyzeQueue.put(finalImgUrl);
            }

        }
    }*/

    /*private void getHttps(String url, HTMLDocument htmlDoc) throws InterruptedException {
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

                //System.out.println(finalLinkUrl);
                urlToAnalyzeQueue.put(finalLinkUrl);
            }
        }
    }*/


}
