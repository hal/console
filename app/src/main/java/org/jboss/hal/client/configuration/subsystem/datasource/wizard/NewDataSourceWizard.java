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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import java.util.List;

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSource;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplates;
import org.jboss.hal.client.configuration.subsystem.datasource.JdbcDriver;
import org.jboss.hal.config.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.wizard.State.*;

/**
 * @author Harald Pehl
 */
public class NewDataSourceWizard extends Wizard<Context, State> {

    public NewDataSourceWizard(final MetadataRegistry metadataRegistry,
            final Environment environment,
            final Resources resources,
            final DataSourceTemplates templates,
            final List<DataSource> existingDataSources,
            final List<JdbcDriver> drivers,
            final boolean xa) {

        super(Ids.DATA_SOURCE_WIZARD,
                resources.messages().addResourceTitle(xa ? Names.XA_DATASOURCE : Names.DATASOURCE),
                new Context(environment.isStandalone(), xa));

        AddressTemplate dataSourceTemplate = xa ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE;
        Metadata dataSourceMetadata = metadataRegistry.lookup(dataSourceTemplate);
        Metadata driverMetadata = metadataRegistry.lookup(JDBC_DRIVER_TEMPLATE);

        addStep(CHOOSE_TEMPLATE, new ChooseTemplateStep(this, templates, resources, xa));
        addStep(NAMES, new NamesStep(this, existingDataSources, dataSourceMetadata, resources));
        addStep(DRIVER, new DriverStep(this, drivers, driverMetadata, resources));
        addStep(PROPERTIES, new PropertiesStep(this, resources));
        addStep(CONNECTION, new ConnectionStep(this, resources));
        addStep(SUMMARY, new SummaryStep(this, resources.constants().summary()));
    }

    @Override
    protected State back(final State state) {
        State previous = null;
        switch (state) {
            case CHOOSE_TEMPLATE:
                break;
            case NAMES:
                previous = CHOOSE_TEMPLATE;
                break;
            case DRIVER:
                previous = NAMES;
                break;
            case PROPERTIES:
                previous = DRIVER;
                break;
            case CONNECTION:
                previous = getContext().xa ? PROPERTIES : DRIVER;
                break;
            case SUMMARY:
                previous = CONNECTION;
        }
        return previous;
    }

    @Override
    protected State next(final State state) {
        State next = null;
        switch (state) {
            case CHOOSE_TEMPLATE:
                next = NAMES;
                break;
            case NAMES:
                next = DRIVER;
                break;
            case DRIVER:
                next = getContext().xa ? PROPERTIES : CONNECTION;
                break;
            case PROPERTIES:
                next = CONNECTION;
                break;
            case CONNECTION:
                next = SUMMARY;
                break;
            case SUMMARY:
                break;
        }
        return next;
    }
}
