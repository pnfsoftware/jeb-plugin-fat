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
package com.pnf.plugin.fat;

import com.pnfsoftware.jeb.core.units.NotificationType;
import com.pnfsoftware.jeb.core.units.UnitNotification;

/**
 * Simple class that handles Notifications and abstracts the API to make notification-handling
 * easier
 * 
 * @author carlos
 *
 */
public class Message extends UnitNotification {
    public static final int CORRUPT = 0;
    public static final int AREA_OF_INTEREST = 1;
    public static final int UNSUPPORTED = 2;
    public static final int WARNING = 3;

    private String message;
    private String address;
    private int type;

    /**
     * Creates a new {@code Message} object with the given values
     * 
     * @param message the text message that this {@code Message} object should contain
     * @param address a {@code String} version of the address, if any, that this {@code Message}
     *            object is associated with
     * @param type the type of {@code Message} to create
     */

    public Message(String message, String address, int type) {
        super(null, null);

        this.message = message;
        this.address = address;
        this.type = type;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getDescription() {
        return message;
    }

    @Override
    public NotificationType getType() {
        switch(type) {
        case CORRUPT:
            return NotificationType.CORRUPTION;
        case AREA_OF_INTEREST:
            return NotificationType.AREA_OF_INTEREST;
        case UNSUPPORTED:
            return NotificationType.UNSUPPORTED_FEATURE;
        case WARNING:
            return NotificationType.WARNING;
        default:
            break;
        }

        return null; // should never reach here
    }
}
