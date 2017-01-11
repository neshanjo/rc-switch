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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.server.CloseableService;

public class EmFactory implements Factory<EntityManager> {

    private final EntityManagerFactory emf;
    private final CloseableService closeableService;

    @Inject
    public EmFactory(EntityManagerFactory emf,
            CloseableService closeableService) {
        this.emf = emf;
        this.closeableService = closeableService;
    }

    @Override
    public EntityManager provide() {
        final EntityManager entityManager = emf.createEntityManager();
        closeableService.add(() -> {
            if(entityManager.isOpen()) {
                entityManager.close();
            }
        });
        return entityManager;
    }

    @Override
    public void dispose(EntityManager entityManager) {
        if(entityManager.isOpen()) {
            entityManager.close();
        }
    }
}