package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.core.client.GWT;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.table.Button.Scope;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.HOLD;
import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.ADD;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.SELECT;
import static org.jboss.hal.meta.security.SecurityContext.RWX;
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
        Options<Endpoint> endpointOptions = new ModelNodeTable.Builder<Endpoint>(description)
                .button(CONSTANTS.add(), (event, api) -> switchTo(ADD))
                .button(CONSTANTS.remove(), Scope.SELECTED, (event, api) -> {
                    storage.remove(api.selectedRow());
                    api.clear().add(storage.list()).refresh(HOLD);
                    dialog.setPrimaryButtonDisabled(!table.api().hasSelection());
                })
                .column(NAME_KEY)
                .column("url", "URL", (cell, type, row, meta) -> row.getUrl()) //NON-NLS
                .build();
        table = new ModelNodeTable<>(Ids.ENDPOINT_SELECT, RWX, endpointOptions);

        selectPage = new Elements.Builder()
                .div()
                .p().innerText(CONSTANTS.endpointSelectDescription()).end()
                .add(table.asElement())
                .end().build();

        form = new ModelNodeForm.Builder<Endpoint>(Ids.ENDPOINT_ADD, SecurityContext.RWX, description)
                .addOnly()
                .include(NAME_KEY, SCHEME, HOST, PORT)
                .unsorted()
                .hideButtons()
                .onCancel((form) -> switchTo(SELECT))
                .onSave((form, changedValues) -> {
                    storage.add(form.getModel());
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
            table.api().clear().add(storage.list()).refresh(HOLD);
            dialog.setPrimaryButtonLabel(CONSTANTS.endpointConnect());
            dialog.setPrimaryButtonDisabled(!table.api().hasSelection());
            Elements.setVisible(addPage, false);
            Elements.setVisible(selectPage, true);

        } else if (mode == ADD) {
            dialog.setTitle(CONSTANTS.endpointAddTitle());
            form.add(new Endpoint());
            dialog.setPrimaryButtonLabel(CONSTANTS.add());
            dialog.setPrimaryButtonDisabled(false);
            Elements.setVisible(selectPage, false);
            Elements.setVisible(addPage, true);
        }
        this.mode = mode;
    }

    private boolean onPrimary() {
        if (mode == SELECT) {
            manager.onConnect(table.api().selectedRow());
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
            form.cancel();
            switchTo(SELECT);
        }
        return false; // don't close the dialog!
    }

    void show() {
        dialog.show();
        table.attach();
        table.api().onSelectionChange(api -> dialog.setPrimaryButtonDisabled(!api.hasSelection()));
        table.api().add(storage.list()).refresh(RESET);
        switchTo(SELECT);
    }
}
