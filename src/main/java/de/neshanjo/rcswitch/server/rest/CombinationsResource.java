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

import de.neshanjo.rcswitch.server.data.Combination;
import de.neshanjo.rcswitch.server.data.Configuration;
import de.neshanjo.rcswitch.server.data.Switch;
import de.neshanjo.rcswitch.server.data.Switch.DisplayTab;
import de.neshanjo.rcswitch.server.gpio.SwitchControl;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Johannes C. Schneider
 */
@Path("/combos")
public class CombinationsResource {
    
    @Inject
    private SwitchControl switchControl;
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<String> getCombos() {
        return Configuration.getInstance().getCombinations()
                .stream()
                .map(Combination::getName)
                .collect(toList());
    }
    
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response fire(@PathParam("name") String comboName) {
        final Configuration config = Configuration.getInstance();
        final Combination combo = config.getCombinations().stream()
                .filter(c -> comboName.equals(c.getName()))
                .findAny()
                .orElseThrow(() -> new NotFoundException());
        for (Combination.SwitchOperation op : combo.getSwitchOperations()) {
            final Switch mySwitch = config.getSwitches().stream()
                            .filter(c -> op.getName().equals(c.getName()))
                            .findAny()
                            .orElseThrow(IllegalStateException::new);
            if (op.isPosition()) {
                switchControl.turnOn(mySwitch.getGroup(), mySwitch.getCode());
            } else {
                switchControl.turnOff(mySwitch.getGroup(), mySwitch.getCode());
            }
        }
        return Response.noContent().status(Response.Status.NO_CONTENT).build();
    }
}
