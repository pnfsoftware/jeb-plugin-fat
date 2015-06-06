package com.pnf;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.pnfsoftware.jeb.util.IO;

import de.waldheinz.fs.FsDirectoryEntry;
import de.waldheinz.fs.fat.FatFile;
import de.waldheinz.fs.fat.FatFileSystem;
import de.waldheinz.fs.fat.FatLfnDirectory;
import de.waldheinz.fs.fat.FatLfnDirectoryEntry;
import de.waldheinz.fs.util.RamDisk;

public class FatCore {
	private File tempDir;
	private FatFileSystem image;
	private String type;
	private List<FileEntry> files;

	public FatCore(byte[] stream, boolean readOnly){
		try {
			tempDir = IO.createTempFolder("fat_data");
			RamDisk rd = RamDisk.read(stream);
			image = FatFileSystem.read(rd, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		type = image.getFatType().toString();

		/* Create list of files */
		FatLfnDirectory root = image.getRoot();

		files = new ArrayList<>();

		addAll(tempDir, root);
	}
	
	private void addAll(File root, FatLfnDirectory topDir){
		for(FsDirectoryEntry e: topDir){
			addToList(root, (FatLfnDirectoryEntry) e);
		}
	}

	private void addToList(File root, FatLfnDirectoryEntry e){
		FileEntry entry = null;
		if(e.isFile()){
			// Is a file
			FatFile file = null;
			File f = new File(e.getName());
			try{
				file = e.getFile();
				ByteBuffer buff = ByteBuffer.wrap(new byte[(int)file.getLength()]);
				entry = new FileEntry(f, buff);
				files.add(entry);
			}catch(IOException e1){
				e1.printStackTrace();
			}
		}else{
			// Is a directory, so recurse
			File sub = new File(root, e.getName());
			sub.mkdirs();
			try {
				addAll(sub, e.getDirectory());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public List<FileEntry> getStoredFiles(){
		return files;
	}

	public FatFileSystem getFatSystem(){
		return image;
	}

	public File getOutputDirectory(){
		return tempDir;
	}

	public String getFatType(){
		return type;
	}
}

class FileEntry {
	private ByteBuffer buff;
	private File file;

	public FileEntry(File file, ByteBuffer buff){
		this.buff = buff;
	}

	public byte[] getBytes(){
		return buff.array();
	}

	public File getFile(){
		return file;
	}
}
