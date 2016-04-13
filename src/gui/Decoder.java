package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher 
 */
public class Decoder {
	private static final String DOWNLOAD_PATH = "Downloads";
	private byte[] filebytes;
	
	public Decoder(byte[] bytes){
		filebytes = bytes;
	}
	
	/**
	 * Turns byte array into a file a places it in the Download path(and creates when it not exists)
	 * @param filename the filename
	 */
	public void decode(String filename) {
		File downloadDir = new File(DOWNLOAD_PATH);
		if(!downloadDir.exists())
			try {
				downloadDir.mkdir();
			} catch (SecurityException e) {
				System.out.println("Could not create the directory " + downloadDir.getPath());
			}
		FileOutputStream fos =null;
		try {
			fos = new FileOutputStream(DOWNLOAD_PATH + File.separator + filename);
		} catch (FileNotFoundException e) {
			
		}
		try {
			fos.write(filebytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
