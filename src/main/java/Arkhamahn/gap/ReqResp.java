package Arkhamahn.gap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;

/**
 * Wrapper around a ZAP {@link HttpMessage} providing the request/response details that GAP needs.
 * Ported from the {@code ReqResp} class in GAP.py.
 */
public class ReqResp {

    private final HttpMessage httpMessage;
    private String requestUrl = "";
    private List<String> requestParams = new ArrayList<>();
    private String requestBody = "";
    private String responseHeaders = "";
    private String responseBody = "";
    private String responseMIMEType = "";
    private String responseContentType = "";

    public ReqResp(HttpMessage httpMessage) {
        this.httpMessage = httpMessage;
        init();
    }

    private void init() {
        try {
            HttpRequestHeader reqHeader = httpMessage.getRequestHeader();
            if (reqHeader != null && reqHeader.getURI() != null) {
                String url = reqHeader.getURI().toString();
                url = removeStdPort(url);
                this.requestUrl = url;
                if (httpMessage.getRequestBody() != null) {
                    this.requestBody = httpMessage.getRequestBody().toString();
                }
            }
        } catch (Exception e) {
            GapEngine.debug(e);
        }

        try {
            HttpResponseHeader respHeader = httpMessage.getResponseHeader();
            if (respHeader != null) {
                String responseString = respHeader.toString() + "\n\n";
                this.responseHeaders = responseString;
                if (httpMessage.getResponseBody() != null) {
                    this.responseBody = httpMessage.getResponseBody().toString();
                }
                try {
                    String contentType = respHeader.getHeader("Content-Type");
                    if (contentType != null) {
                        contentType = contentType.strip().split(";")[0];
                        this.responseContentType = contentType;
                        // Extract just the MIME subtype (e.g. "JSON" from "application/json")
                        String mime = contentType.toUpperCase();
                        if (mime.contains("/")) {
                            mime = mime.substring(mime.indexOf("/") + 1);
                        }
                        this.responseMIMEType = mime;
                    } else {
                        this.responseContentType = "";
                        this.responseMIMEType = "";
                    }
                } catch (Exception e) {
                    this.responseContentType = "";
                    this.responseMIMEType = "";
                }
            }
        } catch (Exception e) {
            GapEngine.debug(e);
        }
    }

    public static String removeStdPort(String url) {
        try {
            if (url.contains(":443")) {
                if (url.startsWith("https:") && GapConstants.REGEX_PORT443.matcher(url).find()) {
                    url = GapConstants.REGEX_PORTSUB443.matcher(url).replaceFirst("");
                }
            } else if (url.contains(":80")) {
                if (url.startsWith("http:") && GapConstants.REGEX_PORT80.matcher(url).find()) {
                    url = GapConstants.REGEX_PORTSUB80.matcher(url).replaceFirst("");
                }
            }
        } catch (Exception e) {
            GapEngine.debug(e);
        }
        return url;
    }

    public boolean isRequest() {
        return httpMessage.getRequestHeader() != null
                && httpMessage.getRequestHeader().getURI() != null;
    }

    public boolean isResponse() {
        return httpMessage.getResponseHeader() != null
                && httpMessage.getResponseHeader().getStatusCode() != 0;
    }

    public HttpMessage getMessage() {
        return httpMessage;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public List<String> getRequestParams() {
        return requestParams;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public String getResponseMIMEType() {
        return responseMIMEType;
    }

    public HistoryReference getHistoryReference() {
        return httpMessage.getHistoryRef();
    }
}