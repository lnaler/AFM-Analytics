package ln.afm.model;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.io.IOException;

/**
 * Saves a file to a given location
 * @author Lynette Naler
 *
 */
public class SaveFile {

	private String filePath;
	private boolean append = false;
	
	/**
	 * Constructor. Append defaults to false.
	 * @param filePath path to save the file at
	 */
	public SaveFile(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * Constructor
	 * @param filePath path to save the file at
	 * @param append whether it should add new information at the end of the file or overwrite
	 */
	public SaveFile(String filePath, boolean append)
	{
		this.filePath = filePath;
		this.append = append;
	}
	
	/**
	 * Writes the given lines to the location previously given. If file does not exist, file is created.
	 * @param lines list of lines to add to the file
	 */
	//http://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it-in-java
	public void write(List<String> lines) {
		try
		{
			Path file = Paths.get(filePath);
			if(append)
			{
				Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
			}
			else
			{
				Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.CREATE);
			}
		}
		catch (IOException e)
		{
			System.out.println(e.toString());
		}
		
	}

	/**
	 * Returns whether or not the saver appends
	 * @return true if the saver appends
	 */
	public boolean isAppend() {
		return append;
	}

	/**
	 * Set whether or not the saver appends
	 * @param append true if the saver should append
	 */
	public void setAppend(boolean append) {
		this.append = append;
	}
}
