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
	
	public static final String[] SONGS = {"newmsg.wav"};
	
	//TODO add constants from classes
}
