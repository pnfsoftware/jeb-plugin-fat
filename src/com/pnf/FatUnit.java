package com.pnf;

import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class FatUnit extends AbstractBinaryUnit {
	private static final String FILE_TYPE = "fat_image";
	
	public FatUnit(String name, byte[] data, IUnitProcessor unitProcessor, IUnit parent, IPropertyDefinitionManager pdm){
		super(null, data, FILE_TYPE, name, unitProcessor, parent, pdm);
	}
}
