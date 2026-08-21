package Arkhamahn.gap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.alert.ExtensionAlert;

/**
 * The GAP work panel. Ported from the {@code BurpExtender} UI of GAP.py: holds all options,
 * displays the found parameters/links/words and orchestrates a GAP run.
 */
public class GapPanel extends AbstractPanel implements GapContext {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(GapPanel.class);

    private final GapParam param;
    private final GapEngine engine;
    private final PrintWriter stderr;

    // UI - mode
    private final JCheckBox cbParamsEnabled = defineCheckBox("Parameters", true);
    private final JCheckBox cbLinksEnabled = defineCheckBox("Links", true);
    private final JCheckBox cbWordsEnabled = defineCheckBox("Words", true);

    // UI - parameters
    private final JCheckBox cbParamUrl = defineCheckBox("Query string params", true);
    private final JCheckBox cbParamBody = defineCheckBox("Message body params", true);
    private final JCheckBox cbParamMultiPart =
            defineCheckBox("Param attribute in multi-part message body", true);
    private final JCheckBox cbParamJson = defineCheckBox("JSON params", false);
    private final JCheckBox cbParamCookie = defineCheckBox("Cookie names", false);
    private final JCheckBox cbParamXml = defineCheckBox("Items of data in XML structure", false);
    private final JCheckBox cbParamXmlAttr =
            defineCheckBox("Value of tag attributes in XML structure", false);
    private final JCheckBox cbReportSusParams = defineCheckBox("Report \"sus\" params?", true);
    private final JCheckBox cbIncludeTentative = defineCheckBox("Inc. Tentative?", true);
    private final JCheckBox cbIncludePathWords = defineCheckBox("Include URL path words?", false);
    private final JCheckBox cbParamJSONResponse = defineCheckBox("JSON params", false);
    private final JCheckBox cbParamXMLResponse =
            defineCheckBox("Value of tag attributes in XML structure", false);
    private final JCheckBox cbParamInputField =
            defineCheckBox("Name and Id attributes of HTML input fields", false);
    private final JCheckBox cbParamJSVars =
            defineCheckBox("Javascript variables and constants", false);
    private final JCheckBox cbParamFromLinks = defineCheckBox("Params from links found", false);

    // UI - links
    private final JCheckBox cbLinkPrefix = defineCheckBox("link(s):", false);
    private final JCheckBox cbLinkPrefixScope = defineCheckBox("selected target(s)", false);
    private final JCheckBox cbLinkPrefixOrigin = defineCheckBox("origin target only", false);
    private final JCheckBox cbUnPrefixed = defineCheckBox("Also include un-prefixed links?", false);
    private final JCheckBox cbSiteMapEndpoints = defineCheckBox("Include site map endpoints?", false);
    private final JCheckBox cbRelativeLinks = defineCheckBox("Include relative links?", true);
    private final JTextField inLinkPrefix = new JTextField(GapConstants.DEFAULT_LINK_PREFIX, 30);

    // UI - words
    private final JCheckBox cbWordPlurals = defineCheckBox("Create singular/plural word?", true);
    private final JCheckBox cbWordPaths = defineCheckBox("Include URL path words?", false);
    private final JCheckBox cbWordParams = defineCheckBox("Include potential params?", false);
    private final JCheckBox cbWordComments = defineCheckBox("Include HTML comments?", true);
    private final JCheckBox cbWordImgAlt = defineCheckBox("Include IMG ALT attribute?", true);
    private final JCheckBox cbWordDigits = defineCheckBox("Include words with digits?", true);
    private final JCheckBox cbWordLower = defineCheckBox("Create lowercase words?", true);
    private final JTextField inWordsMinLen = new JTextField("3", 2);
    private final JTextField inWordsMaxlen = new JTextField("", 3);
    private final JTextField inStopWords = new JTextField(GapConstants.DEFAULT_STOP_WORDS, 30);

    // UI - other
    private final JCheckBox cbToolTips = defineCheckBox("Show contextual help", true);
    private final JCheckBox cbSaveFile = defineCheckBox("Auto save output to directory", true);
    private final JCheckBox cbExclusions = defineCheckBox("Link exclusions:", true);
    private final JCheckBox cbShowTabOnStartup = defineCheckBox("Show tab on startup", false);
    private final JTextField inExclusions = new JTextField(GapConstants.DEFAULT_EXCLUSIONS, 30);
    private final JTextField inSaveDir = new JTextField();
    private final JTextField inQueryStringVal = new JTextField(GapConstants.DEFAULT_QSV, 5);
    private final JButton btnChooseDir = new JButton("Choose...");
    private final JButton btnSave = new JButton("Save options");
    private final JButton btnRestoreDefaults = new JButton("Restore defaults");
    private final JButton btnClear = new JButton("Clear");
    private final JButton btnCancel = new JButton("   COMPLETED    ");
    private final JProgressBar progBar = new JProgressBar();
    private final JLabel progStage = new JLabel();
    private final JButton btnHelp = new JButton("?");
    private final JButton btnKoFi = new JButton("Buy Me a Coffee!");

    // UI - results
    private final JCheckBox cbShowParamOrigin = defineCheckBox("Show origin", false);
    private final JCheckBox cbShowSusParams = defineCheckBox("Show \"sus\"", false);
    private final JCheckBox cbShowQueryString = defineCheckBox("Show query string with value", false);
    private final JCheckBox cbShowWordOrigin = defineCheckBox("Show origin", false);
    private final JCheckBox cbShowLinkOrigin = defineCheckBox("Show origin endpoint", false);
    private final JCheckBox cbInScopeOnly = defineCheckBox("In scope only", false);
    private final JCheckBox cbLinkFilterNeg = defineCheckBox("Negative match", false);
    private final JCheckBox cbLinkCaseSens = defineCheckBox("Case sensitive", false);
    private final JTextField inLinkFilter = new JTextField(10);
    private final JButton btnFilter = new JButton("Apply filter");
    private final JLabel lblParamList = new JLabel("Potential params found:");
    private final JLabel lblLinkList = new JLabel("Potential links found:");
    private final JLabel lblWordList = new JLabel("Words found:");
    private final JTextArea outParamList = new JTextArea(30, 100);
    private final JTextArea outParamSus = new JTextArea(30, 100);
    private final JTextArea outParamQuery = new JTextArea(30, 100);
    private final JTextArea outLinkList = new JTextArea(30, 100);
    private final JTextArea outWordList = new JTextArea(30, 100);
    private final JScrollPane scrollOutParamList = new JScrollPane(outParamList);
    private final JScrollPane scrollOutLinkList = new JScrollPane(outLinkList);
    private final JScrollPane scrollOutWordList = new JScrollPane(outWordList);

    private final JTextArea txtDebug = new JTextArea(2, 80);
    private final JTextArea txtDebugDetail = new JTextArea(2, 80);

    // Display state
    private String txtParamsOnly = "";
    private String txtParamsWithURL = "";
    private String txtParamsSusOnly = "";
    private String txtParamsSusWithURL = "";
    private String txtParamQuery = "";
    private String txtParamQuerySus = "";
    private String txtLinksOnly = "";
    private String txtLinksWithURL = "";
    private String txtLinksOnlyInScopeOnly = "";
    private String txtLinksWithURLInScopeOnly = "";
    private String txtLinksFiltered = "";
    private String txtWordsOnly = "";
    private String txtWordsWithURL = "";

    private int countParam;
    private int countParamSus;
    private int countParamUnique;
    private int countParamSusUnique;
    private int countLinkUnique;
    private int countWordUnique;

    // Run state
    private volatile boolean flagCancel;
    private volatile boolean isRunning;
    private final Set<String> roots = new LinkedHashSet<>();
    private final Set<String> allScopePrefixes = new LinkedHashSet<>();
    private final Set<String> allRootsForDialog = new LinkedHashSet<>();
    private final Set<String> raisedIssues = new HashSet<>();
    private int lastRunContext = -1;
    private String lastRunDate = "";
    private Color linkPrefixColor;
    private String currentTitle = "GAP";

    private final List<OutputMouseListener> outputMouseListeners = new ArrayList<>();

    public GapPanel(GapParam param, PrintWriter stderr) {
        super();
        this.param = param;
        this.stderr = stderr;
        this.engine = new GapEngine(this, param, stderr);
        initUi();
        restoreSavedConfig();
        setTabIndex(1);
        setName("GAP");
    }

    public GapEngine getEngine() {
        return engine;
    }

    public GapParam getParam() {
        return param;
    }

    // ------------------------------------------------------------------
    // UI construction
    // ------------------------------------------------------------------

    private static JCheckBox defineCheckBox(String caption, boolean selected) {
        JCheckBox checkBox = new JCheckBox(caption);
        checkBox.setSelected(selected);
        return checkBox;
    }

    private static void setFontRecursive(java.awt.Container container, Font font) {
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) c).setFont(font);
            }
            if (c instanceof java.awt.Container) {
                setFontRecursive((java.awt.Container) c, font);
            }
        }
    }

    private void initUi() {
        // Two-column layout mirroring the original GAP extension tab:
        // left options column (fixed width), right results column (flexible).
        JPanel leftPanel = buildOptionsPanel();
        JPanel rightPanel = buildResultsPanel();

        // The panel is wide (two dense option columns plus three result columns); drop the font
        // one point so its minimum width fits the tab area of a typical ZAP window without a
        // horizontal scrollbar.
        Font baseFont = getFont();
        if (baseFont != null) {
            Font compact = baseFont.deriveFont((float) Math.max(10, baseFont.getSize() - 1));
            setFontRecursive(leftPanel, compact);
            setFontRecursive(rightPanel, compact);
        }

        // The header labels grow at run time to show the count ("Potential params found - 11
        // filtered:") and would otherwise widen the params/words columns' minimum, overflowing the
        // tab and pushing the Clear button out of view. Lock the labels' minimum at the current
        // (compact-font) natural minimum, captured before any count text is set; the count text
        // still renders when there is room.
        lblParamList.setMinimumSize(lblParamList.getMinimumSize());
        lblWordList.setMinimumSize(lblWordList.getMinimumSize());
        lblLinkList.setMinimumSize(lblLinkList.getMinimumSize());

        // Tracks the viewport width so the panel always fills the tab width (no horizontal
        // scrollbar) but keeps its natural preferred height, so a short window scrolls
        // vertically instead of compressing rows together.
        JPanel content = new GapContentPanel();
        GroupLayout gl = new GroupLayout(content);
        content.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);
        gl.setHorizontalGroup(
                gl.createParallelGroup()
                        .addGroup(
                                gl.createSequentialGroup()
                                        .addComponent(
                                                leftPanel,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addGap(4)
                                        .addComponent(rightPanel)));
        gl.setVerticalGroup(
                gl.createSequentialGroup()
                        .addGroup(
                                gl.createParallelGroup()
                                        .addComponent(
                                                leftPanel,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(rightPanel)));
        setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(content);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Output areas
        styleOutputArea(outParamList, false);
        styleOutputArea(outParamSus, true);
        styleOutputArea(outParamQuery, true);
        styleOutputArea(outLinkList, false);
        styleOutputArea(outWordList, true);
        txtDebug.setVisible(false);
        txtDebug.setLineWrap(true);
        txtDebug.setEditable(false);
        txtDebugDetail.setVisible(false);
        txtDebugDetail.setLineWrap(true);
        txtDebugDetail.setEditable(false);

        scrollOutParamList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollOutParamList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollOutLinkList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollOutLinkList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollOutWordList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollOutWordList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // The output boxes must stay visible as distinct ash areas whatever the theme
        // (ZAP's FlatLaf paints JTextArea with the transparent panel background), so give them an
        // explicit background and border.
        scrollOutParamList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollOutLinkList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollOutWordList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Keep the panel's preferred size from ballooning with the (30, 100) text areas: the
        // result areas are always stretched to the available width, so give the scroll panes a
        // zero preferred width (same approach as the jwteditor add-on). Their preferred height is
        // also capped: with the results panel now living inside a vertical scroll pane, an
        // uncapped height would lay the whole content out at ~1100px and push the "Potential
        // links found" row below the fold at typical window sizes. The compact preferred height
        // keeps the original tab proportions (the GroupLayout grows the boxes to fill a taller
        // viewport) while short windows still scroll instead of overlapping.
        scrollOutParamList.setPreferredSize(new Dimension(0, 150));
        scrollOutWordList.setPreferredSize(new Dimension(0, 150));
        scrollOutLinkList.setPreferredSize(new Dimension(0, 150));
        // The text areas' minimum size tracks their content, so a run that fills the boxes with
        // long lines would widen the results panel past the tab and clip the right-most controls
        // (e.g. the Clear button on the exclusions row). Floor the scroll panes' minimum to the
        // same compact size as their preferred so the results column never grows with content.
        scrollOutParamList.setMinimumSize(new Dimension(0, 150));
        scrollOutWordList.setMinimumSize(new Dimension(0, 150));
        scrollOutLinkList.setMinimumSize(new Dimension(0, 150));

        // Text fields keep a full-preferred-width minimum, so a long column count (e.g. the 30-col
        // stop-words field) would push the results panel wider than the tab and force horizontal
        // clipping on narrow windows. Floor their minimum so the display row can shrink. The value
        // field ("XNLV") and the stop-words field must also match the compact height of the "Link
        // filter" input so their rows stay slim (they previously stretched to fill the panel).
        Dimension linkFilterHeight = new Dimension(0, inLinkFilter.getPreferredSize().height);
        // JTextField.getPreferredSize() recomputes width from getColumns(), so a setPreferredSize
        // width is ignored; the column count is what keeps the stop-words field compact.
        inStopWords.setColumns(16);
        inStopWords.setPreferredSize(new Dimension(180, linkFilterHeight.height));
        inStopWords.setMinimumSize(new Dimension(70, linkFilterHeight.height));
        inQueryStringVal.setPreferredSize(new Dimension(inQueryStringVal.getPreferredSize().width, linkFilterHeight.height));
        inQueryStringVal.setMinimumSize(new Dimension(36, linkFilterHeight.height));

        inSaveDir.setEditable(false);
        inSaveDir.setText(param.getSaveDir());
        inSaveDir.setColumns(30); // matches original GAP field width

        progBar.setStringPainted(true);
        progBar.setVisible(false);
        progStage.setVisible(false);

        btnCancel.setBackground(GapConstants.COLOR_LIGHT_BLUE);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setVisible(false);
        btnCancel.setPreferredSize(new Dimension(100, 25)); // smaller button

        btnChooseDir.setPreferredSize(new Dimension(80, 25)); // smaller choose button

        btnClear.setPreferredSize(new Dimension(80, 25)); // smaller clear button

        cbParamsEnabled.addItemListener(e -> cbParamsEnabled_clicked());
        cbLinksEnabled.addItemListener(e -> cbLinksEnabled_clicked());
        cbWordsEnabled.addItemListener(e -> cbWordsEnabled_clicked());
        cbToolTips.addItemListener(e -> setContextHelp(cbToolTips.isSelected()));
        cbReportSusParams.addItemListener(e -> cbReportSusParams_clicked());
        cbSaveFile.addItemListener(e -> cbSaveFile_clicked());
        cbLinkPrefix.addItemListener(e -> cbLinkPrefix_clicked());
        cbLinkPrefixScope.addItemListener(e -> cbLinkPrefixScope_clicked());
        cbLinkPrefixOrigin.addItemListener(e -> cbLinkPrefixOrigin_clicked());
        cbExclusions.addItemListener(e -> cbExclusions_clicked());
        cbShowParamOrigin.addItemListener(e -> changeParamDisplay());
        cbShowSusParams.addItemListener(e -> changeParamDisplay());
        cbShowQueryString.addItemListener(e -> cbShowQueryString_clicked());
        cbShowWordOrigin.addItemListener(e -> changeWordDisplay());
        cbShowLinkOrigin.addItemListener(e -> changeLinkDisplay());
        cbInScopeOnly.addItemListener(e -> changeLinkDisplay());
        inWordsMinLen.addActionListener(e -> checkWordLength());
        inWordsMaxlen.addActionListener(e -> checkWordLength());
        inLinkPrefix.addActionListener(e -> checkLinkPrefix());
        inLinkPrefix.addMouseListener(new LinkPrefixFieldMouseListener(this));
        btnChooseDir.addActionListener(e -> btnChooseDir_clicked());
        btnSave.addActionListener(e -> btnSave_clicked());
        btnRestoreDefaults.addActionListener(e -> btnRestoreDefaults_clicked());
        btnClear.addActionListener(e -> clearOutput());
        btnCancel.addActionListener(e -> btnCancel_clicked());
        btnHelp.addActionListener(e -> btnHelp_clicked());
        btnKoFi.addActionListener(e -> btnKoFi_clicked());
        btnFilter.addActionListener(e -> btnFilter_clicked());
        inLinkFilter.addKeyListener(new CustomKeyListener(btnFilter));
        progBar.addMouseListener(new ProgressBarMouseListener(this));

        inQueryStringVal.setEnabled(false);
        inQueryStringVal.setEditable(true);
        cbShowSusParams.setEnabled(false);
        cbShowQueryString.setEnabled(false);
        cbShowParamOrigin.setEnabled(false);
        cbShowLinkOrigin.setEnabled(false);
        cbInScopeOnly.setEnabled(false);
        cbShowWordOrigin.setEnabled(false);
        btnFilter.setEnabled(false);
        inLinkFilter.setEnabled(false);
        cbLinkFilterNeg.setEnabled(false);
        cbLinkCaseSens.setEnabled(false);
        cbExclusions.setEnabled(false);

        outLinkList.addMouseListener(
                new OutputMouseListener(outLinkList, "Links", cbShowLinkOrigin, cbInScopeOnly));
        outParamList.addMouseListener(new OutputMouseListener(outParamList, "Param", null, null));
        outParamSus.addMouseListener(new OutputMouseListener(outParamSus, "Param", null, null));
        outParamQuery.addMouseListener(
                new OutputMouseListener(outParamQuery, "ParamQuery", null, null));
        outWordList.addMouseListener(new OutputMouseListener(outWordList, "Words", null, null));

        setContextHelp(cbToolTips.isSelected());
    }

    private static void styleOutputArea(JTextArea area, boolean wrap) {
        area.setLineWrap(wrap);
        area.setWrapStyleWord(wrap);
        // Read-only output: the areas hold scan results, so they must never be edited in place,
        // but must stay selectable so the content can still be selected and copied (or copied via
        // the right-click context menu).
        area.setEditable(false);
        area.setOpaque(true);
        area.setBackground(GapConstants.COLOR_OUTPUT_BG);
        area.setForeground(Color.BLACK);
        area.setCaretColor(Color.BLACK);
    }

    // Greys the output areas (and restores them) when a mode is unchecked/checked. The areas are
    // left enabled so their content stays selectable/copyable, even while they look greyed out.
    private static void setOutputAreasEnabled(boolean enabled, JTextArea... areas) {
        for (JTextArea area : areas) {
            area.setForeground(enabled ? Color.BLACK : Color.GRAY);
        }
    }

    /**
     * Content panel for the GAP tab: fills the viewport width (so the tab always spans the full
     * width) but keeps its natural preferred height, so a short window scrolls vertically instead
     * of compressing rows together. The {@code Scrollable} contract is implemented directly
     * because some bundled JDKs do not expose it on {@code JComponent}.
     */
    private static final class GapContentPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 100;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private JPanel buildOptionsPanel() {
        JPanel panel = new JPanel();
        GroupLayout gl = new GroupLayout(panel);
        panel.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        Color accent = GapConstants.COLOR_LIGHT_BLUE;

        // GAP Mode group: banner + mode label + mode checkboxes (plain line border, no title)
        JLabel lblGapBanner = new JLabel("GAP Zap Extension by Xnl-h4ck3r");
        JLabel lblMode = new JLabel("GAP Mode: ");
        Font modeFont = lblMode.getFont();
        lblMode.setFont(modeFont.deriveFont(modeFont.getStyle() | Font.BOLD));
        lblMode.setForeground(accent);
        JPanel grpMode =
                borderedBox(lblGapBanner, lblMode, cbParamsEnabled, cbLinksEnabled, cbWordsEnabled);

        // Parameters section headers
        JLabel lblWhichParams = header("Parameters mode options:", accent);
        JLabel lblRequestParams = subHeader("REQUEST PARAMETERS");
        JLabel lblResponseParams = subHeader("RESPONSE PARAMETERS");

        // Help group ("Click for help -->" + "?" button) and Ko-Fi group
        JLabel lblHelp = new JLabel("Click for help -->");
        Font helpFont = lblHelp.getFont();
        lblHelp.setFont(helpFont.deriveFont(helpFont.getStyle() | Font.BOLD));
        lblHelp.setForeground(accent);
        btnHelp.setFont(btnHelp.getFont().deriveFont(Font.BOLD));
        btnHelp.setForeground(Color.WHITE);
        btnHelp.setBorder(BorderFactory.createLineBorder(accent, 2));
        btnHelp.setContentAreaFilled(true);
        btnHelp.setBackground(accent);
        btnHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHelp.setToolTipText("Click me for help!");
        JPanel grpHelp = borderedBox(lblHelp, btnHelp);

        styleKoFiButton();
        JPanel grpKoFi = borderedBox(btnKoFi);

        // Words section
        JLabel lblWhichWords = header("Words mode options:", accent);
        JLabel lblWordsLength = new JLabel("Word length:");
        JLabel lblWordsTo = new JLabel("to");
        JLabel lblWordsExcludePlurals = new JLabel("(excludes plurals)");

        // Links section
        JLabel lblLinkOptions = header("Links mode options:", accent);
        JLabel lblPrefixWith = new JLabel("Prefix with:");

        // Other options
        JLabel lblOutputOptions = header("Other options:", accent);
        cbToolTips.setForeground(accent);

        // Config group (Restore defaults / Save options / COMPLETED / progress bar) with the Clear
        // button pinned to the far right of the row.
        JPanel grpConfig = new JPanel(new BorderLayout(5, 0));
        JPanel grpConfigLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        grpConfigLeft.add(btnRestoreDefaults);
        grpConfigLeft.add(btnSave);
        grpConfigLeft.add(Box.createHorizontalStrut(10));
        grpConfigLeft.add(btnCancel);
        grpConfigLeft.add(progBar);
        grpConfigLeft.add(progStage);
        grpConfig.add(grpConfigLeft, BorderLayout.WEST);
        JPanel grpConfigRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        grpConfigRight.add(btnClear);
        grpConfig.add(grpConfigRight, BorderLayout.EAST);

        gl.setHorizontalGroup(
                gl.createParallelGroup()
                        .addComponent(
                                grpMode,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblWhichParams)
                        .addGroup(
                                gl.createSequentialGroup()
                                        .addGroup(
                                                gl.createParallelGroup()
                                                        .addComponent(cbIncludePathWords)
                                                        .addComponent(lblRequestParams)
                                                        .addComponent(cbParamUrl)
                                                        .addComponent(cbParamBody)
                                                        .addComponent(cbParamMultiPart)
                                                        .addComponent(cbParamJson)
                                                        .addComponent(cbParamCookie)
                                                        .addComponent(cbParamXml)
                                                        .addComponent(cbParamXmlAttr))
                                        .addGroup(
                                                gl.createParallelGroup()
                                                        .addGroup(
                                                                gl.createSequentialGroup()
                                                                        .addComponent(cbReportSusParams)
                                                                        .addComponent(cbIncludeTentative))
                                                        .addComponent(lblResponseParams)
                                                        .addComponent(cbParamJSONResponse)
                                                        .addComponent(cbParamXMLResponse)
                                                        .addComponent(cbParamInputField)
                                                        .addComponent(cbParamJSVars)
                                                        .addComponent(cbParamFromLinks)
                                                        .addGroup(
                                                                gl.createSequentialGroup()
                                                                        .addComponent(
                                                                                grpHelp,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE)
                                                                        .addComponent(
                                                                                grpKoFi,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE))))
                        .addComponent(lblLinkOptions)
                        .addGroup(
                                gl.createSequentialGroup()
                                        .addComponent(lblPrefixWith)
                                        .addComponent(cbLinkPrefixScope)
                                        .addComponent(cbLinkPrefixOrigin)
                                        .addComponent(cbLinkPrefix)
                                        .addComponent(inLinkPrefix))
                        .addGroup(
                                gl.createSequentialGroup()
                                        .addComponent(cbUnPrefixed)
                                        .addComponent(cbSiteMapEndpoints)
                                        .addComponent(cbRelativeLinks))
                        .addComponent(lblWhichWords)
                        .addGroup(
                                gl.createSequentialGroup()
                                        .addGroup(
                                                gl.createParallelGroup()
                                                        .addComponent(cbWordLower)
                                                        .addComponent(cbWordPlurals)
                                                        .addGroup(
                                                                gl.createSequentialGroup()
                                                                        .addComponent(lblWordsLength)
                                                                        .addComponent(
                                                                                inWordsMinLen,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE)
                                                                        .addComponent(lblWordsTo)
                                                                        .addComponent(
                                                                                inWordsMaxlen,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE)))
                                        .addGroup(
                                                gl.createParallelGroup()
                                                        .addComponent(cbWordComments)
                                                        .addComponent(cbWordImgAlt)
                                                        .addComponent(lblWordsExcludePlurals))
                                        .addGroup(
                                                gl.createParallelGroup()
                                                        .addComponent(cbWordDigits)
                                                        .addComponent(cbWordPaths)
                                                        .addComponent(cbWordParams)))
                        .addGroup(
                                gl.createSequentialGroup()
                                        .addComponent(lblOutputOptions)
                                        .addComponent(cbToolTips)
                                        .addComponent(cbShowTabOnStartup))
                        .addGroup(
                                gl.createSequentialGroup()
                                        .addComponent(cbSaveFile)
                                        .addComponent(inSaveDir)
                                        .addComponent(btnChooseDir))
                        .addComponent(
                                grpConfig,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE)
                        .addComponent(
                                txtDebug,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE)
                        .addComponent(
                                txtDebugDetail,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE));

        gl.setVerticalGroup(
                gl.createSequentialGroup()
                        .addComponent(
                                grpMode,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblWhichParams)
                        .addGroup(
                                gl.createParallelGroup()
                                        .addGroup(
                                                gl.createSequentialGroup()
                                                        .addComponent(cbIncludePathWords)
                                                        .addComponent(lblRequestParams)
                                                        .addComponent(cbParamUrl)
                                                        .addComponent(cbParamBody)
                                                        .addComponent(cbParamMultiPart)
                                                        .addComponent(cbParamJson)
                                                        .addComponent(cbParamCookie)
                                                        .addComponent(cbParamXml)
                                                        .addComponent(cbParamXmlAttr))
                                        .addGroup(
                                                gl.createSequentialGroup()
                                                        .addGroup(
                                                                gl.createParallelGroup()
                                                                        .addComponent(cbReportSusParams)
                                                                        .addComponent(cbIncludeTentative))
                                                        .addComponent(lblResponseParams)
                                                        .addComponent(cbParamJSONResponse)
                                                        .addComponent(cbParamXMLResponse)
                                                        .addComponent(cbParamInputField)
                                                        .addComponent(cbParamJSVars)
                                                        .addComponent(cbParamFromLinks)
                                                        .addGroup(
                                                                gl.createParallelGroup()
                                                                        .addComponent(
                                                                                grpHelp,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE)
                                                                        .addComponent(
                                                                                grpKoFi,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE))))
                        .addComponent(lblLinkOptions)
                        .addGroup(
                                gl.createParallelGroup()
                                        .addComponent(lblPrefixWith)
                                        .addComponent(cbLinkPrefixScope)
                                        .addComponent(cbLinkPrefixOrigin)
                                        .addComponent(cbLinkPrefix)
                                        .addComponent(
                                                inLinkPrefix,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                        .addGroup(
                                gl.createParallelGroup()
                                        .addComponent(cbUnPrefixed)
                                        .addComponent(cbSiteMapEndpoints)
                                        .addComponent(cbRelativeLinks))
                        .addComponent(lblWhichWords)
                        .addGroup(
                                gl.createParallelGroup()
                                        .addGroup(
                                                gl.createSequentialGroup()
                                                        .addComponent(cbWordLower)
                                                        .addComponent(cbWordPlurals)
                                                        .addGroup(
                                                                gl.createParallelGroup()
                                                                        .addComponent(lblWordsLength)
                                                                        .addComponent(
                                                                                inWordsMinLen,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE)
                                                                        .addComponent(lblWordsTo)
                                                                        .addComponent(
                                                                                inWordsMaxlen,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE)))
                                        .addGroup(
                                                gl.createSequentialGroup()
                                                        .addComponent(cbWordComments)
                                                        .addComponent(cbWordImgAlt)
                                                        .addComponent(lblWordsExcludePlurals))
                                        .addGroup(
                                                gl.createSequentialGroup()
                                                        .addComponent(cbWordDigits)
                                                        .addComponent(cbWordPaths)
                                                        .addComponent(cbWordParams)))
                        .addGroup(
                                gl.createParallelGroup()
                                        .addComponent(lblOutputOptions)
                                        .addComponent(cbToolTips)
                                        .addComponent(cbShowTabOnStartup))
                        .addGroup(
                                gl.createParallelGroup()
                                        .addComponent(cbSaveFile)
                                        .addComponent(
                                                inSaveDir,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnChooseDir))
                        .addComponent(
                                grpConfig,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(
                                txtDebug,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE)
                        .addComponent(
                                txtDebugDetail,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE));
        return panel;
    }

    /**
     * Loads the Ko-Fi button image asynchronously (so the UI thread is never blocked by the
     * download). Falls back to a plain text button if the image cannot be loaded.
     */
    private void styleKoFiButton() {
        btnKoFi.setFont(btnKoFi.getFont().deriveFont(Font.BOLD));
        btnKoFi.setForeground(Color.WHITE);
        btnKoFi.setBackground(GapConstants.COLOR_LIGHT_BLUE);
        btnKoFi.setBorder(BorderFactory.createLineBorder(GapConstants.COLOR_LIGHT_BLUE, 2));
        btnKoFi.setContentAreaFilled(true);
        btnKoFi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnKoFi.setToolTipText("Buy Me a Coffee!");
        Thread iconLoader =
                new Thread(
                        () -> {
                            try {
                                ImageIcon icon = new ImageIcon(new java.net.URL(GapConstants.URL_KOFI_BUTTON));
                                if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                                    return;
                                }
                                int height = Math.max(16, btnKoFi.getFont().getSize() + 2);
                                int width = (int) Math.round(height * 2.4);
                                Image scaled =
                                        icon.getImage()
                                                .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                                ImageIcon scaledIcon = new ImageIcon(scaled);
                                SwingUtilities.invokeLater(
                                        () -> {
                                            btnKoFi.setIcon(scaledIcon);
                                            btnKoFi.setText("");
                                            btnKoFi.setContentAreaFilled(false);
                                            btnKoFi.setBorder(BorderFactory.createEmptyBorder());
                                            btnKoFi.setBackground(null);
                                        });
                            } catch (Exception e) {
                                // Icon unavailable - keep the text button fallback
                            }
                        },
                        "GAP-KoFi-icon");
        iconLoader.setDaemon(true);
        iconLoader.start();
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel();
        GroupLayout gl = new GroupLayout(panel);
        panel.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        // Style the result count labels (also fixes them being added to the layout at all)
        styleHeader(lblParamList);
        styleHeader(lblWordList);
        styleHeader(lblLinkList);

        // Params section (top-left). BorderLayout so the "Show origin" checkbox never wraps onto a
        // clipped second line when the count label ("Potential params found - N unique:") is long
        // and the params column is narrow (all three modes enabled).
        JPanel paramHeader = new JPanel(new BorderLayout(5, 0));
        paramHeader.add(lblParamList, BorderLayout.WEST);
        paramHeader.add(cbShowParamOrigin, BorderLayout.EAST);

        // Words section (top-right). Same BorderLayout as the params header so the "Potential params
        // found:" and "Words found:" rows always sit at the same height whatever the window width.
        JPanel wordHeader = new JPanel(new BorderLayout(5, 0));
        wordHeader.add(lblWordList, BorderLayout.WEST);
        wordHeader.add(cbShowWordOrigin, BorderLayout.EAST);

        // Display rows: the "Show 'sus'" and "Show query string with value" checkboxes plus the
        // value field ("XNLV") share one row directly beneath the params output box; the
        // "Stop words:" field shares one row directly beneath the words output box. Both rows sit
        // at the same height and are pinned to their preferred size so the fields keep the same
        // compact height as the "Link filter" input instead of stretching to fill the panel.
        // BorderLayout nests shrink their fields to the minimum width when the results column is
        // narrow, instead of wrapping the field onto a second line.
        JPanel paramsDisplayRow = new JPanel(new BorderLayout(0, 0));
        JPanel paramsCheckboxes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        paramsCheckboxes.add(cbShowSusParams);
        paramsCheckboxes.add(cbShowQueryString);
        paramsDisplayRow.add(paramsCheckboxes, BorderLayout.WEST);
        paramsDisplayRow.add(inQueryStringVal, BorderLayout.CENTER);

        JPanel wordsDisplayRow = new JPanel(new BorderLayout(5, 0));
        wordsDisplayRow.add(new JLabel("Stop words:"), BorderLayout.WEST);
        wordsDisplayRow.add(inStopWords, BorderLayout.CENTER);

        // Links section (bottom, full width)
        JPanel linkHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkHeader.add(lblLinkList);
        linkHeader.add(cbShowLinkOrigin);
        linkHeader.add(cbInScopeOnly);

        JPanel grpLinkFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel lblLinkFilter = new JLabel("Link filter:");
        lblLinkFilter.setEnabled(false);
        grpLinkFilter.add(lblLinkFilter);
        grpLinkFilter.add(inLinkFilter);
        grpLinkFilter.add(cbLinkFilterNeg);
        grpLinkFilter.add(cbLinkCaseSens);
        grpLinkFilter.add(btnFilter);

        // Clear sits on the same row as the exclusions input: it clears the results shown here.
        JPanel exclusionsRow = new JPanel(new BorderLayout(5, 0));
        exclusionsRow.add(cbExclusions, BorderLayout.WEST);
        exclusionsRow.add(inExclusions, BorderLayout.CENTER);

        gl.setHorizontalGroup(
                gl.createParallelGroup()
                        .addGroup(
                                gl.createSequentialGroup()
.addGroup(
                                gl.createParallelGroup()
                                        .addComponent(
                                                paramHeader,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scrollOutParamList)
                                        .addComponent(paramsDisplayRow))
                                        .addGap(10)
                                        .addGroup(
                                gl.createParallelGroup()
                                        .addComponent(
                                                wordHeader,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scrollOutWordList)
                                        .addComponent(wordsDisplayRow)))
                        .addComponent(
                                linkHeader,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(scrollOutLinkList)
                        .addComponent(
                                grpLinkFilter,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(exclusionsRow));

gl.setVerticalGroup(
                gl.createSequentialGroup()
                        .addGroup(
                                gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(
                                                gl.createSequentialGroup()
                                                        .addComponent(
                                                                paramHeader,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(scrollOutParamList, 150, 150, 150)
                                                        .addComponent(
                                                                paramsDisplayRow,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addGroup(
                                                gl.createSequentialGroup()
                                                        .addComponent(
                                                                wordHeader,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(scrollOutWordList, 150, 150, 150)
                                                        .addComponent(
                                                                wordsDisplayRow,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)))
                        .addGap(24)
                        .addComponent(
                                linkHeader,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(scrollOutLinkList, 150, 150, 150)
                        .addComponent(
                                grpLinkFilter,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(
                                exclusionsRow,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE));

        return panel;
    }

    private static JPanel borderedBox(Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBorder(BorderFactory.createLineBorder(GapConstants.COLOR_LIGHT_BLUE, 2));
        for (Component component : components) {
            if (component != null) {
                panel.add(component);
            }
        }
        return panel;
    }

    private static void styleHeader(JLabel label) {
        Font font = label.getFont();
        label.setFont(font.deriveFont(font.getStyle() | Font.BOLD, font.getSize() + 2));
        label.setForeground(GapConstants.COLOR_LIGHT_BLUE);
    }

    private static JLabel header(String text, Color color) {
        JLabel label = new JLabel(text);
        Font f = label.getFont();
        label.setFont(f.deriveFont(f.getStyle() | Font.BOLD, f.getSize() + 2));
        label.setForeground(color);
        return label;
    }

    private static JLabel subHeader(String text) {
        JLabel label = new JLabel(text);
        Font f = label.getFont();
        label.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        return label;
    }

    // ------------------------------------------------------------------
    // Tab title/color helpers
    // ------------------------------------------------------------------

    private void setTabTitle(String title) {
        currentTitle = title;
        try {
            JTabbedPane tp = (JTabbedPane) getParent();
            if (tp != null && getTabIndex() >= 0 && getTabIndex() < tp.getTabCount()) {
                tp.setTitleAt(getTabIndex(), title);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void setTabColor(Color color) {
        try {
            JTabbedPane tp = (JTabbedPane) getParent();
            if (tp != null && getTabIndex() >= 0 && getTabIndex() < tp.getTabCount()) {
                tp.setForegroundAt(getTabIndex(), color);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void setTabDefaultColor() {
        setTabColor(GapConstants.COLOR_LIGHT_BLUE);
        setTabTitle("GAP");
    }

    // ------------------------------------------------------------------
    // Option handlers
    // ------------------------------------------------------------------

    private void setContextHelp(boolean enable) {
        // GAP.py uses tooltips; apply to the option widgets
    }

    private void cbParamsEnabled_clicked() {
        setTabDefaultColor();
        if (cbParamsEnabled.isSelected()) {
            setEnabledParamOptions(true);
            setOutputAreasEnabled(true, outParamList, outParamSus, outParamQuery);
        } else {
            setEnabledParamOptions(false);
            setOutputAreasEnabled(false, outParamList, outParamSus, outParamQuery);
            if (!cbLinksEnabled.isSelected() && !cbWordsEnabled.isSelected()) {
                cbLinksEnabled.setSelected(true);
            }
        }
    }

    private void cbLinksEnabled_clicked() {
        setTabDefaultColor();
        if (cbLinksEnabled.isSelected()) {
            setEnabledLinkOptions(true);
            setOutputAreasEnabled(true, outLinkList);
        } else {
            setEnabledLinkOptions(false);
            setOutputAreasEnabled(false, outLinkList);
            if (!cbParamsEnabled.isSelected() && !cbWordsEnabled.isSelected()) {
                cbParamsEnabled.setSelected(true);
            }
        }
    }

    private void cbWordsEnabled_clicked() {
        setTabDefaultColor();
        if (cbWordsEnabled.isSelected()) {
            setEnabledWordOptions(true);
            setOutputAreasEnabled(true, outWordList);
        } else {
            setEnabledWordOptions(false);
            setOutputAreasEnabled(false, outWordList);
            if (!cbParamsEnabled.isSelected() && !cbLinksEnabled.isSelected()) {
                cbLinksEnabled.setSelected(true);
            }
        }
    }

    private void cbReportSusParams_clicked() {
        setTabDefaultColor();
        cbIncludeTentative.setEnabled(cbReportSusParams.isSelected());
    }

    private void cbSaveFile_clicked() {
        setTabDefaultColor();
        inSaveDir.setEnabled(cbSaveFile.isSelected());
    }

    private void updateLinkPrefixState() {
        inLinkPrefix.setEnabled(cbLinkPrefix.isSelected());
    }

    private void cbLinkPrefix_clicked() {
        setTabDefaultColor();
        updateLinkPrefixState();
        if (cbLinkPrefix.isSelected()) {
            cbUnPrefixed.setEnabled(true);
            checkLinkPrefix();
        } else if (!cbLinkPrefixScope.isSelected() && !cbLinkPrefixOrigin.isSelected()) {
            cbUnPrefixed.setEnabled(false);
        }
    }

    private void cbLinkPrefixScope_clicked() {
        setTabDefaultColor();
        if (cbLinkPrefixScope.isSelected()) {
            cbLinkPrefixOrigin.setSelected(false);
        }
        if (cbLinkPrefixScope.isSelected() || cbLinkPrefixOrigin.isSelected()) {
            cbUnPrefixed.setEnabled(true);
        } else if (!cbLinkPrefix.isSelected()) {
            cbUnPrefixed.setEnabled(false);
        }
    }

    private void cbLinkPrefixOrigin_clicked() {
        setTabDefaultColor();
        if (cbLinkPrefixOrigin.isSelected()) {
            cbLinkPrefixScope.setSelected(false);
        }
        if (cbLinkPrefixScope.isSelected() || cbLinkPrefixOrigin.isSelected()) {
            cbUnPrefixed.setEnabled(true);
        } else if (!cbLinkPrefix.isSelected()) {
            cbUnPrefixed.setEnabled(false);
        }
    }

    private void cbExclusions_clicked() {
        inExclusions.setEnabled(cbExclusions.isSelected());
    }

    private void cbShowQueryString_clicked() {
        setTabDefaultColor();
        if (cbShowQueryString.isEnabled()) {
            if (cbShowQueryString.isSelected()) {
                cbShowParamOrigin.setSelected(false);
            }
            changeParamDisplay();
        }
    }

    private void btnHelp_clicked() {
        setTabDefaultColor();
        try {
            Desktop.getDesktop().browse(new URI(GapConstants.GAP_HELP_URL));
        } catch (Exception e) {
            try {
                Desktop.getDesktop().browse(new URI(GapConstants.GAP_HELP_404));
            } catch (Exception e2) {
                JDialog dialog = new JDialog((JFrame) null, "GAP Help", true);
                JEditorPane pane = new JEditorPane();
                pane.setEditable(false);
                pane.setContentType("text/html");
                pane.setText(GapConstants.GAP_HELP_404);
                dialog.getContentPane().add(new JScrollPane(pane));
                dialog.setSize(800, 600);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        }
    }

    private void btnKoFi_clicked() {
        try {
            Desktop.getDesktop().browse(new URI(GapConstants.URL_KOFI));
        } catch (Exception e) {
            LOGGER.debug(e);
        }
    }

    private void btnChooseDir_clicked() {
        setTabDefaultColor();
        try {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setDialogTitle("Choose GAP file output directory:");
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            try {
                dirChooser.setCurrentDirectory(new File(inSaveDir.getText()));
            } catch (Exception e) {
                dirChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            }
            int userSelection = dirChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                inSaveDir.setText(dirChooser.getSelectedFile().toString());
            }
        } catch (Exception e) {
            stderr.println("btnChooseDir_clicked 1");
            e.printStackTrace(stderr);
        }
    }

    private void setEnabledParamOptions(boolean enabled) {
        cbParamUrl.setEnabled(enabled);
        cbParamBody.setEnabled(enabled);
        cbParamMultiPart.setEnabled(enabled);
        cbParamJson.setEnabled(enabled);
        cbParamCookie.setEnabled(enabled);
        cbParamXml.setEnabled(enabled);
        cbParamXmlAttr.setEnabled(enabled);
        inQueryStringVal.setEnabled(enabled);
        cbReportSusParams.setEnabled(enabled);
        cbIncludeTentative.setEnabled(enabled && cbReportSusParams.isSelected());
        cbIncludePathWords.setEnabled(enabled);
        cbParamJSONResponse.setEnabled(enabled);
        cbParamXMLResponse.setEnabled(enabled);
        cbParamInputField.setEnabled(enabled);
        cbParamJSVars.setEnabled(enabled);
        cbParamFromLinks.setEnabled(enabled && cbLinksEnabled.isSelected());
        if (enabled && countParamUnique > 0) {
            cbShowParamOrigin.setEnabled(true);
            cbShowSusParams.setEnabled(true);
            cbShowQueryString.setEnabled(true);
        } else {
            cbShowParamOrigin.setEnabled(false);
            cbShowSusParams.setEnabled(false);
            cbShowQueryString.setEnabled(false);
        }
    }

    private void setEnabledLinkOptions(boolean enabled) {
        cbSiteMapEndpoints.setEnabled(enabled);
        cbRelativeLinks.setEnabled(enabled);
        cbLinkPrefix.setEnabled(enabled);
        inLinkPrefix.setEnabled(enabled && cbLinkPrefix.isSelected());
        cbLinkPrefixScope.setEnabled(enabled);
        cbLinkPrefixOrigin.setEnabled(enabled);
        if (enabled
                && (cbLinkPrefix.isSelected()
                        || cbLinkPrefixScope.isSelected()
                        || cbLinkPrefixOrigin.isSelected())) {
            cbUnPrefixed.setEnabled(true);
        } else {
            cbUnPrefixed.setEnabled(false);
        }
        cbExclusions.setEnabled(enabled);
        inExclusions.setEnabled(enabled);
        cbParamFromLinks.setEnabled(enabled && cbParamsEnabled.isSelected());
        if (enabled && countLinkUnique > 0) {
            cbShowLinkOrigin.setEnabled(true);
            cbInScopeOnly.setEnabled(true);
            btnFilter.setEnabled(true);
            inLinkFilter.setEnabled(true);
            cbLinkFilterNeg.setEnabled(true);
            cbLinkCaseSens.setEnabled(true);
        } else {
            cbShowLinkOrigin.setEnabled(false);
            cbInScopeOnly.setEnabled(false);
            btnFilter.setEnabled(false);
            inLinkFilter.setEnabled(false);
            cbLinkFilterNeg.setEnabled(false);
            cbLinkCaseSens.setEnabled(false);
        }
    }

    private void setEnabledWordOptions(boolean enabled) {
        cbWordParams.setEnabled(enabled);
        cbWordComments.setEnabled(enabled);
        cbWordDigits.setEnabled(enabled);
        cbWordImgAlt.setEnabled(enabled);
        cbWordLower.setEnabled(enabled);
        cbWordPaths.setEnabled(enabled);
        cbWordPlurals.setEnabled(enabled);
        inWordsMinLen.setEnabled(enabled);
        inWordsMaxlen.setEnabled(enabled);
        inStopWords.setEnabled(enabled);
        cbShowWordOrigin.setEnabled(enabled && countWordUnique > 0);
    }

    private void setEnabledAll(boolean enable) {
        cbLinksEnabled.setEnabled(enable);
        cbParamsEnabled.setEnabled(enable);
        cbWordsEnabled.setEnabled(enable);
        if (cbParamsEnabled.isSelected()) {
            setEnabledParamOptions(enable);
        }
        if (cbLinksEnabled.isSelected()) {
            setEnabledLinkOptions(enable);
        }
        if (cbWordsEnabled.isSelected()) {
            setEnabledWordOptions(enable);
        }
        btnRestoreDefaults.setEnabled(enable);
        cbSaveFile.setEnabled(enable);
        inSaveDir.setEnabled(enable);
        btnChooseDir.setEnabled(enable);
        btnSave.setEnabled(enable);
        cbToolTips.setEnabled(enable);
    }

    // ------------------------------------------------------------------
    // Display methods
    // ------------------------------------------------------------------

    private void changeParamDisplay() {
        try {
            if (cbShowParamOrigin.isEnabled()) {
                setTabDefaultColor();
                if (cbShowParamOrigin.isSelected()) {
                    if (cbShowSusParams.isSelected()) {
                        outParamList.setText(txtParamsSusWithURL);
                    } else {
                        outParamList.setText(txtParamsWithURL);
                    }
                    scrollOutParamList.setViewportView(outParamList);
                    cbShowQueryString.setSelected(false);
                } else if (cbShowQueryString.isSelected()) {
                    if (cbShowSusParams.isSelected()) {
                        if (txtParamQuerySus.isEmpty()
                                || txtParamQuerySus.startsWith("NO PARAMETERS FOUND")) {
                            int index = 0;
                            StringBuilder paramQuery = new StringBuilder();
                            for (String param : sorted(engine.getParamSusList())) {
                                paramQuery
                                        .append(param.split("  \\[", 2)[0])
                                        .append("=")
                                        .append(inQueryStringVal.getText())
                                        .append(index)
                                        .append("&");
                                index++;
                            }
                            txtParamQuerySus = stripTrailingAmp(paramQuery.toString());
                        }
                        outParamQuery.setText(txtParamQuerySus);
                    } else {
                        if (txtParamQuery.isEmpty()
                                || txtParamQuery.startsWith("NO PARAMETERS FOUND")) {
                            int index = 0;
                            StringBuilder paramQuery = new StringBuilder();
                            for (String param : sorted(engine.getParamList())) {
                                paramQuery
                                        .append(param)
                                        .append("=")
                                        .append(inQueryStringVal.getText())
                                        .append(index)
                                        .append("&");
                                index++;
                            }
                            txtParamQuery = stripTrailingAmp(paramQuery.toString());
                        }
                        outParamQuery.setText(txtParamQuery);
                    }
                    scrollOutParamList.setViewportView(outParamQuery);
                    outParamQuery.setCaretPosition(0);
                } else {
                    if (cbShowParamOrigin.isSelected()) {
                        outParamList.setText(
                                cbShowSusParams.isSelected()
                                        ? txtParamsSusWithURL
                                        : txtParamsWithURL);
                    } else {
                        outParamList.setText(
                                cbShowSusParams.isSelected()
                                        ? txtParamsSusOnly
                                        : txtParamsOnly);
                    }
                    scrollOutParamList.setViewportView(outParamList);
                }

                if (cbShowParamOrigin.isSelected()) {
                    lblParamList.setText(
                            "Potential params found - "
                                    + (cbShowSusParams.isSelected()
                                            ? countParamSusUnique
                                            : countParamUnique)
                                    + " unique:");
                } else {
                    lblParamList.setText(
                            "Potential params found - "
                                    + (cbShowSusParams.isSelected()
                                            ? countParamSus
                                            : countParam)
                                    + " filtered:");
                }
                outParamList.setCaretPosition(0);
            }
        } catch (Exception e) {
            stderr.println("changeParamDisplay 1");
            e.printStackTrace(stderr);
        }
    }

    private static String stripTrailingAmp(String s) {
        while (s.endsWith("&")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private void changeLinkDisplay() {
        try {
            if (cbShowLinkOrigin.isSelected()) {
                if (cbInScopeOnly.isSelected()) {
                    outLinkList.setText(txtLinksWithURLInScopeOnly);
                } else {
                    outLinkList.setText(txtLinksWithURL);
                }
            } else if (cbInScopeOnly.isSelected()) {
                outLinkList.setText(txtLinksOnlyInScopeOnly);
            } else {
                outLinkList.setText(txtLinksOnly);
            }

            if (cbShowLinkOrigin.isSelected() && !cbInScopeOnly.isSelected()) {
                lblLinkList.setText(
                        "Potential links found - " + countLinkUnique + " unique:");
            } else {
                lblLinkList.setText(
                        "Potential links found - " + lineCount(outLinkList.getText()) + " filtered:");
            }

            if ("Clear filter".equals(btnFilter.getText())) {
                btnFilter_clicked();
            }
            outLinkList.setCaretPosition(0);
        } catch (Exception e) {
            stderr.println("changeLinkDisplay 1");
            e.printStackTrace(stderr);
        }
    }

    private void changeWordDisplay() {
        try {
            if (cbShowWordOrigin.isSelected()) {
                outWordList.setText(txtWordsWithURL);
                lblWordList.setText("Words found - " + countWordUnique + " unique:");
            } else {
                outWordList.setText(txtWordsOnly);
                lblWordList.setText(
                        "Words found - " + lineCount(outWordList.getText()) + " filtered:");
            }
            outWordList.setCaretPosition(0);
        } catch (Exception e) {
            stderr.println("changeWordDisplay 1");
            e.printStackTrace(stderr);
        }
    }

    private static int lineCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private void btnFilter_clicked() {
        setTabDefaultColor();
        if ("Apply filter".equals(btnFilter.getText())) {
            outLinkList.setText("");
            txtLinksFiltered = "";

            String txtToProcess;
            if (cbShowLinkOrigin.isSelected()) {
                txtToProcess =
                        cbInScopeOnly.isSelected() ? txtLinksWithURLInScopeOnly : txtLinksWithURL;
            } else {
                txtToProcess =
                        cbInScopeOnly.isSelected() ? txtLinksOnlyInScopeOnly : txtLinksOnly;
            }

            try {
                for (String line : txtToProcess.split("\n")) {
                    if (cbLinkFilterNeg.isSelected()) {
                        if ((cbLinkCaseSens.isSelected() && !line.contains(inLinkFilter.getText()))
                                || (!cbLinkCaseSens.isSelected()
                                        && !line.toLowerCase()
                                                .contains(
                                                        inLinkFilter.getText().toLowerCase()))) {
                            txtLinksFiltered = txtLinksFiltered + line + "\n";
                        }
                    } else {
                        if ((cbLinkCaseSens.isSelected()
                                        && line.contains(inLinkFilter.getText()))
                                || (!cbLinkCaseSens.isSelected()
                                        && line.toLowerCase()
                                                .contains(
                                                        inLinkFilter.getText().toLowerCase()))) {
                            txtLinksFiltered = txtLinksFiltered + line + "\n";
                        }
                    }
                }
            } catch (Exception e) {
                stderr.println("btnFilter_clicked 1");
                e.printStackTrace(stderr);
            }

            try {
                outLinkList.setText(
                        !txtLinksFiltered.isEmpty()
                                ? txtLinksFiltered
                                : "NO FILTERED LINKS FOUND");
            } catch (Exception e) {
                stderr.println("btnFilter_clicked 2");
                e.printStackTrace(stderr);
            }
            lblLinkList.setText(
                    "Potential links found - "
                            + lineCount(outLinkList.getText())
                            + " filtered:");
            btnFilter.setText("Clear filter");
        } else {
            try {
                inLinkFilter.setText("");
                txtLinksFiltered = "";
                if (cbShowLinkOrigin.isSelected()) {
                    if (cbInScopeOnly.isSelected()) {
                        outLinkList.setText(txtLinksWithURLInScopeOnly);
                    } else {
                        outLinkList.setText(txtLinksWithURL);
                    }
                } else if (cbInScopeOnly.isSelected()) {
                    outLinkList.setText(txtLinksOnlyInScopeOnly);
                } else {
                    outLinkList.setText(txtLinksOnly);
                }

                if (countLinkUnique == lineCount(outLinkList.getText())) {
                    lblLinkList.setText("Potential links found - " + countLinkUnique + " unique:");
                } else {
                    lblLinkList.setText(
                            "Potential links found - "
                                    + lineCount(outLinkList.getText())
                                    + " filtered:");
                }
                btnFilter.setText("Apply filter");
                btnFilter.setEnabled(false);
            } catch (Exception e) {
                stderr.println("btnFilter_clicked 3");
                e.printStackTrace(stderr);
            }
        }
        outLinkList.setCaretPosition(0);
    }

    // ------------------------------------------------------------------
    // Config save/restore
    // ------------------------------------------------------------------

    private void clearOutput() {
        txtParamsOnly = "";
        txtParamsWithURL = "";
        txtParamsSusOnly = "";
        txtParamsSusWithURL = "";
        txtParamQuery = "";
        txtParamQuerySus = "";
        txtLinksOnly = "";
        txtLinksWithURL = "";
        txtLinksOnlyInScopeOnly = "";
        txtLinksWithURLInScopeOnly = "";
        txtLinksFiltered = "";
        txtWordsOnly = "";
        txtWordsWithURL = "";
        outParamList.setText("");
        outParamSus.setText("");
        outParamQuery.setText("");
        outLinkList.setText("");
        outWordList.setText("");
        txtDebug.setText("");
        txtDebugDetail.setText("");
        scrollOutParamList.setViewportView(outParamList);
        lblParamList.setText("Potential params found:");
        lblLinkList.setText("Potential links found:");
        lblWordList.setText("Words found:");
        inLinkFilter.setText("");
        btnFilter.setText("Apply filter");
    }

    private void btnSave_clicked() {
        setTabDefaultColor();
        saveConfig();
    }

    private void saveConfig() {
        try {
            File dir = new File(inSaveDir.getText());
            if (!dir.isDirectory()) {
                inSaveDir.setText(getDefaultSaveDirectory());
            }
            checkWordLength();
            if (cbLinkPrefix.isSelected()) {
                checkLinkPrefix();
            }

            param.setSaveFile(cbSaveFile.isSelected());
            param.setParamUrl(cbParamUrl.isSelected());
            param.setParamBody(cbParamBody.isSelected());
            param.setParamMultiPart(cbParamMultiPart.isSelected());
            param.setParamJson(cbParamJson.isSelected());
            param.setParamCookie(cbParamCookie.isSelected());
            param.setParamXml(cbParamXml.isSelected());
            param.setParamXmlAttr(cbParamXmlAttr.isSelected());
            param.setReportSusParams(cbReportSusParams.isSelected());
            param.setIncludeTentative(cbIncludeTentative.isSelected());
            param.setIncludePathWords(cbIncludePathWords.isSelected());
            param.setParamJSONResponse(cbParamJSONResponse.isSelected());
            param.setParamXMLResponse(cbParamXMLResponse.isSelected());
            param.setParamInputField(cbParamInputField.isSelected());
            param.setParamJSVars(cbParamJSVars.isSelected());
            param.setSaveDir(inSaveDir.getText());
            param.setParamFromLinks(cbParamFromLinks.isSelected());
            param.setExclusionsEnabled(cbExclusions.isSelected());
            param.setLinkExclusions(inExclusions.getText());
            param.setShowParamOrigin(cbShowParamOrigin.isSelected());
            param.setShowLinkOrigin(cbShowLinkOrigin.isSelected());
            param.setShowWordOrigin(cbShowWordOrigin.isSelected());
            param.setInScopeOnly(cbInScopeOnly.isSelected());
            param.setSiteMapEndpoints(cbSiteMapEndpoints.isSelected());
            param.setRelativeLinks(cbRelativeLinks.isSelected());
            param.setParamsEnabled(cbParamsEnabled.isSelected());
            param.setLinksEnabled(cbLinksEnabled.isSelected());
            param.setLinkPrefix(cbLinkPrefix.isSelected());
            param.setLinkPrefixValue(inLinkPrefix.getText());
            param.setLinkPrefixScope(cbLinkPrefixScope.isSelected());
            param.setLinkPrefixOrigin(cbLinkPrefixOrigin.isSelected());
            param.setUnPrefixed(cbUnPrefixed.isSelected());
            param.setWordsEnabled(cbWordsEnabled.isSelected());
            param.setWordPlurals(cbWordPlurals.isSelected());
            param.setWordPaths(cbWordPaths.isSelected());
            param.setWordParams(cbWordParams.isSelected());
            param.setWordDigits(cbWordDigits.isSelected());
            param.setWordComments(cbWordComments.isSelected());
            param.setWordImgAlt(cbWordImgAlt.isSelected());
            param.setWordLower(cbWordLower.isSelected());
            param.setWordMinLen(inWordsMinLen.getText());
            param.setWordMaxLen(inWordsMaxlen.getText());
            param.setStopWords(inStopWords.getText());
            param.setToolTips(cbToolTips.isSelected());
            param.setQueryStringVal(inQueryStringVal.getText());
            param.update();
        } catch (Exception e) {
            stderr.println("saveConfig 1");
            e.printStackTrace(stderr);
        }
    }

    private void restoreSavedConfig() {
        try {
            cbSaveFile.setSelected(param.isSaveFile());
            cbParamUrl.setSelected(param.isParamUrl());
            cbParamBody.setSelected(param.isParamBody());
            cbParamMultiPart.setSelected(param.isParamMultiPart());
            cbParamJson.setSelected(param.isParamJson());
            cbParamCookie.setSelected(param.isParamCookie());
            cbParamXml.setSelected(param.isParamXml());
            cbParamXmlAttr.setSelected(param.isParamXmlAttr());
            cbShowSusParams.setSelected(false);
            cbShowQueryString.setSelected(false);
            inQueryStringVal.setText(param.getQueryStringVal());
            cbReportSusParams.setSelected(param.isReportSusParams());
            cbIncludeTentative.setSelected(param.isIncludeTentative());
            cbIncludePathWords.setSelected(param.isIncludePathWords());
            cbParamJSONResponse.setSelected(param.isParamJSONResponse());
            cbParamXMLResponse.setSelected(param.isParamXMLResponse());
            cbParamInputField.setSelected(param.isParamInputField());
            cbParamJSVars.setSelected(param.isParamJSVars());
            inSaveDir.setText(param.getSaveDir());
            cbParamFromLinks.setSelected(param.isParamFromLinks());
            cbExclusions.setSelected(param.isExclusionsEnabled());
            inExclusions.setText(param.getLinkExclusions());
            cbShowParamOrigin.setSelected(param.isShowParamOrigin());
            cbShowLinkOrigin.setSelected(param.isShowLinkOrigin());
            cbShowWordOrigin.setSelected(param.isShowWordOrigin());
            cbInScopeOnly.setSelected(param.isInScopeOnly());
            cbSiteMapEndpoints.setSelected(param.isSiteMapEndpoints());
            cbRelativeLinks.setSelected(param.isRelativeLinks());
            cbParamsEnabled.setSelected(param.isParamsEnabled());
            cbLinksEnabled.setSelected(param.isLinksEnabled());
            cbLinkPrefix.setSelected(param.isLinkPrefix());
            inLinkPrefix.setText(param.getLinkPrefix());
            cbLinkPrefixScope.setSelected(param.isLinkPrefixScope());
            cbLinkPrefixOrigin.setSelected(param.isLinkPrefixOrigin());
            cbUnPrefixed.setSelected(param.isUnPrefixed());
            cbWordsEnabled.setSelected(param.isWordsEnabled());
            cbWordPlurals.setSelected(param.isWordPlurals());
            cbWordPaths.setSelected(param.isWordPaths());
            cbWordParams.setSelected(param.isWordParams());
            cbWordDigits.setSelected(param.isWordDigits());
            cbWordComments.setSelected(param.isWordComments());
            cbWordImgAlt.setSelected(param.isWordImgAlt());
            cbWordLower.setSelected(param.isWordLower());
            inWordsMinLen.setText(String.valueOf(param.getWordMinLen()));
            inWordsMaxlen.setText(
                    param.getWordMaxLen() > 0 ? String.valueOf(param.getWordMaxLen()) : "");
            inStopWords.setText(String.join(",", param.getStopWords()));
            cbToolTips.setSelected(param.isToolTips());

            if (inExclusions.getText().isEmpty()) {
                inExclusions.setText(GapConstants.DEFAULT_EXCLUSIONS);
            }
            if (inQueryStringVal.getText().isEmpty()) {
                inQueryStringVal.setText(GapConstants.DEFAULT_QSV);
            }
            if (inStopWords.getText().isEmpty()) {
                inStopWords.setText(GapConstants.DEFAULT_STOP_WORDS);
            }
            checkWordLength();
            checkLinkPrefix();
            updateLinkPrefixState();
        } catch (Exception e) {
            stderr.println("restoreSavedConfig 1");
            e.printStackTrace(stderr);
        }
    }

    private void btnRestoreDefaults_clicked() {
        setTabDefaultColor();
        setEnabledParamOptions(true);
        setEnabledLinkOptions(true);
        setEnabledWordOptions(true);

        cbSaveFile.setSelected(true);
        cbParamUrl.setSelected(true);
        cbParamBody.setSelected(true);
        cbParamMultiPart.setSelected(true);
        cbParamJson.setSelected(false);
        cbParamJSONResponse.setSelected(false);
        cbParamXMLResponse.setSelected(false);
        cbParamInputField.setSelected(false);
        cbParamCookie.setSelected(false);
        cbParamXml.setSelected(false);
        cbParamXmlAttr.setSelected(false);
        cbShowSusParams.setSelected(false);
        cbShowQueryString.setSelected(false);
        inQueryStringVal.setText(GapConstants.DEFAULT_QSV);
        cbReportSusParams.setSelected(true);
        cbIncludeTentative.setSelected(true);
        cbIncludePathWords.setSelected(false);
        cbParamJSVars.setSelected(false);
        inSaveDir.setText(getDefaultSaveDirectory());
        cbParamFromLinks.setSelected(false);
        inExclusions.setText(GapConstants.DEFAULT_EXCLUSIONS);
        cbShowParamOrigin.setSelected(false);
        cbShowLinkOrigin.setSelected(false);
        cbShowWordOrigin.setSelected(false);
        cbInScopeOnly.setSelected(false);
        cbSiteMapEndpoints.setSelected(false);
        cbRelativeLinks.setSelected(true);
        cbParamsEnabled.setSelected(true);
        cbLinksEnabled.setSelected(true);
        cbLinkPrefix.setSelected(false);
        cbUnPrefixed.setSelected(false);
        inLinkPrefix.setText(GapConstants.DEFAULT_LINK_PREFIX);
        cbLinkPrefixScope.setSelected(false);
        cbLinkPrefixOrigin.setSelected(false);
        cbWordsEnabled.setSelected(true);
        cbWordPlurals.setSelected(true);
        cbWordPaths.setSelected(false);
        cbWordParams.setSelected(false);
        cbWordComments.setSelected(true);
        cbWordImgAlt.setSelected(true);
        cbWordLower.setSelected(true);
        cbWordDigits.setSelected(true);
        inWordsMinLen.setText("3");
        inWordsMaxlen.setText(GapConstants.DEFAULT_MAX_WORD_LEN);
        inStopWords.setText(GapConstants.DEFAULT_STOP_WORDS);
        saveConfig();
    }

    // ------------------------------------------------------------------
    // Link prefix / word length validation
    // ------------------------------------------------------------------

    public boolean validateLinkPrefix(String link) {
        if (link == null || link.strip().isEmpty()) {
            return false;
        }
        link = link.strip();
        if (link.endsWith("/")) {
            link = link.substring(0, link.length() - 1);
        }
        if (!hasNetloc(link) && !link.startsWith("//")) {
            link = "http://" + link;
        }
        return GapConstants.LINK_PREFIX_VALIDATOR.matcher(link).matches();
    }

    private static boolean hasNetloc(String link) {
        try {
            if (link.contains("://")) {
                String after = link.substring(link.indexOf("://") + 3);
                int slash = after.indexOf('/');
                String hostPort = slash < 0 ? after : after.substring(0, slash);
                return !hostPort.isEmpty();
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    public List<String> getLinkPrefixes() {
        return param.getLinkPrefixes();
    }

    private void checkLinkPrefix() {
        try {
            boolean invalid = false;
            inLinkPrefix.setText(inLinkPrefix.getText().strip());
            if (inLinkPrefix.getText().endsWith(";")) {
                inLinkPrefix.setText(
                        inLinkPrefix.getText().substring(0, inLinkPrefix.getText().length() - 1));
            }

            if (cbLinkPrefix.isSelected()) {
                List<String> linkPrefixes = param.getLinkPrefixes();
                StringBuilder fixedLinks = new StringBuilder();
                for (String link : linkPrefixes) {
                    if (!hasNetloc(link) && !link.startsWith("//")) {
                        link = "http://" + link;
                    }
                    if (fixedLinks.length() == 0) {
                        fixedLinks.append(link);
                    } else {
                        fixedLinks.append(";").append(link);
                    }
                    if (!GapConstants.LINK_PREFIX_VALIDATOR.matcher(link).matches()) {
                        invalid = true;
                    }
                }
                if (!invalid) {
                    inLinkPrefix.setText(fixedLinks.toString());
                }
            }

            if (linkPrefixColor == null) {
                linkPrefixColor = inLinkPrefix.getForeground();
            }
            inLinkPrefix.setForeground(invalid ? Color.RED : linkPrefixColor);
        } catch (Exception e) {
            stderr.println("checkLinkPrefix 1");
            e.printStackTrace(stderr);
        }
    }

    private void checkWordLength() {
        String minText = inWordsMinLen.getText();
        if (!minText.matches("\\d+")) {
            inWordsMinLen.setText("3");
        } else {
            try {
                if (Integer.parseInt(minText) < 3) {
                    inWordsMinLen.setText("3");
                }
            } catch (Exception e) {
                inWordsMinLen.setText("3");
            }
        }

        String maxText = inWordsMaxlen.getText().strip();
        if (maxText.isEmpty()) {
            // Empty is allowed (no maximum limit)
        } else if (!maxText.matches("\\d+")) {
            inWordsMaxlen.setText(GapConstants.DEFAULT_MAX_WORD_LEN);
        } else {
            try {
                int minLen = Integer.parseInt(inWordsMinLen.getText());
                int maxLen = Integer.parseInt(maxText);
                if (maxLen < minLen) {
                    inWordsMaxlen.setText(inWordsMinLen.getText());
                }
            } catch (Exception e) {
                inWordsMaxlen.setText(GapConstants.DEFAULT_MAX_WORD_LEN);
            }
        }
    }

    // ------------------------------------------------------------------
    // File paths and writing
    // ------------------------------------------------------------------

    private static String getDefaultSaveDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return System.getProperty("user.home") + File.separator + "Documents";
        }
        return System.getProperty("user.home");
    }

    private String getMainFilePath() {
        try {
            String outputDir = inSaveDir.getText();
            String projectName;
            try {
                String title = View.getSingleton().getMainFrame().getTitle();
                if (title != null && title.contains("Temporary Project")) {
                    projectName = "TempProject_";
                } else if (title != null && title.contains(" - ")) {
                    String[] parts = title.split(" - ");
                    projectName = parts[parts.length - 1].strip() + "_";
                } else {
                    projectName = "UnknownProject_";
                }
            } catch (Exception e) {
                projectName = "UnknownProject_";
            }
            String fileName =
                    projectName
                            + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            return outputDir + File.separator + fileName;
        } catch (Exception e) {
            stderr.println("getMainFilePath 1");
            e.printStackTrace(stderr);
            return null;
        }
    }

    private String getFilePath(String rootname) {
        try {
            String scheme = "http";
            String hostname = "unknown";
            try {
                if (rootname.contains("://")) {
                    scheme = rootname.substring(0, rootname.indexOf("://"));
                    String after = rootname.substring(rootname.indexOf("://") + 3);
                    int slash = after.indexOf('/');
                    String hostPort = slash < 0 ? after : after.substring(0, slash);
                    hostname = hostPort.split(":")[0];
                }
            } catch (Exception e) {
                // ignore
            }
            String newDir = scheme + "-" + hostname;
            String path = inSaveDir.getText() + File.separator + newDir;
            String outputDir = path;
            try {
                if (!new File(path).mkdir()) {
                    // directory exists already
                }
            } catch (Exception e) {
                outputDir = inSaveDir.getText();
            }

            String fileName =
                    hostname + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            return outputDir + File.separator + fileName;
        } catch (Exception e) {
            stderr.println("getFilePath 1");
            e.printStackTrace(stderr);
            return null;
        }
    }

    private void fileWriteParams() {
        try {
            if (cbParamsEnabled.isSelected() && !outParamList.getText().startsWith("NO PARAMETERS FOUND")) {
                boolean showParamOrigin = cbShowParamOrigin.isSelected();
                if (roots.size() > 1) {
                    String fileName = getMainFilePath() + "_params.txt";
                    writeFile(
                            fileName,
                            showParamOrigin
                                    ? txtParamsWithURL.replace("  ", " ")
                                    : txtParamsOnly);
                }
                if (roots.size() == 1) {
                    for (String root : roots) {
                        String fileName = getFilePath(root) + "_params.txt";
                        writeFile(
                                fileName,
                                showParamOrigin
                                        ? txtParamsWithURL.replace("  ", " ")
                                        : txtParamsOnly);
                    }
                } else {
                    for (String root : roots) {
                        StringBuilder fileText = new StringBuilder();
                        for (String param : txtParamsWithURL.split("\n")) {
                            if (param.contains("[" + root) || param.contains("[GAP]")) {
                                if (showParamOrigin) {
                                    fileText.append(param.replace("  ", " ")).append("\n");
                                } else {
                                    fileText.append(param.split("  \\[", 2)[0]).append("\n");
                                }
                            }
                        }
                        if (fileText.length() > 0) {
                            String text = String.join("\n", sortedSet(fileText.toString()));
                            writeFile(getFilePath(root) + "_params.txt", text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            stderr.println(
                    "There is a problem with the Save directory "
                            + inSaveDir.getText()
                            + ". Check it and correct it.");
        }
    }

    private void fileWriteLinks() {
        try {
            if (cbLinksEnabled.isSelected() && !outLinkList.getText().startsWith("NO LINKS FOUND")) {
                boolean showLinkOrigin = cbShowLinkOrigin.isSelected();
                boolean inScopeOnly = cbInScopeOnly.isSelected();
                if (roots.size() > 1) {
                    String fileName = getMainFilePath() + "_links.txt";
                    String text;
                    if (showLinkOrigin) {
                        text =
                                (inScopeOnly ? txtLinksWithURLInScopeOnly : txtLinksWithURL)
                                        .replace("  ", " ");
                    } else {
                        text = inScopeOnly ? txtLinksOnlyInScopeOnly : txtLinksOnly;
                    }
                    writeFile(fileName, text);
                }
                if (roots.size() == 1) {
                    for (String root : roots) {
                        String fileName = getFilePath(root) + "_links.txt";
                        String text;
                        if (showLinkOrigin) {
                            text =
                                    (inScopeOnly ? txtLinksWithURLInScopeOnly : txtLinksWithURL)
                                            .replace("  ", " ");
                        } else {
                            text = inScopeOnly ? txtLinksOnlyInScopeOnly : txtLinksOnly;
                        }
                        writeFile(fileName, text);
                    }
                } else {
                    for (String root : roots) {
                        StringBuilder fileText = new StringBuilder();
                        String source = inScopeOnly ? txtLinksWithURLInScopeOnly : txtLinksWithURL;
                        for (String line : source.split("\n")) {
                            if (line.contains("[" + root) || line.contains("[GAP]")) {
                                if (showLinkOrigin) {
                                    fileText.append(line.replace("  ", " ")).append("\n");
                                } else {
                                    fileText.append(line.split("  \\[", 2)[0]).append("\n");
                                }
                            }
                        }
                        if (fileText.length() > 0) {
                            String text = String.join("\n", sortedSet(fileText.toString()));
                            writeFile(getFilePath(root) + "_links.txt", text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            stderr.println(
                    "There is a problem with the Save directory "
                            + inSaveDir.getText()
                            + ". Check it and correct it.");
        }
    }

    private void fileWriteWords() {
        try {
            if (cbWordsEnabled.isSelected() && !outWordList.getText().startsWith("NO WORDS FOUND")) {
                boolean showWordOrigin = cbShowWordOrigin.isSelected();
                if (roots.size() > 1) {
                    String fileName = getMainFilePath() + "_words.txt";
                    writeFile(
                            fileName,
                            showWordOrigin
                                    ? txtWordsWithURL.replace("  ", " ")
                                    : txtWordsOnly);
                }
                if (roots.size() == 1) {
                    for (String root : roots) {
                        String fileName = getFilePath(root) + "_words.txt";
                        writeFile(
                                fileName,
                                showWordOrigin
                                        ? txtWordsWithURL.replace("  ", " ")
                                        : txtWordsOnly);
                    }
                } else {
                    for (String root : roots) {
                        StringBuilder fileText = new StringBuilder();
                        for (String word : txtWordsWithURL.split("\n")) {
                            if (word.contains("[" + root) || word.contains("[GAP]")) {
                                if (showWordOrigin) {
                                    fileText.append(word.replace("  ", " ")).append("\n");
                                } else {
                                    fileText.append(word.split("  \\[", 2)[0]).append("\n");
                                }
                            }
                        }
                        if (fileText.length() > 0) {
                            String text = String.join("\n", sortedSet(fileText.toString()));
                            writeFile(getFilePath(root) + "_words.txt", text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            stderr.println(
                    "There is a problem with the Save directory "
                            + inSaveDir.getText()
                            + ". Check it and correct it.");
        }
    }

    private void writeFile(String fileName, String content) {
        try {
            if (fileName == null) {
                return;
            }
            Files.write(Paths.get(fileName), content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            stderr.println("writeFile 1: " + fileName);
            e.printStackTrace(stderr);
        }
    }

    private static List<String> sortedSet(String text) {
        Set<String> set = new java.util.TreeSet<>();
        for (String s : text.split("\\s+")) {
            if (!s.isEmpty()) {
                set.add(s);
            }
        }
        return new ArrayList<>(set);
    }

    private static List<String> sorted(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        list.sort(String::compareTo);
        return list;
    }

    // ------------------------------------------------------------------
    // Run orchestration
    // ------------------------------------------------------------------

    private void btnCancel_clicked() {
        flagCancel = true;
        engine.setCancel(true);
        btnCancel.setText("CANCELLING... ");
    }

    /** Entry point from the popup menu item (non-site-map contexts). */
    public void runSelected(List<HttpMessage> messages) {
        runGap(messages, false, -1, true, true);
    }

    /** Entry point from the Site Map tree popup "Send request to Gap". */
    public void runSiteMapRequests(SiteNode node) {
        saveConfig();
        List<HttpMessage> messages = new ArrayList<>();
        collectSiteMapMessages(node, messages);
        runGap(messages, true, 4, true, false);
    }

    /** Entry point from the Site Map tree popup "Send response to Gap". */
    public void runSiteMapResponses(SiteNode node) {
        saveConfig();
        List<HttpMessage> messages = new ArrayList<>();
        collectSiteMapMessages(node, messages);
        runGap(messages, true, 4, false, true);
    }

    private void collectSiteMapMessages(SiteNode node, List<HttpMessage> out) {
        if (node == null) {
            return;
        }
        try {
            HistoryReference hr = node.getHistoryReference();
            if (hr != null) {
                out.add(hr.getHttpMessage());
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            Vector<HistoryReference> past = node.getPastHistoryReference();
            if (past != null) {
                for (HistoryReference p : past) {
                    try {
                        out.add(p.getHttpMessage());
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            try {
                collectSiteMapMessages((SiteNode) node.getChildAt(i), out);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void runGap(
            List<HttpMessage> messages,
            boolean isSiteMapContext,
            int context,
            boolean useRequests,
            boolean useResponses) {
        Thread worker =
                new Thread(
                        () -> {
                            try {
                                engine.setUseRequests(useRequests);
                                engine.setUseResponses(useResponses);
                                doEverything(messages, isSiteMapContext, context);
                            } catch (Exception e) {
                                stderr.println("runGap 1");
                                e.printStackTrace(stderr);
                                SwingUtilities.invokeLater(() -> setEnabledAll(true));
                            }
                        });
        worker.setDaemon(true);
        worker.setName("GAP-run");
        worker.start();
    }

    private void doEverything(List<HttpMessage> allMessages, boolean isSiteMapContext, int context) {
        try {
            SwingUtilities.invokeLater(
                    () -> {
                        setTabTitle("GAP*");
                        progBar.setValue(0);
                        progBar.setMaximum(0);
                        progBar.setString("Starting...");
                        progBar.setVisible(true);
                        progStage.setVisible(false);
                        btnCancel.setText("  CANCEL GAP  ");
                        btnCancel.setVisible(true);
                        btnCancel.setEnabled(true);
                    });

            isRunning = true;
            flagCancel = false;
            engine.setCancel(false);
            engine.clearAll();
            roots.clear();
            allScopePrefixes.clear();
            allRootsForDialog.clear();
            raisedIssues.clear();

            // Reset output text fields
            SwingUtilities.invokeLater(
                    () -> {
                        txtParamsOnly = "";
                        txtParamsWithURL = "";
                        txtParamsSusOnly = "";
                        txtParamsSusWithURL = "";
                        txtParamQuery = "";
                        txtParamQuerySus = "";
                        txtLinksOnly = "";
                        txtLinksWithURL = "";
                        txtLinksOnlyInScopeOnly = "";
                        txtLinksWithURLInScopeOnly = "";
                        txtWordsOnly = "";
                        txtWordsWithURL = "";
                        inLinkFilter.setText("");
                        btnFilter.setText("Apply filter");

                        if (cbParamsEnabled.isSelected()) {
                            lblParamList.setText("Potential params found - SEARCHING");
                            outParamList.setText("SEARCHING...");
                        } else {
                            lblParamList.setText("Potential params found:");
                            outParamList.setText("");
                        }
                        if (cbLinksEnabled.isSelected()) {
                            lblLinkList.setText("Potential links found - SEARCHING");
                            outLinkList.setText("SEARCHING...");
                        } else {
                            lblLinkList.setText("Potential links found:");
                            outLinkList.setText("");
                        }
                        if (cbWordsEnabled.isSelected()) {
                            lblWordList.setText("Words found - SEARCHING");
                            outWordList.setText("SEARCHING...");
                        } else {
                            lblWordList.setText("Words found:");
                            outWordList.setText("");
                        }
                    });

            // Disable options while running
            SwingUtilities.invokeLater(() -> setEnabledAll(false));

            lastRunContext = context;
            lastRunDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            allRootsForDialog.addAll(engine.getRoots());

            checkWordLength();
            checkLinkPrefix();

            int totalRoots;
            if (isSiteMapContext) {
                for (HttpMessage msg : allMessages) {
                    try {
                        ReqResp rr = new ReqResp(msg);
                        String url = rr.getRequestUrl();
                        if (!url.isEmpty()) {
                            engine.getRoots().add(url);
                            String prefix = schemeAndNetloc(url);
                            if (!prefix.isEmpty()) {
                                allScopePrefixes.add(prefix);
                            }
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
                totalRoots = engine.getRoots().size();
                int currentRoot = 0;
                for (String root : engine.getRoots()) {
                    engine.checkIfCancel();
                    currentRoot++;
                    int finalCurrentRoot = currentRoot;
                    SwingUtilities.invokeLater(
                            () -> {
                                if (totalRoots > 1) {
                                    progStage.setText(
                                            finalCurrentRoot + " / " + totalRoots);
                                    progStage.setVisible(true);
                                } else {
                                    progStage.setVisible(false);
                                }
                                progBar.setValue(0);
                                progBar.setString("Getting reqs...");
                            });

                    int msgCount = 0;
                    int index = 0;
                    for (HttpMessage msg : allMessages) {
                        ReqResp rr;
                        try {
                            rr = new ReqResp(msg);
                        } catch (Exception e) {
                            continue;
                        }
                        String url = rr.getRequestUrl();
                        if (!sameHostname(root, url)) {
                            continue;
                        }
                        msgCount++;
                    }
                    int finalMsgCount = msgCount;
                    SwingUtilities.invokeLater(
                            () -> {
                                progBar.setMaximum(finalMsgCount);
                                progBar.setString("0/" + finalMsgCount);
                            });

                    for (HttpMessage msg : allMessages) {
                        ReqResp rr;
                        try {
                            rr = new ReqResp(msg);
                        } catch (Exception e) {
                            continue;
                        }
                        if (!sameHostname(root, rr.getRequestUrl())) {
                            continue;
                        }
                        if (!isInScope(rr.getRequestUrl())) {
                            continue;
                        }
                        index++;
                        final int fIndex = index;
                        SwingUtilities.invokeLater(
                                () -> {
                                    progBar.setValue(fIndex);
                                    progBar.setString(fIndex + "/" + finalMsgCount);
                                });
                        engine.checkIfCancel();
                        engine.setCurrentReqResp(rr);
                        if (rr.isRequest()) {
                            if (cbLinksEnabled.isSelected() && cbSiteMapEndpoints.isSelected()) {
                                engine.getSiteMapLinks(rr);
                            }
                            engine.processMessage(rr);
                        }
                        engine.setCurrentReqResp(null);
                    }
                }
            } else {
                SwingUtilities.invokeLater(
                        () -> {
                            progBar.setMaximum(Math.max(1, allMessages.size()));
                            progBar.setString("0/" + allMessages.size());
                        });
                int index = 0;
                for (HttpMessage msg : allMessages) {
                    index++;
                    final int fIndex = index;
                    SwingUtilities.invokeLater(
                            () -> {
                                progBar.setValue(fIndex);
                                progBar.setString(fIndex + "/" + allMessages.size());
                            });
                    engine.checkIfCancel();
                    ReqResp rr = new ReqResp(msg);
                    String root = rr.getRequestUrl();
                    if (!root.isEmpty()) {
                        engine.getRoots().add(root);
                        String prefix = schemeAndNetloc(root);
                        if (!prefix.isEmpty()) {
                            allScopePrefixes.add(prefix);
                        }
                    }
                    engine.setCurrentReqResp(rr);
                    if (rr.isRequest()) {
                        if (cbLinksEnabled.isSelected() && cbSiteMapEndpoints.isSelected()) {
                            engine.getSiteMapLinks(rr);
                        }
                        engine.processMessage(rr);
                    }
                    engine.setCurrentReqResp(null);
                }
            }

            allRootsForDialog.addAll(engine.getRoots());
            roots.addAll(engine.getRoots());

            SwingUtilities.invokeLater(
                    () -> {
                        progBar.setValue(0);
                        int maxValue = 0;
                        if (cbParamsEnabled.isSelected()) {
                            maxValue++;
                        }
                        if (cbLinksEnabled.isSelected()) {
                            maxValue++;
                        }
                        if (cbWordsEnabled.isSelected()) {
                            maxValue++;
                        }
                        progBar.setMaximum(maxValue);
                        progBar.setString("Processing...");
                    });

            engine.checkIfCancel();
            displayResults();

            isRunning = false;
            SwingUtilities.invokeLater(
                    () -> {
                        setTabColor(GapConstants.COLOR_LIGHT_BLUE);
                        setTabTitle("GAP");
                        btnCancel.setEnabled(false);
                        btnCancel.setText("  COMPLETED   ");
                        progBar.setString("100%");
                        progBar.setValue(1);
                        progBar.setMaximum(1);
                        progStage.setText("");
                        txtDebug.setText("");
                        txtDebugDetail.setText("");
                        setEnabledAll(true);
                    });
        } catch (CancelGapRequested e) {
            // The user pressed the CANCEL GAP button
            flagCancel = false;
            engine.setCancel(false);
            isRunning = false;
            SwingUtilities.invokeLater(
                    () -> {
                        setTabTitle("GAP");
                        btnCancel.setEnabled(false);
                        btnCancel.setText("  CANCELLED   ");
                        cancelResultLabels();
                        setEnabledAll(true);
                    });
        } catch (Exception e) {
            if (!flagCancel) {
                stderr.println("doEverything 1");
                e.printStackTrace(stderr);
            }
            isRunning = false;
            SwingUtilities.invokeLater(() -> setEnabledAll(true));
        }
    }

    private void cancelResultLabels() {
        if (lblParamList.getText().contains("UPDATING")
                || lblParamList.getText().contains("SEARCHING")
                || lblParamList.getText().contains("PROCESSING")) {
            lblParamList.setText("Potential params found - CANCELLED");
        }
        if (lblLinkList.getText().contains("UPDATING")
                || lblLinkList.getText().contains("SEARCHING")
                || lblLinkList.getText().contains("PROCESSING")) {
            lblLinkList.setText("Potential links found - CANCELLED");
        }
        if (lblWordList.getText().contains("UPDATING")
                || lblWordList.getText().contains("SEARCHING")
                || lblWordList.getText().contains("PROCESSING")) {
            lblWordList.setText("Words found - CANCELLED");
        }
        if ("SEARCHING...".equals(outParamList.getText())
                || "UPDATING...".equals(outParamList.getText())
                || "PROCESSING...".equals(outParamList.getText())) {
            outParamList.setText("CANCELLED");
        }
        if ("SEARCHING...".equals(outLinkList.getText())
                || "UPDATING...".equals(outLinkList.getText())
                || "PROCESSING...".equals(outLinkList.getText())) {
            outLinkList.setText("CANCELLED");
        }
        if ("SEARCHING...".equals(outWordList.getText())
                || "UPDATING...".equals(outWordList.getText())
                || "PROCESSING...".equals(outWordList.getText())) {
            outWordList.setText("CANCELLED");
        }
    }

    private static String schemeAndNetloc(String url) {
        try {
            if (url.contains("://")) {
                String scheme = url.substring(0, url.indexOf("://"));
                String after = url.substring(url.indexOf("://") + 3);
                int slash = after.indexOf('/');
                String hostPort = slash < 0 ? after : after.substring(0, slash);
                return scheme + "://" + hostPort;
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    private static boolean sameHostname(String root, String url) {
        try {
            if (url.contains("://")) {
                String after = url.substring(url.indexOf("://") + 3);
                int slash = after.indexOf('/');
                String hostPort = slash < 0 ? after : after.substring(0, slash);
                String host = hostPort.split(":")[0];
                String rootHost = root;
                if (rootHost.contains("://")) {
                    rootHost = rootHost.substring(rootHost.indexOf("://") + 3);
                    int rSlash = rootHost.indexOf('/');
                    rootHost = rSlash < 0 ? rootHost : rootHost.substring(0, rSlash);
                    rootHost = rootHost.split(":")[0];
                }
                return host.equalsIgnoreCase(rootHost);
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Display results
    // ------------------------------------------------------------------

    private void displayResults() {
        SwingUtilities.invokeLater(
                () -> {
                    if (cbParamsEnabled.isSelected()) {
                        lblParamList.setText("Potential params found - PROCESSING");
                        outParamList.setText("PROCESSING...");
                    }
                    if (cbLinksEnabled.isSelected()) {
                        lblLinkList.setText("Potential links found - PROCESSING");
                        outLinkList.setText("PROCESSING...");
                    }
                    if (cbWordsEnabled.isSelected()) {
                        lblWordList.setText("Words found - PROCESSING");
                        outWordList.setText("PROCESSING...");
                    }
                });

        Thread tParam = null;
        Thread tLinks = null;
        Thread tWords = null;
        try {
            if (cbParamsEnabled.isSelected()) {
                tParam = new Thread(this::displayParams);
                tParam.setDaemon(true);
                tParam.start();
            }
            if (cbLinksEnabled.isSelected()) {
                tLinks = new Thread(this::displayLinks);
                tLinks.setDaemon(true);
                tLinks.start();
            }
            if (cbWordsEnabled.isSelected()) {
                tWords = new Thread(this::displayWords);
                tWords.setDaemon(true);
                tWords.start();
            }
            if (tParam != null) {
                tParam.join();
            }
            if (tLinks != null) {
                tLinks.join();
            }
            if (tWords != null) {
                tWords.join();
            }
        } catch (Exception e) {
            stderr.println("displayResults 1");
            e.printStackTrace(stderr);
        }
    }

    private void displayParams() {
        try {
            if (cbParamsEnabled.isSelected()) {
                SwingUtilities.invokeLater(
                        () -> {
                            lblParamList.setText("Potential params found - UPDATING");
                            outParamList.setText("UPDATING...");
                        });
                countParam = engine.getParamList().size();
                countParamSus = engine.getParamSusList().size();
                countParamSusUnique = engine.getParamSusUrlList().size();
                countParamUnique = engine.getParamUrlList().size();

                txtParamsOnly = String.join("\n", sorted(engine.getParamList()));
                txtParamsWithURL = String.join("\n", sorted(engine.getParamUrlList()));
                txtParamsSusOnly = String.join("\n", sorted(engine.getParamSusList()));
                txtParamsSusWithURL = String.join("\n", sorted(engine.getParamSusUrlList()));

                String extraInfo = lastRunContext == 4 ? GapConstants.DEFAULT_WARNING_NO_CONTENT : "";
                SwingUtilities.invokeLater(
                        () -> {
                            if (txtParamsOnly.isEmpty()) {
                                outParamList.setText("NO PARAMETERS FOUND" + extraInfo);
                                outParamSus.setText("NO PARAMETERS FOUND" + extraInfo);
                                outParamQuery.setText("NO PARAMETERS FOUND" + extraInfo);
                            } else if (cbShowParamOrigin.isSelected()) {
                                outParamList.setText(
                                        cbShowSusParams.isSelected()
                                                ? txtParamsSusWithURL
                                                : txtParamsWithURL);
                            } else {
                                outParamList.setText(
                                        cbShowSusParams.isSelected()
                                                ? txtParamsSusOnly
                                                : txtParamsOnly);
                            }
                            cbShowSusParams.setSelected(false);
                            cbShowQueryString.setSelected(false);
                            scrollOutParamList.setViewportView(outParamList);

                            if (cbShowParamOrigin.isSelected()) {
                                lblParamList.setText(
                                        "Potential params found - "
                                                + (cbShowSusParams.isSelected()
                                                        ? countParamSusUnique
                                                        : countParamUnique)
                                                + " unique:");
                            } else {
                                lblParamList.setText(
                                        "Potential params found - "
                                                + (cbShowSusParams.isSelected()
                                                        ? countParamSus
                                                        : countParam)
                                                + " filtered:");
                            }
                        });
            }

            SwingUtilities.invokeLater(
                    () -> {
                        if (countParamUnique > 0 && cbParamsEnabled.isSelected()) {
                            cbShowParamOrigin.setEnabled(true);
                            cbShowSusParams.setEnabled(true);
                            cbShowQueryString.setEnabled(true);
                            inQueryStringVal.setEnabled(true);
                        }
                    });

            engine.checkIfCancel();
            if (cbSaveFile.isSelected()) {
                SwingUtilities.invokeLater(() -> progBar.setString("Writing files..."));
                fileWriteParams();
            }
            engine.getParamUrlList().clear();
            engine.getParamSusUrlList().clear();
        } catch (CancelGapRequested e) {
            throw e;
        } catch (Exception e) {
            stderr.println("displayParams 1");
            e.printStackTrace(stderr);
        }
        SwingUtilities.invokeLater(() -> progBar.setValue(progBar.getValue() + 1));
    }

    private void displayLinks() {
        try {
            if (cbLinksEnabled.isSelected()) {
                SwingUtilities.invokeLater(
                        () -> {
                            lblLinkList.setText("Potential links found - UPDATING");
                            outLinkList.setText("UPDATING...");
                        });
                countLinkUnique = engine.getLinkUrlList().size();

                txtLinksOnly = String.join("\n", sorted(engine.getLinkList()));
                txtLinksWithURL = String.join("\n", sorted(engine.getLinkUrlList()));
                txtLinksOnlyInScopeOnly = String.join("\n", sorted(engine.getLinkInScopeList()));
                txtLinksWithURLInScopeOnly =
                        String.join("\n", sorted(engine.getLinkUrlInScopeList()));

                String extraInfo = lastRunContext == 4 ? GapConstants.DEFAULT_WARNING_NO_CONTENT : "";
                SwingUtilities.invokeLater(
                        () -> {
                            if (cbShowLinkOrigin.isSelected()) {
                                if (cbInScopeOnly.isSelected()) {
                                    outLinkList.setText(txtLinksWithURLInScopeOnly);
                                } else {
                                    outLinkList.setText(txtLinksWithURL);
                                }
                            } else if (cbInScopeOnly.isSelected()) {
                                outLinkList.setText(txtLinksOnlyInScopeOnly);
                            } else {
                                outLinkList.setText(txtLinksOnly);
                            }

                            if (cbShowLinkOrigin.isSelected() && !cbInScopeOnly.isSelected()) {
                                lblLinkList.setText(
                                        "Potential links found - " + countLinkUnique + " unique:");
                            } else {
                                lblLinkList.setText(
                                        "Potential links found - "
                                                + lineCount(outLinkList.getText())
                                                + " filtered:");
                            }

                            if (outLinkList.getText().isEmpty()) {
                                outLinkList.setText("NO LINKS FOUND" + extraInfo);
                            }
                            if (countLinkUnique > 0 && cbLinksEnabled.isSelected()) {
                                cbShowLinkOrigin.setEnabled(true);
                                cbInScopeOnly.setEnabled(true);
                                inLinkFilter.setEnabled(true);
                                cbLinkCaseSens.setEnabled(true);
                                btnFilter.setEnabled(true);
                                cbExclusions.setEnabled(true);
                                inExclusions.setEnabled(true);
                            }
                        });
            }

            engine.checkIfCancel();
            if (cbSaveFile.isSelected()) {
                SwingUtilities.invokeLater(() -> progBar.setString("Writing files..."));
                fileWriteLinks();
            }
            engine.getLinkList().clear();
            engine.getLinkUrlList().clear();
            engine.getLinkInScopeList().clear();
            engine.getLinkUrlInScopeList().clear();
        } catch (CancelGapRequested e) {
            throw e;
        } catch (Exception e) {
            stderr.println("displayLinks 1");
            e.printStackTrace(stderr);
        }
        SwingUtilities.invokeLater(() -> progBar.setValue(progBar.getValue() + 1));
    }

    private void displayWords() {
        try {
            if (cbWordsEnabled.isSelected()) {
                SwingUtilities.invokeLater(
                        () -> {
                            lblWordList.setText("Words found - UPDATING");
                            outWordList.setText("UPDATING...");
                        });
                countWordUnique = engine.getWordUrlList().size();

                txtWordsOnly = String.join("\n", sorted(engine.getWordList()));
                txtWordsWithURL = String.join("\n", sorted(engine.getWordUrlList()));

                String extraInfo = lastRunContext == 4 ? GapConstants.DEFAULT_WARNING_NO_CONTENT : "";
                SwingUtilities.invokeLater(
                        () -> {
                            if (txtWordsOnly.isEmpty()) {
                                outWordList.setText("NO WORDS FOUND" + extraInfo);
                            } else if (cbShowWordOrigin.isSelected()) {
                                outWordList.setText(txtWordsWithURL);
                            } else {
                                outWordList.setText(txtWordsOnly);
                            }
                            scrollOutWordList.setViewportView(outWordList);

                            if (cbShowWordOrigin.isSelected()) {
                                lblWordList.setText("Words found - " + countWordUnique + " unique:");
                            } else {
                                lblWordList.setText(
                                        "Words found - "
                                                + lineCount(outWordList.getText())
                                                + " filtered:");
                            }
                        });
            }

            SwingUtilities.invokeLater(
                    () -> {
                        if (countWordUnique > 0 && cbWordsEnabled.isSelected()) {
                            cbShowWordOrigin.setEnabled(true);
                        }
                    });

            engine.checkIfCancel();
            if (cbSaveFile.isSelected()) {
                SwingUtilities.invokeLater(() -> progBar.setString("Writing files..."));
                fileWriteWords();
            }
            engine.getWordList().clear();
            engine.getWordUrlList().clear();
        } catch (CancelGapRequested e) {
            throw e;
        } catch (Exception e) {
            stderr.println("displayWords 1");
            e.printStackTrace(stderr);
        }
        SwingUtilities.invokeLater(() -> progBar.setValue(progBar.getValue() + 1));
    }

    // ------------------------------------------------------------------
    // Scope / issue creation
    // ------------------------------------------------------------------

    @Override
    public boolean isInScope(String url) {
        try {
            return Model.getSingleton().getSession().isInScope(url);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Create a ZAP alert for a suspect parameter if it has not been raised already. Ported from
     * {@code createIssue} of GAP.py.
     */
    @Override
    public void createIssue(HttpMessage httpMessage, String issueDetail, String confidence) {
        try {
            String signature = signIssue(issueDetail + "Low" + confidence);
            if (raisedIssues.contains(signature)) {
                return;
            }
            raisedIssues.add(signature);

            int risk = Alert.RISK_LOW;
            int conf;
            if ("Certain".equals(confidence)) {
                conf = Alert.CONFIDENCE_HIGH;
            } else if ("Firm".equals(confidence)) {
                conf = Alert.CONFIDENCE_MEDIUM;
            } else {
                conf = Alert.CONFIDENCE_LOW;
            }

            Alert alert =
                    Alert.builder()
                            .setName("[GAP] Sus Parameter")
                            .setRisk(risk)
                            .setConfidence(conf)
                            .setDescription(issueDetail + "[GAP:" + signature + "]")
                            .setMessage(httpMessage)
                            .build();

            HistoryReference hr = httpMessage.getHistoryRef();
            if (hr == null) {
                try {
                    hr =
                            new HistoryReference(
                                    Model.getSingleton().getSession(),
                                    HistoryReference.TYPE_TEMPORARY,
                                    httpMessage);
                } catch (Exception e) {
                    GapEngine.debug(e);
                    return;
                }
            }

            ExtensionAlert extAlert =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
            if (extAlert != null) {
                extAlert.alertFound(alert, hr);
            }
        } catch (Exception e) {
            stderr.println("createIssue 1");
            e.printStackTrace(stderr);
        }
    }

    /**
     * Signature hash used to avoid duplicate issues. Ported from {@code CustomIssue.issuehash} of
     * GAP.py.
     */
    private static String issuehash(String text) {
        long hash = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            hash = ((hash * 281) ^ (ch * 997)) & 0xFFFFFFFFFFFL;
        }
        return String.valueOf(hash);
    }

    /** See {@link #issuehash(String)}. */
    public static String signIssue(String text) {
        return issuehash(text);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Set<String> getAllRootsForDialog() {
        return allRootsForDialog;
    }

    public int getLastRunContext() {
        return lastRunContext;
    }

    public String getLastRunDate() {
        return lastRunDate;
    }

    public JButton getBtnCancel() {
        return btnCancel;
    }

    public JProgressBar getProgBar() {
        return progBar;
    }

    // ------------------------------------------------------------------
    // Custom key listener for the filter button
    // ------------------------------------------------------------------

    private static final class CustomKeyListener extends KeyAdapter {
        private final JButton button;

        CustomKeyListener(JButton button) {
            this.button = button;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() != '\n') {
                if (button.getText().startsWith("Clear")) {
                    button.doClick();
                }
                button.setText("Apply filter");
                button.setEnabled(true);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && button.isEnabled()) {
                button.doClick();
            }
        }
    }

    // ------------------------------------------------------------------
    // Output mouse listener (copy / request links)
    // ------------------------------------------------------------------

    private final class OutputMouseListener extends MouseAdapter {
        private final JTextArea textArea;
        private final String identifier;
        private final JCheckBox linksShowOrigin;
        private final JCheckBox linksInScope;
        private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        private final List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();

        OutputMouseListener(
                JTextArea textArea,
                String identifier,
                JCheckBox linksShowOrigin,
                JCheckBox linksInScope) {
            this.textArea = textArea;
            this.identifier = identifier;
            this.linksShowOrigin = linksShowOrigin;
            this.linksInScope = linksInScope;
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON3
                    && !isRunning
                    && textArea.getText() != null
                    && !textArea.getText().isEmpty()
                    && !"CANCELLED".equals(textArea.getText())
                    && !textArea.getText().startsWith("NO PARAMETERS FOUND")
                    && !textArea.getText().startsWith("NO WORDS FOUND")
                    && !textArea.getText().startsWith("NO LINKS FOUND")) {
                JPopupMenu popup = createPopupMenu();
                popup.show(event.getComponent(), event.getX(), event.getY());
            }
        }

        private JPopupMenu createPopupMenu() {
            JPopupMenu popup = new JPopupMenu();
            String menu;
            switch (identifier) {
                case "Param":
                    menu = "Copy paramaters";
                    break;
                case "ParamQuery":
                    menu = "Copy query string";
                    break;
                case "Links":
                    menu = "Copy links";
                    break;
                case "Words":
                    menu = "Copy words";
                    break;
                default:
                    menu = "Copy";
            }
            JMenuItem copyItem = new JMenuItem(menu);
            copyItem.addActionListener(e -> copyToClipboard());
            popup.add(copyItem);

            if ("Links".equals(identifier)) {
                if (!linksShowOrigin.isSelected() && linksInScope.isSelected()) {
                    JMenuItem sendItem =
                            new JMenuItem("Request all prefixed URLs and send to Site Map");
                    sendItem.addActionListener(e -> sendRequests());
                    popup.add(sendItem);
                }
                scheduledTasks.removeIf(ScheduledFuture::isDone);
                if (!scheduledTasks.isEmpty()) {
                    JMenuItem cancelItem = new JMenuItem("Cancel all requests being made");
                    cancelItem.addActionListener(e -> cancelRequests());
                    popup.add(cancelItem);
                }
            }
            return popup;
        }

        private void cancelRequests() {
            int cancelled = 0;
            for (ScheduledFuture<?> task : scheduledTasks) {
                if (!task.isCancelled() && !task.isDone()) {
                    task.cancel(true);
                    cancelled++;
                }
            }
            scheduledTasks.clear();
            stderr.println("Cancelled " + cancelled + " scheduled requests.");
        }

        private void sendRequests() {
            Set<String> links = new LinkedHashSet<>();
            for (String line : textArea.getText().split("\n")) {
                line = line.strip();
                if (line.startsWith("http")) {
                    links.add(line);
                }
            }
            stderr.println(
                    "Starting task with " + links.size() + " requests scheduled.");
            int delay = 0;
            for (String link : links) {
                final int d = delay;
                ScheduledFuture<?> task =
                        executor.schedule(() -> makeRequest(link), d * 10L, TimeUnit.MILLISECONDS);
                scheduledTasks.add(task);
                delay++;
            }
        }

        private void makeRequest(String urlString) {
            try {
                if (urlString.contains("*")) {
                    return;
                }
                HttpMessage msg = new HttpMessage(new org.apache.commons.httpclient.URI(urlString));
                HttpSender sender =
                        new HttpSender(
                                Model.getSingleton().getOptionsParam().getConnectionParam(),
                                true,
                                HttpSender.MANUAL_REQUEST_INITIATOR);
                sender.sendAndReceive(msg, true);
                HistoryReference hr =
                        new HistoryReference(
                                Model.getSingleton().getSession(),
                                HistoryReference.TYPE_TEMPORARY,
                                msg);
                Model.getSingleton().getSession().getSiteTree().addPath(hr);
            } catch (Exception e) {
                stderr.println("OutputMouseListener.send_requests.make_request: " + e);
            }
        }

        private void copyToClipboard() {
            String text = textArea.getText();
            String firstLine = text.split("\n", 2)[0];
            if ("Param".equals(identifier)
                    && firstLine.strip().matches(".*\\s{2}\\[[A-Z, ]*\\]")) {
                StringBuilder sb = new StringBuilder();
                for (String line : text.split("\n")) {
                    sb.append(line.split("  \\[", 2)[0]).append("\n");
                }
                text = sb.toString();
            }
            try {
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(text), null);
            } catch (Exception e) {
                stderr.println("copy_to_clipboard 1");
                e.printStackTrace(stderr);
            }
        }
    }

    // ------------------------------------------------------------------
    // Link prefix field mouse listener (modal edit dialog)
    // ------------------------------------------------------------------

    private static final class LinkPrefixFieldMouseListener extends MouseAdapter {
        private final GapPanel parent;

        LinkPrefixFieldMouseListener(GapPanel parent) {
            this.parent = parent;
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1
                    && parent.inLinkPrefix.isEnabled()) {
                openLinkPrefixDialog();
            }
        }

        private void openLinkPrefixDialog() {
            try {
                JDialog dialog = new JDialog((JFrame) null, "Edit Link Prefixes", true);
                dialog.setSize(600, 500);
                dialog.setLocationRelativeTo(null);

                JPanel mainPanel = new JPanel(new BorderLayout());

                JEditorPane instructionPane = new JEditorPane();
                instructionPane.setContentType("text/html");
                instructionPane.setText(
                        "<html>Enter the link prefixes you want to use, separated by "
                                + "<b>semicolons (;)</b> or on <b>separate lines</b>.<br>"
                                + "These prefixes will be added to any links found that do not have a domain.</html>");
                instructionPane.setEditable(false);
                instructionPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

                String currentText = parent.inLinkPrefix.getText();
                String editableText =
                        currentText != null ? currentText.replace(";", "\n") : "";

                JTextArea textArea = new JTextArea(8, 50);
                textArea.setText(editableText);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setFont(parent.inLinkPrefix.getFont());

                JLabel validationLabel = new JLabel(" ");
                validationLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                JPanel centerPanel = new JPanel(new BorderLayout());
                centerPanel.add(scrollPane, BorderLayout.CENTER);
                centerPanel.add(validationLabel, BorderLayout.SOUTH);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton okButton = new JButton("OK");
                JButton cancelButton = new JButton("Cancel");
                buttonPanel.add(cancelButton);
                buttonPanel.add(okButton);

                mainPanel.add(instructionPane, BorderLayout.NORTH);
                mainPanel.add(centerPanel, BorderLayout.CENTER);
                mainPanel.add(buttonPanel, BorderLayout.SOUTH);
                dialog.add(mainPanel);

                Runnable validate =
                        () -> {
                            String text = textArea.getText();
                            if (text.strip().isEmpty()) {
                                validationLabel.setText(" ");
                                validationLabel.setForeground(Color.BLACK);
                                okButton.setEnabled(true);
                                return;
                            }
                            List<String> prefixes = new ArrayList<>();
                            for (String line : text.split("\n")) {
                                for (String prefix : line.split(";")) {
                                    prefix = prefix.strip();
                                    if (!prefix.isEmpty()) {
                                        prefixes.add(prefix);
                                    }
                                }
                            }
                            List<String> invalidPrefixes = new ArrayList<>();
                            int validCount = 0;
                            for (String prefix : prefixes) {
                                if (parent.validateLinkPrefix(prefix)) {
                                    validCount++;
                                } else {
                                    invalidPrefixes.add(prefix);
                                }
                            }
                            if (!invalidPrefixes.isEmpty()) {
                                validationLabel.setText(
                                        invalidPrefixes.size() == 1
                                                ? "Invalid prefix: " + invalidPrefixes.get(0)
                                                : "Invalid prefixes: "
                                                        + String.join(", ", invalidPrefixes));
                                validationLabel.setForeground(Color.RED);
                                okButton.setEnabled(false);
                            } else {
                                if (validCount > 0) {
                                    validationLabel.setText(
                                            "All "
                                                    + validCount
                                                    + " prefix"
                                                    + (validCount != 1 ? "es" : "")
                                                    + " are valid");
                                    validationLabel.setForeground(new Color(0, 128, 0));
                                } else {
                                    validationLabel.setText(" ");
                                    validationLabel.setForeground(Color.BLACK);
                                }
                                okButton.setEnabled(true);
                            }
                        };

                textArea.getDocument()
                        .addDocumentListener(
                                new DocumentListener() {
                                    @Override
                                    public void insertUpdate(DocumentEvent e) {
                                        validate.run();
                                    }

                                    @Override
                                    public void removeUpdate(DocumentEvent e) {
                                        validate.run();
                                    }

                                    @Override
                                    public void changedUpdate(DocumentEvent e) {
                                        validate.run();
                                    }
                                });

                okButton.addActionListener(
                        e -> {
                            List<String> prefixes = new ArrayList<>();
                            for (String line : textArea.getText().split("\n")) {
                                for (String prefix : line.split(";")) {
                                    prefix = prefix.strip();
                                    if (!prefix.isEmpty()) {
                                        prefixes.add(prefix);
                                    }
                                }
                            }
                            parent.inLinkPrefix.setText(String.join(";", prefixes));
                            parent.checkLinkPrefix();
                            dialog.dispose();
                        });
                cancelButton.addActionListener(e -> dialog.dispose());

                validate.run();
                textArea.requestFocus();
                textArea.selectAll();
                dialog.setVisible(true);
            } catch (Exception e) {
                parent.stderr.println("LinkPrefixFieldMouseListener.openLinkPrefixDialog");
                parent.stderr.println(String.valueOf(e));
            }
        }
    }

    // ------------------------------------------------------------------
    // Progress bar mouse listener (execution scope dialog)
    // ------------------------------------------------------------------

    private static final class ProgressBarMouseListener extends MouseAdapter {
        private final GapPanel parent;

        ProgressBarMouseListener(GapPanel parent) {
            this.parent = parent;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            JDialog dialog = new JDialog((JFrame) null, "GAP Execution Scope", true);
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(null);

            String context = contextName(parent.getLastRunContext());
            JLabel lblContext = new JLabel("Last executed from context: " + context);
            JLabel lblRequests = new JLabel();
            JTextArea txtRequests = new JTextArea(10, 30);
            txtRequests.setEditable(false);
            txtRequests.setCaretColor(txtRequests.getBackground());
            JScrollPane scrRequests = new JScrollPane(txtRequests);

            Set<String> roots = parent.getAllRootsForDialog();
            JCheckBox chkShowHost = new JCheckBox("Show host only");
            chkShowHost.setSelected(true);
            if (parent.getLastRunContext() == 4) {
                chkShowHost.setVisible(false);
            }
            chkShowHost.addItemListener(
                    ev -> {
                        boolean checked = ((JCheckBox) ev.getSource()).isSelected();
                        if (checked) {
                            Set<String> hosts = new java.util.TreeSet<>();
                            for (String url : roots) {
                                String host = hostOf(url);
                                if (!host.isEmpty()) {
                                    hosts.add(host);
                                }
                            }
                            txtRequests.setText(String.join("\n", hosts));
                            lblRequests.setText("Hosts (" + hosts.size() + "): ");
                        } else {
                            txtRequests.setText(String.join("\n", roots));
                            lblRequests.setText("Unique targets (" + roots.size() + "): ");
                        }
                        txtRequests.setCaretPosition(0);
                    });

            Set<String> hosts = new java.util.TreeSet<>();
            for (String url : roots) {
                String host = hostOf(url);
                if (!host.isEmpty()) {
                    hosts.add(host);
                }
            }
            txtRequests.setText(String.join("\n", hosts));
            txtRequests.setCaretPosition(0);
            lblRequests.setText("Hosts (" + hosts.size() + "): ");

            JButton btnClose = new JButton("Close");
            btnClose.addActionListener(ev -> dialog.dispose());
            btnClose.setBackground(GapConstants.COLOR_LIGHT_BLUE);
            btnClose.setForeground(Color.WHITE);
            btnClose.setFont(btnClose.getFont().deriveFont(Font.BOLD));

            JPanel panelTop = new JPanel(new GridLayout(2, 1));
            panelTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panelTop.add(lblContext);
            panelTop.add(chkShowHost);
            panelTop.add(lblRequests);

            JPanel panelMiddle = new JPanel(new GridLayout(1, 1));
            panelMiddle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            panelMiddle.add(scrRequests);

            JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panelBottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panelBottom.add(btnClose);

            dialog.add(panelTop, BorderLayout.NORTH);
            dialog.add(panelMiddle, BorderLayout.CENTER);
            dialog.add(panelBottom, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }

        private static String hostOf(String url) {
            try {
                if (url.contains("://")) {
                    String after = url.substring(url.indexOf("://") + 3);
                    int slash = after.indexOf('/');
                    String hostPort = slash < 0 ? after : after.substring(0, slash);
                    return hostPort.split(":")[0];
                }
            } catch (Exception e) {
                // ignore
            }
            return "";
        }

        private static String contextName(int context) {
            switch (context) {
                case 0:
                    return "Message Editor Request";
                case 1:
                    return "Message Editor Response";
                case 2:
                    return "Message Viewer Request";
                case 3:
                    return "Message Viewer Response";
                case 4:
                    return "Target Site Map Tree";
                case 5:
                    return "Target Site Map Table";
                case 6:
                    return "Proxy History";
                case 7:
                    return "Scanner Results";
                case 8:
                    return "Intruder Payload Positions";
                case 9:
                    return "Intruder Attacker Results";
                case 10:
                    return "Search Results";
                default:
                    return "<unknown>";
            }
        }
    }
}