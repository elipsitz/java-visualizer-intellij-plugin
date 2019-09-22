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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.aegamesi.java_visualizer.ui.Constants.*;

public class VisualizationPanel extends JPanel {
	private ExecutionTrace trace = null;
    private double scale = 1.0;

	private List<ValueComponent> referenceComponents;
	private List<PointerConnection> pointerConnections;
	private StackPanel stackPanel;
	private HeapPanel heapPanel;

	private PointerConnection selectedPointer;

	public VisualizationPanel() {
		setBackground(colorBackground);
		setLayout(null);
		referenceComponents = new ArrayList<>();
		pointerConnections = new ArrayList<>();

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
                int px = (int) (e.getX() / scale);
                int py = (int) (e.getY() / scale);
                PointerConnection sel = getSelectedPointer(px, py);
				if (sel != selectedPointer) {
					if (selectedPointer != null) {
						selectedPointer.setSelected(false);
					}
					selectedPointer = sel;
					if (selectedPointer != null) {
						selectedPointer.setSelected(true);
					}
					repaint();
				}
			}
		});
	}

    public void setTrace(ExecutionTrace t) {
        this.trace = t;
        refreshUI();
    }

    public void setScale(double scale) {
        this.scale = scale;
        if (this.trace != null) {
            refreshUI();
        }
    }

    private void refreshUI() {
        referenceComponents.clear();
        removeAll();

        buildUI();

        revalidate();
        repaint();
    }

	private void buildUI() {
		JLabel labelStack = new CustomJLabel("Call Stack", JLabel.RIGHT);
		JLabel labelHeap = new CustomJLabel("Objects", JLabel.LEFT);
		labelStack.setForeground(Constants.colorText);
		labelHeap.setForeground(Constants.colorText);
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

        int outerWidth = (padOuter * 2) + stackWidth + padCenter + heapWidth;
        int outerHeight = (padOuter * 2) + labelHeight + padTitle + Math.max(sizeStack.height, sizeHeap.height);
        setPreferredSize(new Dimension((int) (outerWidth * scale), (int) (outerHeight * scale)));
	}

	private void computePointerPaths() {
		if (heapPanel == null) {
			return;
		}
		pointerConnections.clear();

		for (ValueComponent ref : referenceComponents) {
			Rectangle refBounds = getRelativeBounds(this, ref);
			long refId = ref.getValue().reference;
			HeapEntityComponent obj = heapPanel.getHeapComponents().get(refId);
			if (obj == null) {
				continue; // shouldn't happen...
			}
			Rectangle objBounds = getRelativeBounds(this, obj);
			if (refBounds == null || objBounds == null) {
				continue;
			}

			PointerConnection p = new PointerConnection(
					ref.isActive(),
					refBounds.x + refBounds.width - (pointerWidth / 2.0),
					refBounds.y + (refBounds.height / 2.0),
					objBounds.x,
					objBounds.y + (objBounds.height / 2.0)
			);
			pointerConnections.add(p);
		}
	}

	@Override
	protected void paintChildren(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;
        g.scale(scale, scale);

        super.paintChildren(g);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (PointerConnection p : pointerConnections) {
			p.paint(g);
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

	private PointerConnection getSelectedPointer(int x, int y) {
		if (pointerConnections == null) {
			return null;
		}
		PointerConnection selected = null;
		Iterator<PointerConnection> i = pointerConnections.iterator();
		while (i.hasNext()) {
			PointerConnection p = i.next();
			if (p.isNear(x, y)) {
				selected = p;
				i.remove();
				break;
			}
		}
		if (selected != null) {
			pointerConnections.add(selected);
		}
		return selected;
	}
}
