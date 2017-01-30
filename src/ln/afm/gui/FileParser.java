package ln.afm.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.swing.JTextArea;

public class FileParser {
	private static final Logger LOGGER = Logger.getLogger(FileParser.class.getName() ); //TODO Implement logging
	private JTextArea userLog;
	private CurveData userCurve;
	private String[] units;
	//final static Charset ENCODING = StandardCharsets.UTF_8;
	
	
	
	public FileParser(JTextArea log, CurveData curve) {
		userLog = log;
		userCurve = curve;
		userCurve.setAlpha(5.0);
	}

	public boolean readFile (File inFile) throws IOException
	{
	    try (BufferedReader reader = new BufferedReader(new FileReader(inFile))){
	      String line = null;
	      userLog.append("Attempting to read data..." +"\n");
	      boolean unitsChecked = false;
	      while ((line = reader.readLine()) != null) {
	    	  if(unitsChecked == true)
	    	  {
	    		  if(!processLine(line))
	    		  {
	    			  return false;
	    		  }
	    	  }
	    	  if(unitsChecked == false)
	    	  {
	    		  units = checkUnits(line);
	    		  unitsChecked = true;
	    		  if(units == null)
	    		  {
	    			  userLog.append("ERROR: The file must have the units in the header row." + "\n");
	    			  return false;
	    		  }
	    		  userCurve.setUnits(units);
	    	  }
	    	  userLog.append("Processed: " + line + "\n");
	      }
	    }
	    catch(IOException e)
	    {
	    	userLog.append("ERROR: Their was an error in reading the file." + "\n");
	    	userLog.append(e.toString());
	    	return false;
	    }
	    return true;
	}
	
	public String[] checkUnits(String firstLine) //TODO Check error formatting
	{
		userLog.append("Checking for units..." + "\n");
		String[] input = firstLine.split("\t");
		input = removeSpaces(input);
		//userLog.append("Just received: " + input[0] + " + " + input[1] + "\n");
		if(isDouble(input[0]) || isDouble(input[1]))
		{
			return null;
		}
		return input;
	}
	
	private boolean processLine(String line) //TODO Error handling: Should be a number, error if not
	{
		String dataStr[] = line.split("\t");
		dataStr = removeSpaces(dataStr);
		
		if(isDouble(dataStr[0]) && isDouble(dataStr[1]))
		{
			double data[] = new double[2];
			data[0] = Double.parseDouble(dataStr[0]);
			data[1] = Double.parseDouble(dataStr[1]);
			return userCurve.appendValues(data);
		}
		userLog.append("ERROR: Non-numeric data." + "\n");
		return false;
	}
	
	private String[] removeSpaces(String[] in)
	{
		for(int i = 0; i < in.length; i++)
		{
			in[i] = in[i].replaceAll("\\s+", ""); //http://stackoverflow.com/questions/15633228/how-to-remove-all-white-spaces-in-java
		}
		return in;
	}
	
//	public static boolean isDouble(String s) {
//	    return isDouble(s,10);
//	}
	
	public static boolean isDouble(String s) { //http://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
		if(s == null)
		{
			return false;
		}
	    Scanner sc = new Scanner(s.trim());
	    if(!sc.hasNextDouble())
	    {
	    	sc.close();
	    	return false;
	    }
	    // we know it starts with a valid int, now make sure
	    // there's nothing left!
	    sc.nextDouble();
	    boolean result = !sc.hasNext();
	    sc.close();
	    return result;
	}

	public CurveData getData()
	{
		return userCurve;
	}
}
