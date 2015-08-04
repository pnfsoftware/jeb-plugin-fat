package com.pnf.streams;

import java.nio.ByteBuffer;

/**
 * The Document-based implementation of a {@code Stream} object.
 * @author carlos
 *
 */
public class DocumentStream extends Stream{
	private ByteBuffer buff;
	private boolean processed;

	/**
	 * Creates a new {@code DocumentStream} associated with the given byte data
	 * @param parent parent {@code ContainerStream} to associate this {@code Stream} with
	 * @param rawName the name of this stream
	 * @param buff a {@code ByteBuffer} containing all data associated with this stream
	 * @param readError a flag indicating whether an I/O error occurred during read operations
	 */
	public DocumentStream(ContainerStream parent, String rawName, ByteBuffer buff, boolean readError) {
		super(parent, rawName, readError);
		this.buff = buff;
	}

	/**
	 * Retrieves the data associated with this stream
	 * @return a {@code ByteBuffer} containing the data stored within this stream
	 */
	public ByteBuffer getBuffer(){
		return buff;
	}

	public String toString(){
		return toString(0);
	}

	public String toString(int tabs){
		StringBuffer tabsBuff = new StringBuffer(tabs+1);
		for(int i = 0; i < tabs; i++)
			tabsBuff.append("  ");

		return tabsBuff.toString() + getRawName();
	}

	/**
	 * Checks whether this stream has been processed already or not
	 * @return true if this stream has been processed, false otherwise
	 */
	public boolean isProcessed(){
		return processed;
	}

	/**
	 * Sets the current processed state of this stream
	 * @param isProcessed the new processed state of this stream
	 */
	public void setProcessed(boolean isProcessed){
		processed = isProcessed;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public void addChild(Stream e) {
		return;
	}
}
