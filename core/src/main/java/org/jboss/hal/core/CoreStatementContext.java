/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.core;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.ProfileSelectionEvent.ProfileSelectionHandler;
import org.jboss.hal.meta.StatementContext;

import javax.inject.Inject;
import java.util.EnumMap;
import java.util.Map;

import static org.jboss.hal.meta.StatementContext.Key.*;

/**
 * @author Harald Pehl
 */
public class CoreStatementContext implements StatementContext, ProfileSelectionHandler {

    private final Environment environment;
    private final Map<Key, String> context;

    @Inject
    public CoreStatementContext(Environment environment, EventBus eventBus) {
        this.environment = environment;

        context = new EnumMap<>(Key.class);
        context.put(ANY_PROFILE, "*");
        context.put(ANY_GROUP, "*");
        context.put(SELECTED_PROFILE, null);
        context.put(SELECTED_GROUP, null);
        context.put(SELECTED_HOST, null);
        context.put(SELECTED_SERVER, null);

        eventBus.addHandler(ProfileSelectionEvent.getType(), this);
    }

    public String resolve(final String key) {
        // not supported
        return null;
    }

    @Override
    public String[] resolveTuple(final String key) {
        if (!environment.isStandalone()) {
            Key validKey = Key.fromKey(key);
            if (validKey != null && context.containsKey(validKey)) {
                String value = context.get(validKey);
                if (value != null) {
                    return new String[]{validKey.resource(), value};
                }
            }
        }
        return null;
    }

    @Override
    public String selectedProfile() {
        return environment.isStandalone() ? null : context.get(SELECTED_PROFILE);
    }

    @Override
    public void onProfileSelected(final ProfileSelectionEvent event) {
        context.put(SELECTED_PROFILE, event.getProfile());
    }
}
