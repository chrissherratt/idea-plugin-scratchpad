package org.intellij.scratchpad.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.intellij.scratchpad.ScratchPadProjectComponent;

public class SortTab extends TabAction {

    public SortTab() {
        tabMustExist = true;
    }

    public void actionPerformed(AnActionEvent e) {
        ScratchPadProjectComponent scratchPad = getScratchPadComponent(e);
        if (scratchPad!=null) {
            scratchPad.sortContents();
        }
    }

}
