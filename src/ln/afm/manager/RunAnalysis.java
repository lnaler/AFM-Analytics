package ln.afm.manager;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.ejml.data.DenseMatrix64F;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ln.afm.gui.AfmDisplay;
import ln.afm.model.CurveData;
import ln.afm.solver.DoglegLS;
import ln.afm.solver.DoglegLSLM;
import ln.afm.solver.Point2D;
import ln.afm.solver.RobustFit;

/**
 * Processes and fits AFM Data with a Power curve
 * @author Lynette Naler
 *
 */
public class RunAnalysis {
	
	private JTextArea userLog;
	private double sensFactor;
	private double sprConst;
	private double alpha;
	private double impactZ;
	
	private boolean hasLimit;
	private double limitPercent;
	private double gelSize;
	
	private double poissonsRatio;
	private double tanAlpha;
	
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;
	private double avgY;
	
	private double slope;
	private double expo;
	private double R2 = 0.0;
	private double youngs;
	
	ArrayList<WeightedObservedPoint> points;
	List<Point2D> dlPoints;
	
	
	/*
	 * Columns are : 	0.Z(nm)		1.D(V)		2.D(nm)		3.Z-Z0(nm)		4.D-D0(nm)		5.D-D0(m) 
	 * 					6.del(nm)	7.del(m)	8.F(N)		9.ln(del)		10.ln(F)
	 */
	private DenseMatrix64F dataMatrix;

	public RunAnalysis(CurveData data, JTextArea log, double[] inputs, boolean limitZ) { //TODO ERROR HANDLING
		userLog = log;
		sensFactor = inputs[0];
		sprConst = inputs[1];
		alpha = inputs[2]; //DEG
		impactZ = inputs[3];
		limitPercent = inputs[4];
		gelSize = inputs[5];
		
		hasLimit = limitZ;
		
		poissonsRatio = 0.25;
		tanAlpha = Math.tan(Math.toRadians(alpha));
		userLog.append("tanAlpha is: " + tanAlpha +"\n");
		points = new ArrayList<WeightedObservedPoint>();
		dlPoints = new ArrayList<Point2D>();
		
		log.append("Running: " + sensFactor + " " + sprConst + " " + alpha + " " + impactZ + " " + limitPercent + " " + gelSize + " " + hasLimit + "\n");
		initMatrix(data);
		
		minX = 0;
		minY = 0;
		maxX = 0;
		maxY = 0;
	}
	
	/**
	 * Initializes a matrix with given data
	 * @param data to be put in the matrix
	 */
	private void initMatrix(CurveData data)
	{
		userLog.append("Initializing matrix..." + "\n");
		double[][] curveArray = data.toArray();
		int numCols = 11;
		int numRows = curveArray[0].length;
		dataMatrix = new DenseMatrix64F(numRows, numCols);
		for(int i=0;i<2;i++)
		{
			for(int j=0;j<numRows;j++)
			{
				dataMatrix.set(j, i, curveArray[i][j]);
			}
		}
	}

	/**
	 * Returns the index of the value closest to that given in a given column
	 * @param value Value to be matched
	 * @param column Column to be searched
	 * @return Index of closest value, default 0
	 */
	private int getClosest(double value, int column) //TODO ERROR HANDLING
	{
		userLog.append("Find closest value..." + "\n");
		int closestIndex = 0;
		double closestDistance = value - dataMatrix.get(0, column);
		double tempDist = 0;
		for(int i=0;i < dataMatrix.numRows;i++)
		{
			tempDist = value - dataMatrix.get(i, column);
			tempDist = Math.abs(tempDist);
			if(tempDist < closestDistance)
			{
				closestDistance = tempDist;
				closestIndex = i;
			}
		}
		userLog.append("Closest value is: " + dataMatrix.get(closestIndex, column) +"\n");
		return closestIndex;
	}
	
	/**
	 * Calculate the average D value of the four values preceding the impactZ value (inclusive)
	 * @return Average D0 value
	 */
	private double getAvgD0()
	{
		int lastIndex = getClosest(impactZ, 0) + 1;
		int firstIndex = lastIndex - 4;
		double currentTotal = 0;
		for(int i = firstIndex; i < lastIndex; i++)
		{
			currentTotal = currentTotal + dataMatrix.get(i, 2);
		}
		double avgD0 = currentTotal/4;
		userLog.append("Average D0 value is: " + avgD0 + "\n");
		return avgD0;
	}
	
	/**
	 * Calculates the distance
	 */
	private void calcDist()
	{
		for(int i = 0;i < dataMatrix.numRows;i++)
		{
			double temp = dataMatrix.get(i, 1)*sensFactor;
			dataMatrix.set(i,2,temp);
		}
	}
	
	/**
	 * Calculates the z differences
	 */
	private void calcZDiffs()
	{
		for(int i = 0;i < dataMatrix.numRows;i++)
		{
			double temp = dataMatrix.get(i, 0) - impactZ;
			dataMatrix.set(i,3,temp);
		}
	}
	
	/**
	 * Calculates the d differences
	 */
	private void calcDDiffs()
	{
		double avgD0 = getAvgD0();
		for(int i = 0;i < dataMatrix.numRows;i++)
		{
			double temp = dataMatrix.get(i, 2) - avgD0;
			dataMatrix.set(i,4,temp);
			dataMatrix.set(i,5,(temp/1000000000));
		}
		
	}
	
	/**
	 * Calculates the delta value
	 */
	private void calcDelta()
	{
		for(int i = 0;i < dataMatrix.numRows; i++)
		{
			double temp = dataMatrix.get(i, 3) - dataMatrix.get(i,4);
			dataMatrix.set(i,6,temp);
			dataMatrix.set(i,7,temp/1000000000); //Off the cuff conversion
			dataMatrix.set(i,9,Math.log(temp/1000000000));
		}
	}
	
	/**
	 * Calculates the force
	 */
	private void calcForce()
	{
		for(int i = 0;i < dataMatrix.numRows; i++)
		{
			double temp = dataMatrix.get(i,5)*sprConst;
			dataMatrix.set(i,8,temp);
			dataMatrix.set(i,10,Math.log(temp));
		}
	}
	
	/**
	 * Calculate our matrix
	 */
	private void calcMatrix()
	{
		calcDist();
		calcZDiffs();
		calcDDiffs();
		calcDelta();
		calcForce();
		userLog.append("Matrix calculated" + "\n");
	}
	
	/**
	 * Return the index of the last NaN in a given column
	 * @param column Column of the matrix we want to search
	 * @return The index of the last NaN in the column
	 */
	private int getLastNaN(int column)
	{
		int currentLast = -1;
		for(int i=0; i < dataMatrix.numRows; i++)
		{
			if(Double.isNaN(dataMatrix.get(i, column)))
			{
				currentLast = i;
			}
			i++;
		}
		return currentLast;
	}
	
	/**
	 * Puts the data together and fits the matrix
	 * @return XYDataset of processed data and power trend line
	 */
	private XYDataset toWeightedMatrix(boolean goodFit)
	{
		int start = getLastNaN(10);
		int last = getLimit();
		if(last <= start) //If the last NaN occurs after the data limit
		{
			last = dataMatrix.numRows;
			AfmDisplay.infoBox("Gel size error. Reverting to complete usage.", "ERROR");
		}
		if(start == (dataMatrix.numRows - 1))
		{
			AfmDisplay.infoBox("All ln(F) are NaN", "ERROR");
			return new XYSeriesCollection();
		}
		double weight = 1;
		double xval = 0;
		double yval = 0;
		double tempYAvg = 0;
		XYSeries data = new XYSeries("raw");
		for(int i=start+1;i<last;i++)
		{
			xval = dataMatrix.get(i, 7);
			yval = dataMatrix.get(i, 8);
			WeightedObservedPoint point = new WeightedObservedPoint(weight, xval, yval); //TODO consolidate
        	points.add(point);
        	dlPoints.add(new Point2D(Math.log(xval), Math.log(yval)));
        	data.add(xval, yval);
        	tempYAvg = tempYAvg + yval;
		}
		
		minX = data.getMinX();
		minY = data.getMinY();
		
		maxX = data.getMaxX();
		maxY = data.getMaxY();
		avgY = tempYAvg/(last-start); //We need this to calculate R
		
		double[] trends = fitMatrix(goodFit);
		XYSeries powTrend = new XYSeries("power");
		for(int i=start+1;i < last;i++)//Generate our trend data
		{
			xval = dataMatrix.get(i,7);
			powTrend.add(xval, trends[0]*Math.pow(xval, trends[1]));
		}
		
		final XYSeriesCollection dataset = new XYSeriesCollection();          
	    dataset.addSeries(data);
	    dataset.addSeries(powTrend);
	    return dataset;
	}
	
	/**
	 * Calculate's the Young's Modulus from the given slope
	 * @param inSlope Slope of the processed data from fitter
	 * @return Young's Modulus (kPA)
	 */
	private double calcYoungs(double inSlope)
	{
		youngs = ((1-Math.pow(poissonsRatio, 2))*inSlope*Math.PI)/(1000*2*tanAlpha);
		return youngs;
	}
	
	/**
	 * Create our solver and fit it to our data points
	 * @return {Slope, 2 (exponent), 0, 0}
	 */
	private double[] fitMatrix(boolean goodFit)
	{
		double coeffs[] = {0, 2.0}; //right now we're forcing it to ax^2
		if(goodFit)
		{	
			DoglegLSLM dlSolver = new DoglegLSLM();
			coeffs[0] = dlSolver.getFit(dlPoints);
	        slope = coeffs[0];
	        expo = coeffs[1];
		}
		if(!goodFit)
		{
			AfmDisplay.infoBox("Force fit is not yet implemented. Pardon the placeholder", "Error");
//			DoglegLS dSolver = new DoglegLS();
//			RobustFit rFitter = new RobustFit();
//			coeffs[0] = rFitter.getFit(dlPoints);
//			//coeffs[0] = dSolver.getFit(dlPoints);
//			slope = coeffs[0];
//			expo = coeffs[1];
		}
        userLog.append("Curve Slope: " + coeffs[0] + " Exp: " + coeffs[1] + "\n");
        double[] results = {coeffs[0], coeffs[1], 0, 0}; //TODO update to remove old linear vars
        return results;
	}
	
	/**
	 * Generate the Force-Indentation Chart
	 * @return Force-Indentation Chart with Power trend line
	 */
	public JFreeChart getXYChart(boolean goodFit)
	{
		String xtitle = "Indentation" + " (m)";
		String ytitle = "Force" + " (N)";
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
		         "",
		         xtitle,
		         ytitle,
		         toWeightedMatrix(goodFit), //this is our data
		         PlotOrientation.VERTICAL ,
		         true, //include legend
		         true,
		         false);
		NumberAxis domain = (NumberAxis) xylineChart.getXYPlot().getDomainAxis();
        domain.setRange(minX, maxX);
        domain.setVerticalTickLabels(true);
        NumberAxis range = (NumberAxis) xylineChart.getXYPlot().getRangeAxis();
        range.setRange(minY, maxY);
		return xylineChart;
	}

	/**
	 * Runs the analyst and returns the processed data chart with trend lines
	 * @return Force-Indentation chart with Power trend line
	 */
	public JFreeChart run(boolean goodFit)
	{
		userLog.append("Running..." + "\n");
		calcMatrix(); // perform calculations
		JFreeChart forceIndentation = getXYChart(goodFit); //get chart
		return forceIndentation;
	}
	
	/**
	 * Returns the calculated results to the caller
	 * @return {slope, exponent, r-squared, young's}
	 */
	public double[] getResults()
	{
		R2 = getRSquared();
		double[] results = {slope, expo, R2, calcYoungs(slope)};
		return results;
	}
	
	/**
	 * Checks if the data is limited and returns an appropriate end index accordingly
	 * @return the index of the ending value + 1 or number or rows if limit exceeds matrix length
	 */
	private int getLimit()
	{
		if(hasLimit)
		{
			double maxZ = impactZ + (limitPercent/100)*gelSize;
			return getClosest(maxZ,0) + 1;
		}
		return dataMatrix.numRows;
	}
	
	/**
	 * Calculates R-Squared, a goodness of fit measure
	 * @return R-Squared number
	 */
	private double getRSquared()
	{
		double predY;
		double SSE = 0;
		double SSTO = 0;
		for(WeightedObservedPoint point:points)
		{
			predY = slope*Math.pow(point.getX(), expo);
			SSE = SSE + Math.pow((point.getY() - predY), 2);
			SSTO = SSTO + Math.pow((point.getY() - avgY), 2);
		}
		double result = (1.0-(SSE/SSTO));
		return result;
	}
}
