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

import com.pnf.plugin.fat.streams.ContainerStream;
import com.pnf.plugin.fat.streams.DocumentStream;

import de.waldheinz.fs.FsDirectory;
import de.waldheinz.fs.FsDirectoryEntry;
import de.waldheinz.fs.FsFile;
import de.waldheinz.fs.fat.FatFileSystem;
import de.waldheinz.fs.util.RamDisk;

public class FatProcessor {
    private FatFileSystem image;
    private String type;
    private ContainerStream root;

    /**
     * Creates a new {@code FatCore} object from the given byte stream.
     * 
     * <p>
     * <i>Note: Calling this constructor <b>will not</b> extract any files from
     * the image bytes. For extracting the files, see {@link dumpFiles}.</i>
     * </p>
     * 
     * @param stream
     *            byte array representation of a FAT image
     * @throws IOException
     */
    public FatProcessor(String name, byte[] stream) {
        try {
            // Create in-memory representation of disk image
            RamDisk rd = RamDisk.read(stream);

            // Open as read-only to prevent accidental writes
            image = FatFileSystem.read(rd, true);
        }
        catch(IOException e) {
        }

        // Store type of FAT image
        type = image.getFatType().toString();
        root = new ContainerStream(null, name, false);

        /* Create list of files */
        FsDirectory fsRoot = image.getRoot();

        // Recursively add entries for each file inside the image
        addAll(root, fsRoot);
    }

    private void addAll(ContainerStream parentDir, FsDirectory fatDir) {
        // Iterate through all entries in the current fs directory
        boolean readError = false;
        for(FsDirectoryEntry e : fatDir) {
            // If it's a directory, recurse deeper
            if(e.isDirectory()) {
                // Recurse and parse files within the current directory
                try {
                    fatDir = e.getDirectory();
                }
                catch(IOException i) {
                    readError = true;
                }

                ContainerStream curr = new ContainerStream(parentDir, e.getName(), readError);
                addAll(curr, fatDir);
            }
            else {
                // Retrieve chained representation of file in image
                FsFile fatFile = null;

                try {
                    fatFile = e.getFile();
                }
                catch(IOException i) {
                    readError = true;
                }

                // Create ByteBuffer around contents of file. Todo: update so
                // files with more than Integer.MAX_VALUE bytes are stored
                // properly (possibly using a ByteBuffer array).
                ByteBuffer buff = ByteBuffer.allocate((int)fatFile.getLength());

                // Read data from image file into buffer
                try {
                    fatFile.read(0, buff);
                }
                catch(IOException i) {
                    readError = true;
                }

                // constructor handles adding to parent
                new DocumentStream(parentDir, e.getName(), buff, readError);
            }
        }
    }

    /**
     * Returns a {@code ContainerStream} representing the entire FAT storage
     * 
     * @return a {@code ContainerStream} object
     */

    public ContainerStream getRootStream() {
        return root;
    }

    /**
     * Retrieve a {@code String} representation of the filesystem type this
     * image represents.
     * 
     * @return a {@code String} representation of this image's filesystem type
     */

    public String getFatType() {
        return type;
    }
}
