package com.aegamesi.java_visualizer.ui;

import java.awt.geom.QuadCurve2D;

class PointerShape extends QuadCurve2D.Double {
	// Same logic as JsPlumb StateMachine connector
	private static final double curviness = 10.0;

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
	PointerShape(double x0, double y0, double x1, double y1) {
		int segment = computeSegment(x0, y0, x1, y1);
		double midx = (x0 + x1) / 2.0;
		double midy = (y0 + y1) / 2.0;

		double cy = midy - curviness;
		double cx = midx + ((segment == 1 || segment == 3) ? -curviness : curviness);

		setCurve(x0, y0, cx, cy, x1, y1);
	}
}
