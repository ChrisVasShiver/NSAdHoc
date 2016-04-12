package helper;

import javax.swing.filechooser.FileFilter;

import gui.FileTypeFilter;

/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public interface Constants {
	public static final FileFilter docFilter = new FileTypeFilter(".docx", "Microsoft Word Documents");
	public static final FileFilter pdfFilter = new FileTypeFilter(".pdf", "PDF Documents");
	public static final FileFilter xlsFilter = new FileTypeFilter(".xlsx", "Microsoft Excel Documents");
	public static final FileFilter jpgFilter = new FileTypeFilter(".jpg", "JPG Image");
	//TODO add constants from classes
	
	public static final String[] SONGS = {"newmsg.wav"};

	public static final int MULTI_SOCKET_PORT = 6789;
	public static final int UNI_SOCKET_PORT = 7000;
	public static final int PACKET_TIMEOUT = 3000;
	public static final int MAX_PACKET_SIZE = 1024;
	
}
