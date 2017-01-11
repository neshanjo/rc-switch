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
import de.neshanjo.rcswitch.server.data.Event;
import de.neshanjo.rcswitch.server.data.Event_;
import de.neshanjo.rcswitch.server.data.Switch;
import de.neshanjo.rcswitch.server.gpio.SwitchControl;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Johannes Schneider
 */
public class EventTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(EventTask.class);

    private final EntityManagerFactory emf;
    private final SwitchControl switchControl;
    private Date lastRun;

    public EventTask(EntityManagerFactory emf, SwitchControl switchControl) {
        this.emf = emf;
        this.switchControl = switchControl;
        lastRun = new Date(System.currentTimeMillis()
                - Configuration.getInstance().getBackgroundTaskIntervalInSeconds() * 1000);
    }

    @Override
    public void run() {
        final EntityManager em = emf.createEntityManager();
        try {
            final Date currentRun = new Date();
            final CriteriaBuilder cb = em.getCriteriaBuilder();
            final CriteriaQuery<Event> query = cb.createQuery(Event.class);
            final Root<Event> event = query.from(Event.class);
            query.select(event)
                    .where(cb.between(event.get(Event_.executionTime), lastRun, currentRun))
                    .orderBy(cb.asc(event.get(Event_.executionTime)));
            final List<Event> eventsToExecute = em.createQuery(query).getResultList();
            lastRun = currentRun;

            if (eventsToExecute.isEmpty()) {
                LOG.debug("No event found to execute");
                return;
            }
            for (Event eventToExecute : eventsToExecute) {
                LOG.info("Executing event " + eventToExecute);
                final Optional<Switch> optionalSwitch
                        = Configuration.getInstance().getSwitches()
                        .stream()
                        .filter(sw -> sw.getName().equals(eventToExecute.getSwitchName()))
                        .findFirst();
                if (!optionalSwitch.isPresent()) {
                    LOG.error("Could not find switch with name " + eventToExecute.getSwitchName() + ". Deleting event.");
                    deleteEvent(em, eventToExecute);
                } else {
                    final Switch sw = optionalSwitch.get();
                    if (eventToExecute.isSwitchPosition()) {
                        switchControl.turnOn(sw.getGroup(), sw.getCode());
                    } else {
                        switchControl.turnOff(sw.getGroup(), sw.getCode());
                    }
                    if (eventToExecute.isRepeating()) {
                        LOG.info("Rescheduling repeating event");
                        eventToExecute.setExecutionTime(new Date(eventToExecute.getExecutionTime().getTime()
                                + 24 * 60 * 60 * 1000)); //add one day
                        updateEvent(em, eventToExecute);
                    } else {
                        deleteEvent(em, eventToExecute);
                    }
                }
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    private void deleteEvent(EntityManager em, Event event) {
        em.getTransaction().begin();
        em.remove(event);
        em.getTransaction().commit();
    }

    private void updateEvent(EntityManager em, Event event) {
        em.getTransaction().begin();
        em.merge(event);
        em.getTransaction().commit();
    }
}
