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


/**
 * Created by adrian on 04/09/15.
 */
public class FileAnalyzer implements Runnable {

    private Integer threadId;
    private BlockingQueue<MonitorMessage> monitorQueue;
    private BlockingQueue<UrlMessage> urlToAnalyzeQueue;
    private BlockingQueue<Pair<String, UrlMessage>> fileToAnalyzedQueue;

    public FileAnalyzer(Integer threadId, BlockingQueue<Pair<String, UrlMessage>> fileQueue, BlockingQueue<UrlMessage> urlQueue, BlockingQueue<MonitorMessage> monitorQueue) {
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
                Pair<String, UrlMessage> file = fileToAnalyzedQueue.take();
                this.monitorQueue.put(new ThreadUpdateMessage(new ThreadState(ThreadState.Type.HTML_ANALYZER, this.threadId, ThreadState.Status.WORKING)));
                //System.out.println("Analyzing url: "+file.getFirst());
                analyzeFile(file.getFirst(), file.getSecond());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /*public  List<String>extractLinks(String filename) throws IOException {
        final ArrayList<String> result = new ArrayList<String>();

        File in = new File(filename);
        Document doc = Jsoup.parse(in, null);

        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");

        // href ...
        for (Element link : links) {
            result.add(link.attr("abs:href"));
        }

        // img ...
        for (Element src : media) {
            result.add(src.attr("abs:src"));
        }

        // js, css, ...
        for (Element link : imports) {
            result.add(link.attr("abs:href"));
        }
        return result;
    }*/

    public void analyzeFile (String fileName, UrlMessage url) throws InterruptedException {
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

        getResource(url, htmlDoc, HTML.Tag.IMG, HTML.Attribute.SRC);
        getResource(url, htmlDoc, HTML.Tag.A, HTML.Attribute.HREF);
        getResource(url, htmlDoc, HTML.Tag.LINK, HTML.Attribute.HREF);
        getResource(url, htmlDoc, HTML.Tag.SCRIPT, HTML.Attribute.SRC);


    }


    private void getResource(UrlMessage url, HTMLDocument htmlDoc, HTML.Tag tag, HTML.Attribute attribute) throws InterruptedException {
        for (HTMLDocument.Iterator iterator = htmlDoc.getIterator(tag); iterator.isValid(); iterator.next()) {
            AttributeSet attributes = iterator.getAttributes();
            String src = (String) attributes.getAttribute(attribute);

            if (src != null) {

                URI u = null;
                try {
                    u = new URI(src);
                    String finalUrl;
                    if (u.isAbsolute()) {
                        finalUrl = src;
                    } else {
                        if (src.startsWith("//")) {
                            int index = url.getUrl().indexOf(":");
                            String protocol = url.getUrl().substring(0,index);
                            finalUrl = protocol + ":" + src;
                        } else {
                            finalUrl = u.resolve(url.getUrl()).normalize().toString();
                        }
                    }

                    urlToAnalyzeQueue.put(new UrlMessage(url.getIteration(), finalUrl));
                }catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

            }
        }
    }

}
