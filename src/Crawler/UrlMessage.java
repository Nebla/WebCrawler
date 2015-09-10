package Crawler;

/**
 * Created by adrian on 10/09/15.
 */
public class UrlMessage {

    private Integer iteration;
    private String url;

    public UrlMessage (Integer iteration, String url) {
        this.iteration = iteration;
        this.url = url;
    }

    public Integer getIteration() {
        return iteration;
    }

    public String getUrl() {
        return url;
    }
}
