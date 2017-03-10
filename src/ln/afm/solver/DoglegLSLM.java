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
 * NOTICE: This file was adapted from 
 * https://github.com/lessthanoptimal/ddogleg/blob/v0.7/examples/src/org/ddogleg/example/ExampleMinimization.java
 */


package ln.afm.solver;

import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.UtilOptimize;

import ln.afm.gui.AfmDisplay;

import java.util.List;

public class DoglegLSLM {
	
	//double progress = 0;
	
	public double getFit(List<Point2D> points, int iterations) {
		// Define the function being optimized and create the optimizer
		UnconstrainedLeastSquares optimizer = FactoryOptimization.leastSquaresLM(1e-3, true);
		PowerFxn fxn = new PowerFxn(points);
		
		// if no jacobian is specified it will be computed numerically
		optimizer.setFunction(fxn,null);

		// provide it an extremely crude initial estimate of the line equation
		optimizer.initialize(new double[]{0.5},1e-12,1e-12);

		// iterate 500 times or until it converges.
		// Manually iteration is possible too if more control over is required
		UtilOptimize.process(optimizer, iterations); //TODO give access to this
		System.out.println("Num iterations: " + iterations);
		double found[] = optimizer.getParameters();

		if(Double.isNaN(optimizer.getFunctionValue()))
		{
			AfmDisplay.infoBox("Failed to converge. Please try a different input", "ERROR");
		}
		return found[0];
	}
	
	
	
//	public class dlListener implements ActionListener {
//
//		@Override
//		public void actionPerformed(ActionEvent arg0) {
//			Manager.update
//			
//		}
//	}
}
