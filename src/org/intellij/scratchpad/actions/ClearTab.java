package org.intellij.scratchpad.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.intellij.scratchpad.ScratchPadProjectComponent;

public class ClearTab extends TabAction {

    public ClearTab() {
        tabMustExist = true;
    }

    public void actionPerformed(AnActionEvent e) {
        ScratchPadProjectComponent scratchPad = getScratchPadComponent(e);
        if (scratchPad!=null)
            scratchPad.clearTab();
    }
}
