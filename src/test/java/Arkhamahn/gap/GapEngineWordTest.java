package Arkhamahn.gap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

/** Exercises word extraction from HTML: uppercase words, comments, img alt, plurals, limits. */
class GapEngineWordTest extends GapTestSupport {

    private void process(String responseBody, String contentType) throws Exception {
        HttpMessage msg = message("http://example.com/page", null, contentType, responseBody);
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
    }

    private void process(String responseBody) throws Exception {
        process(responseBody, "text/html");
    }

    private static final String HTML =
            "<html><head>"
                    + "<title>GAP TEST</title>"
                    + "<meta name=\"description\" content=\"PORTAL SECURITY\">"
                    + "<meta name=\"keywords\" content=\"GAP FINDER\">"
                    + "</head><body>"
                    + "<img src=\"x.png\" alt=\"PROFILE ICON\">"
                    + "<!-- COMMENT WORD GOLD -->"
                    + "<p>THE QUICK BROWN FOX</p>"
                    + "</body></html>";

    @Test
    void uppercaseWordsExtracted() throws Exception {
        process(HTML);
        Set<String> words = engine.getWordList();
        assertContains(words, "GAP");
        assertContains(words, "SECURITY");
        assertContains(words, "QUICK");
        assertContains(words, "BROWN");
        // stop word excluded
        assertFalse(words.contains("THE"), "stop word 'THE' must be excluded");
    }

    @Test
    void lowercaseWordsNotExtractedByDefault() throws Exception {
        process("<html><body><p>the quick brown fox jumps</p></body></html>");
        assertTrue(engine.getWordList().isEmpty());
    }

    @Test
    void wordLowerAddsLowercaseVariant() throws Exception {
        process(HTML);
        assertContains(engine.getWordList(), "gap");
        assertContains(engine.getWordList(), "portal");
    }

    @Test
    void wordLowerDisabled() throws Exception {
        param.setWordLower(false);
        process(HTML);
        assertFalse(engine.getWordList().contains("gap"));
        assertContains(engine.getWordList(), "GAP");
    }

    @Test
    void pluralsAdded() throws Exception {
        process(HTML);
        assertContains(engine.getWordList(), "FOXES");
        assertContains(engine.getWordList(), "GAPS");
    }

    @Test
    void pluralsDisabled() throws Exception {
        param.setWordPlurals(false);
        process(HTML);
        assertContains(engine.getWordList(), "FOX");
        assertFalse(engine.getWordList().contains("FOXES"));
    }

    @Test
    void commentsControlledByOption() throws Exception {
        process(HTML);
        assertContains(engine.getWordList(), "COMMENT");
        assertContains(engine.getWordList(), "GOLD");

        engine.clearAll();
        param.setWordComments(false);
        process(HTML);
        assertFalse(engine.getWordList().contains("COMMENT"));
    }

    @Test
    void imgAltControlledByOption() throws Exception {
        process(HTML);
        assertContains(engine.getWordList(), "PROFILE");

        engine.clearAll();
        param.setWordImgAlt(false);
        process(HTML);
        assertFalse(engine.getWordList().contains("PROFILE"));
    }

    @Test
    void minLengthFilter() throws Exception {
        param.setWordMinLen("8");
        process(HTML);
        assertContains(engine.getWordList(), "SECURITY");
        assertFalse(engine.getWordList().contains("GAP"), "3-char word should be filtered");
        assertFalse(engine.getWordList().contains("FOX"));
    }

    @Test
    void maxLengthFilter() throws Exception {
        param.setWordMaxLen("5");
        process(HTML);
        assertFalse(engine.getWordList().contains("SECURITY"));
        assertContains(engine.getWordList(), "QUICK");
    }

    @Test
    void digitsControlledByOption() throws Exception {
        process("<html><body><p>ADMIN123 PANEL2</p></body></html>");
        assertContains(engine.getWordList(), "ADMIN123");

        engine.clearAll();
        param.setWordDigits(false);
        process("<html><body><p>ADMIN123 PANEL2</p></body></html>");
        assertFalse(engine.getWordList().contains("ADMIN123"));
    }

    @Test
    void scriptsAndStylesExcluded() throws Exception {
        process(
                "<html><head><style>.b {color: RED}</style></head>"
                        + "<body><script>var hiddenToken = \"SECRETVALUE\";</script>"
                        + "<p>VISIBLE</p></body></html>");
        Set<String> words = engine.getWordList();
        assertContains(words, "VISIBLE");
        assertFalse(words.contains("SECRETVALUE"), "script content must be excluded");
    }

    @Test
    void robotsTxtReservedWordsSkipped() throws Exception {
        HttpMessage msg =
                message(
                        "http://example.com/robots.txt",
                        null,
                        "text/plain",
                        "USER-AGENT: *\nDISALLOW: /x\nALLOW: /y\nSITEMAP: http://example.com/sitemap.xml\n");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
        Set<String> words = engine.getWordList();
        assertFalse(words.contains("DISALLOW"));
        assertFalse(words.contains("ALLOW"));
        assertFalse(words.contains("USER-AGENT"));
        assertFalse(words.contains("SITEMAP"));
    }

    @Test
    void sanitizeWordRemovesEncodedChars() {
        assertEquals("GAPTEST", engine.sanitizeWord("GAP%20TEST"));
        assertEquals("GAPTEST", engine.sanitizeWord("GAP \"TEST\""));
        assertEquals("GAPTEST", engine.sanitizeWord("GAP(TEST)"));
    }

    @Test
    void processPluralCases() {
        assertEquals("CAT", engine.processPlural("CATS"));
        assertEquals("BOXES", engine.processPlural("BOX"));
        assertEquals("GOES", engine.processPlural("GO"));
        assertEquals("BABIES", engine.processPlural("BABY"));
        assertEquals("DAYS", engine.processPlural("DAY"));
        assertEquals("", engine.processPlural("GORGEOUS"));
        assertEquals("GAPS", engine.processPlural("GAP"));
    }

    @Test
    void wordParamsAddsParamNamesAsWords() throws Exception {
        param.setWordParams(true);
        HttpMessage msg =
                message(
                        "http://example.com/login?username=bob&sessionkey=abc",
                        "password=secret",
                        null,
                        null);
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.processMessage(rr);
        assertContains(engine.getWordList(), "username");
        assertContains(engine.getWordList(), "password");
        assertContains(engine.getWordList(), "sessionkey");
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }
}