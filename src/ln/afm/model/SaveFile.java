package ln.afm.model;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.io.IOException;

public class SaveFile {

	private String filePath;
	private boolean append = false;
	
	public SaveFile(String filePath) {
		this.filePath = filePath;
	}
	
	public SaveFile(String filePath, boolean append)
	{
		this.filePath = filePath;
		this.append = append;
	}
	
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

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}
}
