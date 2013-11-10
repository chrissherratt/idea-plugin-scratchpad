package org.intellij.scratchpad.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import org.intellij.scratchpad.ScratchPadProjectComponent;

public class RemoveTab extends TabAction {

    public RemoveTab() {
        tabMustExist = true;
    }

    public void actionPerformed(AnActionEvent e) {
        ScratchPadProjectComponent scratchPad = getScratchPadComponent(e);
        if (scratchPad != null)
            scratchPad.removeTab();
    }

}
