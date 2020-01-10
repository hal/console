/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core;

import java.util.EnumMap;
import java.util.Map;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.configuration.ProfileSelectionEvent;
import org.jboss.hal.core.configuration.ProfileSelectionEvent.ProfileSelectionHandler;
import org.jboss.hal.core.runtime.group.ServerGroupSelectionEvent;
import org.jboss.hal.core.runtime.group.ServerGroupSelectionEvent.ServerGroupSelectionHandler;
import org.jboss.hal.core.runtime.host.HostSelectionEvent;
import org.jboss.hal.core.runtime.host.HostSelectionEvent.HostSelectionHandler;
import org.jboss.hal.core.runtime.server.ServerSelectionEvent;
import org.jboss.hal.core.runtime.server.ServerSelectionEvent.ServerSelectionHandler;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.meta.StatementContext.Expression.*;

public class CoreStatementContext implements StatementContext,
        ProfileSelectionHandler, ServerGroupSelectionHandler, HostSelectionHandler, ServerSelectionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CoreStatementContext.class);

    private final Environment environment;
    private final Map<Expression, String> context;

    @Inject
    public CoreStatementContext(Environment environment, EventBus eventBus) {
        this.environment = environment;

        context = new EnumMap<>(Expression.class);
        context.put(DOMAIN_CONTROLLER, null);
        context.put(SELECTED_PROFILE, null);
        context.put(SELECTED_GROUP, null);
        context.put(SELECTED_HOST, null);
        context.put(SELECTED_SERVER_CONFIG, null);
        context.put(SELECTED_SERVER, null);

        eventBus.addHandler(ProfileSelectionEvent.getType(), this);
        eventBus.addHandler(ServerGroupSelectionEvent.getType(), this);
        eventBus.addHandler(HostSelectionEvent.getType(), this);
        eventBus.addHandler(ServerSelectionEvent.getType(), this);
    }

    public String resolve(String resource, AddressTemplate template) {
        // not supported
        return null;
    }

    @Override
    public String[] resolveTuple(String placeholder, AddressTemplate template) {
        if (!environment.isStandalone()) {
            Expression validExpression = Expression.from(placeholder);
            if (validExpression != null) {
                if (validExpression == DOMAIN_CONTROLLER) {
                    return new String[]{validExpression.resource(), environment.getDomainController()};
                } else if (context.containsKey(validExpression)) {
                    String value = context.get(validExpression);
                    if (value != null) {
                        return new String[]{validExpression.resource(), value};
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void onProfileSelection(ProfileSelectionEvent event) {
        context.put(SELECTED_PROFILE, event.getProfile());
        logger.info("Selected profile {}", event.getProfile());
    }

    @Override
    public void onServerGroupSelection(ServerGroupSelectionEvent event) {
        context.put(SELECTED_GROUP, event.getServerGroup());
        logger.info("Selected server-group {}", event.getServerGroup());
    }

    @Override
    public void onHostSelection(HostSelectionEvent event) {
        context.put(SELECTED_HOST, event.getHost());
        logger.info("Selected host {}", event.getHost());
    }

    @Override
    public void onServerSelection(ServerSelectionEvent event) {
        context.put(SELECTED_SERVER_CONFIG, event.getServer());
        context.put(SELECTED_SERVER, event.getServer());
        logger.info("Selected server {}", event.getServer());
    }

    @Override
    public String domainController() {
        return environment.getDomainController();
    }

    @Override
    public String selectedProfile() {
        return environment.isStandalone() ? null : context.get(SELECTED_PROFILE);
    }

    @Override
    public String selectedServerGroup() {
        return environment.isStandalone() ? null : context.get(SELECTED_GROUP);
    }

    @Override
    public String selectedHost() {
        return environment.isStandalone() ? null : context.get(SELECTED_HOST);
    }

    @Override
    public String selectedServerConfig() {
        return context.get(SELECTED_SERVER_CONFIG);
    }

    @Override
    public String selectedServer() {
        return context.get(SELECTED_SERVER);
    }
}
