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
package de.neshanjo.rcswitch.server.data;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Johannes Schneider
 */
@Data
public class Combination {

    @Data
    public static class SwitchOperation {

        private String name;
        private boolean position;
    }

    private String name;
    private List<SwitchOperation> switchOperations = new ArrayList<>();

}
