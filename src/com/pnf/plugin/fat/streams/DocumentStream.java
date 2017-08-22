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

import java.nio.ByteBuffer;

/**
 * The Document-based implementation of a {@code Stream} object.
 * 
 * @author carlos
 *
 */
public class DocumentStream extends Stream {
    private ByteBuffer buff;
    private boolean processed;

    /**
     * Creates a new {@code DocumentStream} associated with the given byte data
     * 
     * @param parent parent {@code ContainerStream} to associate this {@code Stream} with
     * @param rawName the name of this stream
     * @param buff a {@code ByteBuffer} containing all data associated with this stream
     * @param readError a flag indicating whether an I/O error occurred during read operations
     */
    public DocumentStream(ContainerStream parent, String rawName, ByteBuffer buff, boolean readError) {
        super(parent, rawName, readError);
        this.buff = buff;
    }

    /**
     * Retrieves the data associated with this stream
     * 
     * @return a {@code ByteBuffer} containing the data stored within this stream
     */
    public ByteBuffer getBuffer() {
        return buff;
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int tabs) {
        StringBuffer tabsBuff = new StringBuffer(tabs + 1);
        for(int i = 0; i < tabs; i++)
            tabsBuff.append("  ");

        return tabsBuff.toString() + getRawName();
    }

    /**
     * Checks whether this stream has been processed already or not
     * 
     * @return true if this stream has been processed, false otherwise
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Sets the current processed state of this stream
     * 
     * @param isProcessed the new processed state of this stream
     */
    public void setProcessed(boolean isProcessed) {
        processed = isProcessed;
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public void addChild(Stream e) {
        return;
    }
}
