/*******************************************************************************
 * Copyright (c) 2015 PNF Software, Inc.
 *
 *     https://www.pnfsoftware.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.pnf.plugin.fat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.Version;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.logging.GlobalLog;
import com.pnfsoftware.jeb.util.logging.ILogger;

public class FatPlugin extends AbstractUnitIdentifier {
    static final String ID = "fat";
    private static final int[] FAT_BOOT_SIG = {(byte)0x55, (byte)0xAA};
    private static final int FAT_BOOT_SIG_OFFSET = 0x200 - FAT_BOOT_SIG.length;
    private static final int[] OBB_SIG = {(byte)0x83, (byte)0x99, (byte)0x05, (byte)0x01};

    public static ILogger LOG = GlobalLog.getLogger(FatPlugin.class);

    public FatPlugin() {
        super(ID, 0);
    }

    @Override
    public PluginInformation getPluginInformation() {
        // requires JEB 2.1
        return new PluginInformation("FAT Plugin", "Parser for FAT filesystem images", "PNF Software", Version.create(
                1, 0, 1), Version.create(2, 1), null);
    }

    public boolean canIdentify(IInput input, IUnitCreator unit) {
        if(input == null) {
            return false;
        }

        // First check to make sure that we are not parsing an OBB file (FAT
        // image with OBB footer)
        boolean isObb = true;

        try(SeekableByteChannel ch = input.getChannel()) {
            ch.position(ch.size() - OBB_SIG.length);
            ByteBuffer buff = ByteBuffer.allocate(OBB_SIG.length);
            ch.read(buff);
            isObb = checkBytes(buff.array(), 0, OBB_SIG);
        }
        catch(IOException | IllegalArgumentException e) {
            isObb = false;
        }

        // Then check for FAT boot sector signature
        return !isObb && checkBytes(input, FAT_BOOT_SIG_OFFSET, FAT_BOOT_SIG);
    }

    @Override
    public IUnit prepare(String name, IInput data, IUnitProcessor processor, IUnitCreator unit) {
        IUnit fatUnit = new FatUnit(name, data, processor, unit, getPropertyDefinitionManager());
        fatUnit.process();
        return fatUnit;
    }
}
