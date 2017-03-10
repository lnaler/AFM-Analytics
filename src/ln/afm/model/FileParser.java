package ln.afm.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Parses a file and reads in the data
 * @author Lynette Naler
 *
 */
public class FileParser {
	private CurveData userCurve;
	private String[] units;
	//final static Charset ENCODING = StandardCharsets.UTF_8;
	
	
	/**
	 * Constructor for FileParser
	 * @param curve The CurveData to be appended to
	 */
	public FileParser(CurveData curve) {
		//userLog = log;
		userCurve = curve; //Don't think this is actually necessary
	}

	/**
	 * Reads in and parses a file
	 * @param inFile File to be parsed
	 * @return True if file successfully parsed
	 * @throws IOException
	 */
	public boolean readFile (File inFile) throws IOException
	{
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader((new FileInputStream(inFile)), "WINDOWS-1252"))){
	      String line = null;
	      //userLog.append("Attempting to read data..." +"\n");
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
	    		  if(units[0].equals(""))
	    		  {
	    			  //userLog.append("ERROR: The file must have the units in the header row." + "\n");
	    			  return false;
	    		  }
	    		  userCurve.setUnits(units);
	    	  }
	    	  //userLog.append("Processed: " + line + "\n");
	      }
	      reader.close();
	    }
	    catch(IOException e)
	    {
	    	//userLog.append("ERROR: Their was an error in reading the file." + "\n");
	    	//userLog.append(e.toString());
	    	return false;
	    }
	    return true;
	}
	
	/**
	 * Checks if the first line contains units
	 * @param firstLine The first line in the file
	 * @return String[] of the units
	 */
	public String[] checkUnits(String firstLine) //TODO Check error formatting, check unit
	{
		//userLog.append("Checking for units..." + "\n");
		String[] input = firstLine.split("\t");
		input = removeSpaces(input);
		//System.out.println("First line is: " + input[0]);
		if(input.length != 2)
		{
			input = new String[2];
			input[0] = "";
			input[1] = "";
			return input;
		}
		if(isDouble(input[0]) || isDouble(input[1]))
		{
			input[0] = "";
			input[1] = "";
		}
		if(!Converter.hasUnits(input[0], input[1]))
		{
			input[0] = "";
			input[1] = "";
		}
		return input;
	}
	
	/**
	 * Processes a line from the file and adds to our data
	 * @param line Line to be processed
	 * @return True if processing was successful
	 */
	private boolean processLine(String line) //TODO Error handling: Should be a number, error if not
	{
		if(line.contains("\\*"))
		{
			return false;
		}
		String dataStr[] = line.split("\t");
		dataStr = removeSpaces(dataStr);
		
		if(dataStr.length != 2)
		{
			return false;
		}
		if(isDouble(dataStr[0]) && isDouble(dataStr[1]))
		{
			double data[] = new double[2];
			data[0] = Double.parseDouble(dataStr[0]);
			data[1] = Double.parseDouble(dataStr[1]);
			return userCurve.appendValues(data);
		}
		//userLog.append("ERROR: Non-numeric data." + "\n");
		return false;
	}
	
	/**
	 * Removes all spaces from a string
	 * @param in String to be edited
	 * @return String without spaces
	 */
	public static String[] removeSpaces(String[] in)
	{
		for(int i = 0; i < in.length; i++)
		{
			in[i] = removeSpaces(in[i]);
		}
		return in;
	}
	
	private static String removeSpaces(String in)
	{
		return in.replaceAll("\\s+", ""); //http://stackoverflow.com/questions/15633228/how-to-remove-all-white-spaces-in-java
	}
	
	/**
	 * Checks if a string is a double
	 * @param s String to be checked
	 * @return True if it is a double
	 */
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

	/**
	 * Returns the data read
	 * @return CurveData from the file read
	 */
	public CurveData getData()
	{
		return userCurve;
	}
	
	/**
	 * Reads a number of config values from the given file
	 * @param filePath the location of the config file
	 * @param numValues how many config values to be read in
	 * @return the config values read in
	 */
	public static double[] readConfigValues(String filePath, int numValues)
	{
		Path file = Paths.get(filePath);
		double[] results = new double[numValues];
		Arrays.fill(results, -1.0);
		if(Files.exists(file))
		{
			try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) 
			{
			    String line = null;
			    int i = 0;
			    while ((line = reader.readLine()) != null && i < numValues) 
			    {
			    	line = removeSpaces(line);
					if(isDouble(line))
					{
						results[i] = Double.parseDouble(line);
					}
					i++;
			    }
			} 
			catch (IOException e) 
			{
			    System.out.println("IOException: " + e.toString());
			}
		}	
		return results;
	}
}
