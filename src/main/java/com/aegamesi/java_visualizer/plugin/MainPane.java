package com.aegamesi.java_visualizer.plugin;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.ui.VisualizationPanel;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

class MainPane extends JPanel {
	private JLabel placeholderLabel;
	private VisualizationPanel viz;

    private final float[] ZOOM_LEVELS = {0.25f, 0.333f, 0.5f, 0.666f, 0.75f, 0.8f, 0.9f, 1.0f, 1.1f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f, 4.0f};

	MainPane() {
		setLayout(new BorderLayout());

		String text = "No execution trace loaded: make sure you've stopped on a breakpoint.";
		placeholderLabel = new JLabel(text, SwingConstants.CENTER);
		add(placeholderLabel);
	}

	void setTrace(ExecutionTrace trace) {
		if (viz == null) {
			remove(placeholderLabel);
			viz = new VisualizationPanel();
            viz.setScale(getZoom());
			JBScrollPane scrollPane = new JBScrollPane(viz);
			scrollPane.setBorder(null);
			add(scrollPane);
			revalidate();
		}
		viz.setTrace(trace);
	}

    void zoom(int direction) {
        if (viz != null) {
            float currentZoom = getZoom();
            int closestLevel = -1;
            float closestLevelDistance = Float.MAX_VALUE;
            for (int i = 0; i < ZOOM_LEVELS.length; i += 1) {
                float dist = Math.abs(ZOOM_LEVELS[i] - currentZoom);
                if (dist < closestLevelDistance) {
                    closestLevelDistance = dist;
                    closestLevel = i;
                }
            }

            int level = Math.max(0, Math.min(ZOOM_LEVELS.length - 1, closestLevel + direction));
            float newZoom = ZOOM_LEVELS[level];
            PropertiesComponent.getInstance().setValue(JavaVisualizerManager.KEY_ZOOM, newZoom, 1.0f);
            viz.setScale(newZoom);
        }
    }

    private float getZoom() {
        return PropertiesComponent.getInstance().getFloat(JavaVisualizerManager.KEY_ZOOM, 1.0f);
    }
}
