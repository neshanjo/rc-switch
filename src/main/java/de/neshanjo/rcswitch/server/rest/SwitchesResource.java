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

import de.neshanjo.rcswitch.server.data.Configuration;
import de.neshanjo.rcswitch.server.data.Switch;
import de.neshanjo.rcswitch.server.data.Switch.DisplayTab;
import de.neshanjo.rcswitch.server.gpio.SwitchControl;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Johannes C. Schneider
 */
@Path("/switches")
public class SwitchesResource {
    
    public enum Operation {
        on, off
    }

    @Inject
    private SwitchControl switchControl;
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Switch> getSwitches(@QueryParam("displayTab") DisplayTab displayTab) {
        if (displayTab == null) {
            return Configuration.getInstance().getSwitches();
        }
        return Configuration.getInstance().getSwitches()
                .stream()
                .filter(s -> s.getDisplayTab() == displayTab)
                .collect(toList());
    }
    
    /**
     * 
     * @param group switch group (as coded in the remote control)
     * @param code code of the socket, A=1, B=2, ...
     * @param operation
     * @return 
     */
    @Path("{group}/{code}/{operation}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getEvent(@PathParam("group") String group, 
            @PathParam("code") int code,
            @PathParam("operation") Operation operation) {
        if (operation == Operation.on) {
            switchControl.turnOn(group, code);
        } else {
            switchControl.turnOff(group, code);
        }
        return Response.noContent().status(Response.Status.NO_CONTENT).build();
    }
}
