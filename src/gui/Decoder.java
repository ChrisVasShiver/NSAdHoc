package gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Decoder {
	private String filename;
	
	public Decoder(String file){
		filename=file;
		decode();
	}
	
	public byte[] decode(){
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
