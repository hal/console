package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSource;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplates;
import org.jboss.hal.client.configuration.subsystem.datasource.JdbcDriver;
import org.jboss.hal.config.Environment;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import java.util.List;

import static org.jboss.hal.client.configuration.subsystem.datasource.wizard.State.*;

/**
 * @author Harald Pehl
 */
public class NewDataSourceWizard extends Wizard<Context, State> {

    public NewDataSourceWizard(final Environment environment,
            final Resources resources,
            final Metadata metadata,
            final DataSourceTemplates templates,
            final List<DataSource> existingDataSources,
            final List<JdbcDriver> drivers,
            final boolean xa) {

        super(Ids.DATA_SOURCE_WIZARD,
                resources.messages().addResourceTitle(xa ? Names.XA_DATASOURCE : Names.DATASOURCE),
                new Context(environment.isStandalone(), xa));

        addStep(CHOOSE_TEMPLATE, new ChooseTemplateStep(this, templates, resources, xa));
        addStep(NAMES, new NamesStep(this, existingDataSources, resources));
        addStep(DRIVER, new DriverStep(this, drivers, resources));
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
