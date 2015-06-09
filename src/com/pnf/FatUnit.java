package com.pnf;

import java.util.List;

import com.pnf.FatProcessor.ImageFileEntry;
import com.pnfsoftware.jeb.core.events.J;
import com.pnfsoftware.jeb.core.events.JebEvent;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class FatUnit extends AbstractBinaryUnit {
	private static final String FILE_TYPE = "fat_image";
	private StringBuffer desc;
	private boolean descSet = false;

	public FatUnit(String name, byte[] data, IUnitProcessor unitProcessor, IUnit parent, IPropertyDefinitionManager pdm){
		super(null, data, FILE_TYPE, name, unitProcessor, parent, pdm);
	}

	public String getDescription(){
		// First add superclass data

		if(desc == null && descSet == false){
			desc = new StringBuffer(super.getDescription());
			desc.append("\n");
		}

		return desc.toString();
	}

	public boolean process(){
		// Create a FatProcessor object that will handle interfacing with the FAT library
		FatProcessor core = new FatProcessor(getBytes());

		// Check if description is null
		if(desc == null)
			getDescription(); // Populate with data if it is

		// Add FAT specific info to description
		desc.append("Type := " + core.getFatType() + "\n");


		// Retrieve a list of the files stored within the image we are processing
		List<ImageFileEntry> files = core.getStoredFiles();

		// Iterate through files in list of entries and delegate to respective subunits
		for(ImageFileEntry f: files){
			// Create new subunit child
			IUnit child = getUnitProcessor().process(f.getFile().getName(), f.getBuffer().array(), this);

			// Add newly created subunit to list of children
			getChildren().add(child);
		}

		// Throw unit change event
		notifyListeners(new JebEvent(J.UnitChange));

		return true;
	}
}
