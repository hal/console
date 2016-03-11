/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
