package com.pnf;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.IO;

public class FatUnit extends AbstractBinaryUnit {
	private static final String FILE_TYPE = "fat_image";
	
	public FatUnit(String name, byte[] data, IUnitProcessor unitProcessor, IUnit parent, IPropertyDefinitionManager pdm){
		super(null, data, FILE_TYPE, name, unitProcessor, parent, pdm);
	}
	
	public boolean process(){
		File tempDir = null;
		try {
			tempDir = IO.createTempFolder(FILE_TYPE + "_data");
		} catch (IOException e) {
			throw new RuntimeException("Failed to create temporary directory in fat_plugin");
		}
		
		FatCore core = new FatCore(getBytes(), tempDir);
		List<FileOutputEntry> files = core.getStoredFiles();
		
		for(FileOutputEntry f: files){
			IUnit child = getUnitProcessor().process(f.getFile().getName(), f.getBuffer().array(), this);
			getChildren().add(child);
		}
		
		return true;
	}
}
