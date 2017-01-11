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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Johannes Schneider
 */
@Data
public class Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private static volatile Configuration LAZY_CONFIGURATION;

    public static Configuration getInstance() {
        Configuration config = LAZY_CONFIGURATION;
        if (config == null) {
            synchronized (Configuration.class) {
                config = LAZY_CONFIGURATION;
                if (config == null) {
                    final ObjectMapper om = new ObjectMapper();
                    try {
                        config = om.readValue(Configuration.class.getResourceAsStream("/configuration.json"),
                                Configuration.class);
                        LOG.info("Loaded " + config);
                        verifyConfig(config);
                        LAZY_CONFIGURATION = config;
                    } catch (IOException ex) {
                        throw new IllegalStateException("Could not load config", ex);
                    }
                }
            }
        }
        return LAZY_CONFIGURATION;
    }

    private static void verifyConfig(Configuration config) {
        if (config.getBackgroundTaskWakeupUrl() == null || config.getBackgroundTaskWakeupUrl().isEmpty()) {
            throw new IllegalArgumentException("backgroundTaskWakeupUrl must not be null or empty");
        }
        verifySwitchList(config.getSwitches());
        verifyCombinations(config.getSwitches().stream().map(Switch::getName).collect(toSet()), config.getCombinations());
    }

    private static void verifySwitchList(List<Switch> switches) {
        switches.forEach(s -> {
            Objects.requireNonNull(s);
            Objects.requireNonNull(s.getName(), "switch name must not be null");
            Objects.requireNonNull(s.getGroup(), "switch group must not be null");
            Objects.requireNonNull(s.getDisplayTab(), "switch displayTab must not be null");
            if (s.getCode() < 1 || s.getCode() > 5) {
                throw new IllegalArgumentException("switch code must be a value from 1 to 5");
            }
            if (switches.stream().filter(other -> s.getName().equals(other.getName())).count() > 1) {
                throw new IllegalArgumentException("Switch names must be unique. Found duplicate name " + s.getName());
            }
        });
    }

    private static void verifyCombinations(final Set<String> switchNames, List<Combination> combinations) {
        combinations.forEach(comb -> {
            Objects.requireNonNull(comb.getName(), "combination name must not be null");
            if (combinations.stream().filter(other -> comb.getName().equals(other.getName())).count() > 1) {
                throw new IllegalArgumentException("Combination names must be unique. Found duplicate name " + comb
                        .getName());
            }
            if (!comb.getSwitchOperations().stream()
                    .allMatch(op -> switchNames.contains(op.getName()))) {
                throw new IllegalArgumentException("Found switch name in combinations configuration that has not"
                        + " been defined in the switches configuration");
            }
        });
    }

    //Properties
    private String backgroundTaskWakeupUrl;
    private int backgroundTaskWakeupDelayInSeconds = 30;
    private int backgroundTaskIntervalInSeconds = 10;

    private List<Switch> switches = new ArrayList<>();
    private List<Combination> combinations = new ArrayList<>();

}
