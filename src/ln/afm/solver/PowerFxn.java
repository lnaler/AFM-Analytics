package ln.afm.solver;

import java.util.List;
import org.ddogleg.optimization.functions.FunctionNtoM;

/**
 * Power Function of the form y=ax^2 linearized to ln(y)=ln(a)+2*ln(x)
 * @author Lynette Naler
 *
 */
public class PowerFxn implements FunctionNtoM{
	List<Point2D> data;
	double avgY;
	
	/**
	 * Constructs a PowerFxn with the given data. Assumes y and x values are already in log form.
	 * @param inputData Data the PowerFxn will be fit to
	 */
	public PowerFxn(List<Point2D> inputData){
		data = inputData;
		avgY = getAverageY(); //need this to calculate residuals
	}
	
	/**
	 * Calculates the average Y value of the data
	 * @return The average Y value
	 */
	public double getAverageY()
	{
		double sum = 0;
		for(int i = 0;i < data.size(); i++)
		{
			Point2D point = data.get(i);
			sum = sum + point.y;
		}
		return (sum/data.size());
	}
	
	/**
	 * Calculates the function given the equation parameters. b is not currently used.
	 * @param a Slope of the power function
	 * @param b Not currently used. Set to 2.
	 * @param x X value
	 * @return
	 */
	public double func(double a, double b, double x)
	{
		double result = Math.log(a) + 2*x;
		return result;
	}
	
	@Override
	public void process(double[] input, double[] output)
	{
		double a = input[0]; //Our test parameter
		
		//Now we need to calculate and return the residuals at each point x
		for(int i = 0; i < data.size(); i++ ) {
			Point2D p = data.get(i);
			double y_pred = func(a, 0, p.x);
			output[i] = p.y-y_pred;
		}
	}

	@Override
	public int getNumOfInputsN() {
		return 1;
	}

	@Override
	public int getNumOfOutputsM() {
		return data.size();
	}
}
