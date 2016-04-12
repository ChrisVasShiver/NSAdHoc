package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Decoder {
	private static final String DOWNLOAD_PATH = "Download";
	private byte[] filebytes;
	
	public Decoder(byte[] bytes){
		filebytes = bytes;
	}
	
	public void decode(String filename) {
		FileOutputStream fos =null;
		try {
			fos = new FileOutputStream(DOWNLOAD_PATH + File.pathSeparator + filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
