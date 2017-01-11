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
package de.neshanjo.rcswitch.server;

import de.neshanjo.rcswitch.server.gpio.DummySwitchControl;
import de.neshanjo.rcswitch.server.gpio.GpioSwitchControl;
import de.neshanjo.rcswitch.server.gpio.SwitchControl;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Johannes C. Schneider
 */
public class RcSwitchApplication extends ResourceConfig {
    
    private static final Logger LOG = LoggerFactory.getLogger(RcSwitchApplication.class);
    
    private static final String ENV_TEST = "test";
    private static final String ENVIRONMENT_CONFIG = "rcSwitch.environment";
    
    private final boolean testMode;
    
    public RcSwitchApplication() {
    
        testMode = ENV_TEST.equalsIgnoreCase(System.getProperty(ENVIRONMENT_CONFIG));
        if (testMode) {
            LOG.info("Running in test mode");
        } else {
            LOG.info("Running in production mode");
        }
        
        register(new AbstractBinder() {

            @Override
            public void configure() {
                bindFactory(EmfFactory.class).to(EntityManagerFactory.class).in(Singleton.class);
                bindFactory(EmFactory.class).to(EntityManager.class).in(RequestScoped.class);
                if (testMode) {
                    bind(DummySwitchControl.class).to(SwitchControl.class).in(Singleton.class);
                } else {
                    bind(GpioSwitchControl.class).to(SwitchControl.class).in(Singleton.class);
                }
            }
        });
    }

}
