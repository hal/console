package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

/**
 * @author Harald Pehl
 */
class PropertiesStep extends WizardStep<Context, State> {

    private final Element root;

    PropertiesStep(final NewDataSourceWizard wizard, final Resources resources) {
        super(wizard, resources.constants().xaProperties());
        root = new Elements.Builder().p().textContent(Names.NYI).end().build();
    }

    @Override
    public Element asElement() {
        return root;
    }
}
