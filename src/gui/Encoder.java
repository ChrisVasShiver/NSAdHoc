package gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher 
 */
public class Encoder {
	private String filename;
	
	public Encoder(String file){
		filename=file;
	}
	
	/**
	 * Gets file from path and turns it into a byte array
	 * @return the byte array of a file
	 */
	public byte[] encode(){
		Path path = Paths.get(filename);
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
}
