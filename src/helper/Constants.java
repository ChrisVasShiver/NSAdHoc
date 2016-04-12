package helper;

import javax.swing.filechooser.FileFilter;

import gui.FileTypeFilter;

public interface Constants {
	public static final FileFilter docFilter = new FileTypeFilter(".docx", "Microsoft Word Documents");
	public static final FileFilter pdfFilter = new FileTypeFilter(".pdf", "PDF Documents");
	public static final FileFilter xlsFilter = new FileTypeFilter(".xlsx", "Microsoft Excel Documents");
	public static final FileFilter jpgFilter = new FileTypeFilter(".jpg", "JPG Image");
	//TODO add constants from classes
}
