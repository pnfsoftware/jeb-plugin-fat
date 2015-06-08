package com.pnf;

import java.util.List;

import com.pnf.FatCore.FileOutputEntry;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class FatUnit extends AbstractBinaryUnit {
	private static final String FILE_TYPE = "fat_image";
	
	public FatUnit(String name, byte[] data, IUnitProcessor unitProcessor, IUnit parent, IPropertyDefinitionManager pdm){
		super(null, data, FILE_TYPE, name, unitProcessor, parent, pdm);
	}
	
	public boolean process(){
		FatCore core = new FatCore(getBytes());
		List<FileOutputEntry> files = core.getStoredFiles();
		
		for(FileOutputEntry f: files){
			IUnit child = getUnitProcessor().process(f.getFile().getName(), f.getBuffer().array(), this);
			getChildren().add(child);
		}
		
		return true;
	}
}
