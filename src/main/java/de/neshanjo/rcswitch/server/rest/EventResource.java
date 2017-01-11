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
package de.neshanjo.rcswitch.server.rest;

import de.neshanjo.rcswitch.server.data.Event;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Johannes C. Schneider
 */

public class EventResource {

    private final EntityManager em;
    private final Long id;

    public EventResource(EntityManager em, Long id) {
        Objects.requireNonNull(em, () -> "em must not be null");
        Objects.requireNonNull(id, () -> "id must not be null");
        this.em = em;
        this.id = id;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Event get() {
        final Event event = em.find(Event.class, id);
        if (event == null) {
            throw new NotFoundException("Event " + id + " not found");
        }
        return event;
    }
    
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete() {
        final Event event = em.find(Event.class, id);
        if (event == null) {
            throw new NotFoundException("Event " + id + " not found");
        }
        em.getTransaction().begin();
        em.remove(event);
        em.getTransaction().commit();
        return Response.noContent().status(Response.Status.NO_CONTENT).build();
    }
}
