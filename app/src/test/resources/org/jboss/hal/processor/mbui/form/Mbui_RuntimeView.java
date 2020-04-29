package org.jboss.hal.processor.mbui.form;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("org.jboss.hal.processor.mbui.MbuiViewProcessor")
public final class Mbui_RuntimeView extends RuntimeView {

    private final Metadata metadata0;
    private final Map<String, HTMLElement> expressionElements;

    @Inject
    @SuppressWarnings("unchecked")
    public Mbui_RuntimeView(MbuiContext mbuiContext) {
        super(mbuiContext);

        AddressTemplate metadata0Template = AddressTemplate.of("/subsystem=foo");
        this.metadata0 = mbuiContext.metadataRegistry().lookup(metadata0Template);
        this.expressionElements = new HashMap<>();

        form = new ModelNodeForm.Builder<org.jboss.hal.dmr.ModelNode>("form", metadata0)
                .includeRuntime()
                .onSave((form, changedValues) -> saveSingletonForm("Form",
                        metadata0Template.resolve(statementContext()), changedValues, metadata0))
                .prepareReset(form -> resetSingletonForm("Form", metadata0Template.resolve(statementContext()), form,
                        metadata0))
                .build();

        HTMLElement html0;
        HTMLElement root = row()
                .add(column()
                        .add(html0 = div()
                                .innerHtml(SafeHtmlUtils.fromSafeConstant("<h1>Form</h1>"))
                                .get())
                        .add(form)
                )
                .get();
        expressionElements.put("html0", html0);

        registerAttachable(form);

        initElement(root);
    }

    @Override
    public void attach() {
        super.attach();
    }
}
