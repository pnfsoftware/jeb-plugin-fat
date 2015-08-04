package com.pnf.streams;

import java.util.ArrayList;
import java.util.List;

public class ContainerStream extends Stream {
	protected List<DocumentStream> documentStreams;
	protected List<ContainerStream> containerStreams;

	public ContainerStream(ContainerStream parent, String rawName, boolean readError) {
		super(parent, rawName, readError);

		documentStreams = new ArrayList<>();
		containerStreams = new ArrayList<>();
	}

	public List<DocumentStream> getDocumentStreams() {
		return documentStreams;
	}

	public List<ContainerStream> getContainerStreams() {
		return containerStreams;
	}

	public String toString(){
		return toString(0);
	}

	public String toString(int tabs){
		StringBuffer tabsBuff = new StringBuffer(tabs+1);
		String tab;

		String nl = "\n";

		for(int i = 0; i < tabs; i++)
			tabsBuff.append("  ");

		tab = tabsBuff.toString();

		StringBuffer buff = new StringBuffer();
		buff.append(tab + getRawName() + nl);

		for(Stream s: containerStreams){
			buff.append(tab + s.toString(tabs+1) + nl);
		}

		for(Stream s: documentStreams)
			buff.append(tab + s.toString(tabs+1) + nl);

		buff.setCharAt(buff.length()-1, '\0');
		return buff.toString();
	}

	public void addChild(Stream e){
		if(e.isContainer())
			containerStreams.add((ContainerStream)e);
		else
			documentStreams.add((DocumentStream)e);
	}

	public boolean isContainer() {
		return true;
	}
}