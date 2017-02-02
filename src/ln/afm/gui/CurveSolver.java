package ln.afm.gui;

import java.util.*;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.linear.DiagonalMatrix;

class PowerFunction implements ParametricUnivariateFunction {
    public double value(double x, double... parameters) {
        return parameters[0]*Math.pow(x, 2);
    }

    // Jacobian matrix of the above. In this case, this is just an array of
    // partial derivatives of the above function, with one element for each parameter.
    public double[] gradient(double x, double... parameters) {
        final double a = parameters[0];

        return new double[] {
            2.0*a*x
        };
    }
}

public class CurveSolver extends AbstractCurveFitter {
    protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
        final int len = points.size();
        final double[] target  = new double[len];
        final double[] weights = new double[len];
        final double[] initialGuess = {1.0};

        int i = 0;
        for(WeightedObservedPoint point : points) {
            target[i]  = point.getY();
            weights[i] = point.getWeight();
            i += 1;
        }

        final AbstractCurveFitter.TheoreticalValuesFunction model = new
            AbstractCurveFitter.TheoreticalValuesFunction(new PowerFunction(), points);

        return new LeastSquaresBuilder().
            maxEvaluations(Integer.MAX_VALUE).
            maxIterations(Integer.MAX_VALUE).
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
//        	WeightedObservedPoint point = new WeightedObservedPoint(1.0,Math.log(i),(2*Math.log(i)+Math.log(2)));
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