/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.services.importexport;

/**
 * Import dialog options wrapper class.
 * @author Michael
 */
public class ImportDialogOptions {
	
	private final boolean allowMultipleFileSelection;
	private final boolean allowZipFileSelection;

	private ImportDialogOptions(boolean allowMultipleFileSelection, boolean allowZipFileSelection) {
		this.allowMultipleFileSelection = allowMultipleFileSelection;
		this.allowZipFileSelection = allowZipFileSelection;
	}
	
	public static ImportDialogOptions disallow() {
		return new ImportDialogOptions(false, false);
	}
	
	public static ImportDialogOptions allowMultiple() {
		return new ImportDialogOptions(true, true);
	}
	
	public static ImportDialogOptions allowZip() {
		return new ImportDialogOptions(false, true);
	}
	
	public ImportDialogOptions andAllowMultiple() {
		return new ImportDialogOptions(true, allowZipFileSelection);
	}
	
	public ImportDialogOptions andAllowZip() {
		return new ImportDialogOptions(allowMultipleFileSelection, true);
	}

	public boolean isAllowMultiple() {
		return allowMultipleFileSelection;
	}

	public boolean isAllowZip() {
		return allowZipFileSelection;
	}
	
}
