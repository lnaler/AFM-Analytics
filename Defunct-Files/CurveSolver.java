package ln.afm.gui;

import java.util.Collection;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

//http://stackoverflow.com/questions/11335127/how-to-use-java-math-commons-curvefitter
class PowerFunction implements ParametricUnivariateFunction {
    public double value(double x, double... parameters) {
    	final double a = parameters[0];
        final double b = parameters[1];
        
        //return a*Math.pow(x, b);
    	//return a*Math.pow(x, 2);
    	return (Math.log(a) + b*Math.log(x));
    }

    // Jacobian matrix of the above. In this case, this is just an array of
    // partial derivatives of the above function, with one element for each parameter.
    public double[] gradient(double x, double... parameters) {
        final double a = parameters[0];
        final double b = parameters[1];
        
        return new double[] {
        	//Math.pow(x, 2)
        	//Math.pow(x, b),
            //Math.log(x)*a*Math.pow(x, b)
        	1/a,
        	Math.log(x)
        };
    }
}

public class CurveSolver extends AbstractCurveFitter {
	private final int MAX_ITER = 1000;
	
    protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
        final int len = points.size();
        final double[] target  = new double[len];
        final double[] weights = new double[len];
        //final double[] initialGuess = {1.0};
        final double[] initialGuess = {1.0, 1.0};

        int i = 0;
        for(WeightedObservedPoint point : points) {
            target[i]  = Math.log(point.getY());
            weights[i] = point.getWeight();
            i += 1;
        }

        final AbstractCurveFitter.TheoreticalValuesFunction model = new
            AbstractCurveFitter.TheoreticalValuesFunction(new PowerFunction(), points);

        return new LeastSquaresBuilder().
//            maxEvaluations(Integer.MAX_VALUE).
//            maxIterations(Integer.MAX_VALUE).
    		maxEvaluations(MAX_ITER).
            maxIterations(MAX_ITER).
            start(initialGuess).
            target(target).
            weight(new DiagonalMatrix(weights)).
            model(model.getModelFunction(), model.getModelFunctionJacobian()).
            build();
    }

//    public static void main(String[] args) {
//        CurveSolver fitter = new CurveSolver();
//        ArrayList<WeightedObservedPoint> points = new ArrayList<WeightedObservedPoint>();
//
//        // Add points here; for instance,
//        for(int i=1;i < 25;i++)
//        {
//        	WeightedObservedPoint point = new WeightedObservedPoint(1.0, i, 1.731*i*i);
//        	points.add(point);
//        }
////        WeightedObservedPoint point = new WeightedObservedPoint(1.0,
////            1.0,
////            1.0);
////        points.add(point);
//
//        final double coeffs[] = fitter.fit(points);
//        System.out.println(Arrays.toString(coeffs));
//    }
}