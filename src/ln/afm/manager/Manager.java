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

public class Manager {
	
	List<File> allFiles = new ArrayList<File>();
	List<CurveData> allData = new ArrayList<CurveData>();
	private int numParsed;
	
	private int currentData;
	boolean goodFit = true;
	
	static String configFile = "afm-local.config";
	static int numConfigs = 5; 
	
	public static final int JPEG = 0;
	public static final int PNG = 1;

	public Manager() {
		numParsed = 0;
	}

	private void setCurrentData(int dataIndex)
	{
		currentData = dataIndex;
	}

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
	public void calcMatrix(boolean goodFit)
	{
		allData.get(currentData).calcMatrix();
		allData.get(currentData).generateXYChart();
	}
	
	/**
	 * Create our solver and fit it to our data points
	 * @return {Slope, 2 (exponent), 0, 0}
	 */
	public static double[] fitMatrix(List<Point2D> dlPoints, int iterations)
	{
		double coeffs[] = {0, 2.0}; //right now we're forcing it to ax^2
		DoglegLSLM dlSolver = new DoglegLSLM();
		coeffs[0] = dlSolver.getFit(dlPoints, iterations);
        System.out.print("Curve Slope: " + coeffs[0] + " Exp: " + coeffs[1] + "\n");
        double[] results = {coeffs[0], coeffs[1], 0, 0}; //TODO update to remove old linear vars
        return results;
		
//		double coeffs[] = {0, 2.0};
//		GeneticLinReg glr = new GeneticLinReg(dlPoints, iterations);
//		glr.run();
//		coeffs[0] = glr.getSlope();
//		double[] results = {coeffs[0], coeffs[1], 0 ,0};
//		return results;
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
		//allData.get(currentData).getXYChart(); //get chart
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
	
	public JFreeChart viewRawGraph()
	{
		String[] testUnits = {"nm","V"};
		allData.get(currentData).changeUnits(testUnits);
		//data.printData();
		return allData.get(currentData).getRawData();
	}
	
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
			String[] temp = new String[numConfigs];
			Arrays.fill(results, 0.0);
			Arrays.fill(temp, "0");
			results[4] = 5000;
			temp[4] = "5000";
			setConfigValues(temp);
		}
		return results;
	}
	
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
	
	public void clearAllFiles()
	{
		allFiles = new ArrayList<File>();
		allData = new ArrayList<CurveData>();
		numParsed = 0;
	}
	
	public boolean hasRun()
	{
		return allData.get(currentData).hasRun();
	}
	
	public JFreeChart getPreviousRun()
	{
		return allData.get(currentData).getXYChart();
	}
	
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
	
}
