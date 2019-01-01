package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.model.HeapEntity;
import com.aegamesi.java_visualizer.model.Value;
import com.intellij.util.containers.hash.HashMap;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VisualizationPanel extends JPanel {
	private ExecutionTrace trace = null;

	private List<ValueComponent> referenceComponents;
	private Map<Long, HeapEntityComponent> heapEntityComponents;

	private List<Shape> pointerShapes;

	public VisualizationPanel() {
		setBackground(Constants.colorBackground);
		setLayout(null);
		referenceComponents = new ArrayList<>();
		heapEntityComponents = new HashMap<>();
		pointerShapes = new ArrayList<>();
	}

	public void setTrace(ExecutionTrace t) {
		this.trace = t;
		referenceComponents.clear();
		heapEntityComponents.clear();
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
		for (int i = 0; i < trace.frames.size(); i++) {
			frames.add(new StackFrameComponent(this, trace.frames.get(i), i == 0));
			frames.add(Box.createVerticalStrut(8));
		}
		add(frames);

		Box heap = Box.createVerticalBox();
		for (Map.Entry<Long, HeapEntity> pair : trace.heap.entrySet()) {
			HeapEntityComponent obj = new HeapEntityComponent(this, pair.getValue());
			heapEntityComponents.put(pair.getKey(), obj);
			heap.add(obj);
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

	private void computePointerPaths() {
		pointerShapes.clear();

		for (ValueComponent ref : referenceComponents) {
			Rectangle refBounds = getTrueComponentBounds(ref);
			long refId = ref.getValue().reference;
			Rectangle objBounds = getTrueComponentBounds(heapEntityComponents.get(refId));

			Shape p = constructPath(
					refBounds.x + refBounds.width,
					refBounds.y + (refBounds.height / 2.0),
					objBounds.x,
					objBounds.y + (objBounds.height / 2.0)
			);
			pointerShapes.add(p);
		}
	}

	private Shape constructPath(double x0, double y0, double x1, double y1) {
		double dist = Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));

		if (dist < 50.0) {
			return new Line2D.Double(x0, y0, x1, y1);
		} else {
			/*
			// Line style: cubic bezier
			double delta = 30.0;
			return new CubicCurve2D.Double(
					x0, y0,
					x0 + delta, y0,
					x1 - delta, y1,
					x1, y1
			);*/
			// Line style: "state machine"
			return new PointerShape(x0, y0, x1, y1);
		}
	}

	@Override
	protected void paintChildren(Graphics _g) {
		super.paintChildren(_g);
		Graphics2D g = (Graphics2D) _g;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Constants.colorPointer);
		for (Shape path : pointerShapes) {
			g.draw(path);
		}
	}

	@Override
	public void validate() {
		super.validate();

		computePointerPaths();
	}

	/**
	 * Returns the Bounds of the component, relative to this panel.
	 */
	private Rectangle getTrueComponentBounds(Component c) {
		Rectangle r = c.getBounds();
		c = c.getParent();
		while (c != null && c != this) {
			Point loc = c.getLocation();
			r.translate(loc.x, loc.y);
			c = c.getParent();
		}
		return r;
	}

	void registerValueComponent(ValueComponent component) {
		if (component.getValue().type == Value.Type.REFERENCE) {
			referenceComponents.add(component);
		}
	}
}
