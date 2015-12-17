package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.TextColumn;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.table.DataTableButton;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.ballroom.table.DataTableButton.Target.ROW;
import static org.jboss.hal.ballroom.table.DataTableButton.Target.TABLE;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.ADD;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.SELECT;
import static org.jboss.hal.resources.Names.*;

/**
 * Modal dialog to manage bootstrap servers. The dialog offers a page to connect to an existing server and a page to
 * add new servers.
 *
 * @author Harald Pehl
 */
class EndpointDialog {

    enum Mode {SELECT, ADD}


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final EndpointResources RESOURCES = GWT.create(EndpointResources.class);

    private final EndpointManager manager;
    private final EndpointStorage storage;
    private final Element selectPage;
    private final Element addPage;
    private final ModelNodeForm<Endpoint> form;

    private Mode mode;
    private ModelNodeTable<Endpoint> table;
    private Dialog dialog;

    EndpointDialog(final EndpointManager manager, final EndpointStorage storage) {
        this.manager = manager;
        this.storage = storage;

        ResourceDescription description = StaticResourceDescription.from(RESOURCES.endpoint());
        table = new ModelNodeTable.Builder<>(Ids.ENDPOINT_SELECT, Endpoint::getName, SecurityContext.RWX, description)
                .addButton(new DataTableButton(CONSTANTS.add(), TABLE, event -> switchTo(ADD)))
                .addButton(new DataTableButton(CONSTANTS.remove(), ROW, event -> {
                    storage.remove(table.selectedElement());
                    table.setData(storage.list());
                }))
                .addColumn(NAME_KEY)
                .build();

        table.addColumn(new TextColumn<Endpoint>() {
            @Override
            public String getValue(final Endpoint endpoint) {
                return endpoint.getUrl();
            }
        }, "URL"); //NON-NLS
        table.onSelectionChange(selectionChangeEvent -> dialog.setPrimaryButtonDisabled(!table.hasSelection()));
        table.setData(storage.list());

        selectPage = new Elements.Builder()
                .div()
                .p().innerText(CONSTANTS.endpointSelectDescription()).end()
                .add(table.asElement())
                .end().build();

        form = new ModelNodeForm.Builder<Endpoint>(Ids.ENDPOINT_ADD, SecurityContext.RWX, description)
                .editOnly()
                .hideButtons()
                .onCancel(() -> switchTo(SELECT))
                .onSave((changedValues) -> {
                    // form is valid here
                    ModelNode node = new ModelNode();
                    node.get(NAME_KEY).set(String.valueOf(changedValues.get(NAME_KEY)));
                    node.get(SCHEME).set(String.valueOf(changedValues.get(SCHEME)));
                    node.get(HOST).set(String.valueOf(changedValues.get(HOST)));
                    if (changedValues.containsKey(PORT)) {
                        node.get(PORT).set((Integer) changedValues.get(PORT));
                    }
                    storage.add(new Endpoint(node));
                    switchTo(SELECT);
                })
                .build();

        addPage = new Elements.Builder()
                .div()
                .p().innerText(CONSTANTS.endpointAddDescription()).end()
                .add(form.asElement())
                .end().build();

        dialog = new Dialog.Builder(CONSTANTS.endpointSelectTitle())
                .add(selectPage, addPage)
                .primary(CONSTANTS.endpointConnect(), this::onPrimary)
                .secondary(this::onSecondary)
                .closeIcon(false)
                .closeOnEsc(false)
                .build();
    }

    private void switchTo(final Mode mode) {
        if (mode == SELECT) {
            dialog.setTitle(CONSTANTS.endpointSelectTitle());
            table.setData(storage.list());
            dialog.setPrimaryButtonLabel(CONSTANTS.endpointConnect());
            dialog.setPrimaryButtonDisabled(!table.hasSelection());
            Elements.setVisible(addPage, false);
            Elements.setVisible(selectPage, true);

        } else if (mode == ADD) {
            dialog.setTitle(CONSTANTS.endpointAddTitle());
            form.clearValues();
            form.edit(new Endpoint(new ModelNode()));
            dialog.setPrimaryButtonLabel(CONSTANTS.add());
            Elements.setVisible(selectPage, false);
            Elements.setVisible(addPage, true);
        }
        this.mode = mode;
    }

    private boolean onPrimary() {
        if (mode == SELECT) {
            manager.onConnect(table.selectedElement());
            return true;
        } else if (mode == ADD) {
            form.save();
            switchTo(SELECT);
            return false;
        }
        return false;
    }

    private boolean onSecondary() {
        if (mode == SELECT) {
            // TODO Show an error message "You need to select a management interface"
        } else if (mode == ADD) {
            form.cancel();
            switchTo(SELECT);
        }
        return false; // don't close the dialog!
    }

    void show() {
        switchTo(SELECT);
        dialog.show();
    }
}
