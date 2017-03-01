package ln.afm.solver;

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
 */


import org.ddogleg.fitting.modelset.DistanceFromModel;
import org.ddogleg.fitting.modelset.ModelGenerator;
import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.fitting.modelset.ransac.Ransac;
import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.UtilOptimize;

import ln.afm.gui.AfmDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Simple example demonstrating how to robustly fit a line to a noisy set of points.
 *
 * @author Peter Abeles
 */
public class RobustFit {
	
	public double getFit(List<Point2D> points) {
		
		//------------------------ Compute the solution
		// Let it know how to compute the model and fit errors
		ModelManager<Line2D> manager = new LineManager();
		ModelGenerator<Line2D,Point2D> generator = new LineGenerator();
		DistanceFromModel<Line2D,Point2D> distance = new DistanceFromLine();

		// RANSAC or LMedS work well here
		ModelMatcher<Line2D,Point2D> alg =
				new Ransac<Line2D,Point2D>(234234,manager,generator,distance,500,0.01);
//		ModelMatcher<Line2D,Point2D> alg =
//				new LeastMedianOfSquares<Line2D, Point2D>(234234,100,0.1,0.5,generator,distance);

		if( !alg.process(points) )
			throw new RuntimeException("Robust fit failed!");

		// let's look at the results
		Line2D found = alg.getModelParameters();

		// notice how all the noisy points were removed and an accurate line was estimated?
		System.out.println("Found line   "+found);
		System.out.println("Match set size = "+alg.getMatchSet().size());
		
		return 0.0;
	}
}