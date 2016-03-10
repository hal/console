package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Names;

/**
 * @author Harald Pehl
 */
class SummaryStep extends WizardStep<Context, State> {

    private final Element root;

    SummaryStep(final NewDataSourceWizard wizard, final String title) {
        super(wizard, title);
        root = new Elements.Builder().p().textContent(Names.NYI).end().build();
    }

    @Override
    public Element asElement() {
        return root;
    }
}
