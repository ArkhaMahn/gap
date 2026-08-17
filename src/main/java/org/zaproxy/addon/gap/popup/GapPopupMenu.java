/*
 * GAP - Get All Parameters, Links and Words
 * Port of the GAP Burp extension (https://github.com/xnl-h4ck3r/GAP-Burp-Extension)
 * to a ZAP add-on.
 */
package org.zaproxy.addon.gap.popup;

import org.parosproxy.paros.Constant;
import org.zaproxy.addon.gap.GapPanel;
import org.zaproxy.zap.view.popup.PopupMenuSiteNodeContainer;

/**
 * Site Map tree "GAP" submenu, exposing a request-only and a response-only run.
 */
public class GapPopupMenu extends PopupMenuSiteNodeContainer {

    private static final long serialVersionUID = 1L;

    public GapPopupMenu(GapPanel gapPanel) {
        super(Constant.messages.getString("gap.popup.name"));
        // Higher weight places item earlier in the context menu (above weight 0 items)
        setWeight(1);
        add(new GapSiteMapPopupRequestMenuItem(gapPanel));
        add(new GapSiteMapPopupResponseMenuItem(gapPanel));
    }
}
