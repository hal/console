/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta.security;

import java.util.Set;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.Settings;
import org.jboss.hal.config.Settings.Key;
import org.jboss.hal.config.User;
import org.jboss.hal.db.Document;
import org.jboss.hal.db.PouchDB;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AbstractDatabase;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;

import static java.util.stream.Collectors.joining;

import static org.jboss.hal.config.AccessControlProvider.RBAC;

public class SecurityContextDatabase extends AbstractDatabase<SecurityContext> {

    private static final String SECURITY_CONTEXT_TYPE = "security context";

    private final Environment environment;
    private final Settings settings;
    private String name;
    private PouchDB database;

    @Inject
    public SecurityContextDatabase(StatementContext statementContext, Environment environment, Settings settings) {
        super(new SecurityContextStatementContext(statementContext, environment), SECURITY_CONTEXT_TYPE);
        this.environment = environment;
        this.settings = settings;
    }

    @Override
    public String name() {
        if (name == null) {
            String roles;
            String provider = environment.getAccessControlProvider().name();
            if (environment.getAccessControlProvider() == RBAC) {
                Set<String> runAs = settings.get(Key.RUN_AS).asSet();
                if (runAs.isEmpty()) {
                    roles = User.current().getRoles().stream().map(Role::getId).collect(joining("-"));
                } else {
                    roles = String.join("-", runAs);
                }
            } else {
                roles = "";
            }
            name = Ids.build("hal-db-sc",
                    provider,
                    roles,
                    environment.getHalBuild().name(),
                    environment.getManagementVersion().toString());
        }
        return name;
    }

    @Override
    public SecurityContext asMetadata(Document document) {
        return new SecurityContext(ModelNode.fromBase64(document.getAsAny(PAYLOAD).asString()));
    }

    @Override
    public Document asDocument(ResourceAddress address, SecurityContext securityContext) {
        Document document = Document.of(address.toString());
        document.set(PAYLOAD, securityContext.toBase64String());
        return document;

    }

    @Override
    protected PouchDB database() {
        if (database == null) {
            database = new PouchDB(name());
        }
        return database;
    }
}
