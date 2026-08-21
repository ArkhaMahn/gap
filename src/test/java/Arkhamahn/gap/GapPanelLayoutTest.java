package Arkhamahn.gap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Lays out {@link GapPanel} headlessly to exercise the GroupLayout graph (catches components that
 * are not registered in both groups) and sanity-checks the two-column structure against the
 * original GAP tab layout.
 */
class GapPanelLayoutTest {

    private GapPanel panel;
    private Container content;

    @BeforeEach
    void setUp() {
        GapParam param = new GapParam();
        param.parse();
        panel = new GapPanel(param, new PrintWriter(new StringWriter(), true));
        // The panel's root is BorderLayout with a JScrollPane in CENTER; constrain the inner
        // content view directly to simulate the panel being sized to a real tab.
        JScrollPane scroll = (JScrollPane) panel.getComponent(0);
        content = (Container) scroll.getViewport().getView();
        content.setSize(1920, 940);
    }

    private void layoutAll(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layoutAll((Container) child);
            }
        }
    }

    private static boolean isReachable(Component target, Container root) {
        for (Component child : root.getComponents()) {
            if (child == target) {
                return true;
            }
            if (child instanceof Container && isReachable(target, (Container) child)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void layoutCompletesWithoutErrors() {
        layoutAll(content);
        // If any GroupLayout component is missing from a group this throws IllegalArgumentException.
    }

    @Test
    void resultCountLabelsAreActuallyShown() throws Exception {
        // Regression: lblParamList/lblWordList/lblLinkList were setText'd but never added to a
        // layout, so the "potential params found - N filtered:" counters were invisible.
        String[] names = {"lblParamList", "lblWordList", "lblLinkList"};
        for (String name : names) {
            Field field = GapPanel.class.getDeclaredField(name);
            field.setAccessible(true);
            JLabel label = (JLabel) field.get(panel);
            assertNotNull(label, name + " field must exist");
            assertTrue(
                    isReachable(label, panel),
                    name + " must be added to the component tree (was orphaned before)");
        }
    }

    @Test
    void twoColumnStructureIsPreserved() throws Exception {
        layoutAll(content);
        Field linkList = GapPanel.class.getDeclaredField("scrollOutLinkList");
        linkList.setAccessible(true);
        Field paramList = GapPanel.class.getDeclaredField("scrollOutParamList");
        paramList.setAccessible(true);
        Field wordList = GapPanel.class.getDeclaredField("scrollOutWordList");
        wordList.setAccessible(true);

        Component links = (Component) linkList.get(panel);
        Component params = (Component) paramList.get(panel);
        Component words = (Component) wordList.get(panel);

        // Options column is on the left, results column on the right: the links list
        // (results) must sit to the right of the params options checkbox column.
        Field cbParamUrl = GapPanel.class.getDeclaredField("cbParamUrl");
        cbParamUrl.setAccessible(true);
        Component paramUrl = (Component) cbParamUrl.get(panel);

        int linksLeft = absoluteX(links);
        int paramsLeft = absoluteX(paramUrl);
        assertTrue(
                linksLeft > paramsLeft,
                "results column (links x=" + linksLeft + ") must be right of options column (cbParamUrl x=" + paramsLeft + ")");

        // Words and params result columns side by side, words to the right of params.
        int wordsLeft = absoluteX(words);
        int paramsResultLeft = absoluteX(params);
        assertTrue(
                wordsLeft >= paramsResultLeft,
                "words column (x=" + wordsLeft + ") must not overlap params column (x=" + paramsResultLeft + ")");
    }

    @Test
    void noHorizontalOverflowAtTypicalWindowWidths() {
        // Regression: the root panel had no layout manager (JPanel's default FlowLayout), so the
        // ~3100px-wide content panel was laid out at its preferred size and overflowed the ZAP
        // workbench, forcing horizontal scroll. The root must be BorderLayout-constrained so the
        // content always fits the tab width, with the columns shrinking to absorb it.
        for (int width : new int[] {1920, 1440, 1280, 1152}) {
            panel.setSize(width, 940);
            layoutAll(panel);
            for (Component c : new Component[] {panel, content, panel.getComponent(0)}) {
                assertTrue(
                        c.getX() + c.getWidth() <= width,
                        "component " + c.getClass().getSimpleName() + " overflows width " + width
                                + " (right edge " + (c.getX() + c.getWidth()) + ")");
            }
            assertTrue(
                    content.getWidth() <= width,
                    "content must fit width " + width + " (was " + content.getWidth() + ")");
        }
    }

    @Test
    void shortWindowScrollsVerticallyInsteadOfOverlapping() {
        // Regression: the GAP panel used to compress its rows when the tab was shorter than the
        // panel's natural height, pushing the "Potential links found" row against/over the content
        // above it. The content must now live in a vertical scroll pane: it keeps its natural
        // height and fills the width, so a short window scrolls instead of overlapping.
        JScrollPane scroll = (JScrollPane) panel.getComponent(0);
        int prefHeight = content.getPreferredSize().height;

        panel.setSize(1400, prefHeight + 100);
        layoutAll(panel);
        assertTrue(
                !scroll.getVerticalScrollBar().isVisible(),
                "tall window must not need a vertical scrollbar");
        assertTrue(
                content.getWidth() <= panel.getWidth()
                        && content.getWidth() == scroll.getViewport().getExtentSize().width,
                "content must track the viewport width (no horizontal scrollbar): content="
                        + content.getWidth()
                        + " viewport="
                        + scroll.getViewport().getExtentSize().width);

        panel.setSize(1400, Math.max(200, prefHeight - 300));
        layoutAll(panel);
        assertTrue(
                scroll.getVerticalScrollBar().isVisible(),
                "short window must show the vertical scrollbar");
        assertTrue(
                content.getHeight() > scroll.getViewport().getExtentSize().height,
                "content must keep its natural height and scroll instead of compressing (content="
                        + content.getHeight()
                        + " viewport="
                        + scroll.getViewport().getExtentSize().height
                        + ")");
        assertTrue(
                content.getWidth() == scroll.getViewport().getExtentSize().width,
                "no horizontal scrollbar even on a short window");
    }

    @Test
    void outputAreasAreReadOnlyButVisible() throws Exception {
        // Regression: ZAP's FlatLaf paints JTextArea with the transparent panel background, so the
        // output boxes were invisible. They must be opaque ash, read-only (they hold scan output)
        // but still selectable/copyable.
        String[] names = {"outParamList", "outParamSus", "outParamQuery", "outLinkList", "outWordList"};
        for (String name : names) {
            Field field = GapPanel.class.getDeclaredField(name);
            field.setAccessible(true);
            JTextArea area = (JTextArea) field.get(panel);
            assertTrue(!area.isEditable(), name + " must be read-only (output is not user-editable)");
            assertTrue(area.isOpaque(), name + " must be opaque (visible in FlatLaf)");
            assertTrue(
                    area.getBackground().equals(GapConstants.COLOR_OUTPUT_BG),
                    name + " must have the ash output background");
        }
    }

    @Test
    void uncheckingModeGreysBoxAndOptionsInsteadOfRemovingThem() throws Exception {
        // The mode checkboxes must keep the output box and its options in the layout when
        // unchecked, greying them out instead of hiding them. The greyed box must stay enabled so
        // its content can still be selected and copied.
        JScrollPane paramPane = (JScrollPane) field(panel, "scrollOutParamList");
        JScrollPane linkPane = (JScrollPane) field(panel, "scrollOutLinkList");
        JScrollPane wordPane = (JScrollPane) field(panel, "scrollOutWordList");
        JTextArea outParam = (JTextArea) field(panel, "outParamList");
        JTextArea outLink = (JTextArea) field(panel, "outLinkList");
        JTextArea outWord = (JTextArea) field(panel, "outWordList");
        JCheckBox cbParams = (JCheckBox) field(panel, "cbParamsEnabled");
        JCheckBox cbLinks = (JCheckBox) field(panel, "cbLinksEnabled");
        JCheckBox cbWords = (JCheckBox) field(panel, "cbWordsEnabled");
        JCheckBox cbParamUrl = (JCheckBox) field(panel, "cbParamUrl");
        JCheckBox cbInScopeOnly = (JCheckBox) field(panel, "cbInScopeOnly");
        JCheckBox cbShowWordOrigin = (JCheckBox) field(panel, "cbShowWordOrigin");
        JCheckBox cbSiteMapEndpoints = (JCheckBox) field(panel, "cbSiteMapEndpoints");
        JCheckBox cbWordPlurals = (JCheckBox) field(panel, "cbWordPlurals");

        assertTrue(cbParams.isSelected() && cbLinks.isSelected() && cbWords.isSelected());

        cbParams.doClick(); // uncheck params
        assertTrue(
                paramPane.isVisible(), "params output box must remain visible (not removed)");
        assertTrue(
                outParam.getForeground().equals(Color.GRAY),
                "params output must be greyed out when the mode is unchecked");
        assertTrue(!cbParamUrl.isEnabled(), "params options must be disabled when unchecked");
        assertTrue(outParam.isEnabled(), "greyed output must stay enabled so it can be copied");
        assertTrue(!outParam.isEditable(), "greyed output must remain read-only");

        cbParams.doClick(); // re-check
        assertTrue(
                outParam.getForeground().equals(Color.BLACK),
                "params output must be restored when the mode is re-checked");
        assertTrue(cbParamUrl.isEnabled(), "params options must be re-enabled");

        cbLinks.doClick(); // uncheck links
        assertTrue(linkPane.isVisible(), "links output box must remain visible (not removed)");
        assertTrue(outLink.getForeground().equals(Color.GRAY), "links output must be greyed out");
        assertTrue(!cbInScopeOnly.isEnabled(), "links options must be disabled when unchecked");
        cbLinks.doClick(); // re-check
        assertTrue(outLink.getForeground().equals(Color.BLACK), "links output must be restored");
        assertTrue(
                cbSiteMapEndpoints.isEnabled(), "links options must be re-enabled");

        cbWords.doClick(); // uncheck words
        assertTrue(wordPane.isVisible(), "words output box must remain visible (not removed)");
        assertTrue(outWord.getForeground().equals(Color.GRAY), "words output must be greyed out");
        assertTrue(!cbShowWordOrigin.isEnabled(), "words options must be disabled when unchecked");
        cbWords.doClick(); // re-check
        assertTrue(outWord.getForeground().equals(Color.BLACK), "words output must be restored");
        assertTrue(cbWordPlurals.isEnabled(), "words options must be re-enabled");
    }

    @Test
    void displayAndStopWordsRowsShareOneLineBelowTheirOutputBoxes() throws Exception {
        // Regression: the "Show 'sus'"/"Show query string with value" checkboxes used to sit on
        // their own line above the value field, and the value/stop-words fields used to stretch
        // to fill the panel. Now the two checkboxes and the value field ("XNLV") share one row
        // directly beneath the params output box, and the stop-words field shares the same row
        // beneath the words output box, all aligned and compact (same height as the "Link filter"
        // input) and never overlapping the links section below.
        JTextField qsv = (JTextField) field(panel, "inQueryStringVal");
        JTextField stopWords = (JTextField) field(panel, "inStopWords");
        JCheckBox cbSus = (JCheckBox) field(panel, "cbShowSusParams");
        JCheckBox cbQs = (JCheckBox) field(panel, "cbShowQueryString");
        Component scrollParam = (Component) field(panel, "scrollOutParamList");
        Component scrollWord = (Component) field(panel, "scrollOutWordList");
        Component linkPane = (Component) field(panel, "scrollOutLinkList");

        for (int width : new int[] {1920, 1680, 1400, 1280, 1152}) {
            panel.setSize(width, 700);
            layoutAll(panel);
            int rowTop = absoluteY(qsv);
            assertTrue(
                    Math.abs(absoluteY(cbSus) - rowTop) <= 2
                            && Math.abs(absoluteY(cbQs) - rowTop) <= 2,
                    "at width "
                            + width
                            + " the 'Show sus'/'Show query string' checkboxes must share the row "
                            + "with the value field (sus y="
                            + absoluteY(cbSus)
                            + ", qs y="
                            + absoluteY(cbQs)
                            + ", value y="
                            + rowTop
                            + ")");
            assertTrue(
                    Math.abs(absoluteY(stopWords) - rowTop) <= 2,
                    "at width "
                            + width
                            + " the stop-words field must share the same row as the value field "
                            + "(stopWords y="
                            + absoluteY(stopWords)
                            + " vs value y="
                            + rowTop
                            + ")");
            assertTrue(
                    rowTop >= absoluteY(scrollParam) + scrollParam.getHeight() - 2
                            && absoluteY(stopWords)
                                    >= absoluteY(scrollWord) + scrollWord.getHeight() - 2,
                    "at width "
                            + width
                            + " the display rows must sit directly beneath the params/words output "
                            + "boxes (row top "
                            + rowTop
                            + " vs params box bottom "
                            + (absoluteY(scrollParam) + scrollParam.getHeight())
                            + ", words box bottom "
                            + (absoluteY(scrollWord) + scrollWord.getHeight())
                            + ")");
            assertTrue(
                    rowTop + qsv.getHeight() <= absoluteY(linkPane),
                    "at width "
                            + width
                            + " the display rows must not overlap the links section below (row "
                            + "bottom "
                            + (rowTop + qsv.getHeight())
                            + " vs links top "
                            + absoluteY(linkPane)
                            + ")");
            assertTrue(
                    qsv.getHeight() <= 25 && stopWords.getHeight() <= 25,
                    "at width "
                            + width
                            + " the value/stop-words fields must stay compact (not stretched): qsv "
                            + qsv.getHeight()
                            + "px, stopWords "
                            + stopWords.getHeight()
                            + "px");
            assertTrue(
                    qsv.getWidth() >= 30,
                    "at width "
                            + width
                            + " the value field must stay wide enough to show its content (was "
                            + qsv.getWidth()
                            + "px)");
            assertTrue(
                    stopWords.getWidth() >= 60,
                    "at width "
                            + width
                            + " the stop-words field must stay wide enough to show its content (was "
                            + stopWords.getWidth()
                            + "px)");
        }
    }

    @Test
    void stopWordsFieldStaysOnTheDisplayRow() throws Exception {
        // Regression: at narrow widths the word column shrank and the "Stop words" field wrapped
        // onto a second line that collided with the "Potential links found" row below. The field
        // must now stay on the single display row (BorderLayout nests shrink instead of wrapping),
        // fully visible and never overlapping the links section.
        Field stopWordsField = GapPanel.class.getDeclaredField("inStopWords");
        stopWordsField.setAccessible(true);
        Field qsvField = GapPanel.class.getDeclaredField("inQueryStringVal");
        qsvField.setAccessible(true);
        Field linkListField = GapPanel.class.getDeclaredField("lblLinkList");
        linkListField.setAccessible(true);
        JTextField stopWords = (JTextField) stopWordsField.get(panel);
        JTextField qsv = (JTextField) qsvField.get(panel);
        Component linkHeader = (Component) linkListField.get(panel);
        Component linkPane = (Component) field(panel, "scrollOutLinkList");
        Container rightPanel = (Container) linkPane.getParent();

        for (int width : new int[] {1920, 1600, 1400, 1200}) {
            panel.setSize(width, 700);
            layoutAll(panel);
            assertTrue(
                    Math.abs(absoluteY(stopWords) - absoluteY(qsv)) <= 2,
                    "at width "
                            + width
                            + " the stop-words field must stay on the same row as the value field "
                            + "(stopWords y="
                            + absoluteY(stopWords)
                            + " vs qsv y="
                            + absoluteY(qsv)
                            + ")");
            assertTrue(
                    absoluteX(stopWords) + stopWords.getWidth()
                            <= absoluteX(rightPanel) + rightPanel.getWidth(),
                    "at width "
                            + width
                            + " the stop-words field must stay fully visible inside the results "
                            + "panel (right edge "
                            + (absoluteX(stopWords) + stopWords.getWidth())
                            + " vs panel "
                            + (absoluteX(rightPanel) + rightPanel.getWidth())
                            + ")");
            assertTrue(
                    absoluteY(stopWords) + stopWords.getHeight() <= absoluteY(linkHeader),
                    "at width "
                            + width
                            + " the stop-words field must not overlap the links section below "
                            + "(bottom "
                            + (absoluteY(stopWords) + stopWords.getHeight())
                            + " vs links top "
                            + absoluteY(linkHeader)
                            + ")");
        }
    }

    @Test
    void clearButtonSitsAtTheFarRightOfTheSaveOptionsRow() throws Exception {
        // The Clear button must live on the "Save options" row (the config row at the bottom of
        // the options column), pinned to its far right, and be compact (same smaller size as the
        // other secondary buttons) rather than stretched to the results column height.
        JScrollPane scroll = (JScrollPane) panel.getComponent(0);
        Container content = (Container) scroll.getViewport().getView();
        Container leftPanel = (Container) content.getComponent(0);

        javax.swing.AbstractButton btnClear = (javax.swing.AbstractButton) field(panel, "btnClear");
        javax.swing.AbstractButton btnSave = (javax.swing.AbstractButton) field(panel, "btnSave");

        Component ancestor = btnClear;
        while (ancestor.getParent() != content) {
            ancestor = ancestor.getParent();
        }
        assertTrue(
                ancestor == leftPanel,
                "Clear button must live on the options column (the 'Save options' row), found "
                        + ancestor.getClass().getSimpleName());

        panel.setSize(1400, 700);
        layoutAll(panel);
        assertTrue(
                absoluteX(btnClear) > absoluteX(btnSave),
                "Clear button (x="
                        + absoluteX(btnClear)
                        + ") must sit to the right of Save options (x="
                        + absoluteX(btnSave)
                        + ")");
        assertTrue(
                btnClear.getPreferredSize().height <= 25,
                "Clear button must stay compact (preferred height "
                        + btnClear.getPreferredSize().height
                        + ")");
    }

    @Test
    void clearButtonClearsAllOutput() throws Exception {
        // The Clear button must reset every output area and its count label to a blank state.
        Field btnField = GapPanel.class.getDeclaredField("btnClear");
        btnField.setAccessible(true);
        JTextArea out = (JTextArea) field(panel, "outLinkList");
        JTextArea outWord = (JTextArea) field(panel, "outWordList");
        JLabel lbl = (JLabel) field(panel, "lblLinkList");
        out.setText("one\n two\n three");
        outWord.setText("alpha\n beta");
        lbl.setText("Potential links found - 3 filtered:");

        javax.swing.AbstractButton btnClear = (javax.swing.AbstractButton) btnField.get(panel);
        btnClear.doClick();

        assertEquals("", out.getText(), "link output must be cleared");
        assertEquals("", outWord.getText(), "word output must be cleared");
        assertEquals("Potential links found:", lbl.getText(), "label must be reset");
    }

    private static Object field(Object obj, String name) throws Exception {
        java.lang.reflect.Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    private static int absoluteX(Component component) {
        int x = 0;
        Component c = component;
        while (c != null) {
            x += c.getX();
            c = c.getParent();
        }
        return x;
    }

    private static int absoluteY(Component component) {
        int y = 0;
        Component c = component;
        while (c != null) {
            y += c.getY();
            c = c.getParent();
        }
        return y;
    }
}
