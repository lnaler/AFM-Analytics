package ln.afm.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import ln.afm.model.FileParser;
import ln.afm.model.SaveFile;
import ln.afm.solver.DoglegLS;
import ln.afm.solver.DoglegLSLM;
import ln.afm.solver.Point2D;
import ln.afm.solver.RobustFit;

public class Manager {
	
	CurveData currentData;
	boolean goodFit = true;
	
	static String configFile = "afm-local.config";
	static int numConfigs = 4; 

	public Manager() {
	}

	public void setCurrentData(CurveData cData)
	{
		currentData = cData;
		
	}

	
	/**
	 * Initializes a matrix with given data
	 * @param data to be put in the matrix
	 */
	public void initMatrix()
	{
		currentData.initMatrix();
	}
	
	/**
	 * Calculate our matrix
	 */
	public void calcMatrix(boolean goodFit)
	{
		currentData.calcMatrix();
		currentData.generateXYChart();
	}
	
	/**
	 * Create our solver and fit it to our data points
	 * @return {Slope, 2 (exponent), 0, 0}
	 */
	public static double[] fitMatrix(List<Point2D> dlPoints)
	{
		double coeffs[] = {0, 2.0}; //right now we're forcing it to ax^2
		DoglegLSLM dlSolver = new DoglegLSLM();
		coeffs[0] = dlSolver.getFit(dlPoints);
        System.out.print("Curve Slope: " + coeffs[0] + " Exp: " + coeffs[1] + "\n");
        double[] results = {coeffs[0], coeffs[1], 0, 0}; //TODO update to remove old linear vars
        return results;
	}
	
	/**
	 * Runs the analyst and returns the processed data chart with trend lines
	 * @return Force-Indentation chart with Power trend line
	 */
	public JFreeChart run(boolean goodFit)
	{
		System.out.print("Running..." + "\n");
		initMatrix();
		calcMatrix(goodFit); // perform calculations
		currentData.getXYChart(); //get chart
		return currentData.getXYChart();
	}
	
	/**
	 * Returns the calculated results to the caller
	 * @return {slope, exponent, r-squared, young's}
	 */
	public double[] getResults()
	{
		return currentData.getResults();
	}
	
	public boolean parseFile(File dataFile)
	{
		boolean dataUpload = false;
		CurveData data = new CurveData();
		FileParser fP = new FileParser(data);
		//see if we can parse out some data
		try {
			dataUpload = fP.readFile(dataFile);
			data = fP.getData();
		} 
		catch (IOException e){
			e.printStackTrace();
			AfmDisplay.infoBox("Error parsing file.", "ERROR");
		}
		if(dataUpload)
		{
			setCurrentData(data);
		}
		return dataUpload;
	}
	
	public JFreeChart viewRawGraph()
	{
		String[] testUnits = {"nm","V"};
		currentData.changeUnits(testUnits);
		//data.printData();
		return currentData.getRawData();
	}
	
	public void setParameters (double[] inputs, boolean limitZ) { //TODO ERROR HANDLING
		double[] configs = getConfigValues();
		
		currentData.setPoissonsRatio(configs[0]);
		currentData.setAlpha(configs[1]);
		currentData.setSpringConstant(configs[2]);
		currentData.setSensitivity(configs[3]);
		
		
		//currentData.setSensitivity(inputs[0]);
		//currentData.setSpringConstant(inputs[1]);
		//currentData.setAlpha(inputs[2]);
		currentData.setImpactZ(inputs[3]);
		currentData.setLimitPercent(inputs[4]);
		currentData.setGelSize(inputs[5]);
		//currentData.setPoissonsRatio(0.25);
		
		currentData.makeLimited(limitZ);
	}
	
	public void setConfigValues(String[] values)
	{
		FileParser.removeSpaces(values);
		String zero = "0";
		for(String val:values)
		{
			if(!FileParser.isDouble(val))
			{
				val = zero;
			}
		}
		List<String> configValues = Arrays.asList(values);
		SaveFile saver = new SaveFile(configFile);
		saver.write(configValues);
	}
	
	//Poisson, alpha, spring, sensitivity
	public double[] getConfigValues()
	{
		double[] results = FileParser.readConfigValues(configFile, numConfigs);
		boolean fileExists = true;
		for(int i=0;i < results.length; i++)
		{
			if(results[i] < 0)
			{
				fileExists = false;
			}
		}
		if(!fileExists)
		{
			String[] temp = new String[results.length];
			Arrays.fill(results, 0.0);
			Arrays.fill(temp, "0");
			setConfigValues(temp);
		}
		return results;
	}
}
