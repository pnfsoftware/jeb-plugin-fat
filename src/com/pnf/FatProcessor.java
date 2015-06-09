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
	private List<ImageFileEntry> files;

	/**
	 * Creates a new {@code FatCore} object from the given byte stream.
	 * 
	 * <p><i>Note: Calling this constructor <b>will not</b> extract any files from the image bytes. For extracting the files, see {@link dumpFiles}.</i></p>
	 * @param stream byte array representation of a FAT image
	 */
	public FatProcessor(byte[] stream){
		try {
			RamDisk rd = RamDisk.read(stream); // Create in-memory represenation of disk image
			image = FatFileSystem.read(rd, true); // Open as read-only to prevent accidental writes
		} catch (IOException e) {
			e.printStackTrace();
		}

		type = image.getFatType().toString(); // Store type of FAT image

		/* Create list of files */
		FsDirectory root = image.getRoot();

		// Create List to hold all the ImageFileEntry objects
		files = new ArrayList<>();

		try{
			// Recursively add entries for each file inside the image
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
			if(!dest.mkdirs()){ // Attempt to create directory if destination does not exists
				throw new RuntimeException("Failed to create output directory " + dest.getAbsolutePath() + ".");
			}
		}

		outputDir = dest;
		return dumpFiles(); // Call internal dumpFiles method after error-checking is done
	}

	private boolean dumpFiles(){	
		FileOutputStream stream = null;

		for(ImageFileEntry e: files){
			// Retrieve file stored within FileOutputEntry object
			File file = e.getFile();

			// Create a new file using the given output directory to resolve paths
			File realFile = outputDir.toPath().resolve(file.getName()).toFile();

			if(VERBOSE) // Only log writing if in verbose mode
				FatPlugin.LOG.info("%s", "Writing: " + realFile.getAbsolutePath());
			try {
				if(realFile.getParentFile().isDirectory()){
					realFile.getParentFile().mkdirs();
				}

				stream = new FileOutputStream(realFile);

				// Retrieve byte array stored within entry and write to file
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
		// Iterate through all entries in the current fs directory
		for(FsDirectoryEntry e: fatDir){
			// If it's a directory, recurse deeper
			if (e.isDirectory()) {
				StringBuffer buff = null;
				if(VERBOSE) // Only log if verbose
					buff = new StringBuffer();
				for (int i = 0; i < tabs; i++)
					buff.append(' ');
				if ((!e.getName().equals(".")) && (!e.getName().equals(".."))){ // Avoid references to current and parent directory, if present
					if(VERBOSE){ // Only log if verbose
						for (int i = 0; i < tabs; i++)
							buff.append("  ");
						FatPlugin.LOG.info("%s", buff.toString() + "[" + e.toString() + "]");
					}

					// Recurse and parse files within the current directory
					addAll(e.getDirectory(), tabs + 1, new File(parentDir, e.getName()));
				}
			} else {
				if(VERBOSE){ // Only log if verbose
					StringBuffer buff = new StringBuffer();
					for (int i = 0; i < tabs; i++)
						buff.append("  ");
					buff.append(e);
					FatPlugin.LOG.info("%s", buff.toString());
				}

				// Create a new File from the given parent directory
				File curFile = new File(parentDir, e.getName());

				// Retrieve chained representation of files in image
				FsFile fatFile = e.getFile();

				// Create ByteBuffer around contents of file. Todo: update so files with more than Integer.MAX_VALUE bytes are stored properly (possibly using a ByteBuffer array).
				ByteBuffer buff = ByteBuffer.allocate((int)fatFile.getLength());

				// Read data from image file into buffer
				fatFile.read(0, buff);

				// Create a new ImageFileEntry from the path of this data within the image and the raw bytes
				ImageFileEntry entry = new ImageFileEntry(curFile, buff);

				// Add the new data and file entry into the list of all entries
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
	 * Returns a {@code List} representation of the {@link ImageFileEntry} objects created from this image.
	 * <br>Note that all {@code File} objects stored within a {@link ImageFileEntry} are not created relative to any root directory.</p>
	 * @return
	 */
	public List<ImageFileEntry> getStoredFiles(){
		return files;
	}

	/**
	 * Retrieve a {@code String} representation of the filesystem type this image represents.
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

	public class ImageFileEntry {
		private ByteBuffer buff;
		private File file;

		/**
		 * Creates an {@code ImageFileEntry} object composed of the given {@link File} path and byte data
		 * @param path to this data inside the image
		 * @param buff byte representation of this entry in the image
		 */
		public ImageFileEntry(File file, ByteBuffer buff){
			this.file = file;
			this.buff = buff;
		}

		/**
		 * Retrieves the byte data associated with this entry
		 * @return a {@link ByteBuffer} that wraps this entry's byte data
		 */

		public ByteBuffer getBuffer(){
			return buff;
		}

		/**
		 * Retrieves the {@link File} representation of this entry's path in the image
		 * @return a {@code File} denoting this entry's path in the image
		 */
		public File getFile(){
			return file;
		}

		/**
		 * @return a String representation of this entry's path inside the image
		 */

		public String toString(){
			return file.toString();
		}
	}
}
