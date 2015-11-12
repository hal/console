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
import org.jboss.hal.resources.HalConstants;
import org.jboss.hal.resources.HalIds;

import static org.jboss.hal.ballroom.table.DataTableButton.Target.ROW;
import static org.jboss.hal.ballroom.table.DataTableButton.Target.TABLE;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.ADD;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.SELECT;

/**
 * Modal dialog to manage bootstrap servers. The dialog offers a page to connect to an existing server and a page to
 * add new servers.
 *
 * @author Harald Pehl
 */
class EndpointDialog {

    enum Mode {SELECT, ADD}


    private static final HalConstants CONSTANTS = GWT.create(HalConstants.class);
    private static final HalIds IDS = GWT.create(HalIds.class);
    private static final EndpointResources RESOURCES = GWT.create(EndpointResources.class);


    private final EndpointManager manager;
    private final EndpointStorage storage;

    private Mode mode;
    private Element selectPage;
    private ModelNodeTable<Endpoint> table;
    private Element addPage;
    private ModelNodeForm<Endpoint> form;
    private Dialog dialog;

    EndpointDialog(final EndpointManager manager, final EndpointStorage storage) {
        this.manager = manager;
        this.storage = storage;

        ResourceDescription description = StaticResourceDescription.from(RESOURCES.endpoint());
        table = new ModelNodeTable.Builder<>(IDS.endpoints_table(),
                Endpoint::getName, SecurityContext.RWX,
                description)
                .addButton(new DataTableButton(CONSTANTS.add(), TABLE, event -> switchTo(ADD)))
                .addButton(
                        new DataTableButton(CONSTANTS.remove(), ROW, event -> {
                            storage.remove(table.selectedElement());
                            table.setData(storage.list());
                        }))
                .addColumn("name")
                .build();
        table.addColumn(new TextColumn<Endpoint>() {
            @Override
            public String getValue(final Endpoint endpoint) {
                return endpoint.getUrl();
            }
        }, "URL");
        table.onSelectionChange(selectionChangeEvent -> dialog.setPrimaryButtonDisabled(!table.hasSelection()));
        table.setData(storage.list());

        selectPage = new Elements.Builder()
                .div()
                .p().innerText(CONSTANTS.endpoint_select_description()).end()
                .add(table.asElement())
                .end().build();

        form = new ModelNodeForm.Builder<Endpoint>(IDS.endpoint_form(), SecurityContext.RWX, description)
                .editOnly()
                .hideButtons()
                .onCancel(() -> switchTo(SELECT))
                .onSave((changedValues) -> {
                    // form is valid here
                    ModelNode node = new ModelNode();
                    node.get("name").set(String.valueOf(changedValues.get("name")));
                    node.get("scheme").set(String.valueOf(changedValues.get("scheme")));
                    node.get("host-name").set(String.valueOf(changedValues.get("host-name")));
                    node.get("port").set((Integer)changedValues.get("port"));
                    storage.add(new Endpoint(node));
                    switchTo(SELECT);
                })
                .build();

        addPage = new Elements.Builder()
                .div()
                .p().innerText(CONSTANTS.endpoint_add_description()).end()
                .add(form.asElement())
                .end().build();

        dialog = new Dialog.Builder(CONSTANTS.endpoint_select_title())
                .add(selectPage, addPage)
                .primary(CONSTANTS.endpoint_connect(), this::onPrimary)
                .secondary(this::onSecondary)
                .closeIcon(false)
                .build();
    }

    private void switchTo(final Mode mode) {
        if (mode == SELECT) {
            dialog.setTitle(CONSTANTS.endpoint_select_title());
            table.setData(storage.list());
            dialog.setPrimaryButtonLabel(CONSTANTS.endpoint_connect());
            dialog.setPrimaryButtonDisabled(!table.hasSelection());
            Elements.setVisible(addPage, false);
            Elements.setVisible(selectPage, true);

        } else if (mode == ADD) {
            dialog.setTitle(CONSTANTS.endpoint_add_title());
            form.clearValues();
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
            return false;
        }
        return false;
    }

    private boolean onSecondary() {
        if (mode == SELECT) {
            // TODO Show an error message "You need to select a management interface"
        } else if (mode == ADD) {
            form.save();
        }
        return false; // don't close the dialog!
    }

    void show() {
        switchTo(SELECT);
        dialog.show();
    }
}
