package ln.afm.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
//import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.ejml.data.DenseMatrix64F;
import org.jfree.chart.axis.NumberAxis;
import ln.afm.gui.AfmDisplay;
import ln.afm.manager.Manager;
import ln.afm.model.CurveData;
import ln.afm.solver.Point2D;

/**
 * Data class that holds the user raw data and conditions
 * @author Lynette Naler
 *
 */
public class CurveData {
	//private static final Logger LOGGER = Logger.getLogger(CurveData.class.getName() ); //TODO
	
	private double sensFactor;
	private double sprConstant;
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
	private double exponent;
	private double R2 = 0.0;
	private double youngs;
	
	private boolean hasRun;
	private boolean smoothGraph;
	private int numPoints;
	private int numIterations;
	
	private List<Double> zdistValues = new ArrayList<Double>();
	private List<Double> voltValues = new ArrayList<Double>();
	
	private String[] units;
	
	ArrayList<WeightedObservedPoint> points;
	List<Point2D> dlPoints;
	List<Point2D> smoothedPoints;
	
	JFreeChart rawData;
	JFreeChart forceData;
	
	
	/*
	 * Columns are : 	0.Z(nm)		1.D(V)		2.D(nm)		3.Z-Z0(nm)		4.D-D0(nm)		5.D-D0(m) 
	 * 					6.del(nm)	7.del(m)	8.F(N)		9.ln(del)		10.ln(F)
	 */
	private DenseMatrix64F dataMatrix;
	private double sigma;
	private int smoothInt;
	
	/**
	 * Constructer for CurveData
	 * @param log Log that will be used to display information
	 */
	public CurveData(){
		points = new ArrayList<WeightedObservedPoint>();
		dlPoints = new ArrayList<Point2D>();
		smoothedPoints = new ArrayList<Point2D>();
		hasRun = false;
		smoothGraph = false;
		
		minX = 0;
		minY = 0;
		maxX = 0;
		maxY = 0;
	}
	
	/**
	 * Checks if values have been set. All values should be > 0.
	 * @return true if it is ready, false otherwise
	 */
	public boolean isReady(){
		if(sensFactor == 0)
		{
			System.out.println("Sensitivity Factor is not set");
			return false;
		}
		else if(sprConstant == 0)
		{
			System.out.println("Spring Constant is not set");
			return false;
		}
		else if(alpha == 0)
		{
			System.out.println("Alpha is not set");
			return false;
		}
		return true;
	}

	/**
	 * Sensitivity getter
	 * @return sensitivity factor
	 */
	public double getSensitivity(){
		return sensFactor;
	}
	/**
	 * Sensitivity setter
	 * @param newSensFactor new Sensitivity Factor
	 */
	public void setSensitivity(double newSensFactor){
		sensFactor = newSensFactor;
	}
	
	/**
	 * Spring Constant getter
	 * @return spring constant
	 */
	public double getSpringConstant(){
		return sprConstant;
	}
	/**
	 * Spring Constant setter
	 * @param newSprConstant new Spring Constant
	 */
	public void setSpringConstant(double newSprConstant){
		sprConstant = newSprConstant;
	}
	
	/**
	 * Alpha getter
	 * @return alpha angle (degrees)
	 */
	public double getAlpha(){
		return alpha;
	}
	/**
	 * Alpha setter
	 * @param newAlpha new alpha angle (degrees)
	 */
	public void setAlpha(double newAlpha){
		alpha = newAlpha;
		tanAlpha = Math.tan(Math.toRadians(newAlpha));
	}
	
	/**
	 * Impact Z getter
	 * @return the location of impact
	 */
	public double getImpactZ(){
		return impactZ;
	}
	/**
	 * Impact Z setter
	 * @param newImpactZ the new location of impact
	 */
	public void setImpactZ(double newImpactZ){
		impactZ = newImpactZ;
	}
	
	/**
	 * Checks if the indent is limited
	 * @return true if indent is limited
	 */
	public boolean isLimited()
	{
		return hasLimit;
	}
	/**
	 * Sets whether the indent is limited
	 * @param ifLimited true if indent is limited
	 */
	public void makeLimited(boolean ifLimited)
	{
		hasLimit = ifLimited;
	}
	
	/**
	 * Gets by what percent the indent was limited
	 * @return the limit percent
	 */
	public double getLimitPercent()
	{
		return limitPercent;
	}
	/**
	 * Set the percent the indent was limited
	 * @param pct the new limit percent
	 */
	public void setLimitPercent(double pct)
	{
		limitPercent = pct;
	}
	
	/**
	 * Gets the gel size
	 * @return the gel size
	 */
	public double getGelSize()
	{
		return gelSize;
	}
	/**
	 * Sets the gel size
	 * @param newGelSize the new gel size
	 */
	public void setGelSize(double newGelSize)
	{
		gelSize = newGelSize;
	}
	
	/**
	 * Gets Poissons Ratio
	 * @return poissons ratio
	 */
	public double getPoissonsRatio()
	{
		return poissonsRatio;
	}
	/**
	 * Sets poissons ratio
	 * @param new poissons ratio
	 */
	public void setPoissonsRatio(double newPoissonsRatio)
	{
		poissonsRatio = newPoissonsRatio;
	}

	
	/**
	 * Attempts to append a pair of values to the data
	 * @param value the Z (0) and Volt (1) values to be appended
	 * @return true if append is successful
	 */
	public boolean appendValues(double[] value)
	{
		if(value.length > 2)
		{
			//userLog.append("Too many arguments per row." + "\n");
			return false;
		}
		if(!(zdistValues.add(value[0]) && voltValues.add(value[1])))
		{
			//userLog.append("Failed to append data." + "\n");
			return false;
		}
		return true;
	}

	/**
	 * Gets the current units
	 * @return current units
	 */
	public String[] getUnits()
	{
		return units;
	}
	/**
	 * Sets the current units
	 * @param newUnits the new units
	 */
	public void setUnits(String[] newUnits)
	{
		units = newUnits;
	}

	/**
	 * Given conversion values, apply the conversion
	 * @param conversions conversion * current value = new value
	 */
	private void multiply(double[] conversions)
	{
		for(int i=0; i < zdistValues.size(); i++)
		{
			zdistValues.set(i, zdistValues.get(i)*conversions[0]);
			voltValues.set(i, voltValues.get(i)*conversions[1]);
		}
	}
	
	/**
	 * Change the data to reflect different units
	 * @param newUnits Units to be converted to
	 * @return true if successful or conversion not need
	 */
	public boolean changeUnits(String[] newUnits)
	{
		boolean zUnits = units[0].equals(newUnits[0]);
		boolean vUnits = units[1].equals(newUnits[1]);
		if(zUnits && vUnits)
		{
			//userLog.append("Units do not need conversion." + "\n");
			return true;
		}
		double[] conversions = Converter.getConversion(units, newUnits);
		multiply(conversions);
		units = newUnits;
		return true; //TODO An instance that returns false?
	}
	
	private XYSeries getMovingAvgPoints()
	{
		XYSeries smoothedData = new XYSeries("smoothed");
		double currentSum = 0;
		for(int i=0;i < (zdistValues.size()-((numPoints+1)/2));i++)
		{
			currentSum = currentSum + voltValues.get(i);
			if(i > (numPoints-1))
			{
				currentSum = currentSum - voltValues.get(i-numPoints);
			}
			if(i >= (numPoints-1))
			{
				smoothedData.add(Double.valueOf(zdistValues.get(i)), Double.valueOf(currentSum/numPoints));
				smoothedPoints.add(new Point2D(zdistValues.get(i).doubleValue(), currentSum/numPoints));
			}
		}
		return smoothedData;
	}
	
	private double gaussianDist(double x, double sigma, int center)
	{
		double partA = 1.0/Math.sqrt(2*Math.PI*sigma);
		double partB = Math.exp(-(Math.pow((x-center), 2))/(2*Math.pow(sigma, 2)));
		return partA*partB;
	}
	
	private double[] getGaussianKernel(int radius, double sigma)
	{
		double[] gaussianKernel = new double[2*radius+1];
		double gaussianSum = 0;
		for(int i=0;i < (2*radius+1);i++)
		{	
			//Performing Simpson's Integration to get discrete values
			gaussianKernel[i] = (1.0/6.0)*(gaussianDist(i-0.5, sigma, radius)+gaussianDist(i+0.5,sigma,radius)+4*(gaussianDist(i,sigma,radius)));
			gaussianSum = gaussianSum + gaussianKernel[i];
		}
		//Need to scale to 1.0
		for(int i=0;i < (2*radius+1);i++)
		{	
			//Performing Simpson's Integration to get discrete values
			gaussianKernel[i] = gaussianKernel[i]/gaussianSum;
			//System.out.println(i + ": " + gaussianKernel[i]);
		}
		return gaussianKernel;
	}
	
	private XYSeries getGaussianKernelPoints()
	{
		int radius = numPoints;
		//double sigma = numPoints/3.0;
		XYSeries smoothedData = new XYSeries("smoothed");
		double[] gaussianKernel = getGaussianKernel(radius, sigma);
		double currentSum = 0;
		for(int i=radius; i < (zdistValues.size()-radius-1);i++)
		{
			currentSum = 0;
			for(int j=0;j < gaussianKernel.length;j++)
			{
				//double temp = gaussianKernel[j]*voltValues.get(i-radius);
				currentSum = currentSum + gaussianKernel[j]*voltValues.get(i-radius+j);
			}
			smoothedData.add(Double.valueOf(zdistValues.get(i)), Double.valueOf(currentSum));
			smoothedPoints.add(new Point2D(zdistValues.get(i).doubleValue(), currentSum));
			//System.out.println("Old: x-" + zdistValues.get(i) + ", y-" + voltValues.get(i)
			//+ " New: y-" + currentSum);
		}
		return smoothedData;
	}
	
//	private XYSeries getSmoothedPoints()
//	{
//		//return getMovingAvgPoints();
//		return getGaussianKernelPoints();
//	}
	
	private XYSeries getSmoothedPoints()
	{
		if(smoothInt == Manager.GAUSSIAN)
		{
			return getGaussianKernelPoints();
		}
		return getMovingAvgPoints();
	}
	
	/**
	 * Creates a XYDataset of the data
	 * @return XYDataset of the data
	 */
	private XYDataset getXYData()
	{
		XYSeries data = new XYSeries("Raw");
		for(int i=0;i < zdistValues.size();i++)
		{
			data.add(zdistValues.get(i), voltValues.get(i));
		}
		final XYSeriesCollection dataset = new XYSeriesCollection();
		
		if(smoothInt != Manager.NO_SMOOTHING)
	    {
	    	dataset.addSeries(getSmoothedPoints());
	    }
	    dataset.addSeries(data);
	    
	    return dataset;
	}
	
	/**
	 * Generates a chart of the data
	 * @return Chart of the data
	 */
	public JFreeChart getRawData()
	{
		String xtitle = "Indentation" + " (" + units[0] + ")";
		String ytitle = "Deflection" + " (" + units[1] + ")";
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
		         "",
		         xtitle,
		         ytitle,
		         getXYData(),
		         PlotOrientation.VERTICAL ,
		         false, //include legend
		         true,
		         false);
		xylineChart.setBackgroundPaint(Color.WHITE);
		return xylineChart;
	}
	
	/**
	 * Prints out the data to the user log
	 */
	public void printData()
	{
		//userLog.append("Printing current data..." + "\n");
		//userLog.append(units[0] + "\t" + units[1] + "\n");
		for(int i=0; i < zdistValues.size(); i++)
		{
			//userLog.append(zdistValues.get(i) + "\t" + voltValues.get(i) + "\n");
		}
	}
	
	/**
	 * Returns a double[][] array of the data
	 * @return double[][] array of the data
	 */
	public double[][] toArray()
	{
		double[][] dataArray = new double[2][zdistValues.size()];
		for(int i=0;i<zdistValues.size();i++)
		{
			dataArray[0][i] = zdistValues.get(i);
			dataArray[1][i] = voltValues.get(i);
		}
		return dataArray;
	}

	
	/**
	 * Initializes a matrix with given data
	 * @param data to be put in the matrix
	 */
	public void initMatrix()
	{
		if(!smoothGraph)
		{
			//userLog.append("Initializing matrix..." + "\n");
			double[][] curveArray = toArray();
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
		if(smoothGraph)
		{
			int numCols = 11;
			int numRows = smoothedPoints.size();
			dataMatrix = new DenseMatrix64F(numRows, numCols);
			for(int j=0;j<numRows;j++)
			{
				Point2D current = smoothedPoints.get(j);
				dataMatrix.set(j, 0, current.x);
				dataMatrix.set(j, 1, current.y);
			}
		}
	}

	/**
	 * Returns the index of the value closest to that given in a given column
	 * @param value Value to be matched
	 * @param column Column to be searched
	 * @return Index of closest value, default 0
	 */
	public int getClosest(double value, int column) //TODO ERROR HANDLING
	{
		//userLog.append("Find closest value..." + "\n");
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
		//userLog.append("Closest value is: " + dataMatrix.get(closestIndex, column) +"\n");
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
		if(firstIndex < 0)
		{
			lastIndex = lastIndex - firstIndex;
			firstIndex = 0;
		}
		if(lastIndex > dataMatrix.numRows)
		{
			firstIndex = firstIndex - (lastIndex - dataMatrix.numRows);
			lastIndex = dataMatrix.numRows;
		}
		double currentTotal = 0;
		for(int i = firstIndex; i < lastIndex; i++)
		{
			currentTotal = currentTotal + dataMatrix.get(i, 2);
		}
		double avgD0 = currentTotal/4;
		//userLog.append("Average D0 value is: " + avgD0 + "\n");
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
			double temp = dataMatrix.get(i,5)*sprConstant;
			dataMatrix.set(i,8,temp);
			dataMatrix.set(i,10,Math.log(temp));
		}
	}
	
	/**
	 * Calculate our matrix
	 */
	public void calcMatrix()
	{
		calcDist();
		calcZDiffs();
		calcDDiffs();
		calcDelta();
		calcForce();
		//userLog.append("Matrix calculated" + "\n");
	}
	
	/**
	 * Return the index of the last NaN in a given column
	 * @param column Column of the matrix we want to search
	 * @return The index of the last NaN in the column
	 */
	private int getLastNaN(int column)
	{
//		int currentLast = -1;
//		for(int i=0; i < dataMatrix.numRows; i++)
//		{
//			if(Double.isNaN(dataMatrix.get(i, column)))
//			{
//				currentLast = i;
//			}
//			i++; <- here was the bug, it was originally a while loop and I didn't delete this
//		}
//		System.out.println("Last NaN at: " + currentLast);
//		return currentLast;
		
		int currentLast = -1;
		int i = dataMatrix.numRows-1;
		boolean lastFound = false;
		while(i >= 0 && !lastFound)
		{
			if(!Double.isFinite(dataMatrix.get(i, column)))
			{
				currentLast = i;
				lastFound = true;
			}
			i--;
		}
		//System.out.println("Last NaN at: " + currentLast);
		return currentLast;
	}
	
	/**
	 * Puts the data together and fits the matrix
	 * @return XYDataset of processed data and power trend line
	 */
	private XYDataset toWeightedMatrix()
	{
		points = new ArrayList<WeightedObservedPoint>();
		dlPoints = new ArrayList<Point2D>();
		int start = Math.max(getLastNaN(10), getLastNaN(9));
		start = Math.max(start, getClosest(impactZ,0));
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
        	tempYAvg = tempYAvg + Math.log(yval);
        	System.out.println("i: " + i + ", logx: " + Math.log(xval) + ", logy: " + Math.log(yval));
		}
		
		minX = data.getMinX();
		minY = data.getMinY();
		
		maxX = data.getMaxX();
		maxY = data.getMaxY();
		avgY = tempYAvg/(last-start-1); //We need this to calculate R
		
		double[] trends = Manager.fitMatrix(dlPoints, numIterations);
		slope = trends[0];
		exponent = trends[1];
		R2 = getRSquared();
		XYSeries powTrend = new XYSeries("power");
		for(int i=start+1;i < last;i++)//Generate our trend data
		{
			xval = dataMatrix.get(i,7);
			powTrend.add(xval, trends[0]*Math.pow(xval, trends[1]));
		}
		
		final XYSeriesCollection dataset = new XYSeriesCollection();          
	    dataset.addSeries(data);
	    dataset.addSeries(powTrend);
	    System.out.println(dataMatrix.toString());
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
	 * Generate the Force-Indentation Chart
	 * @return Force-Indentation Chart with Power trend line
	 */
	public void generateXYChart()
	{
		String xtitle = "Indentation" + " (m)";
		String ytitle = "Force" + " (N)";
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
		         "",
		         xtitle,
		         ytitle,
		         toWeightedMatrix(), //this is our data
		         PlotOrientation.VERTICAL ,
		         true, //include legend
		         true,
		         false);
		NumberAxis domain = (NumberAxis) xylineChart.getXYPlot().getDomainAxis();
        domain.setRange(minX, maxX);
        domain.setVerticalTickLabels(true);
        NumberAxis range = (NumberAxis) xylineChart.getXYPlot().getRangeAxis();
        range.setRange(minY, maxY);
		forceData = xylineChart;
		hasRun = true;
	}
	
	/**
	 * Returns the force chart
	 * @return the force chart of the previous run
	 */
	public JFreeChart getXYChart()
	{
		return forceData;
	}
	
	/**
	 * Returns the calculated results to the caller
	 * @return {slope, exponent, r-squared, young's}
	 */
	public double[] getResults()
	{
		double[] results = {slope, exponent, R2, calcYoungs(slope)};
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
//		ArrayList<WeightedObservedPoint> xpoints = new ArrayList<>();
//		double tempAvg = 0;
//		for(int i=1;i < 21; i++)
//		{
//			xpoints.add(new WeightedObservedPoint(1, i, 15*i*i));
//			tempAvg = tempAvg + Math.log(15*i*i);
//		}
		//tempAvg = tempAvg/20;
		//double tempSlope = 14.5;
		double predY;
		double SSE = 0;
		double SSTO = 0;
		for(int i=0;i < points.size(); i++)
		{
			WeightedObservedPoint point = points.get(i);
			predY = Math.log(slope) + exponent*Math.log(point.getX());
			SSE = SSE + Math.pow((Math.log(point.getY()) - predY), 2);
			SSTO = SSTO + Math.pow((Math.log(point.getY()) - avgY), 2);
//			System.out.println("Spacer");
//			WeightedObservedPoint point = xpoints.get(i);
//			predY = Math.log(tempSlope) + exponent*Math.log(point.getX());
//			SSE = SSE + Math.pow((Math.log(point.getY()) - predY), 2);
//			SSTO = SSTO + Math.pow((Math.log(point.getY()) - tempAvg), 2);
		}
		System.out.println("SSE: " + SSE + ", SSTO: " + SSTO);
		double result = (1.0-(SSE/SSTO));
		if(result < 0)
		{
			result = 0;
		}
		return result;
	}

	/**
	 * Gets the exponent of the fitted curve (pretty much always 2)
	 * @return the exponent of the fitted curve
	 */
	public double getExponent() {
		return exponent;
	}
	/**
	 * Sets the exponent of the fitted curve found
	 * @param exponent the new exponent of the fitted curve
	 */
	public void setExponent(double exponent) {
		this.exponent = exponent;
	}
	
	/**
	 * Gets the slope of the fitted curve
	 * @return the slope of the fitted curve
	 */
	public double getSlope() {
		return slope;
	}
	/**
	 * Sets the slope of the fitted curve
	 * @param slope the new slope
	 */
	public void setSlope(double slope) {
		this.slope = slope;
	}
	
	/**
	 * returns the data in DogLeg format
	 * @return a list of dogleg points
	 */
	public List<Point2D> getDogLegPoints()
	{
		return dlPoints;
	}
	
//	public ArrayList<WeightedObservedPoint> getApachePoints()
//	{
//		return points;
//	}
	
	/**
	 * Checks if the data has been run
	 * @return true if the data has been run
	 */
	public boolean hasRun()
	{
		return hasRun;
	}
	
	/**
	 * Gets the list of strings to export
	 * @param fileName name of the file
	 * @return the list of strings to save
	 */
	public List<String> print(String fileName)
	{
		List<String> output = new ArrayList<String>();
		output.add("File: " + fileName);
		
		if(hasRun)
		{
			output.add(String.format("Impact Z: %.2f nm, Gel Size: %.1f nm, Limited: %b, %.2f pct",
					impactZ, gelSize, hasLimit, limitPercent));
			output.add(String.format("Sensitivity: %.2f nm/V, Spring Constant: %.3f N/m", sensFactor, sprConstant));
			output.add(String.format("Poisson: %.3f, Alpha: %.2f deg", poissonsRatio, alpha));
			output.add(String.format("Slope: %.1f, Exponent: %.3f, R2: %.3f", slope, exponent, R2));
			output.add(String.format("Young's Modulus: %.3f kPa", youngs));
		}
		else
		{
			output.add("Data has not been analyzed.");
		}
		output.add("===========================================");
		return output;
	}

	/**
	 * Gets the number of iterations
	 * @return the number of iterations
	 */
	public int getNumIterations() {
		return numIterations;
	}
	/**
	 * Sets the number of iterations
	 * @param numIterations the new number of iterations
	 */
	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}

	public void setSmoothFit(boolean smoothFit) {
		this.smoothGraph = smoothFit;
	}

	public void setNumPoints(int movingAveragePoints) {
		this.numPoints = movingAveragePoints;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
		
	}

	public void setSmoothInt(int smoothFit) {
		// TODO Auto-generated method stub
		this.smoothInt = smoothFit;
	}
}
