/*
 * GAP - Get All Parameters, Links and Words
 * Context used by the GapEngine for the actions that require a host
 * (ZAP session / UI). Decoupled from GapPanel so the engine is testable.
 */
package org.zaproxy.addon.gap;

import org.parosproxy.paros.network.HttpMessage;

public interface GapContext {

    boolean isInScope(String url);

    void createIssue(HttpMessage httpMessage, String issueDetail, String confidence);
}