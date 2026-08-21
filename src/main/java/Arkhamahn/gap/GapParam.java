package Arkhamahn.gap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/**
 * The GAP add-on options. Holds every user-configurable setting (Ported from the {@code saveConfig}
 * / {@code restoreSavedConfig} / {@code btnRestoreDefaults_clicked} methods of GAP.py).
 *
 * <p>The panel keeps its widgets in sync with this object: loading values in {@code parse()} and
 * writing them back via {@link #update()} whenever the user saves options or restores defaults.
 */
public class GapParam extends AbstractParam {

    private static final Logger LOGGER = LogManager.getLogger(GapParam.class);

    private static final String PARAM_SAVE_FILE = "gap.saveFile";
    private static final String PARAM_PARAM_URL = "gap.paramUrl";
    private static final String PARAM_PARAM_BODY = "gap.paramBody";
    private static final String PARAM_PARAM_MULTIPART = "gap.paramMultiPart";
    private static final String PARAM_PARAM_JSON = "gap.paramJson";
    private static final String PARAM_PARAM_COOKIE = "gap.paramCookie";
    private static final String PARAM_PARAM_XML = "gap.paramXml";
    private static final String PARAM_PARAM_XML_ATTR = "gap.paramXmlAttr";
    private static final String PARAM_REPORT_SUS_PARAMS = "gap.reportSusParams";
    private static final String PARAM_INCLUDE_TENTATIVE = "gap.includeTentative";
    private static final String PARAM_INCLUDE_PATH_WORDS = "gap.includePathWords";
    private static final String PARAM_PARAM_JSON_RESPONSE = "gap.paramJsonResponse";
    private static final String PARAM_PARAM_XML_RESPONSE = "gap.paramXmlResponse";
    private static final String PARAM_PARAM_INPUT_FIELD = "gap.paramInputField";
    private static final String PARAM_PARAM_JS_VARS = "gap.paramJSVars";
    private static final String PARAM_SAVE_DIR = "gap.saveDir";
    private static final String PARAM_PARAM_FROM_LINKS = "gap.paramFromLinks";
    private static final String PARAM_EXCLUSIONS_ENABLED = "gap.exclusionsEnabled";
    private static final String PARAM_LINK_EXCLUSIONS = "gap.linkExclusions";
    private static final String PARAM_SHOW_PARAM_ORIGIN = "gap.showParamOrigin";
    private static final String PARAM_SHOW_LINK_ORIGIN = "gap.showLinkOrigin";
    private static final String PARAM_SHOW_WORD_ORIGIN = "gap.showWordOrigin";
    private static final String PARAM_IN_SCOPE_ONLY = "gap.inScopeOnly";
    private static final String PARAM_SITEMAP_ENDPOINTS = "gap.sitemapEndpoints";
    private static final String PARAM_RELATIVE_LINKS = "gap.relativeLinks";
    private static final String PARAM_PARAMS_ENABLED = "gap.paramsEnabled";
    private static final String PARAM_LINKS_ENABLED = "gap.linksEnabled";
    private static final String PARAM_LINK_PREFIX_CHECKED = "gap.linkPrefixChecked";
    private static final String PARAM_LINK_PREFIX = "gap.linkPrefix";
    private static final String PARAM_LINK_PREFIX_SCOPE = "gap.linkPrefixScopeSelected";
    private static final String PARAM_LINK_PREFIX_ORIGIN = "gap.linkPrefixOriginSelected";
    private static final String PARAM_UNPREFIXED = "gap.unprefixed";
    private static final String PARAM_WORDS_ENABLED = "gap.wordsEnabled";
    private static final String PARAM_WORD_PLURALS = "gap.wordPlurals";
    private static final String PARAM_WORD_PATHS = "gap.wordPaths";
    private static final String PARAM_WORD_PARAMS = "gap.wordParams";
    private static final String PARAM_WORD_DIGITS = "gap.wordDigits";
    private static final String PARAM_WORD_COMMENTS = "gap.wordComments";
    private static final String PARAM_WORD_IMG_ALT = "gap.wordImgAlt";
    private static final String PARAM_WORD_LOWER = "gap.wordLower";
    private static final String PARAM_WORD_MIN_LEN = "gap.wordMinLen";
    private static final String PARAM_WORD_MAX_LEN = "gap.wordMaxLen";
    private static final String PARAM_STOP_WORDS = "gap.stopWords";
    private static final String PARAM_TOOLTIPS = "gap.tooltips";
    private static final String PARAM_QUERY_STRING_VAL = "gap.queryStringVal";
    private static final String PARAM_SHOW_TAB_ON_STARTUP = "gap.showTabOnStartup";

    private boolean saveFile;
    private boolean paramUrl;
    private boolean paramBody;
    private boolean paramMultiPart;
    private boolean paramJson;
    private boolean paramCookie;
    private boolean paramXml;
    private boolean paramXmlAttr;
    private boolean reportSusParams;
    private boolean includeTentative;
    private boolean includePathWords;
    private boolean paramJsonResponse;
    private boolean paramXmlResponse;
    private boolean paramInputField;
    private boolean paramJSVars;
    private String saveDir;
    private boolean paramFromLinks;
    private boolean exclusionsEnabled;
    private String linkExclusions;
    private boolean showParamOrigin;
    private boolean showLinkOrigin;
    private boolean showWordOrigin;
    private boolean inScopeOnly;
    private boolean sitemapEndpoints;
    private boolean relativeLinks;
    private boolean paramsEnabled;
    private boolean linksEnabled;
    private boolean linkPrefixChecked;
    private String linkPrefix;
    private boolean linkPrefixScopeSelected;
    private boolean linkPrefixOriginSelected;
    private boolean unprefixed;
    private boolean wordsEnabled;
    private boolean wordPlurals;
    private boolean wordPaths;
    private boolean wordParams;
    private boolean wordDigits;
    private boolean wordComments;
    private boolean wordImgAlt;
    private boolean wordLower;
    private String wordMinLen;
    private String wordMaxLen;
    private String stopWords;
    private boolean tooltips;
    private String queryStringVal;
    private boolean showTabOnStartup;

    public GapParam() {
        super();
    }

    @Override
    protected void parse() {
        saveFile = readBoolean(PARAM_SAVE_FILE, true);
        paramUrl = readBoolean(PARAM_PARAM_URL, true);
        paramBody = readBoolean(PARAM_PARAM_BODY, true);
        paramMultiPart = readBoolean(PARAM_PARAM_MULTIPART, true);
        paramJson = readBoolean(PARAM_PARAM_JSON, false);
        paramCookie = readBoolean(PARAM_PARAM_COOKIE, false);
        paramXml = readBoolean(PARAM_PARAM_XML, false);
        paramXmlAttr = readBoolean(PARAM_PARAM_XML_ATTR, false);
        reportSusParams = readBoolean(PARAM_REPORT_SUS_PARAMS, true);
        includeTentative = readBoolean(PARAM_INCLUDE_TENTATIVE, true);
        includePathWords = readBoolean(PARAM_INCLUDE_PATH_WORDS, false);
        paramJsonResponse = readBoolean(PARAM_PARAM_JSON_RESPONSE, false);
        paramXmlResponse = readBoolean(PARAM_PARAM_XML_RESPONSE, false);
        paramInputField = readBoolean(PARAM_PARAM_INPUT_FIELD, false);
        paramJSVars = readBoolean(PARAM_PARAM_JS_VARS, false);
        saveDir = readString(PARAM_SAVE_DIR, getDefaultSaveDirectory());
        paramFromLinks = readBoolean(PARAM_PARAM_FROM_LINKS, false);
        exclusionsEnabled = readBoolean(PARAM_EXCLUSIONS_ENABLED, true);
        linkExclusions = readString(PARAM_LINK_EXCLUSIONS, GapConstants.DEFAULT_EXCLUSIONS);
        showParamOrigin = readBoolean(PARAM_SHOW_PARAM_ORIGIN, false);
        showLinkOrigin = readBoolean(PARAM_SHOW_LINK_ORIGIN, false);
        showWordOrigin = readBoolean(PARAM_SHOW_WORD_ORIGIN, false);
        inScopeOnly = readBoolean(PARAM_IN_SCOPE_ONLY, false);
        sitemapEndpoints = readBoolean(PARAM_SITEMAP_ENDPOINTS, false);
        relativeLinks = readBoolean(PARAM_RELATIVE_LINKS, true);
        paramsEnabled = readBoolean(PARAM_PARAMS_ENABLED, true);
        linksEnabled = readBoolean(PARAM_LINKS_ENABLED, true);
        linkPrefixChecked = readBoolean(PARAM_LINK_PREFIX_CHECKED, false);
        linkPrefix = readString(PARAM_LINK_PREFIX, GapConstants.DEFAULT_LINK_PREFIX);
        linkPrefixScopeSelected = readBoolean(PARAM_LINK_PREFIX_SCOPE, false);
        linkPrefixOriginSelected = readBoolean(PARAM_LINK_PREFIX_ORIGIN, false);
        unprefixed = readBoolean(PARAM_UNPREFIXED, false);
        wordsEnabled = readBoolean(PARAM_WORDS_ENABLED, true);
        wordPlurals = readBoolean(PARAM_WORD_PLURALS, true);
        wordPaths = readBoolean(PARAM_WORD_PATHS, false);
        wordParams = readBoolean(PARAM_WORD_PARAMS, false);
        wordDigits = readBoolean(PARAM_WORD_DIGITS, true);
        wordComments = readBoolean(PARAM_WORD_COMMENTS, true);
        wordImgAlt = readBoolean(PARAM_WORD_IMG_ALT, true);
        wordLower = readBoolean(PARAM_WORD_LOWER, true);
        wordMinLen = readString(PARAM_WORD_MIN_LEN, "3");
        wordMaxLen = readString(PARAM_WORD_MAX_LEN, GapConstants.DEFAULT_MAX_WORD_LEN);
        stopWords = readString(PARAM_STOP_WORDS, GapConstants.DEFAULT_STOP_WORDS);
        tooltips = readBoolean(PARAM_TOOLTIPS, true);
        queryStringVal = readString(PARAM_QUERY_STRING_VAL, GapConstants.DEFAULT_QSV);
        showTabOnStartup = readBoolean(PARAM_SHOW_TAB_ON_STARTUP, false);
    }

    private boolean readBoolean(String key, boolean defaultValue) {
        try {
            return getConfig().getBoolean(key, defaultValue);
        } catch (Exception e) {
            LOGGER.warn("Unable to read option {}", key, e);
            return defaultValue;
        }
    }

    private String readString(String key, String defaultValue) {
        try {
            String value = getConfig().getString(key, null);
            if (value == null) {
                return defaultValue;
            }
            return value;
        } catch (Exception e) {
            LOGGER.warn("Unable to read option {}", key, e);
            return defaultValue;
        }
    }

    /** Writes all options back to the ZAP configuration store. */
    public void update() {
        setProperty(PARAM_SAVE_FILE, saveFile);
        setProperty(PARAM_PARAM_URL, paramUrl);
        setProperty(PARAM_PARAM_BODY, paramBody);
        setProperty(PARAM_PARAM_MULTIPART, paramMultiPart);
        setProperty(PARAM_PARAM_JSON, paramJson);
        setProperty(PARAM_PARAM_COOKIE, paramCookie);
        setProperty(PARAM_PARAM_XML, paramXml);
        setProperty(PARAM_PARAM_XML_ATTR, paramXmlAttr);
        setProperty(PARAM_REPORT_SUS_PARAMS, reportSusParams);
        setProperty(PARAM_INCLUDE_TENTATIVE, includeTentative);
        setProperty(PARAM_INCLUDE_PATH_WORDS, includePathWords);
        setProperty(PARAM_PARAM_JSON_RESPONSE, paramJsonResponse);
        setProperty(PARAM_PARAM_XML_RESPONSE, paramXmlResponse);
        setProperty(PARAM_PARAM_INPUT_FIELD, paramInputField);
        setProperty(PARAM_PARAM_JS_VARS, paramJSVars);
        setProperty(PARAM_SAVE_DIR, saveDir);
        setProperty(PARAM_PARAM_FROM_LINKS, paramFromLinks);
        setProperty(PARAM_EXCLUSIONS_ENABLED, exclusionsEnabled);
        setProperty(PARAM_LINK_EXCLUSIONS, linkExclusions);
        setProperty(PARAM_SHOW_PARAM_ORIGIN, showParamOrigin);
        setProperty(PARAM_SHOW_LINK_ORIGIN, showLinkOrigin);
        setProperty(PARAM_SHOW_WORD_ORIGIN, showWordOrigin);
        setProperty(PARAM_IN_SCOPE_ONLY, inScopeOnly);
        setProperty(PARAM_SITEMAP_ENDPOINTS, sitemapEndpoints);
        setProperty(PARAM_RELATIVE_LINKS, relativeLinks);
        setProperty(PARAM_PARAMS_ENABLED, paramsEnabled);
        setProperty(PARAM_LINKS_ENABLED, linksEnabled);
        setProperty(PARAM_LINK_PREFIX_CHECKED, linkPrefixChecked);
        setProperty(PARAM_LINK_PREFIX, linkPrefix);
        setProperty(PARAM_LINK_PREFIX_SCOPE, linkPrefixScopeSelected);
        setProperty(PARAM_LINK_PREFIX_ORIGIN, linkPrefixOriginSelected);
        setProperty(PARAM_UNPREFIXED, unprefixed);
        setProperty(PARAM_WORDS_ENABLED, wordsEnabled);
        setProperty(PARAM_WORD_PLURALS, wordPlurals);
        setProperty(PARAM_WORD_PATHS, wordPaths);
        setProperty(PARAM_WORD_PARAMS, wordParams);
        setProperty(PARAM_WORD_DIGITS, wordDigits);
        setProperty(PARAM_WORD_COMMENTS, wordComments);
        setProperty(PARAM_WORD_IMG_ALT, wordImgAlt);
        setProperty(PARAM_WORD_LOWER, wordLower);
        setProperty(PARAM_WORD_MIN_LEN, wordMinLen);
        setProperty(PARAM_WORD_MAX_LEN, wordMaxLen);
        setProperty(PARAM_STOP_WORDS, stopWords);
        setProperty(PARAM_TOOLTIPS, tooltips);
        setProperty(PARAM_QUERY_STRING_VAL, queryStringVal);
        setProperty(PARAM_SHOW_TAB_ON_STARTUP, showTabOnStartup);
    }

    private void setProperty(String key, Object value) {
        try {
            getConfig().setProperty(key, value);
        } catch (Exception e) {
            LOGGER.warn("Unable to save option {}", key, e);
        }
    }

    private static String getDefaultSaveDirectory() {
        return System.getProperty("user.home");
    }

    // ------------------------------------------------------------------
    // Getters used by the engine
    // ------------------------------------------------------------------

    public boolean isParamsEnabled() {
        return paramsEnabled;
    }

    public boolean isIncludePathWords() {
        return includePathWords;
    }

    public boolean isWordsEnabled() {
        return wordsEnabled;
    }

    public boolean isWordPaths() {
        return wordPaths;
    }

    public boolean isLinksEnabled() {
        return linksEnabled;
    }

    public boolean isParamUrl() {
        return paramUrl;
    }

    public boolean isParamBody() {
        return paramBody;
    }

    public boolean isParamMultiPart() {
        return paramMultiPart;
    }

    public boolean isParamCookie() {
        return paramCookie;
    }

    public boolean isParamJson() {
        return paramJson;
    }

    public boolean isParamXml() {
        return paramXml;
    }

    public boolean isParamXmlAttr() {
        return paramXmlAttr;
    }

    public boolean isParamJSONResponse() {
        return paramJsonResponse;
    }

    public boolean isParamXMLResponse() {
        return paramXmlResponse;
    }

    public boolean isParamInputField() {
        return paramInputField;
    }

    public boolean isParamJSVars() {
        return paramJSVars;
    }

    public boolean isParamFromLinks() {
        return paramFromLinks;
    }

    public boolean isExclusionsEnabled() {
        return exclusionsEnabled;
    }

    public String getLinkExclusions() {
        return linkExclusions;
    }

    public boolean isRelativeLinks() {
        return relativeLinks;
    }

    public boolean isUnPrefixed() {
        return unprefixed;
    }

    public boolean isLinkPrefix() {
        return linkPrefixChecked;
    }

    public boolean isLinkPrefixScope() {
        return linkPrefixScopeSelected;
    }

    public boolean isLinkPrefixOrigin() {
        return linkPrefixOriginSelected;
    }

    public String getLinkPrefix() {
        return linkPrefix;
    }

    /**
     * Split the link prefixes by both newlines and semicolons, returning a list of clean prefixes.
     * Ported from {@code getLinkPrefixes} of GAP.py.
     */
    public List<String> getLinkPrefixes() {
        List<String> prefixes = new ArrayList<>();
        String text = linkPrefix;
        if (text != null && !text.isEmpty()) {
            for (String line : text.split("\n")) {
                for (String prefix : line.split(";")) {
                    prefix = prefix.strip();
                    if (!prefix.isEmpty()) {
                        if (prefix.endsWith("/")) {
                            prefix = prefix.substring(0, prefix.length() - 1);
                        }
                        prefixes.add(prefix);
                    }
                }
            }
        }
        return prefixes;
    }

    public boolean isWordImgAlt() {
        return wordImgAlt;
    }

    public boolean isWordComments() {
        return wordComments;
    }

    public boolean isWordParams() {
        return wordParams;
    }

    public boolean isWordDigits() {
        return wordDigits;
    }

    public boolean isWordPlurals() {
        return wordPlurals;
    }

    public boolean isWordLower() {
        return wordLower;
    }

    public int getWordMinLen() {
        try {
            return Integer.parseInt(wordMinLen.trim());
        } catch (Exception e) {
            return 3;
        }
    }

    /** Returns 0 when no maximum limit is set. */
    public int getWordMaxLen() {
        if (wordMaxLen == null || wordMaxLen.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(wordMaxLen.trim());
        } catch (Exception e) {
            return Integer.parseInt(GapConstants.DEFAULT_MAX_WORD_LEN);
        }
    }

    /** Returns the stop words as a lower-cased set, matching {@code lstStopWords} of GAP.py. */
    public Set<String> getStopWords() {
        Set<String> stop = new HashSet<>();
        String words = stopWords == null ? GapConstants.DEFAULT_STOP_WORDS : stopWords;
        for (String word : words.split(",")) {
            String w = word.strip().toLowerCase();
            if (!w.isEmpty()) {
                stop.add(w);
            }
        }
        return stop;
    }

    public boolean isReportSusParams() {
        return reportSusParams;
    }

    public boolean isIncludeTentative() {
        return includeTentative;
    }

    // ------------------------------------------------------------------
    // Getters / setters used by the panel UI
    // ------------------------------------------------------------------

    public boolean isSaveFile() {
        return saveFile;
    }

    public void setSaveFile(boolean saveFile) {
        this.saveFile = saveFile;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }

    public String getQueryStringVal() {
        return queryStringVal;
    }

    public void setQueryStringVal(String queryStringVal) {
        this.queryStringVal = queryStringVal;
    }

    public boolean isShowParamOrigin() {
        return showParamOrigin;
    }

    public void setShowParamOrigin(boolean showParamOrigin) {
        this.showParamOrigin = showParamOrigin;
    }

    public boolean isShowLinkOrigin() {
        return showLinkOrigin;
    }

    public void setShowLinkOrigin(boolean showLinkOrigin) {
        this.showLinkOrigin = showLinkOrigin;
    }

    public boolean isShowWordOrigin() {
        return showWordOrigin;
    }

    public void setShowWordOrigin(boolean showWordOrigin) {
        this.showWordOrigin = showWordOrigin;
    }

    public boolean isInScopeOnly() {
        return inScopeOnly;
    }

    public void setInScopeOnly(boolean inScopeOnly) {
        this.inScopeOnly = inScopeOnly;
    }

    public boolean isSiteMapEndpoints() {
        return sitemapEndpoints;
    }

    public void setSiteMapEndpoints(boolean sitemapEndpoints) {
        this.sitemapEndpoints = sitemapEndpoints;
    }

    public boolean isToolTips() {
        return tooltips;
    }

    public void setToolTips(boolean tooltips) {
        this.tooltips = tooltips;
    }

    public boolean isShowTabOnStartup() {
        return showTabOnStartup;
    }

    public void setShowTabOnStartup(boolean showTabOnStartup) {
        this.showTabOnStartup = showTabOnStartup;
    }

    public void setParamsEnabled(boolean paramsEnabled) {
        this.paramsEnabled = paramsEnabled;
    }

    public void setIncludePathWords(boolean includePathWords) {
        this.includePathWords = includePathWords;
    }

    public void setWordsEnabled(boolean wordsEnabled) {
        this.wordsEnabled = wordsEnabled;
    }

    public void setWordPaths(boolean wordPaths) {
        this.wordPaths = wordPaths;
    }

    public void setLinksEnabled(boolean linksEnabled) {
        this.linksEnabled = linksEnabled;
    }

    public void setParamUrl(boolean paramUrl) {
        this.paramUrl = paramUrl;
    }

    public void setParamBody(boolean paramBody) {
        this.paramBody = paramBody;
    }

    public void setParamMultiPart(boolean paramMultiPart) {
        this.paramMultiPart = paramMultiPart;
    }

    public void setParamCookie(boolean paramCookie) {
        this.paramCookie = paramCookie;
    }

    public void setParamJson(boolean paramJson) {
        this.paramJson = paramJson;
    }

    public void setParamXml(boolean paramXml) {
        this.paramXml = paramXml;
    }

    public void setParamXmlAttr(boolean paramXmlAttr) {
        this.paramXmlAttr = paramXmlAttr;
    }

    public void setParamJSONResponse(boolean paramJsonResponse) {
        this.paramJsonResponse = paramJsonResponse;
    }

    public void setParamXMLResponse(boolean paramXmlResponse) {
        this.paramXmlResponse = paramXmlResponse;
    }

    public void setParamInputField(boolean paramInputField) {
        this.paramInputField = paramInputField;
    }

    public void setParamJSVars(boolean paramJSVars) {
        this.paramJSVars = paramJSVars;
    }

    public void setParamFromLinks(boolean paramFromLinks) {
        this.paramFromLinks = paramFromLinks;
    }

    public void setExclusionsEnabled(boolean exclusionsEnabled) {
        this.exclusionsEnabled = exclusionsEnabled;
    }

    public void setLinkExclusions(String linkExclusions) {
        this.linkExclusions = linkExclusions;
    }

    public void setRelativeLinks(boolean relativeLinks) {
        this.relativeLinks = relativeLinks;
    }

    public void setUnPrefixed(boolean unprefixed) {
        this.unprefixed = unprefixed;
    }

    public void setLinkPrefix(boolean linkPrefixChecked) {
        this.linkPrefixChecked = linkPrefixChecked;
    }

    public void setLinkPrefixValue(String linkPrefix) {
        this.linkPrefix = linkPrefix;
    }

    public void setLinkPrefixScope(boolean linkPrefixScopeSelected) {
        this.linkPrefixScopeSelected = linkPrefixScopeSelected;
    }

    public void setLinkPrefixOrigin(boolean linkPrefixOriginSelected) {
        this.linkPrefixOriginSelected = linkPrefixOriginSelected;
    }

    public void setWordImgAlt(boolean wordImgAlt) {
        this.wordImgAlt = wordImgAlt;
    }

    public void setWordComments(boolean wordComments) {
        this.wordComments = wordComments;
    }

    public void setWordParams(boolean wordParams) {
        this.wordParams = wordParams;
    }

    public void setWordDigits(boolean wordDigits) {
        this.wordDigits = wordDigits;
    }

    public void setWordPlurals(boolean wordPlurals) {
        this.wordPlurals = wordPlurals;
    }

    public void setWordLower(boolean wordLower) {
        this.wordLower = wordLower;
    }

    public void setWordMinLen(String wordMinLen) {
        this.wordMinLen = wordMinLen;
    }

    public void setWordMaxLen(String wordMaxLen) {
        this.wordMaxLen = wordMaxLen;
    }

    public void setStopWords(String stopWords) {
        this.stopWords = stopWords;
    }

    public void setReportSusParams(boolean reportSusParams) {
        this.reportSusParams = reportSusParams;
    }

    public void setIncludeTentative(boolean includeTentative) {
        this.includeTentative = includeTentative;
    }
}