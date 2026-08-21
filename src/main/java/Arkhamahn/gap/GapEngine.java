package Arkhamahn.gap;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.Tag;
import net.htmlparser.jericho.TextExtractor;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Core engine for GAP. Holds the message processing, link/param/word extraction and the various
 * lists populated during a run. Ported from {@code BurpExtender} in GAP.py.
 */
public class GapEngine {

    public static void debug(String msg) {
        if (GapConstants.DEBUG) {
            System.out.println(msg);
        }
    }

    public static void debug(Exception e) {
        if (GapConstants.DEBUG) {
            e.printStackTrace();
        }
    }

    private final PrintWriter stderr;
    private final GapParam param;
    private final GapContext context;

    private ReqResp currentReqResp;
    private boolean currentContentTypeInclude;
    private boolean flagCancel;
    private boolean useRequests = true;
    private boolean useResponses = true;

    public void setUseRequests(boolean useRequests) {
        this.useRequests = useRequests;
    }

    public void setUseResponses(boolean useResponses) {
        this.useResponses = useResponses;
    }

    private final Set<String> roots = new HashSet<>();
    private final Set<String> allScopePrefixes = new HashSet<>();
    private final Set<String> dictCheckedLinks = new HashSet<>();

    private final Set<String> paramList = new HashSet<>();
    private final Set<String> paramUrlList = new HashSet<>();
    private final Set<String> paramSusList = new HashSet<>();
    private final Set<String> paramSusUrlList = new HashSet<>();
    private final Set<String> linkList = new HashSet<>();
    private final Set<String> linkInScopeList = new HashSet<>();
    private final Set<String> linkUrlList = new HashSet<>();
    private final Set<String> linkUrlInScopeList = new HashSet<>();
    private final Set<String> wordList = new HashSet<>();
    private final Set<String> wordUrlList = new HashSet<>();
    private final Set<String> susParamIssue = new HashSet<>();
    private final Set<String> raisedIssues = new HashSet<>();

    public GapEngine(GapContext context, GapParam param, PrintWriter stderr) {
        this.context = context;
        this.param = param;
        this.stderr = stderr;
    }

    public Set<String> getRoots() {
        return roots;
    }

    public Set<String> getAllScopePrefixes() {
        return allScopePrefixes;
    }

    public Set<String> getParamList() {
        return paramList;
    }

    public Set<String> getParamUrlList() {
        return paramUrlList;
    }

    public Set<String> getParamSusList() {
        return paramSusList;
    }

    public Set<String> getParamSusUrlList() {
        return paramSusUrlList;
    }

    public Set<String> getLinkList() {
        return linkList;
    }

    public Set<String> getLinkInScopeList() {
        return linkInScopeList;
    }

    public Set<String> getLinkUrlList() {
        return linkUrlList;
    }

    public Set<String> getLinkUrlInScopeList() {
        return linkUrlInScopeList;
    }

    public Set<String> getWordList() {
        return wordList;
    }

    public Set<String> getWordUrlList() {
        return wordUrlList;
    }

    public boolean isCancel() {
        return flagCancel;
    }

    public void setCancel(boolean flag) {
        this.flagCancel = flag;
    }

    public void checkIfCancel() {
        if (flagCancel) {
            throw new CancelGapRequested("User pressed CANCEL GAP button.");
        }
    }

    public void clearAll() {
        roots.clear();
        paramList.clear();
        paramUrlList.clear();
        paramSusList.clear();
        paramSusUrlList.clear();
        linkList.clear();
        linkInScopeList.clear();
        linkUrlList.clear();
        linkUrlInScopeList.clear();
        wordList.clear();
        wordUrlList.clear();
        allScopePrefixes.clear();
        susParamIssue.clear();
        raisedIssues.clear();
        dictCheckedLinks.clear();
        flagCancel = false;
    }

    public void scopeChanged() {
        dictCheckedLinks.clear();
    }

    /**
     * Process a single message. Ported from {@code processMessage}.
     */
    public void processMessage(ReqResp reqResp) {
        try {
            if (GapConstants.DEBUG) {
                debug("Current request: " + reqResp.getRequestUrl());
            }
            if (!reqResp.getResponseContentType().equals("")) {
                currentContentTypeInclude = includeContentType(reqResp);
            } else {
                currentContentTypeInclude = false;
            }

            if (param.isParamsEnabled() && useRequests) {
                if (reqResp.isRequest()) {
                    getBurpParams(reqResp);
                    getRequestParams(reqResp);
                }
            }
            if (param.isParamsEnabled() && useResponses) {
                if (reqResp.isResponse()) {
                    getResponseParams(reqResp);
                }
            }

            if (((param.isParamsEnabled() && param.isIncludePathWords())
                            || (param.isWordsEnabled() && param.isWordPaths()))
                    && (useRequests || useResponses)
                    && isLinkInScope(reqResp.getRequestUrl())) {
                getPathWords(reqResp);
            }

            if (useResponses && reqResp.isResponse()) {
                if (param.isLinksEnabled()) {
                    getResponseLinks(reqResp);
                }
                if (param.isWordsEnabled()) {
                    getResponseWords(reqResp);
                }
            }
        } catch (Exception e) {
            stderr.println("processMessage 1");
            e.printStackTrace(stderr);
        }
    }

    /**
     * Get Burp/ZAP identified request parameters. Ported from {@code getBurpParams}.
     */
    public void getBurpParams(ReqResp reqResp) {
        try {
            for (String p : getRequestParamNames(reqResp)) {
                checkIfCancel();
                addParameter(p, "Certain", "BURP");
            }
        } catch (Exception e) {
            stderr.println("getBurpParams 1");
            e.printStackTrace(stderr);
        }
    }

    /**
     * Get the parameter names ZAP identifies from the request URL and body.
     */
    private List<String> getRequestParamNames(ReqResp reqResp) {
        List<String> names = new ArrayList<>();
        HttpMessage msg = reqResp.getMessage();
        try {
            if (param.isParamUrl()) {
                try {
                    org.zaproxy.zap.model.ParameterParser parser =
                            new org.zaproxy.zap.model.StandardParameterParser();
                    for (org.zaproxy.zap.model.NameValuePair p :
                            parser.getParameters(msg, org.parosproxy.paros.network.HtmlParameter.Type.url)) {
                        names.add(p.getName());
                    }
                } catch (Exception e) {
                    stderr.println("getRequestParamNames url");
                    e.printStackTrace(stderr);
                }
            }
            if (param.isParamBody()) {
                try {
                    org.zaproxy.zap.model.ParameterParser parser =
                            new org.zaproxy.zap.model.StandardParameterParser();
                    for (org.zaproxy.zap.model.NameValuePair p :
                            parser.getParameters(msg, org.parosproxy.paros.network.HtmlParameter.Type.form)) {
                        names.add(p.getName());
                    }
                } catch (Exception e) {
                    stderr.println("getRequestParamNames body");
                    e.printStackTrace(stderr);
                }
            }
            if (param.isParamMultiPart()) {
                try {
                    String body = reqResp.getRequestBody();
                    Matcher mp = GapConstants.REGEX_MULTIPARTNAME.matcher(body);
                    while (mp.find()) {
                        names.add(mp.group(1));
                    }
                } catch (Exception e) {
                    stderr.println("getRequestParamNames multipart");
                    e.printStackTrace(stderr);
                }
            }
            if (param.isParamCookie()) {
                String cookieHeader = msg.getRequestHeader().getHeader("Cookie");
                if (cookieHeader != null) {
                    for (String part : cookieHeader.split(";")) {
                        int eq = part.indexOf('=');
                        if (eq > 0) {
                            names.add(part.substring(0, eq).trim());
                        }
                    }
                }
            }
            if (param.isParamJson()) {
                String body = reqResp.getRequestBody();
                Matcher m = GapConstants.REGEX_PARAMSJSON.matcher(body);
                while (m.find()) {
                    String block = m.group();
                    Matcher km = GapConstants.REGEX_PARAMSJSONPARAMS.matcher(block);
                    while (km.find()) {
                        names.add(km.group());
                    }
                }
            }
            if (param.isParamXml()) {
                Matcher m = GapConstants.REGEX_XMLATTR.matcher(reqResp.getRequestBody());
                while (m.find()) {
                    names.add(m.group(1));
                }
            }
            if (param.isParamXmlAttr()) {
                Matcher m = GapConstants.REGEX_XMLATTR.matcher(reqResp.getRequestBody());
                while (m.find()) {
                    names.add(m.group(1));
                }
            }
        } catch (Exception e) {
            stderr.println("getBurpParams 2");
            e.printStackTrace(stderr);
        }
        return names;
    }

    /**
     * Get potential parameters from JSON in the request. Ported from {@code getRequestParams}.
     */
    public void getRequestParams(ReqResp reqResp) {
        try {
            Set<String> paramsProcessed = new HashSet<>();
            Matcher possibleJson = GapConstants.REGEX_PARAMSJSON.matcher(reqResp.getRequestBody());
            while (possibleJson.find()) {
                checkIfCancel();
                String key = possibleJson.group();
                if (key != null && !key.equals("")) {
                    Matcher params = GapConstants.REGEX_PARAMSJSONPARAMS.matcher(key);
                    while (params.find()) {
                        checkIfCancel();
                        String p = params.group();
                        if (!paramsProcessed.contains(p)) {
                            paramsProcessed.add(p);
                            if (p != null && !p.equals("")) {
                                debug("  getRequestParams param: " + p);
                                addParameter(p, "Firm", "REQUEST");
                            }
                        }
                    }
                }
            }
        } catch (CancelGapRequested e) {
            // propagate
        } catch (Exception e) {
            if (!flagCancel) {
                stderr.println("getRequestParams 1");
                e.printStackTrace(stderr);
            }
        }
    }

    /**
     * Find the balanced braces block starting at the first '{' at or after start. Ported from
     * {@code find_balanced_braces}.
     */
    private String findBalancedBraces(String text, int start) {
        int end = text.length();
        int i = text.indexOf("{", start);
        if (i == -1) {
            return null;
        }
        java.util.ArrayDeque<Character> stack = new java.util.ArrayDeque<>();
        while (i < text.length()) {
            checkIfCancel();
            if (text.charAt(i) == '{') {
                stack.push('{');
            } else if (text.charAt(i) == '}') {
                stack.pop();
                if (stack.isEmpty()) {
                    end = i + 1;
                    break;
                }
            }
            i++;
        }
        return text.substring(start, end);
    }

    private void processJsonString(String jsonString) {
        Matcher m = GapConstants.REGEX_JSNESTEDPARAM.matcher(jsonString);
        while (m.find()) {
            checkIfCancel();
            String parameter = m.group();
            if (parameter != null && !parameter.equals("")) {
                parameter = parameter.strip();
                if (parameter.endsWith(":")) {
                    parameter = parameter.substring(0, parameter.length() - 1);
                }
                parameter = parameter.replace("'", "").replace("\"", "");
                parameter = parameter.replace("[", "").replace("]", "");
                addParameter(parameter, "Tentative", "RESPONSE");
            }
        }
    }

    /**
     * Get XML and JSON responses, extract keys and add them. Ported from {@code getResponseParams}.
     */
    public void getResponseParams(ReqResp reqResp) {
        try {
            if (currentContentTypeInclude) {
                String body = reqResp.getResponseBody();
                String mimeType = reqResp.getResponseMIMEType();
                Set<String> paramsProcessed = new HashSet<>();

                try {
                    Matcher possibleParams = GapConstants.REGEX_PARAMSPOSSIBLE.matcher(body);
                    while (possibleParams.find()) {
                        checkIfCancel();
                        String param = possibleParams.group().replace("%5c", "");
                        if (param != null && !param.equals("")) {
                            if (!paramsProcessed.contains(param)) {
                                paramsProcessed.add(param);
                                debug("  getResponseParams param: " + param);
                                param = GapConstants.REGEX_PARAMSSUB.matcher(param).replaceAll("");
                                param = param.strip();
                                param = param.replace("\\", "").replace("&", "");
                                addParameter(param, "Tentative", "RESPONSE");
                            }
                        }
                    }
                } catch (Exception e) {
                    stderr.println("getResponseParams 9");
                    e.printStackTrace(stderr);
                }

                if (param.isParamJSONResponse()
                        || param.isParamXMLResponse()
                        || param.isParamInputField()
                        || param.isParamJSVars()) {

                    if (param.isParamJSVars()) {
                        try {
                            Matcher m = GapConstants.REGEX_JSLET.matcher(body);
                            while (m.find()) {
                                checkIfCancel();
                                String key = m.group();
                                if (key != null && !key.equals("")) {
                                    addParameter(key.strip(), "Tentative", "RESPONSE");
                                }
                            }
                        } catch (Exception e) {
                            stderr.println("getResponseParams 1");
                            e.printStackTrace(stderr);
                        }
                        try {
                            Matcher m = GapConstants.REGEX_JSVAR.matcher(body);
                            while (m.find()) {
                                checkIfCancel();
                                String key = m.group();
                                if (key != null && !key.equals("")) {
                                    addParameter(key.strip(), "Tentative", "RESPONSE");
                                }
                            }
                        } catch (Exception e) {
                            stderr.println("getResponseParams 2");
                            e.printStackTrace(stderr);
                        }
                        try {
                            Matcher m = GapConstants.REGEX_JSCONSTS.matcher(body);
                            while (m.find()) {
                                checkIfCancel();
                                String key = m.group();
                                if (key != null && !key.equals("")) {
                                    addParameter(key.strip(), "Tentative", "RESPONSE");
                                }
                            }
                        } catch (Exception e) {
                            stderr.println("getResponseParams 3");
                            e.printStackTrace(stderr);
                        }
                        try {
                            int start = 0;
                            String text = body;
                            while (start < text.length()) {
                                Matcher match = GapConstants.REGEX_JSNESTED.matcher(text);
                                if (!match.find(start)) {
                                    break;
                                }
                                String fullString = findBalancedBraces(text, match.start());
                                if (fullString != null) {
                                    processJsonString(fullString);
                                }
                                int endPos = text.indexOf("{", match.start());
                                if (endPos == -1) {
                                    break;
                                }
                                int balance = 0;
                                int i = endPos;
                                while (i < text.length()) {
                                    if (text.charAt(i) == '{') {
                                        balance++;
                                    } else if (text.charAt(i) == '}') {
                                        balance--;
                                        if (balance == 0) {
                                            endPos = i + 1;
                                            break;
                                        }
                                    }
                                    i++;
                                }
                                if (start == endPos) {
                                    break;
                                }
                                start = endPos;
                            }
                        } catch (Exception e) {
                            stderr.println("getResponseParams 4");
                            e.printStackTrace(stderr);
                        }
                    }

                    if ("JSON".equals(mimeType)) {
                        if (param.isParamJSONResponse()) {
                            try {
                                Matcher m = GapConstants.REGEX_JSONKEYS.matcher(body);
                                while (m.find()) {
                                    checkIfCancel();
                                    addParameter(m.group(1).strip(), "Tentative", "RESPONSE");
                                }
                            } catch (Exception e) {
                                stderr.println("getResponseParams 4");
                                e.printStackTrace(stderr);
                            }
                        }
                    } else if ("XML".equals(mimeType)) {
                        if (param.isParamXMLResponse()) {
                            try {
                                Matcher m = GapConstants.REGEX_XMLATTR.matcher(body);
                                while (m.find()) {
                                    checkIfCancel();
                                    addParameter(m.group(1).strip(), "Tentative", "RESPONSE");
                                }
                            } catch (Exception e) {
                                stderr.println("getResponseParams 5");
                                e.printStackTrace(stderr);
                            }
                        }
                    } else if ("HTML".equals(mimeType) || "JAVASCRIPT".equals(mimeType)) {
                        if (param.isParamInputField()) {
                            try {
                                Matcher htmlKeys = GapConstants.REGEX_HTMLINP.matcher(body);
                                while (htmlKeys.find()) {
                                    checkIfCancel();
                                    String key = htmlKeys.group(2);
                                    Matcher inputName = GapConstants.REGEX_HTMLINP_NAME.matcher(key);
                                    if (inputName.find() && !inputName.group().equals("")) {
                                        String inputNameVal = inputName.group();
                                        inputNameVal = inputNameVal.replace("=", "");
                                        inputNameVal = inputNameVal.replace("\"", "");
                                        inputNameVal = inputNameVal.replace("'", "");
                                        addParameter(inputNameVal.strip(), "Tentative", "RESPONSE");
                                    }
                                    Matcher inputId = GapConstants.REGEX_HTMLINP_ID.matcher(key);
                                    if (inputId.find() && !inputId.group().equals("")) {
                                        String inputIdVal = inputId.group();
                                        inputIdVal = inputIdVal.replace("=", "");
                                        inputIdVal = inputIdVal.replace("\"", "");
                                        inputIdVal = inputIdVal.replace("'", "");
                                        addParameter(inputIdVal.strip(), "Tentative", "RESPONSE");
                                    }
                                }
                            } catch (Exception e) {
                                stderr.println("getResponseParams 6");
                                e.printStackTrace(stderr);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (!flagCancel) {
                stderr.println("getResponseParams 8");
                e.printStackTrace(stderr);
            }
        }
    }

    /**
     * Determine if the passed link should be excluded. Ported from {@code includeLink}.
     */
    public boolean includeLink(String link) {
        boolean include = true;
        try {
            String urlHost = getHost(link);
            if (urlHost != null && !urlHost.equals("")) {
                if (GapConstants.REGEX_VALIDHOST.matcher(urlHost).find() == false) {
                    include = false;
                }
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            if (include) {
                if (countChar(link, '\n') > 1
                        || (link.startsWith("#") && !link.startsWith("#/"))
                        || link.startsWith("$")
                        || link.startsWith("\\")
                        || link.startsWith("/=")
                        || link.startsWith("-")
                        || link.startsWith("...")) {
                    include = false;
                }
                if (include) {
                    include = !link.matches(".*\\s.*");
                }
                if (include) {
                    include = !link.matches(".*\\n.*");
                }
                if (include) {
                    include = link.matches(".*[0-9a-zA-Z].*");
                }
                if (include) {
                    include = !link.matches(".*\\\\[sS].*");
                }
                if (include) {
                    include =
                            !link.matches(
                                    "^(application\\/|image\\/|model\\/|video\\/|audio\\/|text\\/).*");
                }
                for (int i = 0; i < link.length(); i++) {
                    if (link.charAt(i) < 32) {
                        include = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            stderr.println("includeLink 2");
            e.printStackTrace(stderr);
        }

        if (include && param.isExclusionsEnabled()) {
            String[] lstExclusions;
            try {
                lstExclusions = param.getLinkExclusions().split(",");
            } catch (Exception e) {
                lstExclusions = GapConstants.DEFAULT_EXCLUSIONS.split(",");
            }
            String linkWithoutQueryString =
                    link.split("\\?", 2)[0].toLowerCase();
            for (String exc : lstExclusions) {
                try {
                    if (linkWithoutQueryString.contains(exc.toLowerCase())) {
                        include = false;
                    }
                } catch (Exception e) {
                    include = false;
                }
            }
        }
        return include;
    }

    private static int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    private static String getHost(String link) {
        try {
            if (link.contains("://")) {
                String after = link.substring(link.indexOf("://") + 3);
                int slash = after.indexOf('/');
                String hostPort = slash < 0 ? after : after.substring(0, slash);
                int colon = hostPort.indexOf(':');
                return colon < 0 ? hostPort : hostPort.substring(0, colon);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * Determine if the url should be excluded by checking file extensions. Ported from
     * {@code includeFile}.
     */
    public boolean includeFile(String url) {
        boolean include = true;
        String[] exts = GapConstants.FILEEXT_EXCLUSIONS.split(",");
        for (String exc : exts) {
            try {
                if (url.toLowerCase().endsWith(exc.toLowerCase())) {
                    include = false;
                }
            } catch (Exception e) {
                stderr.println("ERROR includeFile 2");
                e.printStackTrace(stderr);
            }
        }
        return include;
    }

    /**
     * Determine if the content type is in the exclusions. Ported from {@code includeContentType}.
     */
    public boolean includeContentType(ReqResp reqResp) {
        boolean include = true;
        try {
            String contentType = reqResp.getResponseContentType();
            String url = reqResp.getRequestUrl();
            String file = url.split("\\?", 2)[0].split("#", 2)[0];
            file = file.substring(file.lastIndexOf('/') + 1);
            if (file.contains(".")) {
                include = includeFile(file);
            }
            if (include && !contentType.equals("")) {
                String[] exts = GapConstants.CONTENTTYPE_EXCLUSIONS.split(",");
                for (String excludeContentType : exts) {
                    checkIfCancel();
                    if (contentType.toLowerCase().equals(excludeContentType.toLowerCase())) {
                        include = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            stderr.println("ERROR includeContentType 1");
            e.printStackTrace(stderr);
        }
        return include;
    }

    /**
     * Strips the link from the first truly unbalanced closing bracket. Ported from
     * {@code stripLinkFromUnbalancedBrackets}.
     */
    public String stripLinkFromUnbalancedBrackets(String link) {
        try {
            String opening = "([{";
            String closing = ")]}";
            java.util.ArrayDeque<Character> stack = new java.util.ArrayDeque<>();
            int lastValidIndex = link.length();
            for (int i = 0; i < link.length(); i++) {
                char c = link.charAt(i);
                int oi = opening.indexOf(c);
                int ci = closing.indexOf(c);
                if (oi >= 0) {
                    stack.push(c);
                } else if (ci >= 0) {
                    if (!stack.isEmpty() && opening.indexOf(stack.peek()) == ci) {
                        stack.pop();
                    } else {
                        lastValidIndex = i;
                        break;
                    }
                }
            }
            if (!stack.isEmpty()) {
                // find index of first opening bracket still on the stack
                char firstOpen = stack.peekLast();
                int idx = link.indexOf(firstOpen);
                if (idx >= 0) {
                    lastValidIndex = Math.min(lastValidIndex, idx);
                }
            }
            return link.substring(0, lastValidIndex);
        } catch (Exception e) {
            stderr.println("ERROR stripLinkFromUnbalancedBrackets 1: " + link);
            e.printStackTrace(stderr);
            return "";
        }
    }

    /**
     * Replace large base64 strings in the body. Ported from {@code clean_body}.
     */
    public String cleanBody(String body) {
        try {
            Matcher m = GapConstants.REGEX_BIGBASE64.matcher(body);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String replacement =
                        m.group(0).length() > 10000 ? "BASE64_REPLACED_BY_GAP" : m.group(0);
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            stderr.println("ERROR truncate_long_lines 1");
            e.printStackTrace(stderr);
        }
        return body;
    }

    private static List<String> safeRegexFindall(Pattern pattern, String string) {
        try {
            List<String> results = new ArrayList<>();
            Matcher m = pattern.matcher(string);
            while (m.find()) {
                results.add(m.group(0));
            }
            return results;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<String> safeRegexFindallChunked(Pattern pattern, String string) {
        try {
            if (string.length() <= GapConstants.LARGE_RESPONSE_THRESHOLD) {
                return safeRegexFindall(pattern, string);
            }
            Set<String> allMatches = new HashSet<>();
            int chunkStart = 0;
            while (chunkStart < string.length()) {
                int chunkEnd = Math.min(chunkStart + GapConstants.CHUNK_SIZE, string.length());
                String chunk = string.substring(chunkStart, chunkEnd);
                allMatches.addAll(safeRegexFindall(pattern, chunk));
                chunkStart += GapConstants.CHUNK_SIZE - GapConstants.CHUNK_OVERLAP;
            }
            return new ArrayList<>(allMatches);
        } catch (Exception e) {
            stderr.println("ERROR safe_regex_findall_chunked 1");
            e.printStackTrace(stderr);
            return safeRegexFindall(pattern, string);
        }
    }

    /**
     * Add a link (and prefix if necessary). Ported from {@code addLink}.
     */
    public void addLink(String url, String origin) {
        boolean relativeUrl = false;
        if (url.startsWith("./") || url.startsWith("../")) {
            relativeUrl = true;
        }
        if (!param.isRelativeLinks() && relativeUrl) {
            return;
        }

        try {
            if (url.equals("")) {
                return;
            }
            Set<String> allUrls = new HashSet<>();
            boolean hasNetloc = hasNetloc(url);

            if (!hasNetloc) {
                if (param.isUnPrefixed()) {
                    linkList.add(url);
                    linkUrlList.add(url + "  [" + origin + "]");
                }

                if (param.isLinkPrefix()
                        || param.isLinkPrefixScope()
                        || param.isLinkPrefixOrigin()) {

                    if (!url.startsWith("/") && !relativeUrl) {
                        url = "/" + url;
                    }

                    if (param.isLinkPrefix()) {
                        List<String> linkPrefixes = param.getLinkPrefixes();
                        for (String link : linkPrefixes) {
                            if (relativeUrl) {
                                allUrls.add(link + "/" + url);
                            } else {
                                allUrls.add(link + url);
                            }
                        }
                    }

                    if (param.isLinkPrefixScope()) {
                        for (String prefix : allScopePrefixes) {
                            if (relativeUrl) {
                                prefix = prefix + "/";
                            }
                            allUrls.add(prefix + url);
                        }
                    }

                    if (param.isLinkPrefixOrigin()) {
                        String originNetloc = getNetloc(origin);
                        if (originNetloc != null && !originNetloc.equals("")) {
                            String originScheme = getScheme(origin);
                            String originPrefix = originScheme + "://" + originNetloc;
                            if (relativeUrl) {
                                originPrefix = originPrefix + "/";
                            }
                            allUrls.add(originPrefix + url);
                        }
                    }
                } else {
                    allUrls.add(url);
                }
            } else {
                allUrls.add(url);
            }

            for (String u : allUrls) {
                checkIfCancel();
                u = ReqResp.removeStdPort(u);
                linkList.add(u);
                linkUrlList.add(u + "  [" + origin + "]");
                try {
                    if (isLinkInScope(u)) {
                        linkInScopeList.add(u);
                        linkUrlInScopeList.add(u + "  [" + origin + "]");
                    }
                } catch (Exception e) {
                    stderr.println("addLink 2");
                    e.printStackTrace(stderr);
                }
            }
        } catch (Exception e) {
            stderr.println("addLink 1");
            e.printStackTrace(stderr);
        }
    }

    private static String getScheme(String url) {
        try {
            if (url.contains("://")) {
                return url.substring(0, url.indexOf("://"));
            }
        } catch (Exception e) {
            // ignore
        }
        return "http";
    }

    private static String getNetloc(String url) {
        try {
            if (url.contains("://")) {
                String after = url.substring(url.indexOf("://") + 3);
                int slash = after.indexOf('/');
                String hostPort = slash < 0 ? after : after.substring(0, slash);
                if (hostPort.contains("?") || hostPort.contains("#")) {
                    hostPort = hostPort.split("[?#]", 2)[0];
                }
                return hostPort;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static boolean hasNetloc(String url) {
        String netloc = getNetloc(url);
        return netloc != null && !netloc.equals("");
    }

    /**
     * Add a potential parameter. Ported from {@code addParameter}.
     */
    public void addParameter(String param, String confidence, String context) {
        try {
            if (param == null) {
                return;
            }
            // url encode non-ascii
            param = encodeNonAscii(param);

            if (!param.equals("")) {
                // split on ?
                if (param.contains("?")) {
                    param = param.split("\\?", 2)[1];
                }

                String origin = encodeNonAscii(currentReqResp != null ? currentReqResp.getRequestUrl() : "");

                param = param.replace("%5b", "")
                        .replace("%5B", "")
                        .replace("%5d", "")
                        .replace("%5D", "");

                param = param.replace("\\", "")
                        .replace("/", "")
                        .replace("quot;", "")
                        .replace("apos;", "")
                        .replace("amp;", "");

                Matcher matchedParam = GapConstants.REGEX_PARAM.matcher(param);
                if (!param.equals("") && matchedParam.find() && matchedParam.group(0).equals(param)) {
                    checkSusParams(param, confidence, context);

                    paramList.add(param);
                    paramUrlList.add(param + "  [" + origin + "]");

                    if (this.param.isWordsEnabled() && this.param.isWordParams()) {
                        addWord(param, origin);
                    }
                }
            }
        } catch (Exception e) {
            stderr.println("addParameter 1");
            e.printStackTrace(stderr);
        }
    }

    private static String encodeNonAscii(String s) {
        try {
            if (s == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c <= 127) {
                    sb.append(c);
                } else {
                    try {
                        byte[] bytes = String.valueOf(c).getBytes("UTF-8");
                        for (byte b : bytes) {
                            sb.append('%').append(String.format("%02X", b & 0xFF));
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return s;
        }
    }

    public String sanitizeWord(String word) {
        try {
            word = encodeNonAscii(word);
            if (!word.equals("")) {
                word = GapConstants.REGEX_WORDSUB.matcher(word).replaceAll("");
            }
            return word;
        } catch (Exception e) {
            stderr.println("sanitizeWord 1");
            e.printStackTrace(stderr);
        }
        return word;
    }

    public void addWord(String word, String origin) {
        try {
            origin = encodeNonAscii(origin);
            word = sanitizeWord(word);

            boolean include = true;
            int minLen = param.getWordMinLen();
            int wordLen = word.strip().length();

            if (wordLen < minLen) {
                include = false;
            } else if (!param.isWordDigits() && word.matches(".*\\d.*")) {
                include = false;
            } else if (param.getStopWords().contains(word.toLowerCase())) {
                include = false;
            } else {
                int maxLen = param.getWordMaxLen();
                if (maxLen > 0 && wordLen > maxLen) {
                    include = false;
                }
            }

            if (include) {
                wordList.add(word.strip());
                wordUrlList.add(word.strip() + "  [" + origin + "]");

                if (param.isWordPlurals()) {
                    String plural = processPlural(word);
                    if (!plural.equals("") && plural.strip().length() >= minLen) {
                        wordList.add(plural);
                        wordUrlList.add(plural + "  [GAP]");
                    }
                }
            }
        } catch (Exception e) {
            stderr.println("addWord 1");
            e.printStackTrace(stderr);
        }
    }

    public String processPlural(String originalWord) {
        try {
            String newWord = "";
            String word = originalWord.strip().toLowerCase();

            if (word.length() > 30
                    || (word.matches(".*\\d.*") && word.length() > 10)
                    || word.endsWith("ous")) {
                newWord = "";
            } else if (word.endsWith("xes") || word.endsWith("oes") || word.endsWith("sses")) {
                newWord = originalWord.substring(0, originalWord.length() - 2);
            } else if (word.endsWith("ies")) {
                if (word.length() == 4) {
                    if (originalWord.equals(originalWord.toUpperCase())) {
                        newWord = originalWord.substring(1) + "IE";
                    } else {
                        newWord = originalWord.substring(1) + "ie";
                    }
                } else {
                    if (originalWord.equals(originalWord.toUpperCase())) {
                        newWord = originalWord.substring(0, originalWord.length() - 3) + "Y";
                    } else {
                        newWord = originalWord.substring(0, originalWord.length() - 3) + "y";
                    }
                }
            } else if (word.endsWith("s") && !word.endsWith("ss")) {
                newWord = originalWord.substring(0, originalWord.length() - 1);
            } else if (word.endsWith("x") || word.endsWith("o") || word.endsWith("ss")) {
                if (originalWord.equals(originalWord.toUpperCase())) {
                    newWord = originalWord + "ES";
                } else {
                    newWord = originalWord + "es";
                }
            } else if (word.endsWith("y")
                    && !word.substring(Math.max(0, word.length() - 2), word.length() - 1)
                            .matches("[aeiou]")) {
                if (originalWord.equals(originalWord.toUpperCase())) {
                    newWord = originalWord.substring(0, originalWord.length() - 1) + "IES";
                } else {
                    newWord = originalWord.substring(0, originalWord.length() - 1) + "ies";
                }
            } else if (word.endsWith("o")
                    && !word.substring(Math.max(0, word.length() - 2), word.length() - 1)
                            .matches("[aeiou]")) {
                if (originalWord.equals(originalWord.toUpperCase())) {
                    newWord = originalWord.substring(0, originalWord.length() - 1) + "ES";
                } else {
                    newWord = originalWord.substring(0, originalWord.length() - 1) + "es";
                }
            } else {
                if (originalWord.equals(originalWord.toUpperCase())) {
                    newWord = originalWord + "S";
                } else {
                    newWord = originalWord + "s";
                }
            }
            return newWord;
        } catch (Exception e) {
            stderr.println("processPlural 1");
            e.printStackTrace(stderr);
        }
        return "";
    }

    /**
     * Determine if a link is in scope according to ZAP. Ported from {@code isLinkInScope}.
     */
    public boolean isLinkInScope(String link) {
        try {
            if (link.contains("[")) {
                link = link.substring(0, link.indexOf("["));
            }
            if (link.contains("(")) {
                link = GapConstants.REGEX_LINKBRACKET.matcher(link).replaceAll("");
                int idx = link.indexOf("(");
                if (idx >= 0) {
                    link = link.substring(0, idx);
                } else if (!link.isEmpty()) {
                    // Mirrors Python link[0:-1] (strip last char) when no opening bracket remains.
                    link = link.substring(0, link.length() - 1);
                }
            }
            if (link.contains("{")) {
                link = GapConstants.REGEX_LINKBRACES.matcher(link).replaceAll("");
                int idx = link.indexOf("{");
                if (idx >= 0) {
                    link = link.substring(0, idx);
                } else if (!link.isEmpty()) {
                    link = link.substring(0, link.length() - 1);
                }
            }

            String newLink = link;
            if (newLink.startsWith("//")) {
                newLink = "http:" + newLink;
            }
            if (newLink.contains("://")) {
                newLink = "http://" + newLink.substring(newLink.indexOf("://") + 3);
            }
            newLink = newLink.replace("*.", "").replace(":*", "").replace("*", "");

            String host = getHost(newLink);
            if (host == null || host.equals("")) {
                return true;
            }
            if (dictCheckedLinks.contains(host)) {
                return true;
            }
            if (host.contains(".")) {
                if (GapConstants.REGEX_VALIDHOST.matcher(host).find()) {
                    boolean inScope = context.isInScope("http://" + host);
                    return inScope;
                }
            }
            dictCheckedLinks.add(host);
        } catch (Exception e) {
            stderr.println("isLinkInScope 1");
            e.printStackTrace(stderr);
        }
        return true;
    }

    /**
     * Add site map links if required. Ported from {@code getSiteMapLinks}.
     */
    public void getSiteMapLinks(ReqResp reqResp) {
        try {
            String url = reqResp.getRequestUrl();
            String urlNoQS = url;
            if (urlNoQS.contains("?")) {
                urlNoQS = urlNoQS.substring(0, urlNoQS.indexOf("?"));
            }
            if (url.length() > 0) {
                if (includeLink(url)) {
                    if (reqResp.isResponse()) {
                        if (currentContentTypeInclude) {
                            if (isLinkInScope(url)) {
                                addLink(url, urlNoQS);
                            }
                        }
                    } else {
                        if (isLinkInScope(url)) {
                            addLink(url, urlNoQS);
                        }
                    }
                }
            }
        } catch (Exception e) {
            stderr.println("getSiteMapLinks 1");
            e.printStackTrace(stderr);
        }
    }

    /**
     * Get a list of links found in the response. Ported from {@code getResponseLinks}.
     */
    public void getResponseLinks(ReqResp reqResp) {
        Set<String> linksProcessed = new HashSet<>();
        String header = reqResp.getResponseHeaders();
        String responseUrl = reqResp.getRequestUrl();

        if (currentContentTypeInclude) {
            try {
                String body = reqResp.getResponseBody();
                body = cleanBody(body);
                body = GapConstants.REGEX_LINKSSLASH.matcher(body).replaceAll("/");
                body = GapConstants.REGEX_LINKSCOLON.matcher(body).replaceAll(":");
                body = body.replace("&quot;", "\"").replace("&nbsp;", " ");

                String search = header.replace(" ", "\n") + body;
                search = search.replace("&#34;", "\"").replace("%22", "\"").replace("\"\"", "\"");

                List<String> linkKeys = new ArrayList<>();
                try {
                    linkKeys.addAll(safeRegexFindallChunked(GapConstants.REGEX_LINKS, search));
                } catch (Exception e) {
                    stderr.println("getResponseParams 4");
                    e.printStackTrace(stderr);
                }

                try {
                    List<String> extraKeys =
                            safeRegexFindallChunked(GapConstants.REGEX_LINKS_EXTRA, search);
                    List<String> validExtraKeys = new ArrayList<>();
                    for (String key : extraKeys) {
                        String suffix = getTldSuffix(key);
                        String domain = getTldDomain(key);
                        if (suffix != null
                                && !suffix.equals("")
                                && !suffix.equals("call")
                                && !suffix.equals("skin")
                                && !suffix.equals("menu")
                                && !suffix.equals("style")
                                && !suffix.equals("rest")
                                && !suffix.equals("next")
                                && !suffix.equals("top")
                                && domain != null
                                && domain.length() > 2
                                && !domain.startsWith("_")
                                && !domain.equals("this")
                                && !domain.equals("self")
                                && !domain.equals("target")
                                && !domain.equals("value")
                                && !domain.equals("values")
                                && !domain.equals("prop")
                                && !domain.equals("properties")
                                && !domain.equals("proparray")
                                && !domain.equals("useragent")
                                && !domain.equals("rect")
                                && !domain.equals("paddiing")
                                && !domain.equals("style")
                                && !domain.equals("rule")
                                && !domain.equals("bound")
                                && !domain.equals("child")
                                && !domain.equals("global")
                                && !domain.equals("element")
                                && !domain.equals("div")
                                && !domain.equals("prototype")
                                && !domain.equals("event")
                                && !domain.equals("feature")
                                && !domain.equals("path")
                                && !(suffix.equals("map") && !domain.equals("js"))
                                && GapConstants.COMMON_TLDS.contains(suffix)) {
                            validExtraKeys.add("//" + key);
                        }
                    }
                    linkKeys.addAll(validExtraKeys);
                } catch (Exception e) {
                    stderr.println("getResponseParams 5");
                    e.printStackTrace(stderr);
                }

                try {
                    Matcher jsbMatch = GapConstants.REGEX_LINKS_JSBUILT.matcher(search);
                    List<String> rawKeys = new ArrayList<>();
                    while (jsbMatch.find()) {
                        rawKeys.add(jsbMatch.group(1));
                    }
                    for (String key : rawKeys) {
                        key = key.strip();
                        if (key.startsWith("'") || key.startsWith("\"") || key.startsWith("`")) {
                            continue;
                        }
                        if (key.contains("<") || key.contains(">")) {
                            continue;
                        }
                        if (!key.contains("/")) {
                            continue;
                        }
                        if (key.startsWith("$")) {
                            continue;
                        }
                        if (key.split("://", -1).length == 2) {
                            int schemeIndex = key.indexOf("://");
                            if (key.substring(0, schemeIndex).contains(".")) {
                                continue;
                            }
                        }
                        if (!key.startsWith("/") && !key.startsWith("http")) {
                            key = "/" + key;
                        }
                        key = key.split("\"", 2)[0].split("'", 2)[0].split("`", 2)[0];
                        debug("JS Built Link: " + key);
                        linkKeys.add(key);
                    }
                } catch (Exception e) {
                    stderr.println("getResponseParams 5");
                    e.printStackTrace(stderr);
                }

                try {
                    Matcher fetchMatch = GapConstants.REGEX_LINKS_FETCH.matcher(search);
                    List<String> rawKeys = new ArrayList<>();
                    while (fetchMatch.find()) {
                        rawKeys.add(fetchMatch.group(1));
                    }
                    for (String key : rawKeys) {
                        key = key.strip();
                        if (!key.startsWith("/") && !key.startsWith("http")) {
                            key = "/" + key;
                        }
                        debug("JS Fetch Link: " + key);
                        linkKeys.add(key);
                    }
                } catch (Exception e) {
                    stderr.println("getResponseParams 6");
                    e.printStackTrace(stderr);
                }

                Set<String> dedup = new HashSet<>(linkKeys);
                for (String key : dedup) {
                    checkIfCancel();
                    if (key != null && key.strip().length() > 1) {
                        String link = key.strip();
                        // GAP.py parity: strip leading and trailing quote chars, spaces, parentheses
                        link = link.replaceAll("^[\"'\\n\\r( ]+|[\"'\\n\\r( ]+$", "");
                        if (linksProcessed.contains(link)) {
                            continue;
                        }
                        linksProcessed.add(link);

                        debug("  getResponseLinks link: " + link);

                        link = link.replaceAll("[\"'\\n\\r( ]+$", "");
                        if (link.contains("\\n")) {
                            link = link.split("\\\\n", 2)[0];
                        }
                        if (link.contains("\\r")) {
                            link = link.split("\\\\r", 2)[0];
                        }
                        link = link.replace("\\.", ".");

                        try {
                            if (!link.equals("")) {
                                char first = link.charAt(0);
                                char last = link.charAt(link.length() - 1);
                                String firstTwo = link.substring(0, Math.min(2, link.length()));
                                String lastTwo = link.substring(Math.max(0, link.length() - 2));

                                if ((first == '"' || first == '\'' || first == '\n' || first == '\r'
                                                || firstTwo.equals("\\n") || firstTwo.equals("\\r"))
                                        && (last == '"' || last == '\'' || last == '\n' || last == '\r'
                                                || lastTwo.equals("\\n") || lastTwo.equals("\\r"))) {
                                    int start = (firstTwo.equals("\\n") || firstTwo.equals("\\r")) ? 2 : 1;
                                    int end = (lastTwo.equals("\\n") || lastTwo.equals("\\r")) ? 2 : 1;
                                    if (link.length() > start + end) {
                                        link = link.substring(start, link.length() - end);
                                    }
                                }

                                link = link.replaceAll("\\\\+$", "");
                                link = link.replaceAll("[>;,=:.|]+$", "");
                                link = link.replaceAll("\\]$", "");

                                if (link.contains("`")) {
                                    link = link.split("`", 2)[0];
                                }

                                link = stripLinkFromUnbalancedBrackets(link);

                                if (GapConstants.REGEX_LINKSEARCH4.matcher(link).find()) {
                                    link = link.split("</", 2)[0];
                                }
                            }
                        } catch (Exception e) {
                            stderr.println("getResponseLinks 2");
                            e.printStackTrace(stderr);
                        }

                        if (!link.equals("")) {
                            if (link.charAt(0) == '.'
                                    && link.length() > 1
                                    && link.charAt(1) != '.'
                                    && link.charAt(1) != '/') {
                                link = link.substring(1);
                            }

                            if (link.contains("//# sourceMappingURL")) {
                                int firstpos = link.lastIndexOf("=");
                                int lastpos = link.indexOf("\n");
                                if (lastpos <= 0) {
                                    lastpos = link.length();
                                }
                                String mapFile = link.substring(firstpos + 1, lastpos);
                                int lastpos2 = responseUrl.lastIndexOf("/");
                                String mapPath = responseUrl.substring(0, lastpos2 + 1);
                                link = mapPath + mapFile;
                                link = link.replace("\n", "");
                            }

                            boolean include = includeLink(link);

                            if (link.startsWith("//")) {
                                link = "http:" + link;
                            }

                            if (include) {
                                addLink(link, responseUrl);

                                if (param.isParamFromLinks()
                                        && param.isParamsEnabled()
                                        && link.contains("?")
                                        && isLinkInScope(link)) {
                                    try {
                                        link = link.replace("%5c", "").replace("\\", "");
                                        link = GapConstants.REGEX_LINKSAND.matcher(link).replaceAll("&");
                                        link = GapConstants.REGEX_LINKSEQUAL.matcher(link).replaceAll("=");
                                        Matcher paramKeys = GapConstants.REGEX_PARAMKEYS.matcher(link);
                                        while (paramKeys.find()) {
                                            String p = paramKeys.group();
                                            checkIfCancel();
                                            if (p != null && !p.equals("")) {
                                                debug("    getResponseLinks param: " + p);
                                                addParameter(p.strip(), "Firm", "RESPLINKS");
                                            }
                                        }
                                    } catch (Exception e) {
                                        stderr.println("getResponseLinks 3");
                                        e.printStackTrace(stderr);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (!flagCancel) {
                    stderr.println("getResponseLinks 1");
                    e.printStackTrace(stderr);
                }
            }
        }

        // Add a link of a js.map file if the X-SourceMap or SourceMap header exists
        try {
            Matcher m = GapConstants.REGEX_SOURCEMAP.matcher(header);
            String mapFile = "";
            if (m.find()) {
                mapFile = m.group();
            }
            if (!mapFile.equals("")) {
                addLink(mapFile, responseUrl);
            }
        } catch (Exception e) {
            stderr.println("getResponseLinks 4");
            e.printStackTrace(stderr);
        }
    }

    private static String getTldSuffix(String key) {
        String host = getHost("http://" + key);
        if (host == null) {
            return null;
        }
        int lastDot = host.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        }
        return host.substring(lastDot + 1).toLowerCase();
    }

    private static String getTldDomain(String key) {
        String host = getHost("http://" + key);
        if (host == null) {
            return null;
        }
        int lastDot = host.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        }
        int prevDot = host.lastIndexOf('.', lastDot - 1);
        return host.substring(prevDot + 1, lastDot);
    }

    /**
     * Get words from the response. Ported from {@code getResponseWords}.
     */
    public void getResponseWords(ReqResp reqResp) {
        try {
            if (currentContentTypeInclude) {
                String contentType = reqResp.getResponseContentType();
                String mimeType = reqResp.getResponseMIMEType();
                String responseUrl = reqResp.getRequestUrl();
                Set<String> wordsProcessed = new HashSet<>();

                if ((mimeType.equals("HTML")
                                || mimeType.equals("XML")
                                || mimeType.equals("JSON")
                                || mimeType.equals("PLAIN")
                                || GapConstants.DEFAULT_WORDS_CONTENT_TYPES.contains(contentType.toLowerCase()))
                        && !responseUrl.toLowerCase().contains(".js.map")) {

                    String body = reqResp.getResponseBody();

                    String allText = "";
                    try {
                        Source source = new Source(body);
                        source.fullSequentialParse();

                        for (StartTag tag : source.getAllStartTags("meta")) {
                            checkIfCancel();
                            String property = tag.getAttributeValue("property");
                            String name = tag.getAttributeValue("name");
                            String content = tag.getAttributeValue("content");
                            if (content == null) {
                                continue;
                            }
                            boolean isMeta =
                                    property != null
                                            && (property.equals("og:title")
                                                    || property.equals("og:description")
                                                    || property.equals("title")
                                                    || property.equals("og:site_name")
                                                    || property.equals("fb:admins"));
                            if (!isMeta && name != null) {
                                isMeta =
                                        name.equals("description")
                                                || name.equals("keywords")
                                                || name.equals("twitter:title")
                                                || name.equals("twitter:description")
                                                || name.equals("application-name")
                                                || name.equals("author")
                                                || name.equals("subject")
                                                || name.equals("copyright")
                                                || name.equals("abstract")
                                                || name.equals("topic")
                                                || name.equals("summary")
                                                || name.equals("owner")
                                                || name.equals("directory")
                                                || name.equals("category")
                                                || name.equals("og:title")
                                                || name.equals("og:type")
                                                || name.equals("og:site_name")
                                                || name.equals("og:description")
                                                || name.equals("csrf-param")
                                                || name.equals("apple-mobile-web-app-title")
                                                || name.equals("twitter:label1")
                                                || name.equals("twitter:data1")
                                                || name.equals("twitter:label2")
                                                || name.equals("twitter:data2")
                                                || name.equals("twitter:title");
                            }
                            if (isMeta) {
                                allText = allText + content + " ";
                            }
                        }

                        for (StartTag tag : source.getAllStartTags("link")) {
                            checkIfCancel();
                            String rel = tag.getAttributeValue("rel");
                            String title = tag.getAttributeValue("title");
                            if (title == null) {
                                continue;
                            }
                            if (rel != null
                                    && (rel.equals("alternate")
                                            || rel.equals("index")
                                            || rel.equals("start")
                                            || rel.equals("prev")
                                            || rel.equals("next")
                                            || rel.equals("search"))) {
                                allText = allText + title + " ";
                            }
                        }

                        if (param.isWordImgAlt()) {
                            for (StartTag tag : source.getAllStartTags("img")) {
                                checkIfCancel();
                                String alt = tag.getAttributeValue("alt");
                                if (alt != null) {
                                    allText = allText + alt + " ";
                                }
                            }
                        }

                        if (param.isWordComments()) {
                            for (Tag comment : source.getAllTags(StartTagType.COMMENT)) {
                                checkIfCancel();
                                String commentText =
                                        ((StartTag) comment).getTagContent().toString();
                                if (commentText != null) {
                                    allText = allText + commentText + " ";
                                }
                            }
                        }

                        // Remove tags we don't want content from
                        StringBuilder filtered = new StringBuilder();
                        for (StartTag tag : source.getAllStartTags()) {
                            String tagName = tag.getName();
                            if (tagName.equals("style") || tagName.equals("script") || tagName.equals("link")) {
                                checkIfCancel();
                            }
                        }
                        TextExtractor extractor =
                                new TextExtractor(source) {
                                    @Override
                                    public boolean excludeElement(StartTag startTag) {
                                        String name = startTag.getName();
                                        return name.equals("style")
                                                || name.equals("script")
                                                || name.equals("link");
                                    }
                                };
                        allText = allText + " " + extractor.toString();
                    } catch (Exception e) {
                        stderr.println("getResponseWords 2");
                        e.printStackTrace(stderr);
                    }

                    Set<String> potentialWords = new HashSet<>();
                    Matcher m = GapConstants.REGEX_WORDS.matcher(allText);
                    while (m.find()) {
                        potentialWords.add(m.group());
                    }

                    for (String word : potentialWords) {
                        checkIfCancel();
                        if (wordsProcessed.contains(word)) {
                            continue;
                        }
                        wordsProcessed.add(word);

                        debug("  getResponseWords word: " + word);

if (responseUrl.toLowerCase().contains("robots.txt")
                                && (word.equalsIgnoreCase("allow")
                                        || word.equalsIgnoreCase("disallow")
                                        || word.equalsIgnoreCase("sitemap")
                                        || word.equalsIgnoreCase("user-agent"))) {
                            continue;
                        }
                        word = sanitizeWord(word);
                        checkIfCancel();

                        if (param.isWordDigits() || !word.matches(".*\\d.*")) {
                            if (word.equals(word.toUpperCase())) {
                                word = word.replace("'", "");
                                int minLen = param.getWordMinLen();
                                int maxLen = param.getWordMaxLen();
                                int wordLen = word.length();
                                boolean lengthValid = wordLen >= minLen && (maxLen <= 0 || wordLen <= maxLen);

                                if (word.length() > 0
                                        && !param.getStopWords().contains(word.toLowerCase())
                                        && lengthValid) {
                                    wordList.add(word);
                                    wordUrlList.add(word + "  [" + responseUrl + "]");
                                    if (param.isWordLower() && !word.equals(word.toLowerCase())) {
                                        wordList.add(word.toLowerCase());
                                        wordUrlList.add(word.toLowerCase() + "  [GAP]");
                                    }
                                    if (param.isWordPlurals()) {
                                        String newWord = processPlural(word);
                                        if (!newWord.equals("")
                                                && newWord.length() >= minLen
                                                && !param.getStopWords().contains(newWord.toLowerCase())) {
                                            wordList.add(newWord);
                                            wordUrlList.add(newWord + "  [GAP]");
                                            if (param.isWordLower() && !newWord.equals(newWord.toLowerCase())) {
                                                wordList.add(newWord.toLowerCase());
                                                wordUrlList.add(newWord.toLowerCase() + "  [GAP]");
                                            }
                                            if (param.isWordLower()
                                                    && word.equals(word.toUpperCase())
                                                    && !word.endsWith("S")
                                                    && newWord.equals(word + "S")) {
                                                String additionalWord = word + "s";
                                                if (additionalWord.length() >= minLen) {
                                                    wordList.add(additionalWord);
                                                    wordUrlList.add(additionalWord + "  [GAP]");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (!flagCancel) {
                stderr.println("getResponseWords 1");
                e.printStackTrace(stderr);
            }
        }
    }

    /**
     * Get words from the URL path. Ported from {@code getPathWords}.
     */
    public void getPathWords(ReqResp reqResp) {
        try {
            String url = reqResp.getRequestUrl();
            String path = getPath(url);
            String[] parts = path.split("[/:?=&#-]+");
            Set<String> words = new HashSet<>();
            for (String part : parts) {
                words.addAll(java.util.Arrays.asList(part.split(",")));
            }

            for (String word : words) {
                if (!word.contains(".")
                        && !word.matches("\\d+")
                        && !(word.length() == 1 && !word.matches("[a-zA-Z]"))
                        && word.length() > 0) {
                    if (param.isWordsEnabled() && param.isWordPaths()) {
                        addWord(word.strip(), url);
                    }
                    if (param.isParamsEnabled() && param.isIncludePathWords()) {
                        addParameter(word.strip(), "Tentative", "PATH");
                    }
                }
            }
        } catch (Exception e) {
            stderr.println("getPathWords 1");
            e.printStackTrace(stderr);
        }
    }

    private static String getPath(String url) {
        try {
            if (url.contains("://")) {
                String after = url.substring(url.indexOf("://") + 3);
                int slash = after.indexOf('/');
                if (slash < 0) {
                    return "";
                }
                String path = after.substring(slash);
                int q = path.indexOf('?');
                if (q >= 0) {
                    path = path.substring(0, q);
                }
                return path;
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    /**
     * Determine the vulnerability types for a sus parameter. Ported from {@code getSusVulnTypes}.
     */
    public String[] getSusVulnTypes(String param) {
        StringBuilder types = new StringBuilder();
        StringBuilder typesMin = new StringBuilder();
        if (GapConstants.SUS_OPENREDIRECT.contains(param)) {
            types.append("Open Redirect, ");
            typesMin.append("OR, ");
        }
        if (GapConstants.SUS_DEBUG.contains(param)) {
            types.append("Active Debugging, ");
            typesMin.append("DEBUG, ");
        }
        if (GapConstants.SUS_XSS.contains(param)) {
            types.append("Cross-site Scripting (XSS), ");
            typesMin.append("XSS, ");
        }
        if (GapConstants.SUS_IDOR.contains(param)) {
            types.append("Insecure Direct Object Reference (IDOR), ");
            typesMin.append("IDOR, ");
        }
        if (GapConstants.SUS_FILEINC.contains(param)) {
            types.append("File Inclusion, ");
            typesMin.append("LFI/RFI, ");
        }
        if (GapConstants.SUS_CMDI.contains(param)) {
            types.append("OS Command Injection, ");
            typesMin.append("CMDi, ");
        }
        if (GapConstants.SUS_SQLI.contains(param)) {
            types.append("SQL Injection (SQLi), ");
            typesMin.append("SQLi, ");
        }
        if (GapConstants.SUS_SSRF.contains(param)) {
            types.append("Server-side Request Forgery (SSRF), ");
            typesMin.append("SSRF, ");
        }
        if (GapConstants.SUS_SSTI.contains(param)) {
            types.append("Server-side Template Injection (SSTI), ");
            typesMin.append("SSTI, ");
        }
        if (GapConstants.SUS_MASSASSIGNMENT.contains(param)) {
            types.append("Mass Assignment, ");
            typesMin.append("MASS-ASSIGN, ");
        }
        String t = types.toString().replaceAll(", $", "");
        String tm = typesMin.toString().replaceAll(", $", "");
        return new String[] {t, tm};
    }

    /**
     * Create a scan issue for a suspect parameter. Ported from {@code checkSusParams}.
     */
    public void checkSusParams(String param, String confidence, String context) {
        try {
            if (param.length() < 20 && GapConstants.REGEX_SUSPARAM.matcher(param).matches()) {
                String origin = currentReqResp != null ? currentReqResp.getRequestUrl() : "";

                String[] vulns = getSusVulnTypes(param);
                String vulnTypes = vulns[0];
                String minVulnTypes = vulns[1];

                if (!flagCancel && !vulnTypes.equals("")) {
                    paramSusList.add(param + "  [" + minVulnTypes + "]");
                    paramSusUrlList.add(param + "  [" + origin + "]");

                    if (this.param.isReportSusParams()) {
                        if (!confidence.equals("Tentative") || (confidence.equals("Tentative") && this.param.isIncludeTentative())) {
                            try {
                                boolean createIssue = true;
                                String paramIssue = param + ":" + origin;
                                String contextDetail = "<br>";
                                if (context.equals("BURP")) {
                                    contextDetail = "The parameter was identified in the Request by ZAP and reported by GAP.<br><br>";
                                } else if (context.equals("REQUEST")) {
                                    contextDetail = "The parameter was identified in the Request by GAP.<br><br>";
                                    susParamIssue.add(paramIssue);
                                } else if (context.equals("RESPONSE")) {
                                    contextDetail = "The potential parameter was identified in the Response by GAP.<br><br>";
                                    susParamIssue.add(paramIssue);
                                } else if (context.equals("PATH")) {
                                    contextDetail = "The potential parameter was identified by GAP because the <b><i>Include URL path words</i></b> option was selected.<br><br>";
                                    if (!susParamIssue.contains(paramIssue)) {
                                        susParamIssue.add(paramIssue);
                                    } else {
                                        createIssue = false;
                                    }
                                } else if (context.equals("RESPLINKS")) {
                                    contextDetail = "The potential parameter was identified by GAP because the <b><i>Params from links found</i></b> option was selected.<br><br>";
                                    if (!susParamIssue.contains(paramIssue)) {
                                        susParamIssue.add(paramIssue);
                                    } else {
                                        createIssue = false;
                                    }
                                }

                                String detail =
                                        "The parameter <b>"
                                                + param
                                                + "</b> was found. This parameter is worthy of further investigation as it is often associated with the following vulnerability type(s): <b>"
                                                + vulnTypes
                                                + "</b><br>"
                                                + contextDetail;

                                if (createIssue && currentReqResp != null) {
                                    GapEngine.this.context.createIssue(
                                            currentReqResp.getMessage(),
                                            detail,
                                            confidence);
                                }
                            } catch (Exception e) {
                                if (GapConstants.DEBUG) {
                                    stderr.println("checkSusParams 2");
                                    e.printStackTrace(stderr);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            stderr.println("checkSusParams 1");
            e.printStackTrace(stderr);
        }
    }

    public ReqResp getCurrentReqResp() {
        return currentReqResp;
    }

    public void setCurrentReqResp(ReqResp reqResp) {
        this.currentReqResp = reqResp;
    }
}