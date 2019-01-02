package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.model.Value;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import static com.aegamesi.java_visualizer.ui.Constants.*;

public class VisualizationPanel extends JPanel {
	private ExecutionTrace trace = null;

	private List<ValueComponent> referenceComponents;
	private List<Shape> pointerShapes;
	private StackPanel stackPanel;
	private HeapPanel heapPanel;

	public VisualizationPanel() {
		setBackground(colorBackground);
		setLayout(null);
		referenceComponents = new ArrayList<>();
		pointerShapes = new ArrayList<>();
	}

	public void setTrace(ExecutionTrace t) {
		this.trace = t;
		referenceComponents.clear();
		removeAll();

		buildUI();

		revalidate();
		repaint();
	}

	private void buildUI() {
		JLabel labelStack = new JLabel("Call Stack", JLabel.RIGHT);
		JLabel labelHeap = new JLabel("Objects", JLabel.LEFT);
		labelStack.setFont(fontTitle);
		labelHeap.setFont(fontTitle);
		stackPanel = new StackPanel(this, trace.frames);
		heapPanel = new HeapPanel(this, trace.heap);

		add(labelStack);
		add(labelHeap);
		add(stackPanel);
		add(heapPanel);

		int labelHeight = Math.max(labelStack.getPreferredSize().height, labelHeap.getPreferredSize().height);
		Dimension sizeStack = stackPanel.getPreferredSize();
		Dimension sizeHeap = heapPanel.getPreferredSize();
		int stackWidth = Math.max(labelStack.getPreferredSize().width, sizeStack.width);
		int heapWidth = Math.max(labelHeap.getPreferredSize().width, sizeHeap.width);
		labelStack.setBounds(padOuter, padOuter, stackWidth, labelHeight);
		labelHeap.setBounds(padOuter + stackWidth + padCenter, padOuter, heapWidth, labelHeight);
		stackPanel.setBounds(padOuter, padOuter + labelHeight + padTitle, stackWidth, sizeStack.height);
		heapPanel.setBounds(padOuter + stackWidth + padCenter, padOuter + labelHeight + padTitle, heapWidth, sizeHeap.height);
		setPreferredSize(new Dimension(
				padOuter + stackWidth + padCenter + heapWidth,
				padOuter + labelHeight + padTitle + Math.max(sizeStack.height, sizeHeap.height)
		));
	}

	private void computePointerPaths() {
		pointerShapes.clear();

		for (ValueComponent ref : referenceComponents) {
			Rectangle refBounds = getRelativeBounds(this, ref);
			long refId = ref.getValue().reference;
			HeapEntityComponent obj = heapPanel.getHeapComponents().get(refId);
			Rectangle objBounds = getRelativeBounds(this, obj);

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
		g.setColor(colorPointer);
		for (Shape path : pointerShapes) {
			g.draw(path);
		}
	}

	@Override
	protected void validateTree() {
		super.validateTree();
		computePointerPaths();
	}

	List<ValueComponent> getReferenceComponents() {
		return referenceComponents;
	}

	void registerValueComponent(ValueComponent component) {
		if (component.getValue().type == Value.Type.REFERENCE) {
			referenceComponents.add(component);
		}
	}
}
