package org.zaproxy.addon.gap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

/** Verifies ReqResp parsing of the wrapped HttpMessage. */
class ReqRespTest {

    private static HttpMessage msg(String url) throws Exception {
        return new HttpMessage(new URI(url));
    }

    @Test
    void removeStdPortHttp() {
        assertEquals("http://example.com/x", ReqResp.removeStdPort("http://example.com:80/x"));
        assertEquals("http://example.com:8080/x", ReqResp.removeStdPort("http://example.com:8080/x"));
    }

    @Test
    void removeStdPortHttps() {
        assertEquals("https://example.com/x", ReqResp.removeStdPort("https://example.com:443/x"));
        assertEquals("https://example.com:8443/x", ReqResp.removeStdPort("https://example.com:8443/x"));
    }

    @Test
    void requestUrlAndBody() throws Exception {
        HttpMessage m = msg("http://example.com/a?x=1");
        m.setRequestBody("foo=bar");
        ReqResp rr = new ReqResp(m);
        assertEquals("http://example.com/a?x=1", rr.getRequestUrl());
        assertEquals("foo=bar", rr.getRequestBody());
    }

    @Test
    void responseContentTypeAndMime() throws Exception {
        HttpMessage m = msg("http://example.com/");
        m.setResponseHeader("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\n\r\n");
        m.setResponseBody("<html></html>");
        ReqResp rr = new ReqResp(m);
        assertEquals("text/html", rr.getResponseContentType());
        assertEquals("HTML", rr.getResponseMIMEType());
    }

    @Test
    void responseWithoutContentType() throws Exception {
        HttpMessage m = msg("http://example.com/");
        m.setResponseHeader("HTTP/1.1 204 No Content\r\n\r\n");
        ReqResp rr = new ReqResp(m);
        assertEquals("", rr.getResponseContentType());
        assertEquals("", rr.getResponseMIMEType());
    }

    @Test
    void isRequestAndIsResponse() throws Exception {
        HttpMessage m = msg("http://example.com/");
        m.setResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        ReqResp rr = new ReqResp(m);
        assertTrue(rr.isRequest());
        assertTrue(rr.isResponse());

        HttpMessage reqOnly = msg("http://example.com/");
        ReqResp rr2 = new ReqResp(reqOnly);
        assertTrue(rr2.isRequest());
        assertFalse(rr2.isResponse());
    }

    @Test
    void removeStdPortLeavesPathAndQuery() {
        assertEquals(
                "https://example.com/path?q=1&r=2",
                ReqResp.removeStdPort("https://example.com:443/path?q=1&r=2"));
    }
}