/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.bootstrap.endpoint;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.ButtonItem;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.ballroom.dialog.Dialog.PRIMARY_POSITION;
import static org.jboss.hal.ballroom.table.RefreshMode.HOLD;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.ADD;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.SELECT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SCHEME;

/**
 * Modal dialog to manage bootstrap servers. The dialog offers a page to connect to an existing server and a page to add new
 * servers.
 */
class EndpointDialog {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);
    private static final EndpointResources RESOURCES = GWT.create(EndpointResources.class);

    private final EndpointManager manager;
    private final EndpointStorage storage;
    private final HTMLElement selectPage;
    private final HTMLElement addPage;
    private final Alert alert;
    private final Form<Endpoint> form;

    private Mode mode;
    private Table<Endpoint> table;
    private Dialog dialog;

    EndpointDialog(EndpointManager manager, EndpointStorage storage) {
        this.manager = manager;
        this.storage = storage;
        Metadata metadata = Metadata.staticDescription(RESOURCES.endpoint());

        table = new ModelNodeTable.Builder<Endpoint>(Ids.ENDPOINT_SELECT, metadata)
                .button(CONSTANTS.add(), table -> switchTo(ADD))
                .button(CONSTANTS.remove(), table -> {
                    storage.remove(table.selectedRow());
                    this.table.update(storage.list(), HOLD);
                    dialog.getButton(PRIMARY_POSITION).disabled = !this.table.hasSelection();
                }, Scope.SELECTED)
                .column(NAME)
                .column("url", "URL", (cell, type, row, meta) -> row.getUrl())
                .build();

        selectPage = div()
                .add(p().textContent(CONSTANTS.endpointSelectDescription()))
                .add(table.element()).element();

        alert = new Alert();
        ButtonItem ping = new ButtonItem(Ids.ENDPOINT_PING, CONSTANTS.ping());
        ping.onClick((event) -> {
            Endpoint endpoint = transientEndpoint();
            manager.pingServer(endpoint, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable throwable) {
                    alert.setIcon(Icons.ERROR)
                            .setText(MESSAGES.endpointError(endpoint.getUrl(), Endpoints.getBaseUrl()));
                    Elements.setVisible(alert.element(), true);
                }

                @Override
                public void onSuccess(Void aVoid) {
                    alert.setIcon(Icons.OK).setText(MESSAGES.endpointOk(endpoint.getUrl()));
                    Elements.setVisible(alert.element(), true);
                }
            });
        });

        form = new ModelNodeForm.Builder<Endpoint>(Ids.ENDPOINT_ADD, metadata)
                .addOnly()
                .include(NAME, SCHEME, HOST, PORT)
                .unboundFormItem(ping)
                .unsorted()
                .onCancel((form) -> switchTo(SELECT))
                .onSave((form, changedValues) -> {
                    Endpoint endpoint = form.getModel();
                    if (!endpoint.hasDefined(HOST)) {
                        endpoint.get(HOST).set(EndpointManager.DEFAULT_HOST);
                    }
                    if (!endpoint.hasDefined(PORT)) {
                        endpoint.get(PORT).set(EndpointManager.DEFAULT_PORT);
                    }
                    storage.add(endpoint);
                    switchTo(SELECT);
                    select(endpoint);
                })
                .build();

        addPage = div()
                .add(p().textContent(CONSTANTS.endpointAddDescription()))
                .add(alert)
                .add(form.element()).element();

        dialog = new Dialog.Builder(CONSTANTS.endpointSelectTitle())
                .add(selectPage, addPage)
                .primary(CONSTANTS.endpointConnect(), this::onPrimary)
                .secondary(this::onSecondary)
                .closeIcon(false)
                .closeOnEsc(false)
                .build();
        dialog.registerAttachable(form, table);
    }

    private void select(Endpoint endpoint) {
        if (mode == SELECT) {
            table.select(endpoint);
        }
    }

    private Endpoint transientEndpoint() {
        Endpoint endpoint = new Endpoint();
        endpoint.setName("__transientEndpoint__"); // NON-NLS
        FormItem<String> scheme = form.getFormItem(SCHEME);
        endpoint.get(SCHEME).set(scheme.getValue());
        FormItem<String> host = form.getFormItem(HOST);
        if (Strings.isNullOrEmpty(host.getValue())) {
            endpoint.get(HOST).set(EndpointManager.DEFAULT_HOST);
        } else {
            endpoint.get(HOST).set(host.getValue());
        }
        FormItem<Number> port = form.getFormItem(PORT);
        if (port.getValue() == null) {
            endpoint.get(PORT).set(EndpointManager.DEFAULT_PORT);
        } else {
            endpoint.get(PORT).set(port.getValue().intValue());
        }
        return endpoint;
    }

    private void switchTo(Mode mode) {
        HTMLButtonElement primaryButton = dialog.getButton(PRIMARY_POSITION);
        if (mode == SELECT) {
            dialog.setTitle(CONSTANTS.endpointSelectTitle());
            table.update(storage.list(), HOLD);
            primaryButton.textContent = CONSTANTS.endpointConnect();
            primaryButton.disabled = !table.hasSelection();
            Elements.setVisible(addPage, false);
            Elements.setVisible(selectPage, true);

        } else if (mode == ADD) {
            dialog.setTitle(CONSTANTS.endpointAddTitle());
            Elements.setVisible(alert.element(), false);
            form.edit(new Endpoint());
            primaryButton.textContent = CONSTANTS.add();
            primaryButton.disabled = false;
            Elements.setVisible(selectPage, false);
            Elements.setVisible(addPage, true);
        }
        this.mode = mode;
    }

    private boolean onPrimary() {
        if (mode == SELECT) {
            manager.onConnect(table.selectedRow());
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

        table.onSelectionChange(t -> dialog.getButton(PRIMARY_POSITION).disabled = !t.hasSelection());
        table.update(storage.list());

        switchTo(SELECT);
        storage.list().stream()
                .filter(Endpoint::isSelected)
                .findAny()
                .ifPresent(this::select);
    }

    enum Mode {
        SELECT, ADD
    }
}
