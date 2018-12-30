package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.model.Frame;
import com.aegamesi.java_visualizer.model.HeapEntity;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;

public class VisualizationPanel extends JPanel {
	private ExecutionTrace trace = null;

	public VisualizationPanel() {
		setBackground(Constants.colorBackground);
		setLayout(null);
	}

	public void setTrace(ExecutionTrace t) {
		this.trace = t;
		removeAll();
		buildUI();

		revalidate();
		repaint();
	}

	private void buildUI() {
		Box frames = Box.createVerticalBox();
		JLabel frameLabel = new JLabel("Frames");
		frameLabel.setFont(Constants.fontMessage);
		frameLabel.setHorizontalAlignment(JLabel.RIGHT);
		frames.add(frameLabel);
		frames.add(Box.createVerticalStrut(24));
		for (Frame f : trace.frames) {
			frames.add(new StackFrameComponent(f));
			frames.add(Box.createVerticalStrut(8));
		}
		add(frames);

		Box heap = Box.createVerticalBox();
		for (HeapEntity e : trace.heap.values()) {
			heap.add(new HeapEntityComponent(e));
			heap.add(Box.createVerticalStrut(8));
		}
		add(heap);

		Dimension sizeFrames = frames.getPreferredSize();
		Dimension sizeHeap = heap.getPreferredSize();
		frames.setBounds(
				Constants.outerPadding,
				Constants.outerPadding,
				sizeFrames.width,
				sizeFrames.height
		);
		heap.setBounds(
				Constants.outerPadding + sizeFrames.width + Constants.centerMargin,
				Constants.outerPadding,
				sizeHeap.width,
				sizeHeap.height
		);

		setPreferredSize(new Dimension(
				Constants.outerPadding + sizeFrames.width + Constants.centerMargin + sizeHeap.width,
				Constants.outerPadding + Math.max(sizeFrames.height, sizeHeap.height)
		));
	}
}
