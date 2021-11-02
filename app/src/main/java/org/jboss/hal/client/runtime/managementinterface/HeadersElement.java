package org.jboss.hal.client.runtime.managementinterface;

import java.util.List;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONSTANT_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_INDEX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;
import static org.jboss.hal.resources.Ids.HEADER;

class HeadersElement
        implements HasPresenter<ConstantHeadersPresenter>, Attachable, IsElement<HTMLElement> {

    private final Resources resources;
    private final HTMLElement root;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private ConstantHeadersPresenter presenter;
    private int pathIndex;

    HeadersElement(Metadata metadata, AddressTemplate template, Resources resources) {
        this.resources = resources;

        LabelBuilder labelBuilder = new LabelBuilder();
        Constraint constraint = Constraint.writable(template, CONSTANT_HEADERS);
        table = new ModelNodeTable.Builder<NamedNode>(Ids.CONSTANT_HEADERS_HEADER_TABLE, metadata)
                .button(resources.constants().add(), t -> addHeader(metadata), constraint)
                .button(resources.constants().remove(),
                        t -> {
                            ModelNode row = t.selectedRow();
                            removeHeader(pathIndex, row.get(HAL_INDEX).asInt(), row.get(NAME).asString());
                        },
                        Scope.SELECTED, constraint)
                .column(NAME)
                .column(VALUE)
                .build();
        form = new ModelNodeForm.Builder<NamedNode>(Ids.CONSTANT_HEADERS_HEADER_FORM, metadata)
                .include(NAME, VALUE)
                .onSave((f, changedValues) -> saveHeader(pathIndex, f.getModel().get(HAL_INDEX).asInt(),
                        f.getModel().get(NAME).asString(), metadata, changedValues))
                .build();

        root = section()
                .add(h(1).textContent(labelBuilder.label(HEADERS)).element())
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(table)
                .add(form)
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
    }

    @Override
    public void detach() {
        table.detach();
        form.detach();
    }

    @Override
    public void setPresenter(ConstantHeadersPresenter presenter) {
        this.presenter = presenter;
    }

    void update(int pathIndex, List<NamedNode> headers) {
        storeIndex(headers);
        this.pathIndex = pathIndex;
        this.form.clear();
        this.table.update(headers);
    }

    private void addHeader(Metadata metadata) {
        LabelBuilder labelBuilder = new LabelBuilder();
        String type = labelBuilder.label(HEADER);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(Ids.CONSTANT_HEADERS_HEADER, Ids.ADD), metadata)
                .addOnly()
                .include(NAME)
                .include(VALUE)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(type), form,
                (name, model) -> {
                    if (model != null) {
                        SafeHtml message = resources.messages().addSuccess(type,
                                model.get(NAME).asString(), labelBuilder.label(HEADERS));
                        presenter.addHeader(pathIndex, model, message);
                    }
                });
        dialog.show();
    }

    private void saveHeader(int pathIndex, int index, String header, Metadata metadata,
            Map<String, Object> changedValues) {
        LabelBuilder labelBuilder = new LabelBuilder();
        String type = labelBuilder.label(HEADER);
        SafeHtml successMessage = resources.messages().modifyResourceSuccess(type, header);
        presenter.saveHeader(pathIndex, index, header, metadata, changedValues, successMessage);
    }

    private void removeHeader(int pathIndex, int index, String header) {
        LabelBuilder labelBuilder = new LabelBuilder();
        String type = labelBuilder.label(HEADER);
        String title = resources.messages().removeConfirmationTitle(type);
        SafeHtml question = resources.messages().removeConfirmationQuestion(header);
        SafeHtml success = resources.messages().removeResourceSuccess(type, header);

        DialogFactory.showConfirmation(title, question,
                () -> presenter.removeHeader(pathIndex, index, header, success));
    }
}