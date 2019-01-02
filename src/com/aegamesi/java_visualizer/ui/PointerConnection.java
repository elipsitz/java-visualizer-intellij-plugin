package com.aegamesi.java_visualizer.ui;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;

class PointerConnection {
	private static final double curviness = 10.0;
	private Shape mainShape;

	private double x1, y1, x2, y2;

	private int computeSegment(double x1, double y1, double x2, double y2) {
		if (x1 <= x2 && y2 <= y1)
			return 1;
		else if (x1 <= x2 && y1 <= y2)
			return 2;
		else if (x2 <= x1 && y2 >= y1)
			return 3;
		return 4;
	}

	// topcenter -> leftmiddle
	PointerConnection(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

		if (dist < 75) {
			mainShape = new Line2D.Double(x1, y1, x2, y2);
		} else {
			// Line style: "state machine" from JsPlumb
			int segment = computeSegment(x1, y1, x2, y2);
			double midx = (x1 + x2) / 2.0;
			double midy = (y1 + y2) / 2.0;

			double cy = midy - curviness;
			double cx = midx + ((segment == 1 || segment == 3) ? -curviness : curviness);

			mainShape = new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2);
		}

		/*
			// Line style: cubic bezier
			double delta = 30.0;
			return new CubicCurve2D.Double(
					x1, y1,
					x1 + delta, y1,
					x2 - delta, y2,
					x2, y2
			);*/
	}

	void paint(Graphics2D g) {
		g.setColor(Constants.colorPointer);
		g.draw(mainShape);
		int r = Constants.pointerSrcRadius;
		g.fillOval((int) (x1 - r), (int) (y1 - r), r * 2, r * 2);
	}
}
