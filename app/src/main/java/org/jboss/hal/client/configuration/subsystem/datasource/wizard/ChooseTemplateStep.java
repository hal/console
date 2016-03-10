package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import com.google.common.collect.FluentIterable;
import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplate;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplates;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;

/**
 * @author Harald Pehl
 */
class ChooseTemplateStep extends WizardStep<Context, State> {

    private final Element root;

    ChooseTemplateStep(final NewDataSourceWizard wizard, final DataSourceTemplates templates,
            final Resources resources, final boolean xa) {
        super(wizard, resources.constants().chooseTemplate());

        Elements.Builder builder = new Elements.Builder().div();
        // @formatter:off
            builder.div().css(CSS.radio)
                .label()
                    .input(InputType.radio)
                        .attr("value", "custom")
                        .on(click, event -> wizard.getContext().template  = null)
                    .span().textContent(resources.constants().custom()).end()
                .end()
            .end();
            // @formatter:on

        for (DataSourceTemplate template : FluentIterable.from(templates).filter(t -> t.getDataSource().isXa() == xa)) {
            // @formatter:off
            builder.div().css(CSS.radio)
                .label()
                    .input(InputType.radio)
                        .attr("value", template.getId())
                        .on(click, event ->
                                wizard.getContext().template = templates.getTemplate(((InputElement)event).getValue()))
                    .span().textContent(template.toString()).end()
                .end()
            .end();
            // @formatter:on
        }
        this.root = builder.end().build();

        InputElement firstRadio = (InputElement) root.querySelector("input[type=radio]"); //NON-NLS
        firstRadio.setChecked(true);
    }

    @Override
    public Element asElement() {
        return root;
    }
}
