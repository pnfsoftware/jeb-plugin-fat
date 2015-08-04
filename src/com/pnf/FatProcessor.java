package com.pnf;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.pnf.streams.ContainerStream;
import com.pnf.streams.DocumentStream;

import de.waldheinz.fs.FsDirectory;
import de.waldheinz.fs.FsDirectoryEntry;
import de.waldheinz.fs.FsFile;
import de.waldheinz.fs.fat.FatFileSystem;
import de.waldheinz.fs.util.RamDisk;

public class FatProcessor {
	private FatFileSystem image;
	private String type;
	private ContainerStream root;

	/**
	 * Creates a new {@code FatCore} object from the given byte stream.
	 * 
	 * <p><i>Note: Calling this constructor <b>will not</b> extract any files from the image bytes. For extracting the files, see {@link dumpFiles}.</i></p>
	 * @param stream byte array representation of a FAT image
	 * @throws IOException 
	 */
	public FatProcessor(String name, byte[] stream){
		try {
			RamDisk rd = RamDisk.read(stream); // Create in-memory representation of disk image
			image = FatFileSystem.read(rd, true); // Open as read-only to prevent accidental writes
		} catch (IOException e) {
		}

		type = image.getFatType().toString(); // Store type of FAT image
		root = new ContainerStream(null, name, false);

		/* Create list of files */
		FsDirectory fsRoot = image.getRoot();

		// Recursively add entries for each file inside the image
		addAll(root, fsRoot);
	}

	private void addAll(ContainerStream parentDir, FsDirectory fatDir){
		// Iterate through all entries in the current fs directory
		boolean readError = false;
		for(FsDirectoryEntry e: fatDir){
			// If it's a directory, recurse deeper
			if (e.isDirectory()) {
				// Recurse and parse files within the current directory
				try{
					fatDir = e.getDirectory();
				}catch(IOException i){
					readError = true;
				}

				ContainerStream curr = new ContainerStream(parentDir, e.getName(), readError);
				addAll(curr, fatDir);
			} else {
				// Retrieve chained representation of file in image
				FsFile fatFile = null;

				try{
					fatFile = e.getFile();
				}catch(IOException i){
					readError = true;
				}

				// Create ByteBuffer around contents of file. Todo: update so files with more than Integer.MAX_VALUE bytes are stored properly (possibly using a ByteBuffer array).
				ByteBuffer buff = ByteBuffer.allocate((int)fatFile.getLength());

				// Read data from image file into buffer
				try{
					fatFile.read(0, buff);
				}catch(IOException i){
					readError = true;
				}

				new DocumentStream(parentDir, e.getName(), buff, readError); // constructor handles adding to parent
			}
		}
	}

	public ContainerStream getRootStream(){
		return root;
	}

	/**
	 * Retrieve a {@code String} representation of the filesystem type this image represents.
	 * @return a {@code String} representation of this image's filesystem type
	 */

	public String getFatType(){
		return type;
	}
}
