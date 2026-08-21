package Arkhamahn.gap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies GapParam defaults, getters and string-splitting helpers. */
class GapParamTest {

    private final GapParam param = new GapParam();

    private void parse() {
        param.parse();
    }

    @Test
    void defaultValues() {
        parse();
        assertTrue(param.isParamsEnabled());
        assertTrue(param.isLinksEnabled());
        assertTrue(param.isWordsEnabled());
        assertTrue(param.isParamUrl());
        assertTrue(param.isParamBody());
        assertTrue(param.isParamMultiPart());
        assertFalse(param.isParamJson());
        assertFalse(param.isParamCookie());
        assertFalse(param.isParamXml());
        assertFalse(param.isParamXmlAttr());
        assertTrue(param.isReportSusParams());
        assertTrue(param.isIncludeTentative());
        assertTrue(param.isExclusionsEnabled());
        assertTrue(param.isRelativeLinks());
        assertTrue(param.isWordPlurals());
        assertTrue(param.isWordComments());
        assertTrue(param.isWordDigits());
        assertTrue(param.isWordLower());
        assertEquals(3, param.getWordMinLen());
        assertEquals(40, param.getWordMaxLen());
    }

    @Test
    void settersRoundTrip() {
        parse();
        param.setParamXml(true);
        param.setWordParams(true);
        param.setLinkPrefixValue("https://test.com");
        param.setQueryStringVal("QSV");
        param.setWordMaxLen("");
        assertTrue(param.isParamXml());
        assertTrue(param.isWordParams());
        assertEquals("https://test.com", param.getLinkPrefix());
        assertEquals("QSV", param.getQueryStringVal());
        assertEquals(0, param.getWordMaxLen());
    }

    @Test
    void emptyWordMaxLenMeansUnlimited() {
        parse();
        param.setWordMaxLen("");
        assertEquals(0, param.getWordMaxLen());
    }

    @Test
    void invalidWordMaxLenFallsBackToDefault() {
        parse();
        param.setWordMaxLen("abc");
        assertEquals(40, param.getWordMaxLen());
    }

    @Test
    void linkPrefixesSplitOnNewlineAndSemicolon() {
        parse();
        param.setLinkPrefixValue("https://a.com\nhttps://b.com;https://c.com/\n");
        List<String> prefixes = param.getLinkPrefixes();
        assertEquals(3, prefixes.size());
        assertTrue(prefixes.contains("https://a.com"));
        assertTrue(prefixes.contains("https://b.com"));
        assertTrue(prefixes.contains("https://c.com"));
    }

    @Test
    void stopWordsAreLowerCasedAndSplit() {
        parse();
        Set<String> stop = param.getStopWords();
        assertTrue(stop.contains("the"));
        assertTrue(stop.contains("and"));
        assertTrue(stop.contains("of"));
        assertTrue(stop.contains("this"));
        assertFalse(stop.isEmpty());
    }

    @Test
    void stopWordsCustom() {
        parse();
        param.setStopWords("foo, BAR, baz ");
        Set<String> stop = param.getStopWords();
        assertTrue(stop.contains("foo"));
        assertTrue(stop.contains("bar"));
        assertTrue(stop.contains("baz"));
        assertEquals(3, stop.size());
    }

    @Test
    void minLenInvalidFallsBack() {
        parse();
        param.setWordMinLen("x");
        assertEquals(3, param.getWordMinLen());
    }
}