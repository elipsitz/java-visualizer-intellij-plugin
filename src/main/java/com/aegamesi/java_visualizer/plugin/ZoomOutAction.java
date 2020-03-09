package com.aegamesi.java_visualizer.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import org.jetbrains.annotations.NotNull;

public class ZoomOutAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MainPane pane = (MainPane) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        if (pane != null) {
            pane.zoom(-1);
        }
    }
}