package ln.afm.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class CurveData {
	private static final Logger LOGGER = Logger.getLogger(CurveData.class.getName() ); //TODO
	
	private double sensFactor;
	private double sprConstant;
	private double alpha;
	private double impactZ;
	
	private List<Double> zdistValues = new ArrayList<Double>();
	private List<Double> voltValues = new ArrayList<Double>();
	
	private String[] units;
	private JTextArea userLog;
	
	public CurveData(JTextArea log){
		userLog = log;
	}
	
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

	public double getSensitivity(){
		return sensFactor;
	}
	public void setSensitivity(double newSensFactor){
		sensFactor = newSensFactor;
	}
	
	public double getSpringConstant(){
		return sprConstant;
	}
	public void setSpringConstant(double newSprConstant){
		sprConstant = newSprConstant;
	}
	
	public double getAlpha(){
		return alpha;
	}
	public void setAlpha(double newAlpha){
		alpha = newAlpha;
	}
	
	public double getImpactZ(){
		return impactZ;
	}
	public void setImpactZ(double newImpactZ){
		impactZ = newImpactZ;
	}
	
	public boolean appendValues(double[] value)
	{
		if(value.length > 2)
		{
			userLog.append("Too many arguments per row." + "\n");
			return false;
		}
		if(!(zdistValues.add(value[0]) && voltValues.add(value[1])))
		{
			userLog.append("Failed to append data." + "\n");
			return false;
		}
		return true;
	}

	public String[] getUnits()
	{
		return units;
	}
	public void setUnits(String[] newUnits)
	{
		units = newUnits;
	}

	private void multiply(double[] conversions)
	{
		for(int i=0; i < zdistValues.size(); i++)
		{
			zdistValues.set(i, zdistValues.get(i)*conversions[0]);
			voltValues.set(i, voltValues.get(i)*conversions[1]);
		}
	}
	
	public boolean changeUnits(String[] newUnits)
	{
		boolean zUnits = units[0].equals(newUnits[0]);
		boolean vUnits = units[1].equals(newUnits[1]);
		if(zUnits && vUnits)
		{
			userLog.append("Units do not need conversion." + "\n");
			return true;
		}
		double[] conversions = Converter.getConversion(units, newUnits);
		multiply(conversions);
		units = newUnits;
		return true;
	}
	
	private XYDataset getXYData()
	{
		XYSeries data = new XYSeries("test");
		for(int i=0;i < zdistValues.size();i++)
		{
			data.add(zdistValues.get(i), voltValues.get(i));
		}
		final XYSeriesCollection dataset = new XYSeriesCollection();          
	    dataset.addSeries(data);
	    return dataset;
	}
	
	public JFreeChart getXYChart()
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
		return xylineChart;
	}
	
	
	public void printData()
	{
		userLog.append("Printing current data..." + "\n");
		userLog.append(units[0] + "\t" + units[1] + "\n");
		for(int i=0; i < zdistValues.size(); i++)
		{
			userLog.append(zdistValues.get(i) + "\t" + voltValues.get(i) + "\n");
		}
	}
	
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
	
}
