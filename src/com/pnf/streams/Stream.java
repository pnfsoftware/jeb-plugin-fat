package com.pnf.streams;

import java.util.List;

import com.pnf.Message;
import com.pnf.MessageHandler;
import com.pnfsoftware.jeb.core.units.UnitNotification;

/**
 * An abstraction of a storage stream. Can be a document containing data, or simply a container for other {@code Stream}s. 
 * @author carlos
 *
 */
public abstract class Stream implements MessageHandler{
	private String rawName;
	private List<UnitNotification> notif;
	private boolean readError;

	/**
	 * Creates a new {@code Stream} object
	 * @param parent the {@code ContainerStream} that this object is nested under
	 * @param rawName the raw name of this stream
	 * @param readError flag indicating whether an I/O error occurred while attempting to read this stream's data
	 */
	public Stream(ContainerStream parent, String rawName, boolean readError){
		this.rawName = rawName;
		this.readError = readError;

		if(parent != null)
			parent.addChild(this);

		if(readError)
			handleMessage("Error while reading byte data", "", Message.CORRUPT);
	}

	/**
	 * Whether there was an I/O error while reading this stream's contents or not
	 * @return true if an I/O error occurred during read operations, false otherwise
	 */
	public boolean hadReadError(){
		return readError;
	}

	/**
	 * Retrieves this {@code Stream}s name
	 * @return a {@code String} with this {@code Stream}s name 
	 */
	public String getRawName(){
		return rawName;
	}

	public int hashCode(){
		return getRawName().hashCode();
	}

	/**
	 * Convenience method for {@code handleMessage(Message m)}
	 */
	public void handleMessage(String message, String address, int type){
		handleMessage(new Message(message, address, type));
	}

	public void handleMessage(Message m){
		notif.add(m);
	}

	/**
	 * Retrieves the list of a notifications associated with this stream
	 * @return a {@code List} of {@code UnitNotification} objects
	 */
	public List<UnitNotification> getNotifications(){
		return notif;
	}

	public abstract String toString();
	
	/**
	 * Returns a String representation of this object, prepended with the given number of tabs
	 * @param tabs
	 * @return
	 */
	public abstract String toString(int tabs);

	/**
	 * Checks whether this {@code Stream} is a container or not.
	 * @return true if this {@code Stream} is a container, false otherwise
	 */
	public abstract boolean isContainer();
	
	/**
	 * Adds a child {@code Stream} to the current object
	 * @param e the child {@code Stream} to add
	 */
	public abstract void addChild(Stream e);
}
