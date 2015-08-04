package com.pnf.streams;

import java.nio.ByteBuffer;

import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class DocumentStream extends Stream{
	private ByteBuffer buff;
	private boolean processed;

	public DocumentStream(ContainerStream parent, String rawName, ByteBuffer buff, boolean readError) {
		super(parent, rawName, readError);

		this.buff = buff;
	}

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

	public boolean isProcessed(){
		return processed;
	}

	public void setProcessed(boolean proc){
		processed = proc;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public void addChild(Stream e) {
		return;
	}

	public IUnit getDisplayUnit(IUnitProcessor iup, IUnit parent, IPropertyDefinitionManager pdm){
		return null;
	}
}
