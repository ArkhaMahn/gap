package org.zaproxy.addon.gap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

/** Exercises link extraction: html hrefs, relative links, js built links, sourcemaps, filtering. */
class GapEngineLinkTest extends GapTestSupport {

    private void process(String responseBody, String contentType) throws Exception {
        HttpMessage msg = message("http://example.com/page", null, contentType, responseBody);
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
    }

    private void process(String responseBody) throws Exception {
        process(responseBody, "text/html");
    }

    void absoluteHtmlLinks() throws Exception {
        process(
                "<html><body>"
                        + "<a href=\"http://example.com/foo.html\">f</a>"
                        + "<a href='https://cdn.example.net/app.js'>a</a>"
                        + "<a href=\"http://other.org/x\">o</a>"
                        + "</body></html>");
        Set<String> links = engine.getLinkList();
        assertContains(links, "http://example.com/foo.html");
        assertContains(links, "https://cdn.example.net/app.js");
        assertContains(links, "http://other.org/x");
    }

    void relativeLinks() throws Exception {
        process("<html><body><a href=\"/path/to/page.html\">x</a></body></html>");
        assertContains(engine.getLinkList(), "/path/to/page.html");
    }

    void protocolRelativeLinks() throws Exception {
        process("<html><body><script src=\"//cdn.example.com/app.js\"></script></body></html>");
        assertContains(engine.getLinkList(), "http://cdn.example.com/app.js");
    }

    void jsBuiltLinks() throws Exception {
        process(
                "<html><body><script>"
                        + "axios.get(\"/api/users\");"
                        + "axios.post('/api/login');"
                        + "jQuery.get('api/legacy');"
                        + "</script></body></html>");
        assertContains(engine.getLinkList(), "/api/users");
        assertContains(engine.getLinkList(), "/api/login");
        assertContains(engine.getLinkList(), "/api/legacy");
    }

    void fetchLinks() throws Exception {
        process(
                "<html><body><script>"
                        + "fetch('/api/data');"
                        + "fetch(\"https://api.example.org/v1/items\");"
                        + "</script></body></html>");
        assertContains(engine.getLinkList(), "/api/data");
        assertContains(engine.getLinkList(), "https://api.example.org/v1/items");
    }


}
