package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.JdbcDriver;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import java.util.List;

/**
 * @author Harald Pehl
 */
class DriverStep extends WizardStep<Context, State> {

    private final Element root;

    DriverStep(final NewDataSourceWizard wizard, final List<JdbcDriver> drivers, final Resources resources) {
        super(wizard, resources.constants().jdbcDriver());
        root = new Elements.Builder().p().textContent(Names.NYI).end().build();
    }

    @Override
    public Element asElement() {
        return root;
    }
}
