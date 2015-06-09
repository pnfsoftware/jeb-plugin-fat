package com.pnf;

import java.util.List;

import com.pnf.FatProcessor.ImageFileEntry;
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
		// Create a FatProcessor object that will handle interfacing with the FAT library
		FatProcessor core = new FatProcessor(getBytes());

		// Retrieve a list of the files stored within the image we are processing
		List<ImageFileEntry> files = core.getStoredFiles();

		// Iterate through files in list of entries and delegate to respective subunits
		for(ImageFileEntry f: files){
			// Create new subunit child
			IUnit child = getUnitProcessor().process(f.getFile().getName(), f.getBuffer().array(), this);

			// Add newly created subunit to list of children
			getChildren().add(child);
		}

		return true;
	}
}
