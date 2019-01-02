package com.aegamesi.java_visualizer.ui;

import com.aegamesi.java_visualizer.model.HeapEntity;
import com.aegamesi.java_visualizer.model.Value;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class HeapPanel extends JPanel {
	private VisualizationPanel viz;
	private Map<Long, HeapEntity> heap;

	private Map<Long, HeapEntityComponent> components = new HashMap<>();

	private Set<Long> layoutCompleted = new HashSet<>();
	private LinkedList<ValueComponent> layoutPending = new LinkedList<>();

	HeapPanel(VisualizationPanel viz, Map<Long, HeapEntity> heap) {
		this.viz = viz;
		this.heap = heap;

		setOpaque(false);
		setLayout(null);

		createComponents();
		computeLayout();
	}

	Map<Long, HeapEntityComponent> getHeapComponents() {
		return components;
	}

	private void createComponents() {
		// Create the heap components (but don't lay them out yet)
		for (Map.Entry<Long, HeapEntity> pair : heap.entrySet()) {
			HeapEntityComponent obj = new HeapEntityComponent(viz, pair.getValue());
			components.put(pair.getKey(), obj);
			add(obj);
		}
	}

	private void computeLayout() {
		layoutPending.addAll(viz.getReferenceComponents());

		Rectangle bounds = new Rectangle();
		int x = 0;
		int y = 0;
		int rowHeight = 0;
		HeapEntityComponent lastEntity = null;
		while (!layoutPending.isEmpty()) {
			ValueComponent vc = layoutPending.removeFirst();
			long id = vc.getValue().reference;
			if (layoutCompleted.contains(id)) {
				continue;
			}
			layoutCompleted.add(id);

			HeapEntityComponent component = components.get(id);
			addValuesToLayout(component.getValueComponents());

			Dimension size = component.getPreferredSize();
			boolean sameRow = lastEntity == null || lastEntity.getEntity().hasSameStructure(component.getEntity());
			if (!sameRow) {
				y += rowHeight + Constants.padHeapVertical;
				x = 0;
				rowHeight = 0;
			}
			Rectangle originBounds = Constants.getRelativeBounds(this, vc);
			if (originBounds != null) {
				x = Math.max(x, originBounds.x + Constants.padHeapContinuation);
			}
			component.setBounds(x, y, size.width, size.height);

			x += size.width + Constants.padHeapHorizontal;
			rowHeight = Math.max(rowHeight, size.height);
			bounds = bounds.union(component.getBounds());

			lastEntity = component;
		}
		setPreferredSize(new Dimension(bounds.width, bounds.height));
	}

	private void addValuesToLayout(List<ValueComponent> vals) {
		for (int i = vals.size() - 1; i >= 0; i -= 1) {
			ValueComponent vc = vals.get(i);
			Value v = vc.getValue();
			if (v.type == Value.Type.REFERENCE) {
				if (!layoutCompleted.contains(v.reference)) {
					layoutPending.addFirst(vc);
				}
			}
		}
	}
}
