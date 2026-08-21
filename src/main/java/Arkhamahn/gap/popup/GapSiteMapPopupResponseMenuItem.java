/*
 * GAP - Get All Parameters, Links and Words
 * Port of the GAP Burp extension (https://github.com/xnl-h4ck3r/GAP-Burp-Extension)
 * to a ZAP add-on.
 */
package Arkhamahn.gap.popup;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.SiteNode;
import Arkhamahn.gap.GapPanel;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

public class GapSiteMapPopupResponseMenuItem extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = 1L;

    private final GapPanel gapPanel;

    public GapSiteMapPopupResponseMenuItem(GapPanel gapPanel) {
        super(Constant.messages.getString("gap.popup.sendResponse"));
        this.gapPanel = gapPanel;
    }

    @Override
    protected void performAction(SiteNode siteNode) {
        if (gapPanel == null || siteNode == null) {
            return;
        }
        gapPanel.runSiteMapResponses(siteNode);
    }
}
