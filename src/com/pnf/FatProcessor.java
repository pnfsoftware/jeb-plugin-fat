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

public class FatProcessor {
	private static boolean VERBOSE = false;

	private File outputDir;
	private FatFileSystem image;
	private String type;
	private List<FileOutputEntry> files;

	/**
	 * Creates a new {@code FatCore} object from the given byte stream. All internal {@link File} objects will be created under the given {@code rootDirectory}.
	 * 
	 * <p><i>Note: Calling this constructor <b>will not</b> extract any files from the image data. For extracting the files, see {@link dumpFiles}.</i></p>
	 * @param stream byte array representation of a FAT image
	 * @param rootDirectory {@link File} object to use as the base directory when creating internal {@link File} entries for the image contents
	 */
	public FatProcessor(byte[] stream){
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

	/**
	 * Dumps the contents of this FAT filesystem image to the given directory.
	 * @param dest Destination directory for this image's contents
	 * @return {@code true} if the image contents were dumped successfully, {@code false} otherwise
	 */

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
			// Retrieve file stored within FileOutputEntry object
			File file = e.getFile();

			// Create a new file using the given output directory to resolve paths
			File realFile = outputDir.toPath().resolve(file.getName()).toFile();

			if(VERBOSE)
				System.out.println("Writing: " + realFile.getAbsolutePath());
			try {
				if(realFile.exists()){
					realFile.delete();
				}else if(realFile.getParentFile().isDirectory()){
					realFile.getParentFile().mkdirs();
				}
				stream = new FileOutputStream(realFile);
				
				// Retrieve ByteBuffer data stored within entry and write to file
				stream.write(e.getBuffer().array());
			} catch (IOException e1) {
				return false;
			} finally{
				// Close stream to avoid resource leaks
				if(stream != null){
					try {
						stream.close();
					} catch (IOException e1) {
						return false;
					}
				}
			}
		}

		// Return true if we made it here without issues
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
					
					// Recurse and parse files within the current directory
					addAll(e.getDirectory(), tabs + 1, new File(parentDir, e.getName()));
				}
			} else {
				if(VERBOSE){
					for (int i = 0; i < tabs; i++)
						System.out.print("  ");
					System.out.println(e);
				}

				// Create a new File from the given parent directory
				File curFile = new File(parentDir, e.getName());
				FsFile fatFile = e.getFile();

				// Create ByteBuffer around contents of file. Todo: update so files with more than Integer.MAX_VALUE bytes are stored properly (possibly using a ByteBuffer array).
				ByteBuffer buff = ByteBuffer.allocate((int)fatFile.getLength());
				fatFile.read(0, buff);
				FileOutputEntry entry = new FileOutputEntry(curFile, buff);
				files.add(entry);
			}
		}
	}

	/**
	 * Enable or disable verbose logging during {@code FatCore} image reading.
	 * <p>Must be called before a {@code FatCore} object has been created.</p>
	 * @param verbose true to enable verbose logging, false otherwise
	 */

	public static void setVerbose(boolean verbose){
		VERBOSE = verbose;
	}

	/**
	 * Returns a {@code List} representation of the {@link FileOutputEntry} objects created from this image.
	 * <br>Note that all {@code File} objects stored within a {@link FileOutputEntry} are not created relative to any root directory.</p>
	 * @return
	 */
	public List<FileOutputEntry> getStoredFiles(){
		return files;
	}

	/**
	 * Retrieve a String representation of the filesystem type this image represents.
	 * @return a {@code String} representation of this image's filesystem type
	 */

	public String getFatType(){
		return type;
	}

	/**
	 * Returns a {@code String} representation of this {@code FatCore} object.
	 */

	public String toString(){
		return type + " filesystem: " + files.toString();
	}

	public class FileOutputEntry {
		private ByteBuffer buff;
		private File file;

		/**
		 * 
		 * @param file
		 * @param buff
		 */
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
}
