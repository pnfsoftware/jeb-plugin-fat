package com.pnf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.pnfsoftware.jeb.util.IO;

import de.waldheinz.fs.FsDirectory;
import de.waldheinz.fs.FsDirectoryEntry;
import de.waldheinz.fs.FsFile;
import de.waldheinz.fs.fat.FatFileSystem;
import de.waldheinz.fs.util.RamDisk;

public class FatCore {
	private File tempDir;
	private FatFileSystem image;
	private String type;
	private List<FileOutputEntry> files;

	public FatCore(byte[] stream){
		try {
			tempDir = IO.createTempFolder("fat_data");

			RamDisk rd = RamDisk.read(stream);
			image = FatFileSystem.read(rd, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		type = image.getFatType().toString();

		/* Create list of files */
		FsDirectory root = image.getRoot();

		files = new ArrayList<>();

		try{
			addAll(root, 0, tempDir);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public boolean dumpFiles(){
		FileOutputStream stream = null;

		for(FileOutputEntry e: files){
			File file = e.getFile();
			System.out.println("Writing: " + file.getAbsolutePath());
			try {
				if(file != null){
					if(file.exists()){
						file.delete();
					}else if(file.getParentFile().isDirectory()){
						file.getParentFile().mkdirs();
					}
					stream = new FileOutputStream(file);
					stream.write(e.getBuffer().array());
				}
			} catch (IOException e1) {
				return false;
			} finally{
				if(stream != null){
					try {
						stream.close();
					} catch (IOException e1) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private void addAll(FsDirectory fatDir, int tabs, File parentDir) throws IOException {
		for(FsDirectoryEntry e: fatDir){
			if (e.isDirectory()) {
				for (int i = 0; i < tabs; i++)
					System.out.print(' ');
				if ((!e.getName().equals(".")) && (!e.getName().equals(".."))){
					for (int i = 0; i < tabs; i++)
						System.out.print("  ");
					System.out.println("[" + e + "]");
					addAll(e.getDirectory(), tabs + 1, new File(parentDir, e.getName()));
				}
			} else {
				for (int i = 0; i < tabs; i++)
					System.out.print("  ");
				System.out.println(e);

				File curFile = new File(parentDir, e.getName());
				FsFile fatFile = e.getFile();

				ByteBuffer buff = ByteBuffer.allocate((int)fatFile.getLength());
				fatFile.read(0, buff);
				FileOutputEntry entry = new FileOutputEntry(curFile, buff);
				files.add(entry);
			}
		}
	}

	public List<FileOutputEntry> getStoredFiles(){
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

	public String toString(){
		return type + " filesystem: " + files.toString();
	}
}

class FileOutputEntry {
	private ByteBuffer buff;
	private File file;

	public FileOutputEntry(File file, ByteBuffer buff){
		this.file = file;
		this.buff = buff;
	}

	public ByteBuffer getBuffer(){
		return buff;
	}

	public File getFile(){
		return file;
	}

	public String toString(){
		return file.toString() + " buff is " + (buff == null ? "NULL" : "NOT null");
	}
}
