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

import de.neshanjo.rcswitch.server.data.Configuration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initially requests the background task resource.
 * 
 * @see BackgroundResource
 * 
 * @author Johannes Schneider
 */
@WebListener
public class BackgroundTaskStarter implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(BackgroundTaskStarter.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final Configuration config = Configuration.getInstance();

        if (config.getBackgroundTaskIntervalInSeconds() == -1) {
            LOG.info("Background task wake up deactivated");
        }
        LOG.info("Waking up background task in " + config.getBackgroundTaskWakeupDelayInSeconds() + "s");
        
        new Thread(() -> {
            try {
                Thread.sleep(config.getBackgroundTaskWakeupDelayInSeconds() * 1000);
            } catch (InterruptedException ex) {
                LOG.warn("inital wait interrupted");
            }
            final Client client = ClientBuilder.newClient();
            final Response response = client.target(config.getBackgroundTaskWakeupUrl())
                    .request().get();
            if (Response.Status.OK.getStatusCode() != response.getStatus()) {
                LOG.error("Could not wake up background task");
            }
        }).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
