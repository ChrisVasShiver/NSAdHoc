package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Encoder {
	private byte[] filebytes;
	
	public Encoder(byte[] bytes){
		filebytes = bytes;
		encode();
	}
	
	public void encode(){
		FileOutputStream fos =null;
		try {
			fos = new FileOutputStream("pathname");
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
