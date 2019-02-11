package org.jboss.hal.processor.mbui.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.template.TemplateUtil;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.GroupedForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("org.jboss.hal.processor.mbui.MbuiViewProcessor")
public final class Mbui_AddWithAttributesView extends AddWithAttributesView {

    private final Metadata metadata0;
    private final Map<String, HTMLElement> expressionElements;

    @Inject
    @SuppressWarnings("unchecked")
    public Mbui_AddWithAttributesView(MbuiContext mbuiContext) {
        super(mbuiContext);

        AddressTemplate metadata0Template = AddressTemplate.of("/subsystem=foo");
        this.metadata0 = mbuiContext.metadataRegistry().lookup(metadata0Template);
        this.expressionElements = new HashMap<>();

        table = new ModelNodeTable.Builder<org.jboss.hal.dmr.NamedNode>("table", metadata0)
                .button(mbuiContext.tableButtonFactory().add(Ids.build("table", Ids.ADD), "Foo",
                        metadata0Template, asList("foo"), (name, address) -> presenter.reload()))
                .columns("name")
                .build();

        HTMLElement html0;
        HTMLElement root = row()
                .add(column()
                        .add(html0 = div()
                                .innerHtml(SafeHtmlUtils.fromSafeConstant("<h1>Table</h1>"))
                                .get())
                        .add(table)
                )
                .get();
        expressionElements.put("html0", html0);

        registerAttachable(table);

        initElement(root);
    }

    @Override
    public void attach() {
        super.attach();
    }
}
