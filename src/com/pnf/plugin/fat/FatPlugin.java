package com.pnf.plugin.fat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.Version;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
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

	public boolean canIdentify(IInput stream, IUnitCreator unit) {
		// First check to make sure that we are not parsing an OBB file (FAT image with OBB footer)
		boolean isObb = true;

		try (SeekableByteChannel ch = stream.getChannel()){
			ch.position(ch.size() - OBB_SIG.length);
			ByteBuffer buff = ByteBuffer.allocate(OBB_SIG.length);
			ch.read(buff);
			isObb = checkBytes(buff, 0, OBB_SIG);
		} catch (IOException e) {
			isObb = false;
		}

		return !isObb && checkBytes(stream, FAT_BOOT_SIG_OFFSET, FAT_BOOT_SIG); // Then check for FAT boot sector signature
	}

	public void initialize(IPropertyDefinitionManager parent) {
		super.initialize(parent);
		/** Add any necessary property definitions here **/
	}

	@Override
	public IUnit prepare(String name, IInput data, IUnitProcessor processor, IUnitCreator unit) {
		IUnit fatUnit = new FatUnit(name, data, processor, unit, getPropertyDefinitionManager());
		fatUnit.process();
		return fatUnit;
	}

	@Override
	public PluginInformation getPluginInformation() {
		return new PluginInformation("FAT Plugin", "", "PNF Software", new Version(1,0,0));
	}
}
