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
package org.jboss.hal.meta.description;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.db.Document;
import org.jboss.hal.db.PouchDB;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AbstractDatabase;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;

public class ResourceDescriptionDatabase extends AbstractDatabase<ResourceDescription> {

    private static final String RESOURCE_DESCRIPTION_TYPE = "resource description";

    private final Environment environment;
    private final Settings settings;
    private PouchDB database;

    @Inject
    public ResourceDescriptionDatabase(StatementContext statementContext, Environment environment, Settings settings) {
        super(new ResourceDescriptionStatementContext(statementContext, environment), RESOURCE_DESCRIPTION_TYPE);
        this.environment = environment;
        this.settings = settings;
    }

    @Override
    public String name() {
        return Ids.build("hal-db-rd",
                environment.getHalBuild().name(),
                settings.get(Settings.Key.LOCALE).value(),
                environment.getManagementVersion().toString());
    }

    @Override
    public ResourceDescription asMetadata(Document document) {
        return new ResourceDescription(ModelNode.fromBase64(document.getAny(PAYLOAD).asString()));
    }

    @Override
    public Document asDocument(ResourceAddress address, ResourceDescription resourceDescription) {
        Document document = Document.of(address.toString());
        document.set(PAYLOAD, resourceDescription.toBase64String());
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
