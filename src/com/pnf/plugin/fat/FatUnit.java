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
import java.io.InputStream;

import com.pnf.plugin.fat.streams.ContainerStream;
import com.pnf.plugin.fat.streams.DocumentStream;
import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.input.BytesInput;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.core.units.impl.ContainerUnit;
import com.pnfsoftware.jeb.util.IO;

public class FatUnit extends AbstractBinaryUnit {
    private StringBuffer desc;
    private boolean descSet = false;

    public FatUnit(String name, IInput data, IUnitProcessor unitProcessor, IUnitCreator parent,
            IPropertyDefinitionManager pdm) {
        super(null, data, FatPlugin.ID, name, unitProcessor, parent, pdm);
    }

    public String getDescription() {
        // First add superclass data

        if(desc == null && descSet == false) {
            desc = new StringBuffer(super.getDescription());
            desc.append("\n");
        }

        return desc.toString();
    }

    public boolean process() {
        if(isProcessed()) {
            return true;
        }

        // Read the entire image into memory
        byte[] bytes = null;
        try(InputStream stream = getInput().getStream()) {
            bytes = IO.readInputStream(stream);
        }
        catch(IOException e) {
            FatPlugin.LOG.catching(e);
        }

        // Create a FatProcessor object that will handle interfacing with the
        // FAT library
        FatProcessor core = new FatProcessor(getName(), bytes);

        // Check if description is null
        if(desc == null)
            getDescription(); // Populate with data if it is

        // Add FAT specific info to description
        desc.append("Type := " + core.getFatType() + "\n");

        // Retrieve a list of the files stored within the image we are
        // processing
        IUnit root = getContainerFor(core.getRootStream(), this);
        addChildUnit(root);

        setProcessed(true);
        return true;
    }

    private ContainerUnit getContainerFor(ContainerStream current, IUnit parentUnit) {
        // Create a ContainerUnit for the current ContainerStream
        ContainerUnit currentUnit = new ContainerUnit(current.getRawName(), getUnitProcessor(), parentUnit,
                getPropertyDefinitionManager());

        // Traverse deeper into ContainerStream hierarchy and add ContainerUnits
        // as we go
        for(ContainerStream c: current.getContainerStreams()) {
            currentUnit.addChildUnit(getContainerFor(c, currentUnit));
        }

        // Add units for any DocumentStreams present at this level
        for(DocumentStream d: current.getDocumentStreams()) {
            currentUnit.addChildUnit(getUnitProcessor().process(d.getRawName(), new BytesInput(d.getBuffer().array()),
                    currentUnit));
        }

        // Return the top-level ContainerUnit
        return currentUnit;
    }
}
