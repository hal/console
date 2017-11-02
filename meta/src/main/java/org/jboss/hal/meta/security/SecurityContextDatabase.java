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
package org.jboss.hal.meta.security;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.db.PouchDB;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AbstractDatabase;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

public class SecurityContextDatabase extends AbstractDatabase<SecurityContext> {

    private static final String SECURITY_CONTEXT_TYPE = "security context";
    @NonNls private static final Logger logger = LoggerFactory.getLogger(SecurityContextDatabase.class);

    private final Environment environment;
    private PouchDB<SecurityContextDocument> database;

    @Inject
    public SecurityContextDatabase(StatementContext statementContext, Environment environment) {
        super(new SecurityContextStatementContext(statementContext, environment), SECURITY_CONTEXT_TYPE);
        this.environment = environment;
    }

    @Override
    protected Single<SecurityContext> lookupAddress(ResourceAddress address) {
        return Single.create(em -> database().get(address.toString()).then(document -> {
                    SecurityContext sc = new SecurityContext(ModelNode.fromBase64(document.data));
                    em.onSuccess(sc);
                    return null;
                },
                failure -> {
                    em.onError(new RuntimeException(String.valueOf(failure)));
                    return null;
                }));
    }

    @Override
    public void add(ResourceAddress address, SecurityContext securityContext) {
        SecurityContextDocument document = new SecurityContextDocument(address, securityContext);
        database().put(document)
                .then(id -> {
                    logger.debug("Add resource description for {}", id);
                    return null;
                })
                .catch_(error -> {
                    logger.error("Unable ");
                    return null;
                });

    }

    private PouchDB<SecurityContextDocument> database() {
        if (database == null) {
            String name = Ids.build("hal-db-sc",
                    environment.getHalBuild().name(),
                    environment.getManagementVersion().toString());
            database = new PouchDB<>(name);
        }
        return database;
    }
}
