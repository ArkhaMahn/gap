package org.zaproxy.addon.gap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

/** Exercises every request/response parameter source: url, body, multipart, json, cookie, xml. */
class GapEngineParamTest extends GapTestSupport {

    private ReqResp rr(HttpMessage msg) {
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        return rr;
    }

    @Test
    void urlQueryParams() throws Exception {
        HttpMessage msg = message("http://example.com/search?q=gap&page=2&sort=asc");
        rr(msg);
        engine.getBurpParams(new ReqResp(msg));
        assertContains(engine.getParamList(), "q");
        assertContains(engine.getParamList(), "page");
        assertContains(engine.getParamList(), "sort");
    }

    @Test
    void urlQueryParamsDisabledByCheckbox() throws Exception {
        param.setParamUrl(false);
        HttpMessage msg = message("http://example.com/search?q=gap");
        rr(msg);
        engine.getBurpParams(new ReqResp(msg));
        assertFalse(engine.getParamList().contains("q"));
    }

    @Test
    void formBodyParams() throws Exception {
        HttpMessage msg =
                message("http://example.com/login", "user=bob&pass=secret&remember=on", null, null);
        msg.getRequestHeader().setHeader(
                org.parosproxy.paros.network.HttpHeader.CONTENT_TYPE,
                org.parosproxy.paros.network.HttpHeader.FORM_URLENCODED_CONTENT_TYPE);
        rr(msg);
        engine.getBurpParams(new ReqResp(msg));
        assertContains(engine.getParamList(), "user");
        assertContains(engine.getParamList(), "pass");
        assertContains(engine.getParamList(), "remember");
    }

    @Test
    void multipartParams() throws Exception {
        String boundary = "AaB03x";
        String body =
                "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"field1\"\r\n\r\n"
                        + "value1\r\n"
                        + "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"field2\"\r\n\r\n"
                        + "value2\r\n"
                        + "--" + boundary + "--\r\n";
        HttpMessage msg =
                message(
                        "http://example.com/upload",
                        body,
                        "multipart/form-data; boundary=" + boundary,
                        null);
        rr(msg);
        engine.getBurpParams(new ReqResp(msg));
        assertContains(engine.getParamList(), "field1");
        assertContains(engine.getParamList(), "field2");
    }

    @Test
    void jsonBodyParams() throws Exception {
        param.setParamJson(true);
        HttpMessage msg =
                message(
                        "http://example.com/api",
                        "{\"username\":\"bob\",\"password\":\"x\",\"nested\":{\"id\":7}}",
                        null,
                        null);
        rr(msg);
        engine.getBurpParams(new ReqResp(msg));
        assertContains(engine.getParamList(), "username");
        assertContains(engine.getParamList(), "password");
    }

    @Test
    void jsonRequestParamsFoundWithoutBurpFlag() throws Exception {
        // getRequestParams always scans the request body for JSON
        HttpMessage msg =
                message(
                        "http://example.com/api",
                        "{\"username\":\"bob\",\"token\":\"abc\"}",
                        null,
                        null);
        rr(msg);
        engine.getRequestParams(new ReqResp(msg));
        assertContains(engine.getParamList(), "username");
        assertContains(engine.getParamList(), "token");
    }

    @Test
    void cookieParams() throws Exception {
        param.setParamCookie(true);
        HttpMessage msg = message("http://example.com/");
        msg.getRequestHeader().setHeader("Cookie", "session=abc123; theme=dark; user=bob");
        rr(msg);
        engine.getBurpParams(new ReqResp(msg));
        assertContains(engine.getParamList(), "session");
        assertContains(engine.getParamList(), "theme");
        assertContains(engine.getParamList(), "user");
    }

    @Test
    void xmlDataParams() throws Exception {
        param.setParamXml(true);
        HttpMessage msg =
                message(
                        "http://example.com/api",
                        "<user><name>Bob</name><age>30</age><role>admin</role></user>",
                        null,
                        null);
        rr(msg);
        engine.getBurpParams(new ReqResp(msg));
        assertContains(engine.getParamList(), "name");
        assertContains(engine.getParamList(), "age");
        assertContains(engine.getParamList(), "role");
    }

    @Test
    void xmlAttrParams() throws Exception {
        param.setParamXmlAttr(true);
        HttpMessage msg =
                message("http://example.com/api", "<item><title>Hello</title></item>", null, null);
        rr(msg);
        engine.getBurpParams(new ReqResp(msg));
        assertContains(engine.getParamList(), "title");
    }

    @Test
    void responseJsonKeys() throws Exception {
        param.setParamJSONResponse(true);
        HttpMessage msg =
                message(
                        "http://example.com/api",
                        null,
                        "application/json",
                        "{\"id\":1,\"name\":\"Bob\",\"items\":[{\"sku\":\"A1\"}]}");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
        assertContains(engine.getParamList(), "id");
        assertContains(engine.getParamList(), "name");
        assertContains(engine.getParamList(), "sku");
    }

    @Test
    void responseXmlAttrParams() throws Exception {
        param.setParamXMLResponse(true);
        HttpMessage msg =
                message(
                        "http://example.com/data",
                        null,
                        "application/xml",
                        "<root><book>GAP</book><chapter>One</chapter></root>");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
        assertContains(engine.getParamList(), "book");
        assertContains(engine.getParamList(), "chapter");
    }

    @Test
    void responseInputFieldParams() throws Exception {
        param.setParamInputField(true);
        HttpMessage msg =
                message(
                        "http://example.com/form",
                        null,
                        "text/html",
                        "<input type=\"text\" name=\"csrf_token\" id=\"form_token\">"
                                + "<textarea name=\"notes\"></textarea>");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
        assertContains(engine.getParamList(), "csrf_token");
        assertContains(engine.getParamList(), "form_token");
        assertContains(engine.getParamList(), "notes");
    }

    @Test
    void responseJsVars() throws Exception {
        param.setParamJSVars(true);
        HttpMessage msg =
                message(
                        "http://example.com/app",
                        null,
                        "text/html",
                        "<script>var config = {key:\"val\"}; let name = \"x\"; const max = 10;"
                                + " var sessionId = \"abc\";</script>");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
        assertContains(engine.getParamList(), "config");
        assertContains(engine.getParamList(), "name");
        assertContains(engine.getParamList(), "max");
        assertContains(engine.getParamList(), "sessionId");
    }

    @Test
    void pathWordsAddedAsParamsWhenEnabled() throws Exception {
        param.setIncludePathWords(true);
        HttpMessage msg =
                message(
                        "http://example.com/admin/users/edit/42",
                        null,
                        "text/html",
                        "<html><body>hi</body></html>");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
        assertContains(engine.getParamList(), "admin");
        assertContains(engine.getParamList(), "users");
        assertContains(engine.getParamList(), "edit");
    }

    @Test
    void paramsFromLinks() throws Exception {
        param.setParamFromLinks(true);
        HttpMessage msg =
                message(
                        "http://example.com/",
                        null,
                        "text/html",
                        "<html><body><a href=\"/page?utm_source=x&ref=home\">link</a></body></html>");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
        assertContains(engine.getParamList(), "utm_source");
        assertContains(engine.getParamList(), "ref");
    }

    @Test
    void nonAsciiJsonParamDroppedLikeGap() throws Exception {
        // GAP.py url-encodes non-ASCII names which then fail REGEX_PARAM and are dropped (parity)
        HttpMessage msg =
                message(
                        "http://example.com/api",
                        "{\"caf\u00e9\":\"x\"}",
                        null,
                        null);
        rr(msg);
        engine.getRequestParams(new ReqResp(msg));
        assertFalse(
                engine.getParamList().contains("caf%C3%A9"),
                "paramList: " + engine.getParamList());
        assertFalse(engine.getParamList().contains("caf\u00e9"));
    }

    @Test
    void parameterOriginRecorded() throws Exception {
        HttpMessage msg = message("http://example.com/a?q=1");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.getBurpParams(rr);
        Set<String> origins = new HashSet<>();
        for (String entry : engine.getParamUrlList()) {
            origins.add(entry);
        }
        assertTrue(
                origins.stream().anyMatch(e -> e.startsWith("q  [http://example.com/a?q=1]")),
                "origin url missing: " + origins);
    }
}