package ln.afm.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import ln.afm.gui.AfmDisplay;
import ln.afm.model.CurveData;
import ln.afm.model.FileParser;
import ln.afm.model.SaveFile;
import ln.afm.solver.DoglegLSLM;
import ln.afm.solver.GeneticLinReg;
import ln.afm.solver.Point2D;

/**
 * Handles the go-between of the GUI and the data/solvers
 * @author Lynette Naler
 *
 */
public class Manager {
	
	//Our files, and associated curve data
	List<File> allFiles = new ArrayList<File>();
	List<CurveData> allData = new ArrayList<CurveData>();
	private int numParsed;
	
	private int currentData;
	
	//Where our config file is and how many parameters
	static String configFile = "afm-local.config";
	static int numConfigs = 5; 
	
	//Export as JPEG or PNG
	public static final int JPEG = 0;
	public static final int PNG = 1;
	
	//Levenberg Marquadt Algorithm?
	private static boolean calcFit = true;

	/**
	 * Constructor
	 */
	public Manager() {
		numParsed = 0;
	}

	/**
	 * Sets the index of the data we are currently working on
	 * @param dataIndex index of the file we are working with
	 */
	private void setCurrentData(int dataIndex)
	{
		currentData = dataIndex;
	}

	/**
	 * Sets the index of the file we are currently working on. Sets the appropriate index in the Manager
	 * @param index index of the file we are working with
	 */
	public void setCurrentFile(int index)
	{
		setCurrentData(index);
	}
	
	/**
	 * Initializes a matrix with given data
	 * @param data to be put in the matrix
	 */
	public void initMatrix()
	{
		allData.get(currentData).initMatrix();
	}
	
	/**
	 * Calculate our matrix
	 */
	public void calcMatrix()
	{
		allData.get(currentData).calcMatrix();
		allData.get(currentData).generateXYChart();
	}
	
	/**
	 * Create our solver (Levenberg or Genetic) and fit it to our data points
	 * @return {Slope, 2 (exponent)}
	 */
	public static double[] fitMatrix(List<Point2D> dlPoints, int iterations)
	{
		double[] results = {0.0, 0.0};
		if(calcFit)
		{
			//Dogleg optimizer
			double coeffs[] = {0, 2.0}; //right now we're forcing it to ax^2
			DoglegLSLM dlSolver = new DoglegLSLM();
			coeffs[0] = dlSolver.getFit(dlPoints, iterations);
	        System.out.print("Curve Slope: " + coeffs[0] + " Exp: " + coeffs[1] + "\n");
	        results[0] = coeffs[0];
	        results[1] = coeffs[1];
	        //return results;
		}
		if(!calcFit)
		{
			//Genetic Algorithm
			double coeffs[] = {0, 2.0};
			GeneticLinReg glr = new GeneticLinReg(dlPoints, iterations);
			glr.run();
			coeffs[0] = glr.getSlope();
			results[0] = coeffs[0];
	        results[1] = coeffs[1];
			//return results;
		}
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
		calcMatrix(); // perform calculations
		Manager.calcFit = goodFit;
		//allData.get(currentData).generateXYChart(); //get chart
		return allData.get(currentData).getXYChart();
	}
	
	/**
	 * Returns the calculated results to the caller
	 * @return {slope, exponent, r-squared, young's}
	 */
	public double[] getResults()
	{
		return allData.get(currentData).getResults();
	}
	
//	public boolean parseFile(File dataFile)
//	{
//		boolean dataUpload = false;
//		CurveData data = new CurveData();
//		FileParser fP = new FileParser(data);
//		//see if we can parse out some data
//		try {
//			dataUpload = fP.readFile(dataFile);
//			//data = fP.getData();
//		} 
//		catch (IOException e){
//			e.printStackTrace();
//			AfmDisplay.infoBox("Error parsing file.", "ERROR");
//		}
//		if(dataUpload)
//		{
//			//setCurrentData(data);
//		}
//		return dataUpload;
//	}
	
	/**
	 * Parses an individual file and add's its data if the upload was successful
	 * @param dataFile
	 * @return
	 */
	public boolean parseFile(File dataFile)
	{
		boolean dataUpload = false;
		CurveData data = new CurveData();
		FileParser fP = new FileParser(data);
		//see if we can parse out some data
		try {
			dataUpload = fP.readFile(dataFile);
			System.out.println("Just parsed: " + dataFile.getName());
			//data = fP.getData();
		} 
		catch (IOException e){
			e.printStackTrace();
			AfmDisplay.infoBox("Error parsing file.", "ERROR");
		}
		if(dataUpload)
		{
			allData.add(data);
		}
		return dataUpload;
	}
	
	/**
	 * Grabs the raw data from the current data
	 * @return a chart showing the raw data
	 */
	public JFreeChart viewRawGraph()
	{
		String[] testUnits = {"nm","V"};
		allData.get(currentData).changeUnits(testUnits);
		//data.printData();
		return allData.get(currentData).getRawData();
	}
	
	/**
	 * Sets the parameters in the current data based on user input and the config file
	 * @param inputs (not used, not used, not used, impact Z, limiting percent, gel size)
	 * @param limitZ a boolean of if the data is limited
	 */
	public void setParameters (double[] inputs, boolean limitZ) { //TODO ERROR HANDLING
		double[] configs = getConfigValues();
		
		allData.get(currentData).setPoissonsRatio(configs[0]);
		allData.get(currentData).setAlpha(configs[1]);
		allData.get(currentData).setSpringConstant(configs[2]);
		allData.get(currentData).setSensitivity(configs[3]);
		
		
		//allData.get(currentData).setSensitivity(inputs[0]);
		//allData.get(currentData).setSpringConstant(inputs[1]);
		//allData.get(currentData).setAlpha(inputs[2]);
		allData.get(currentData).setImpactZ(inputs[3]);
		allData.get(currentData).setLimitPercent(inputs[4]);
		allData.get(currentData).setGelSize(inputs[5]);
		//allData.get(currentData).setPoissonsRatio(0.25);
		
		allData.get(currentData).makeLimited(limitZ);
		
		allData.get(currentData).setNumIterations(((Number)configs[4]).intValue());
	}
	
	/**
	 * Writes given values to the config file, 0 if the value given is not a double
	 * @param values config values (poisson, alpha, spring, sensitivity, iterations)
	 */
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
	/**
	 * Gets the config values from the file, or 0s if no file is present then creates a config
	 * @return (poisson, alpha, spring, sensitivity)
	 */
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
			String[] temp = new String[numConfigs];
			Arrays.fill(results, 0.0);
			Arrays.fill(temp, "0");
			results[4] = 5000;
			temp[4] = "5000";
			setConfigValues(temp);
		}
		return results;
	}
	
	/**
	 * Iterates through the files and attempts to parse. If a file fails, it's removed from the list of files.
	 * @param dataFiles the list of files to upload
	 * @return the list of files successfully uploaded
	 */
	public File[] parseFiles(File[] dataFiles)
	{
		
		setAllFiles(dataFiles);
		boolean[] success = new boolean[allFiles.size()];
		Arrays.fill(success, false);
		int numRemoved = 0;
		for(int i = 0; i < dataFiles.length; i++)
		{
			success[i]=parseFile(dataFiles[i]);
			if(!success[i])
			{
				allFiles.remove(i - numRemoved);
				numRemoved++;
				System.out.println("Just removed: " + dataFiles[i].getName());
			}
		}
		numParsed = allFiles.size();
		File[] output =  allFiles.toArray(new File[numParsed]);
		System.out.println("Spacer line");
		return output;
	}
	
	/**
	 * Sets the manager's list of files to the given list of files
	 * @param inFiles list of files the manager is taking care of
	 */
	private void setAllFiles(File[] inFiles)
	{
		for(int i=0;i < inFiles.length;i++)
		{
			if(!allFiles.contains(inFiles[i]))
			{
				allFiles.add(inFiles[i]);
			}
		}
	}
	
	/**
	 * Clears all files and data
	 */
	public void clearAllFiles()
	{
		allFiles = new ArrayList<File>();
		allData = new ArrayList<CurveData>();
		numParsed = 0;
	}
	
	/**
	 * Checks if the current data has run
	 * @return whether the current data has run
	 */
	public boolean hasRun()
	{
		return allData.get(currentData).hasRun();
	}
	
	/**
	 * Gets the force graph from the previous run
	 * @return the force graph of the previous run
	 */
	public JFreeChart getPreviousRun()
	{
		return allData.get(currentData).getXYChart();
	}
	
	/**
	 * Gets the parameters from the previous run
	 * @return previous parameters (gel size, impact z, limit percent, has limit (1 true))
	 */
	public double[] getPreviousParameters()
	{
		double tempGelSize = allData.get(currentData).getGelSize();
		double tempImpactZ = allData.get(currentData).getImpactZ();
		double tempLimitPercent = allData.get(currentData).getLimitPercent();
		double tempHasLimit = 0.0;
		if(allData.get(currentData).isLimited())
		{
			tempHasLimit = 1.0;
		}
		double[] results = {tempGelSize, tempImpactZ, tempLimitPercent, tempHasLimit};
		return results;
	}
	
	/**
	 * Exports a list of files to a given location, with graphs of the given fileType
	 * @param exportFiles the list of files to export
	 * @param savePath path to save the files to 
	 * @param fileType either Manager.JPEG or Manager.PNG
	 */
	public void export(int[] exportFiles, String savePath, int fileType)
	{
		System.out.println("Readying " + savePath);
		for(int i = 0; i < exportFiles.length; i++)
		{
			System.out.println("Saving: " + allFiles.get(exportFiles[i]).getName());
		}
		
		String filePath = savePath + "\\Analysis.txt";
		for(int i = 0; i < exportFiles.length; i++)
		{
			CurveData saveData = allData.get(exportFiles[i]);
			String saveFile = allFiles.get(exportFiles[i]).getName();
			
			String extension = ".png";
			
			if(fileType == JPEG)
			{
				extension = ".jpg";
			}
			File rawName = new File(savePath, "Raw - " + saveFile + extension);
			File forceName = new File(savePath, "Force - " + saveFile + extension);
			
			SaveFile saver = new SaveFile(filePath, true);
			saver.write(saveData.print(saveFile));
			
			if(fileType == JPEG)
			{
				try {
			        ChartUtilities.saveChartAsJPEG(rawName, saveData.getRawData(), 800, 800);
			        if(saveData.hasRun())
			        {
			        	ChartUtilities.saveChartAsJPEG(forceName, saveData.getXYChart(), 800, 800);
			        }
			    } 
				catch (IOException ex) {
			        System.out.println("Error saving a file");
			    }
			}
			else
			{
				try {
			        ChartUtilities.saveChartAsPNG(rawName, saveData.getRawData(), 800, 800);
			        if(saveData.hasRun())
			        {
			        	ChartUtilities.saveChartAsPNG(forceName, saveData.getXYChart(), 800, 800);
			        }
			    } 
				catch (IOException ex) {
			        System.out.println("Error saving a file");
			    }
			}
			
		}
	}

//	/**
//	 * 
//	 * @param inPoints
//	 * @return
//	 */
//	public static ArrayList<WeightedObservedPoint> toWOPs(List<Point2D> inPoints)
//	{
//		ArrayList<WeightedObservedPoint> results = new ArrayList<>();
//		for(Point2D point:inPoints)
//		{
//			results.add(new WeightedObservedPoint(1.0, point.x, point.y));
//		}
//		return results;
//	}
	
	/**
	 * Returns whether or not it's fitted using Levenberg
	 * @return true if levenberg is used, false for genetic
	 */
	public boolean calcFit()
	{
		return calcFit;
	}
}
