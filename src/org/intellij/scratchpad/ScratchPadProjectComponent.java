package org.intellij.scratchpad;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class ScratchPadProjectComponent implements ProjectComponent, JDOMExternalizable {

    private static final String COMPONENT_NAME = "org.intellij.scratchpad.ScratchPadProjectComponent";
    private static final String CONTEXT_POPUP_GROUP = "ScratchPadContextPopup";
    private static final String CONTEXT_POPUP_PLACE = "ScratchPadContextPopupPlace";
    private static final String TOOLBAR_GROUP = "ScratchPadToolbarGroup";
    private static final String TOOLBAR_PLACE = "ScratchPadToolbarPlace";
    private static final String TOOL_WINDOW_ID = "ScratchPad";

    private static final Icon TOOL_WINDOW_ICON = IconLoader.getIcon("/org/intellij/scratchpad/resources/scratchPad.png", ScratchPadProjectComponent.class);

    private static final Logger LOG = Logger.getLogger(ScratchPadProjectComponent.class.getName());

    private Project project;
    private JTabbedPane tabs;
    private Map<String, Editor> editorMap;
    private transient Map<String, String> savedDocs = null;
    private JFileChooser chooser;

    public ScratchPadProjectComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {
        // do nothing
    }

    public void disposeComponent() {
        // do nothing
    }

    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public void projectOpened() {
        // called when project is opened
        initToolWindow();
    }

    public void projectClosed() {
        // called when project is being closed
        finaliseToolWindow();
        this.project = null;
    }


    private void initToolWindow() {

        editorMap = new HashMap<String, Editor>();

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        JPanel contentPanel = new JPanel(new BorderLayout());

        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction(TOOLBAR_GROUP);
        ActionToolbar toolBar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, actionGroup, true);
        contentPanel.add(toolBar.getComponent(), BorderLayout.NORTH);

        tabs = new ReorderableTabbedPane();
        tabs.setTabPlacement(JTabbedPane.BOTTOM);
        tabs.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                doTabbedPaneMouseClicked(e);
            }
        });
        contentPanel.add(tabs, BorderLayout.CENTER);

        //set tabs
        if (savedDocs != null) {
            Set<String> docNameSet = savedDocs.keySet();
            for (String docName : docNameSet) {
                newTab(docName);
                setTabContents(docName, savedDocs.get(docName));
            }
            savedDocs = null;
        } else {
            newTab("Default");
        }

        ToolWindow toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, contentPanel, ToolWindowAnchor.LEFT);
        toolWindow.setTitle("");
        toolWindow.setIcon(TOOL_WINDOW_ICON);

        chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);

    }

    private void doTabbedPaneMouseClicked(MouseEvent e) {

        if (!(e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3))
            return;

        int tabcount = tabs.getTabCount();
        for (int i = 0; i < tabcount; i++) {
            TabbedPaneUI tpu = tabs.getUI();
            Rectangle rect = tpu.getTabBounds(tabs, i);
            int x = e.getX();
            int y = e.getY();
            if (x < rect.x || x > rect.x + rect.width || y < rect.y || y > rect.y + rect.height)
                continue;

            //setSelectedIndex shouldn't be necessary here, but just in case the listeners get
            // called in different order (?), the callback code for each my menu items assumes
            // that it can use getSelectedIndex() to determine which tab it should operate upon.
            tabs.setSelectedIndex(i);

            ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction(CONTEXT_POPUP_GROUP);
            ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(CONTEXT_POPUP_PLACE, actionGroup);
            popupMenu.getComponent().show(tabs, x, y);
            break;
        }

    }

    private void finaliseToolWindow() {
        while (tabs.getTabCount() > 0) {
            removeTab(0);
        }
        editorMap.clear();
        editorMap = null;
    }

    public void newTab() {
        String newTabName = "New Tab";
        if (editorMap.get(newTabName) != null) {
            int newTabId = 2;
            while (editorMap.get(newTabName + " " + newTabId) != null) {
                newTabId++;
            }
            newTabName += " " + newTabId;
        }
        newTab(newTabName);
    }

    public void newTab(String tabName) {
        Document document = EditorFactory.getInstance().createDocument("");
        Editor editor = EditorFactory.getInstance().createEditor(document, project);
        editorMap.put(tabName, editor);
        JComponent editorComponent = editor.getComponent();
        tabs.addTab(tabName, editorComponent);
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    public void removeTab() {
        int tabIndex = tabs.getSelectedIndex();
        if (tabIndex < 0) return;
        removeTab(tabIndex);
    }

    public void removeTab(int tabIndex) {
        String tabName = tabs.getTitleAt(tabIndex);
        tabs.removeTabAt(tabIndex);
        Editor editor = editorMap.get(tabName);
        editorMap.remove(tabName);
        if (editor == null) return;
        EditorFactory.getInstance().releaseEditor(editor);
    }

    public void renameTab() {
        String oldName = tabs.getTitleAt(tabs.getSelectedIndex());
        String newName = (String) JOptionPane.showInputDialog(tabs, "New name:", "Rename tab", JOptionPane.QUESTION_MESSAGE, null, null, oldName);
        if (newName == null) return;
        renameTab(oldName, newName);
    }

    public void renameTab(String oldName, String newName) {
        if (oldName == null || newName == null) return;
        int tabIndex = -1;
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (oldName.equals(tabs.getTitleAt(i))) {
                tabIndex = i;
                break;
            }
        }
        if (tabIndex < 0) return;
        tabs.setTitleAt(tabIndex, newName);

        editorMap.put(newName, editorMap.get(oldName));
        editorMap.remove(oldName);
    }

    public int getTabCount() {
        if (tabs == null) return 0;
        return tabs.getTabCount();
    }

    public void setTabContents(final String tabName, final String docContents) {
        Runnable runnable = new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        try {
                            Editor editor = editorMap.get(tabName);
                            if (editor==null) return;
                            int length = editor.getDocument().getTextLength();
                            editor.getDocument().deleteString(0, length);
                            editor.getDocument().insertString(0, docContents);
                        }
                        catch (Throwable t) {
                            LOG.throwing(ScratchPadProjectComponent.class.getName(), "setTabContents", t);
                        }
                    }
                });
            }
        };

        CommandProcessor.getInstance().executeCommand(project, runnable, "scratchPad.setTabContents", null);
    }

    public void readExternal(Element element) throws InvalidDataException {
        savedDocs = XMLHelper.readDocuments(element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        Map<String, String> docs = new LinkedHashMap<String, String>();
        if (tabs == null) return;
        for (int i = 0; i < tabs.getTabCount(); i++) {
            String tabName = tabs.getTitleAt(i);
            docs.put(tabName, editorMap.get(tabName).getDocument().getText());
        }
        XMLHelper.writeDocuments(element, docs);
    }

    public void clearTab() {
        int tabIndex = tabs.getSelectedIndex();
        if (tabIndex < 0) return;
        String tabName = tabs.getTitleAt(tabIndex);
        setTabContents(tabName, "");
    }

    public void saveTab() {
        int tabIndex = tabs.getSelectedIndex();
        if (tabIndex < 0) return;

        chooser.setDialogTitle("Save Tab Contents");

        Frame window = JOptionPane.getFrameForComponent(tabs);
        int result = chooser.showSaveDialog(window);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null) return;

        String tabName = tabs.getTitleAt(tabIndex);
        String contents = editorMap.get(tabName).getDocument().getText();

        try {
            FileWriter writer = new FileWriter(selectedFile);
            writer.append(contents);
            writer.close();
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(window, "Unable to save tab contents.\n" + t.getMessage(), "Save Tab Contents", JOptionPane.ERROR_MESSAGE);
            Logger.getAnonymousLogger().throwing(ScratchPadProjectComponent.class.getName(), "saveTab", t);
        }
    }

    public void loadTab() {
        int tabIndex = tabs.getSelectedIndex();
        if (tabIndex < 0) {
            newTab();
            tabIndex = 0;
        }

        chooser.setDialogTitle("Load File Into Tab");
        Frame window = JOptionPane.getFrameForComponent(tabs);
        int result = chooser.showOpenDialog(window);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null) return;

        String tabName = tabs.getTitleAt(tabIndex);

        StringBuffer contentBuffer = new StringBuffer();
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(selectedFile));
            String line;
            while ((line = buffReader.readLine()) != null) {
                if (contentBuffer.length() > 0) contentBuffer.append("\n");
                contentBuffer.append(line);
            }
            buffReader.close();
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(window, "Unable to load file into tab.\n" + t.getMessage(), "Load Tab Contents", JOptionPane.ERROR_MESSAGE);
            Logger.getAnonymousLogger().throwing(ScratchPadProjectComponent.class.getName(), "loadTab", t);
            return;
        }

        setTabContents(tabName, contentBuffer.toString());

    }

    public void sortContents() {
        int tabIndex = tabs.getSelectedIndex();
        if (tabIndex < 0) return;

        String tabName = tabs.getTitleAt(tabIndex);
        String contents = editorMap.get(tabName).getDocument().getText();

        List<String> lines = asList(contents.split("\n"));
        Collections.sort(lines);

        String newContents = StringUtils.join(lines, "\n");

        setTabContents(tabName, newContents);
    }

}
