package com.pnf;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.pnfsoftware.jeb.util.IO;

import de.waldheinz.fs.fat.FatFileSystem;

public class FatCore {
	private File tempDir;
	private FatFileSystem image;
	
	public FatCore(byte[] stream, boolean readOnly){
		try {
			tempDir = IO.createTempFolder("fat_data");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<FileEntry> getStoredFiles(){
		return null;
	}
	
	public FatFileSystem getFatSystem(){
		return image;
	}
	
	public File getOutputDirectory(){
		return tempDir;
	}
}

class FileEntry {
	private byte[] data;
	private String name;
	public FileEntry(byte[] data, String name){
		this.data = data;
		this.name = name;
	}
	
	public byte[] getBytes(){
		return data;
	}
	
	public String getName(){
		return name;
	}
}
