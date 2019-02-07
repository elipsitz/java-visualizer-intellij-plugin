package com.aegamesi.java_visualizer.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;

class PointerConnection {
	private static final double curviness = 10.0;
	private Shape mainShape;
	private Path2D arrow;

	private boolean selected;
	private boolean active;
	private double x1, y1, x2, y2;

	private static int computeSegment(double x1, double y1, double x2, double y2) {
		if (x1 <= x2 && y2 <= y1)
			return 1;
		else if (x1 <= x2 && y1 <= y2)
			return 2;
		else if (x2 <= x1 && y2 >= y1)
			return 3;
		return 4;
	}

	private static Path2D makeArrow() {
		Path2D.Double p = new Path2D.Double();
		p.moveTo(2.0, 0.0);
		p.lineTo(-9.0, -3.5);
		// p.lineTo(-4.5, 0.0);
		p.lineTo(-9.0, 3.5);
		p.closePath();
		return p;
	}

	PointerConnection(boolean active, double x1, double y1, double x2, double y2) {
		this.active = active;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		double dx, dy;

		if (dist < 50) {
			mainShape = new Line2D.Double(x1, y1, x2, y2);
			dx = x2 - x1;
			dy = y2 - y1;
		} else {
			// Line style: "state machine" from JsPlumb
			int segment = computeSegment(x1, y1, x2, y2);
			double midx = (x1 + x2) / 2.0;
			double midy = (y1 + y2) / 2.0;

			double cy = midy - curviness;
			double cx = midx + ((segment == 1 || segment == 3) ? -curviness : curviness);

			mainShape = new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2);
			dx = x2 - cx;
			dy = y2 - cy;


			// Line style: cubic bezier
			/* double delta = 30.0;
			mainShape = new CubicCurve2D.Double(
					x1, y1,
					x1 + delta, y1,
					x2 - delta, y2,
					x2, y2
			);
			dx = 1.0;
			dy = 0.0; */
		}

		arrow = makeArrow();
		arrow.transform(AffineTransform.getRotateInstance(dx, dy));
		arrow.transform(AffineTransform.getTranslateInstance(x2, y2));
	}

	void setSelected(boolean selected) {
		this.selected = selected;
	}

	boolean isNear(int x, int y) {
		if (mainShape.intersects(x - 2, y - 2, 4, 4)) {
			return true;
		}
		return Math.abs(x - x1) < 8 && Math.abs(y - y1) < 8;
	}

	void paint(Graphics2D g) {
		Color c = Constants.colorPointerInactive;
		if (active) {
			c = Constants.colorPointer;
			if (selected) {
				c = Constants.colorPointerSelected;
			}
		}
		g.setColor(c);
		g.draw(mainShape);
		int r = Constants.pointerSrcRadius;
		g.fillOval((int) (x1 - r), (int) (y1 - r), r * 2, r * 2);
		g.fill(arrow);
	}
}
