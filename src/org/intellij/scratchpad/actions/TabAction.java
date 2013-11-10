package org.intellij.scratchpad.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import org.intellij.scratchpad.ScratchPadProjectComponent;

public abstract class TabAction extends AnAction {

    protected boolean tabMustExist = false;

    protected ScratchPadProjectComponent getScratchPadComponent(AnActionEvent e) {
        Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        if (project==null) return null;
        return project.getComponent(ScratchPadProjectComponent.class);
    }

    public void update(AnActionEvent e) {
        if (!tabMustExist) return;
        ScratchPadProjectComponent scratchPad = getScratchPadComponent(e);
        int tabCount = 0;
        if (scratchPad != null) {
            tabCount = scratchPad.getTabCount();
        }
        e.getPresentation().setEnabled(tabCount > 0);
    }


}
