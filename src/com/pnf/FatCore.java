package com.pnf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.waldheinz.fs.FsDirectory;
import de.waldheinz.fs.FsDirectoryEntry;
import de.waldheinz.fs.FsFile;
import de.waldheinz.fs.fat.FatFileSystem;
import de.waldheinz.fs.util.RamDisk;

public class FatCore {
	private static boolean VERBOSE = false;

	private File outputDir;
	private FatFileSystem image;
	private String type;
	private List<FileOutputEntry> files;

	/**
	 * Creates a new {@link FatCore} object from the given byte stream. All internal {@link File} objects will be created under the given {@link rootDirectory}.
	 * 
	 * <p><i>Note: Calling this constructor <b>will not</b> extract any files from the image data. For extracting the files, see {@link dumpFiles}.</i></p>
	 * @param stream byte array representation of a FAT image
	 * @param rootDirectory {@link File} object to use as the base directory when creating internal {@link File} entries for the image contents
	 */
	public FatCore(byte[] stream){
		try {
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
			addAll(root, 0, null);
		}catch(IOException e){
			throw new RuntimeException("Error while attempting to read image.");
		}
	}
	
	public boolean dumpFiles(File dest){
		if(dest == null){
			throw new IllegalArgumentException("Destination directory is null.");
		}else if(!dest.exists()){
			if(!dest.mkdirs()){
				throw new RuntimeException("Failed to create output directory " + dest.getAbsolutePath() + ".");
			}
		}
		
		outputDir = dest;
		return dumpFiles();
	}

	private boolean dumpFiles(){	
		FileOutputStream stream = null;

		for(FileOutputEntry e: files){
			File file = e.getFile();
			File realFile = outputDir.toPath().resolve(file.getName()).toFile();
			
			if(VERBOSE)
				System.out.println("Writing: " + realFile.getAbsolutePath());
			try {
				if(realFile != null){
					if(realFile.exists()){
						realFile.delete();
					}else if(realFile.getParentFile().isDirectory()){
						realFile.getParentFile().mkdirs();
					}
					stream = new FileOutputStream(realFile);
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
				if(VERBOSE)
					for (int i = 0; i < tabs; i++)
						System.out.print(' ');
				if ((!e.getName().equals(".")) && (!e.getName().equals(".."))){
					if(VERBOSE){
						for (int i = 0; i < tabs; i++)
							System.out.print("  ");
						System.out.println("[" + e + "]");
					}
					addAll(e.getDirectory(), tabs + 1, new File(parentDir, e.getName()));
				}
			} else {
				if(VERBOSE){
					for (int i = 0; i < tabs; i++)
						System.out.print("  ");
					System.out.println(e);
				}

				File curFile = new File(parentDir, e.getName());
				FsFile fatFile = e.getFile();

				ByteBuffer buff = ByteBuffer.allocate((int)fatFile.getLength());
				fatFile.read(0, buff);
				FileOutputEntry entry = new FileOutputEntry(curFile, buff);
				files.add(entry);
			}
		}
	}
	
	public static void setVerbose(boolean verbose){
		VERBOSE = verbose;
	}

	public List<FileOutputEntry> getStoredFiles(){
		return files;
	}

	public FatFileSystem getFatSystem(){
		return image;
	}

	public File getOutputDirectory(){
		return outputDir;
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
