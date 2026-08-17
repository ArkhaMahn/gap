/*
 * GAP - Get All Parameters, Links and Words
 * Port of the GAP Burp extension (https://github.com/xnl-h4ck3r/GAP-Burp-Extension)
 * to a ZAP add-on.
 */
package org.zaproxy.addon.gap.popup;

import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.gap.GapPanel;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHttpMessageContainer;

public class GapPopupMenuItem extends PopupMenuItemHttpMessageContainer {

    private static final long serialVersionUID = 1L;

    private final GapPanel gapPanel;

    public GapPopupMenuItem(GapPanel gapPanel) {
        super(Constant.messages.getString("gap.popup.name"));
        this.gapPanel = gapPanel;
    }

    @Override
    protected boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer container) {
        return invoker != Invoker.SITES_PANEL;
    }

    @Override
    protected void performActions(List<HttpMessage> httpMessages) {
        if (gapPanel == null || httpMessages == null || httpMessages.isEmpty()) {
            return;
        }
        gapPanel.runSelected(httpMessages);
    }

    @Override
    protected void performAction(HttpMessage httpMessage) {
        // All messages handled by performActions(List<HttpMessage>).
    }
}