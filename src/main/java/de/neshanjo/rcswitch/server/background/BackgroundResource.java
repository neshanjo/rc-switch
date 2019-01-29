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
package de.neshanjo.rcswitch.server.background;

import java.util.Timer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.neshanjo.rcswitch.server.data.Configuration;
import de.neshanjo.rcswitch.server.gpio.SwitchControl;

/**
 * This resource is used to start (and stop) background tasks. It would be much better to do this in an
 * {@link javax.servlet.ServletContextListener}, but unfortunately, the hk2 dependency injection framework bundled with
 * jersey is not able to inject resources into a ServletContextListener.
 *
 * @see BackgroundTaskStarter
 *
 * @author Johannes Schneider
 */
@Singleton
@Path("/wakeup")
public class BackgroundResource {

    private static final Logger LOG = LoggerFactory.getLogger(BackgroundResource.class);

    private Timer timer;

    @Inject
    private EntityManagerFactory emf;
    @Inject
    private SwitchControl switchControl;

    @PostConstruct
    public void init() {
        LOG.info("Starting background tasks");
        timer = new Timer();
        timer.schedule(new EventTask(emf, switchControl), 0,
                Configuration.getInstance().getBackgroundTaskIntervalInSeconds() * 1000);
        new Thread(new SqsPollingWorker(switchControl)).start();
    }

    @PreDestroy
    public void contextDestroyed() {
        LOG.info("Stopping background tasks");
        timer.cancel();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getStatus() {
        return "ok";
    }

}
