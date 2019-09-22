package com.aegamesi.java_visualizer.plugin;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ZoomInAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PropertiesComponent props = PropertiesComponent.getInstance();
    }
}