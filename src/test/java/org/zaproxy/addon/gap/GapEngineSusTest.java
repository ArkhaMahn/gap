package org.zaproxy.addon.gap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

/** Exercises suspect parameter detection and alert generation. */
class GapEngineSusTest extends GapTestSupport {

    @Test
    void knownSusParamDetected() throws Exception {
        HttpMessage msg = message("http://example.com/");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);
        engine.checkSusParams("id", "Certain", "BURP");
        assertTrue(
                engine.getParamSusList().stream().anyMatch(e -> e.startsWith("id  [")),
                "paramSusList: " + engine.getParamSusList());
        assertTrue(
                engine.getParamSusUrlList().contains("id  [http://example.com/]"),
                "paramSusUrlList: " + engine.getParamSusUrlList());
        assertTrue(
                context.issues.stream().anyMatch(e -> e.contains("IDOR")),
                "issues: " + context.issues);
        assertTrue(
                context.issues.stream().anyMatch(e -> e.contains("SQLi")),
                "issues: " + context.issues);
    }

    @Test
    void ssrfSusParam() throws Exception {
        HttpMessage msg = message("http://example.com/");
        engine.setCurrentReqResp(new ReqResp(msg));
        engine.checkSusParams("redirect", "Certain", "RESPONSE");
        assertTrue(
                context.issues.stream().anyMatch(e -> e.contains("SSRF")),
                "issues: " + context.issues);
    }

    @Test
    void nonSusParamIgnored() throws Exception {
        HttpMessage msg = message("http://example.com/");
        engine.setCurrentReqResp(new ReqResp(msg));
        engine.checkSusParams("xxx", "Certain", "BURP");
        assertTrue(engine.getParamSusList().isEmpty());
        assertTrue(context.issues.isEmpty());
    }

    @Test
    void tentativeRequiresIncludeTentative() throws Exception {
        HttpMessage msg = message("http://example.com/");
        ReqResp rr = new ReqResp(msg);
        engine.setCurrentReqResp(rr);

        param.setIncludeTentative(false);
        engine.checkSusParams("url", "Tentative", "RESPONSE");
        assertTrue(context.issues.isEmpty(), "tentative issue must be suppressed");

        param.setIncludeTentative(true);
        engine.checkSusParams("url", "Tentative", "RESPONSE");
        assertFalse(context.issues.isEmpty(), "tentative issue expected when enabled");
    }

    @Test
    void reportSusParamsSwitch() throws Exception {
        HttpMessage msg = message("http://example.com/");
        engine.setCurrentReqResp(new ReqResp(msg));
        param.setReportSusParams(false);
        engine.checkSusParams("id", "Certain", "BURP");
        assertTrue(context.issues.isEmpty());
        // still recorded in the sus list
        assertFalse(engine.getParamSusList().isEmpty());
    }

    @Test
    void longParamsNotTreatedAsSus() throws Exception {
        HttpMessage msg = message("http://example.com/");
        engine.setCurrentReqResp(new ReqResp(msg));
        String longParam = "abcdefghijklmnopqrstuvwxyz0123456789"; // >= 20 chars
        engine.checkSusParams(longParam, "Certain", "BURP");
        assertTrue(engine.getParamSusList().isEmpty());
    }

    @Test
    void getSusVulnTypesOrder() {
        String[] vulnTypes = engine.getSusVulnTypes("id");
        String types = vulnTypes[0];
        assertTrue(types.contains("Cross-site Scripting (XSS)"));
        assertTrue(types.contains("Insecure Direct Object Reference (IDOR)"));
        assertTrue(types.contains("SQL Injection (SQLi)"));
        assertTrue(types.contains("Server-side Template Injection (SSTI)"));
    }

    @Test
    void getSusVulnTypesEmptyForNormalParam() {
        String[] types = engine.getSusVulnTypes("zzzz");
        org.junit.jupiter.api.Assertions.assertEquals("", types[0]);
        org.junit.jupiter.api.Assertions.assertEquals("", types[1]);
    }
}