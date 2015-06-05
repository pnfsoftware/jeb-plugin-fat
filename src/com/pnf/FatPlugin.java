package com.pnf;

import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.properties.IPropertyManager;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IBinaryFrames;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class FatPlugin extends AbstractUnitIdentifier{
	private static String ID = "fat_plugin";
	private static final int[] FAT_BOOT_SIG = {(byte) 0x55, (byte) 0xAA};
	private static final int FAT_BOOT_SIG_OFFSET = (byte) 0x200;
	
	public FatPlugin() {
		super(ID, 0);
	}

	public boolean identify(byte[] stream, IUnit unit) {
		boolean check = checkBytes(stream, FAT_BOOT_SIG_OFFSET, FAT_BOOT_SIG); // First check for FAT boot sector signature
		
		if(check){
			Utils.LOG.info("%s", "Identified FAT file.");
		}
		
		return check;
	}
	
	public void initialize(IPropertyDefinitionManager parent, IPropertyManager pm) {
        super.initialize(parent, pm);
        /** Add any necessary property definitions here **/
    }

	@Override
	public IUnit prepare(String name, byte[] data, IUnitProcessor processor, IUnit unit) {
		return null;
	}

	@Override
	public PluginInformation getPluginInformation() {
		return new PluginInformation("FAT12 Plugin", "", "0.1", "PNF Software");
	}

	@Override
	public IUnit reload(IBinaryFrames data, IUnitProcessor processor, IUnit unit) {
		return null;
	}
}