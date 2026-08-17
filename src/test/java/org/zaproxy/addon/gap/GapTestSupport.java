package org.zaproxy.addon.gap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Shared helpers for the GAP tests: builds {@link GapParam}, {@link GapEngine} and synthetic
 * {@link HttpMessage}s (request + response) without needing a live ZAP session.
 */
public abstract class GapTestSupport {

    protected GapParam param;
    protected TestContext context;
    protected GapEngine engine;
    protected PrintWriter out;

    @BeforeEach
    void setUp() {
        param = new GapParam();
        param.parse();
        context = new TestContext();
        out = new PrintWriter(new java.io.StringWriter(), true);
        engine = new GapEngine(context, param, out);
    }

    static final class TestContext implements GapContext {
        final Set<String> issues = new HashSet<>();
        boolean inScope = true;

        @Override
        public boolean isInScope(String url) {
            return inScope;
        }

        @Override
        public void createIssue(HttpMessage httpMessage, String issueDetail, String confidence) {
            issues.add(issueDetail + " :: " + confidence);
        }
    }

    protected static HttpMessage message(String requestUrl) throws Exception {
        return new HttpMessage(new URI(requestUrl));
    }

    protected static HttpMessage message(
            String requestUrl, String requestBody, String contentType, String responseBody)
            throws Exception {
        HttpMessage msg = new HttpMessage(new URI(requestUrl));
        if (requestBody != null) {
            msg.setRequestBody(requestBody);
        }
        String header =
                "HTTP/1.1 200 OK\r\n"
                        + (contentType != null && !contentType.isEmpty()
                                ? "Content-Type: " + contentType + "\r\n"
                                : "")
                        + "\r\n";
        msg.setResponseHeader(header);
        if (responseBody != null) {
            msg.setResponseBody(responseBody);
        }
        return msg;
    }

    protected static void assertContains(Set<String> set, String expected) {
        assertTrue(
                set.contains(expected),
                "Expected <" + expected + "> in " + sorted(set));
    }

    protected static void assertNotContains(Set<String> set, String unexpected) {
        assertFalse(
                set.contains(unexpected),
                "Did not expect <" + unexpected + "> in " + sorted(set));
    }

    private static String sorted(Set<String> set) {
        return new java.util.TreeSet<>(set).toString();
    }
}