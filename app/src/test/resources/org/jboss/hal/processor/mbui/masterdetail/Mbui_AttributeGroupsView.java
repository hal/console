package org.jboss.hal.processor.mbui.masterdetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.TemplateUtil;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.GroupedForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("org.jboss.hal.processor.mbui.MbuiViewProcessor")
final class Mbui_AttributeGroupsView extends AttributeGroupsView {

    private final Metadata metadata0;
    private final Map<String, Element> handlebarElements;

    @SuppressWarnings("unchecked")
    Mbui_AttributeGroupsView(MbuiContext mbuiContext) {
        super(mbuiContext);

        AddressTemplate metadata0Template = AddressTemplate.of("/subsystem=*");
        this.metadata0 = mbuiContext.metadataRegistry().lookup(metadata0Template);
        this.handlebarElements = new HashMap<>();

        form = new GroupedForm.Builder<org.jboss.hal.dmr.model.NamedNode>("form", metadata0)
                .customGroup("group-1", "Group 1")
                .include("foo", "bar")
                .end()
                .customGroup("group-2", "Group 2")
                .include("baz", "qux")
                .end()
                .onSave((form, changedValues) -> {
                    String name = form.getModel().getName();
                    saveForm("Form", name, metadata0Template.resolve(mbuiContext.statementContext(), name), changedValues);
                })
                .prepareReset(form -> {
                    String name = form.getModel().getName();
                    resetForm("Form", name, metadata0Template.resolve(mbuiContext.statementContext()), form, metadata0)
                })
                .build();

        Options<org.jboss.hal.dmr.model.NamedNode> tableOptions = new NamedNodeTable.Builder<org.jboss.hal.dmr.model.NamedNode>(metadata0)
                .columns("name")
                .build();
        table = new NamedNodeTable<>("table", tableOptions);

        LayoutBuilder layoutBuilder = new LayoutBuilder()
                .row()
                .column()
                .div()
                .innerHtml(SafeHtmlUtils.fromSafeConstant("<h1>Master-Detail</h1>"))
                .rememberAs("html0")
                .end()
                .add(table)
                .add(form)
                .end()
                .end();
        handlebarElements.put("html0", layoutBuilder.referenceFor("html0"));

        registerAttachable(table);
        registerAttachable(form);

        Element root = layoutBuilder.build();
        initElement(root);
    }

    @Override
    public void attach() {
        super.attach();
        table.bindForm(form);
    }
}
