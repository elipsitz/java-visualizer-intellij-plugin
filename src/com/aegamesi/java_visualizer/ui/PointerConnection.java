package com.aegamesi.java_visualizer.ui;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;

class PointerConnection {
	private static final double curviness = 10.0;
	private Shape mainShape;

	private double x0, y0, x1, y1;

	private int computeSegment(double x0, double y0, double x1, double y1) {
		if (x0 <= x1 && y1 <= y0)
			return 1;
		else if (x0 <= x1 && y0 <= y1)
			return 2;
		else if (x1 <= x0 && y1 >= y0)
			return 3;
		return 4;
	}

	// topcenter -> leftmiddle
	PointerConnection(double x0, double y0, double x1, double y1) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		double dist = Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));

		if (dist < 75) {
			mainShape = new Line2D.Double(x0, y0, x1, y1);
		} else {
			// Line style: "state machine" from JsPlumb
			int segment = computeSegment(x0, y0, x1, y1);
			double midx = (x0 + x1) / 2.0;
			double midy = (y0 + y1) / 2.0;

			double cy = midy - curviness;
			double cx = midx + ((segment == 1 || segment == 3) ? -curviness : curviness);

			mainShape = new QuadCurve2D.Double(x0, y0, cx, cy, x1, y1);
		}

		/*
			// Line style: cubic bezier
			double delta = 30.0;
			return new CubicCurve2D.Double(
					x0, y0,
					x0 + delta, y0,
					x1 - delta, y1,
					x1, y1
			);*/
	}

	void paint(Graphics2D g) {
		g.setColor(Constants.colorPointer);
		g.draw(mainShape);
		int r = Constants.pointerSrcRadius;
		g.fillOval((int) (x0 - r), (int) (y0 - r), r * 2, r * 2);
	}
}
