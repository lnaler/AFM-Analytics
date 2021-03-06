/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * NOTICE: Changes in the file are limited to changing package location. Original
 * package location is org.ddogleg.example.
 */

package ln.afm.solver;

import org.ddogleg.fitting.modelset.ModelGenerator;

import java.util.List;

/**
 * Estimate the parameters of a line using two points
 *
 * @author Peter Abeles
 */
public class LineGenerator implements ModelGenerator<Line2D,Point2D> {

	// a point at the origin (0,0)
	Point2D origin = new Point2D();

	@Override
	public boolean generate(List<Point2D> dataSet, Line2D output) {
		Point2D p1 = dataSet.get(0);
		Point2D p2 = dataSet.get(1);

		// create parametric line equation
		double slopeX = p2.x - p1.x;
		double slopeY = p2.y - p1.y;

		// find the closet point to the origin
		// find the closest point on the line to the point
		double t = slopeX * ( origin.x - p1.x) + slopeY * ( origin.y - p1.y);
		t /= slopeX * slopeX + slopeY * slopeY;

		output.x = p1.x + t*slopeX;
		output.y = p1.y + t*slopeY;

		return true;
	}

	@Override
	public int getMinimumPoints() {
		return 2;
	}
}
