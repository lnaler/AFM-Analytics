package ln.afm.gui;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.ejml.data.DenseMatrix32F;
import org.ejml.data.DenseMatrix64F;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class RunAnalysis {
	
	private CurveData userData;
	private JTextArea userLog;
	private double sensFactor;
	private double sprConst;
	private double alpha;
	private double impactZ;
	
	private double poissonsRatio;
	private double tanAlpha;
	
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;
	
	private double slope;
	private double expo;
	private double R2 = 0.0;
	private double youngs;
	
	private SimpleRegression linReg;
	
	ArrayList<WeightedObservedPoint> points;
	
	
	/**
	 * Columns are : 	0.Z(nm)		1.D(V)		2.D(nm)		3.Z-Z0(nm)		4.D-D0(nm)		5.D-D0(m) 
	 * 					6.del(nm)	7.del(m)	8.F(N)		9.ln(del)		10.ln(F)
	 */
	private DenseMatrix64F dataMatrix;

	public RunAnalysis(CurveData data, JTextArea log, double[] inputs) { //TODO ERROR HANDLING
		userData = data;
		userLog = log;
		sensFactor = inputs[0];
		sprConst = inputs[1];
		alpha = inputs[2]; //DEG
		impactZ = inputs[3];
		
		poissonsRatio = 0.25;
		tanAlpha = Math.tan(Math.toRadians(alpha));
		userLog.append("tanAlpha is: " + tanAlpha +"\n");
		points = new ArrayList<WeightedObservedPoint>();
		
		log.append("Running: " + sensFactor + " " + sprConst + " " + alpha + " " + impactZ + "\n");
		initMatrix(data);
		
		linReg = new SimpleRegression();
		minX = 0;
		minY = 0;
		maxX = 0;
		maxY = 0;
	}
	
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
	
	//returns index of value closest to that given
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
	
	private void calcDist()
	{
		for(int i = 0;i < dataMatrix.numRows;i++)
		{
			double temp = dataMatrix.get(i, 1)*sensFactor;
			dataMatrix.set(i,2,temp);
		}
	}
	
	private void calcZDiffs()
	{
		for(int i = 0;i < dataMatrix.numRows;i++)
		{
			double temp = dataMatrix.get(i, 0) - impactZ;
			dataMatrix.set(i,3,temp);
		}
	}
	
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
	
	private void calcDelta()
	{
		for(int i = 0;i < dataMatrix.numRows; i++)
		{
			double temp = dataMatrix.get(i, 3) - dataMatrix.get(i,4);
			dataMatrix.set(i,6,temp);
			dataMatrix.set(i,7,temp/1000000000);
			dataMatrix.set(i,9,Math.log(temp/1000000000));
		}
	}
	
	private void calcForce()
	{
		for(int i = 0;i < dataMatrix.numRows; i++)
		{
			double temp = dataMatrix.get(i,5)*sprConst;
			dataMatrix.set(i,8,temp);
			dataMatrix.set(i,10,Math.log(temp));
		}
	}
	
	private void calcMatrix()
	{
		calcDist();
		calcZDiffs();
		calcDDiffs();
		calcDelta();
		calcForce();
		userLog.append("Matrix calculated" + "\n");
		System.out.println(dataMatrix.toString());
	}
	
	private int getLastNaN(int column) //Finds first number that is not NaN
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
	
	private XYDataset toWeightedMatrix()
	{
		int start = getLastNaN(10);
		if(start == (dataMatrix.numRows - 1))
		{
			AfmDisplay.infoBox("All ln(F) are NaN", "ERROR");
			return new XYSeriesCollection();
		}
		double weight = 1;
		double xval = 0;
		double yval = 0;
		XYSeries data = new XYSeries("raw");
		for(int i=start+1;i<dataMatrix.numRows;i++)
		{
			xval = dataMatrix.get(i, 7);
			yval = dataMatrix.get(i, 8);
			WeightedObservedPoint point = new WeightedObservedPoint(weight, xval, yval);
        	points.add(point);
        	data.add(xval, yval);
        	linReg.addData(Math.log(xval), Math.log(yval));
		}
		
		minX = data.getMinX();
		minY = data.getMinY();
		
		maxX = data.getMaxX();
		maxY = data.getMaxY();
		
		double[] trends = fitMatrix();
		XYSeries powTrend = new XYSeries("power");
		XYSeries linTrend = new XYSeries("lin");
		for(int i=start+1;i < dataMatrix.numRows;i++)
		{
			xval = dataMatrix.get(i,7);
			powTrend.add(xval, trends[0]*Math.pow(xval, trends[1]));
			linTrend.add(xval, Math.exp(trends[3])*Math.pow(xval, trends[2]));
		}
		
		final XYSeriesCollection dataset = new XYSeriesCollection();          
	    dataset.addSeries(data);
	    dataset.addSeries(powTrend);
	    dataset.addSeries(linTrend);
	    return dataset;
	}
	
	private double calcYoungs(double inSlope)
	{
		double youngs = ((1-Math.pow(poissonsRatio, 2))*inSlope*Math.PI)/(1000*2*tanAlpha);
		return youngs;
	}
	
	private double[] fitMatrix()
	{
		CurveSolver fitter = new CurveSolver();
		final double coeffs[] = fitter.fit(points);
		ArrayList<WeightedObservedPoint> test_points = new ArrayList<WeightedObservedPoint>();
        for(int i=1;i < 25;i++)
        {
        	WeightedObservedPoint point = new WeightedObservedPoint(1.0, i, 1.731*i*i);
        	test_points.add(point);
        }
        final double testCoeffs[] = fitter.fit(test_points);
        slope = coeffs[0];
        expo = coeffs[1];
        userLog.append("Curve Slope: " + coeffs[0] + " Exp: " + coeffs[1] + "\n");
        userLog.append("LinReg Slope: " + linReg.getSlope() + " Int " + linReg.getIntercept() + " R: " + linReg.getRSquare() + "\n");
        double[] results = {coeffs[0], coeffs[1], linReg.getSlope(), linReg.getIntercept()};
        return results;
	}
	
	public JFreeChart getXYChart()
	{
		String xtitle = "Indentation" + " (m)";
		String ytitle = "Force" + " (N)";
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
		         "",
		         xtitle,
		         ytitle,
		         toWeightedMatrix(),
		         PlotOrientation.VERTICAL ,
		         true, //include legend
		         true,
		         false);
		
		double xStep = (maxX - minX)/10;
		double yStep = (maxY - minY)/10;
		NumberAxis domain = (NumberAxis) xylineChart.getXYPlot().getDomainAxis();
        domain.setRange(minX, maxX);
        //domain.setTickUnit(new NumberTickUnit(0.1));
        domain.setVerticalTickLabels(true);
        NumberAxis range = (NumberAxis) xylineChart.getXYPlot().getRangeAxis();
        range.setRange(minY, maxY);
        //range.setTickUnit(new NumberTickUnit(0.1));
		
		return xylineChart;
	}

	public JFreeChart run()
	{
		userLog.append("Running..." + "\n");
		calcMatrix();
		JFreeChart forceIndentation = getXYChart();
		//fitMatrix();
		return forceIndentation;
		//fitMatrix();
	}
	
	public double[] getResults()
	{
		double[] results = {slope, expo, R2, calcYoungs(slope)};
		return results;
	}
}
