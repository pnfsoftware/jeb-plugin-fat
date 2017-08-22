/*******************************************************************************
 * Copyright (c) 2015-2017 PNF Software, Inc.
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
package com.pnf.plugin.fat.streams;

import java.util.ArrayList;
import java.util.List;

public class ContainerStream extends Stream {
    protected List<DocumentStream> documentStreams;
    protected List<ContainerStream> containerStreams;

    /**
     * Creates a new {@code ContainerStream} object
     * 
     * @param parent the parent {@code ContainerStream} to associate this stream with
     * @param rawName the name of this stream
     * @param readError a flag indicating whether an I/O error occurred while attempting to read
     *            this stream
     */
    public ContainerStream(ContainerStream parent, String rawName, boolean readError) {
        super(parent, rawName, readError);

        documentStreams = new ArrayList<>();
        containerStreams = new ArrayList<>();
    }

    /**
     * Retrieves the {@code DocumentStream}s nested within this object
     * 
     * @return a {@code List} of {@code DocumentStream} object
     */
    public List<DocumentStream> getDocumentStreams() {
        return documentStreams;
    }

    /**
     * Retrieves the {@code ContainerStream}s nested within this object
     * 
     * @return a {@code List} of {@code ContainerStream} object
     */
    public List<ContainerStream> getContainerStreams() {
        return containerStreams;
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int tabs) {
        StringBuffer tabsBuff = new StringBuffer(tabs + 1);
        String tab;

        String nl = "\n";

        for(int i = 0; i < tabs; i++)
            tabsBuff.append("  ");

        tab = tabsBuff.toString();

        StringBuffer buff = new StringBuffer();
        buff.append(tab + getRawName() + nl);

        for(Stream s: containerStreams) {
            buff.append(tab + s.toString(tabs + 1) + nl);
        }

        for(Stream s: documentStreams)
            buff.append(tab + s.toString(tabs + 1) + nl);

        buff.setCharAt(buff.length() - 1, '\0');
        return buff.toString();
    }

    public void addChild(Stream e) {
        if(e.isContainer())
            containerStreams.add((ContainerStream)e);
        else
            documentStreams.add((DocumentStream)e);
    }

    public boolean isContainer() {
        return true;
    }
}