/*
 * GAP - Get All Parameters, Links and Words
 * Port of the GAP Burp extension (https://github.com/xnl-h4ck3r/GAP-Burp-Extension)
 * to a ZAP add-on.
 */
package org.zaproxy.addon.gap;

import java.io.PrintWriter;
import java.io.Writer;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.OutputPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.addon.gap.popup.GapPopupMenu;
import org.zaproxy.addon.gap.popup.GapPopupMenuItem;

public class ExtensionGap extends ExtensionAdaptor {

    public static final String NAME = "ExtensionGap";

    private static final Logger LOGGER = LogManager.getLogger(ExtensionGap.class);

    private GapParam gapParam;
    private GapPanel gapPanel;

    public ExtensionGap() {
        super(NAME);
        this.setI18nPrefix("gap");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        this.gapParam = new GapParam();
        Model.getSingleton().getOptionsParam().addParamSet(gapParam);

        if (this.getView() != null) {
            this.gapPanel =
                    new GapPanel(gapParam, new PrintWriter(createOutputWriter(this.getView().getOutputPanel()), true));
            extensionHook.getHookView().addWorkPanel(gapPanel);
            extensionHook.getHookMenu().addPopupMenuItem(
                    new GapPopupMenuItem(gapPanel));
            extensionHook.getHookMenu().addPopupMenuItem(
                    new GapPopupMenu(gapPanel));
        }
    }

    @Override
    public void postInit() {
        if (this.getView() != null && gapPanel != null) {
            // Make the GAP tab deterministically visible and selected on startup, so the layout
            // changes are verifiable without hunting for the tab in the workbench strip.
            View.getSingleton().getMainFrame().getWorkbench().showPanel(gapPanel);
        }
    }

    @Override
    public boolean canUnload() {
        return true;
    }

    private static Writer createOutputWriter(OutputPanel outputPanel) {
        return new Writer() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(char[] cbuf, int off, int len) {
                buffer.append(cbuf, off, len);
            }

            @Override
            public void flush() {
                if (buffer.length() > 0) {
                    String text = buffer.toString();
                    buffer.setLength(0);
                    SwingUtilities.invokeLater(() -> outputPanel.append(text));
                }
            }

            @Override
            public void close() {
                flush();
            }
        };
    }

    @Override
    public void optionsLoaded() {
        super.optionsLoaded();
        if (this.gapParam != null) {
            this.gapParam.update();
        }
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("gap.ext.name");
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("gap.ext.desc");
    }

    @Override
    public String getAuthor() {
        return Constant.messages.getString("gap.ext.author");
    }

    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}