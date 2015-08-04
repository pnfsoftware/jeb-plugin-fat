package com.pnf.streams;

import java.util.List;

import com.pnf.Message;
import com.pnf.MessageHandler;
import com.pnfsoftware.jeb.core.units.UnitNotification;


public abstract class Stream implements MessageHandler{
	private String rawName;
	private List<UnitNotification> notif;
	private boolean readError;

	public Stream(ContainerStream parent, String rawName, boolean readError){
		this.rawName = rawName;
		this.readError = readError;

		if(parent != null)
			parent.addChild(this);

		if(readError)
			handleMessage("Error while reading byte data", "", Message.CORRUPT);
	}

	public boolean hadReadError(){
		return readError;
	}

	public static String fixStreamName(String name, boolean doReplace){
		if(name == null)
			return null;

		StringBuffer buff = new StringBuffer(name.length() + 1);

		for(int i = 0; i < name.length(); i++){
			char c = name.charAt(i);
			if(!Character.isISOControl(c))
				buff.append(c);
			else if(doReplace){
				buff.append('[');
				buff.append(((int) c));
				buff.append(']');
			}
		}

		return buff.toString();
	}

	public String getRawName(){
		return rawName;
	}

	public int hashCode(){
		return getRawName().hashCode();
	}

	public void handleMessage(String message, String address, int type){
		handleMessage(new Message(message, address, type));
	}

	public void handleMessage(Message m){
		notif.add(m);
	}

	public List<UnitNotification> getNotifications(){
		return notif;
	}

	public abstract String toString();
	public abstract String toString(int tabs);

	public abstract boolean isContainer();
	public abstract void addChild(Stream e);
}
