/*
 * Copyright (C) 2016 Johannes C. Schneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.neshanjo.rcswitch.server.gpio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Johannes C. Schneider
 */
public class DummySwitchControl implements SwitchControl {

    private static final Logger LOG = LoggerFactory.getLogger(DummySwitchControl.class);
    
    @Override
    public void turnOn(String group, int switchId) {
        LOG.debug("Turning on switch " + group + ":" + switchId);
    }

    @Override
    public void turnOff(String group, int switchId) {
        LOG.debug("Turning off switch " + group + ":" + switchId);
    }
    
}
