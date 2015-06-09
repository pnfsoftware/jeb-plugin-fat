package com.pnf;

import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.properties.IPropertyManager;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IBinaryFrames;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.logging.GlobalLog;
import com.pnfsoftware.jeb.util.logging.ILogger;

public class FatPlugin extends AbstractUnitIdentifier{
	private static final String ID = "fat_plugin";
	private static final int[] FAT_BOOT_SIG = {(byte) 0x55, (byte) 0xAA};
	private static final int FAT_BOOT_SIG_OFFSET = 0x200 - FAT_BOOT_SIG.length;
	private static final int[] OBB_SIG = {(byte) 0x83, (byte) 0x99, (byte) 0x05, (byte) 0x01};

	public static ILogger LOG = GlobalLog.getLogger(FatPlugin.class);

	public FatPlugin() {
		super(ID, 0);
	}

	public boolean identify(byte[] stream, IUnit unit) {
		
		return !checkBytes(stream, stream.length - OBB_SIG.length, OBB_SIG) // First check to make sure that we are not parsing an OBB file (FAT image with OBB footer)
				&& checkBytes(stream, FAT_BOOT_SIG_OFFSET, FAT_BOOT_SIG); // Then check for FAT boot sector signature
	}

	public void initialize(IPropertyDefinitionManager parent, IPropertyManager pm) {
		super.initialize(parent, pm);
		/** Add any necessary property definitions here **/
	}

	@Override
	public IUnit prepare(String name, byte[] data, IUnitProcessor processor, IUnit unit) {
		IUnit fatUnit = new FatUnit(name, data, processor, unit, getPropertyDefinitionManager());
		fatUnit.process();
		return fatUnit;
	}

	@Override
	public PluginInformation getPluginInformation() {
		return new PluginInformation("FAT Plugin", "", "1.0", "PNF Software");
	}

	@Override
	public IUnit reload(IBinaryFrames data, IUnitProcessor processor, IUnit unit) {
		return null;
	}
}
