/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental.dom.Element;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.ButtonItem;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.table.Button.Scope;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;

import static com.google.common.base.Strings.emptyToNull;
import static org.jboss.hal.ballroom.dialog.Dialog.PRIMARY_POSITION;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.table.Api.RefreshMode.HOLD;
import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.client.bootstrap.endpoint.Endpoint.SCHEME;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.ADD;
import static org.jboss.hal.client.bootstrap.endpoint.EndpointDialog.Mode.SELECT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;

/**
 * Modal dialog to manage bootstrap servers. The dialog offers a page to connect to an existing server and a page to
 * add new servers.
 *
 * @author Harald Pehl
 */
class EndpointDialog {

    enum Mode {SELECT, ADD}


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);
    private static final EndpointResources RESOURCES = GWT.create(EndpointResources.class);

    private final EndpointManager manager;
    private final EndpointStorage storage;
    private final Element selectPage;
    private final Element addPage;
    private final Alert alert;
    private final Form<Endpoint> form;
    private final ButtonItem ping;

    private Mode mode;
    private NamedNodeTable<Endpoint> table;
    private Dialog dialog;

    EndpointDialog(final EndpointManager manager, final EndpointStorage storage) {
        this.manager = manager;
        this.storage = storage;
        Metadata metadata = Metadata.staticDescription(RESOURCES.endpoint());

        Options<Endpoint> endpointOptions = new ModelNodeTable.Builder<Endpoint>(metadata)
                .button(CONSTANTS.add(), (event, api) -> switchTo(ADD))
                .button(CONSTANTS.remove(), Scope.SELECTED, (event, api) -> {
                    storage.remove(api.selectedRow());
                    api.clear().add(storage.list()).refresh(HOLD);
                    dialog.getButton(PRIMARY_POSITION).setDisabled(!table.api().hasSelection());
                })
                .column(NAME)
                .column("url", "URL", (cell, type, row, meta) -> row.getUrl()) //NON-NLS
                .build();
        table = new NamedNodeTable<>(Ids.ENDPOINT_SELECT, endpointOptions);

        selectPage = new Elements.Builder()
                .div()
                .p().textContent(CONSTANTS.endpointSelectDescription()).end()
                .add(table.asElement())
                .end().build();

        alert = new Alert();
        ping = new ButtonItem(Ids.ENDPOINT_PING, CONSTANTS.ping());
        ping.onClick((event) -> {
            Endpoint endpoint = transientEndpoint();
            manager.pingServer(endpoint, new AsyncCallback<Void>() {
                @Override
                public void onFailure(final Throwable throwable) {
                    alert.setIcon(Icons.ERROR).setText(MESSAGES.endpointError(Endpoints.getBaseUrl()));
                    Elements.setVisible(alert.asElement(), true);
                }

                @Override
                public void onSuccess(final Void aVoid) {
                    alert.setIcon(Icons.OK).setText(MESSAGES.endpointOk(endpoint.getUrl()));
                    Elements.setVisible(alert.asElement(), true);
                }
            });
        });
        ping.setEnabled(false);

        form = new ModelNodeForm.Builder<Endpoint>(Ids.ENDPOINT_ADD, metadata)
                .addOnly()
                .include(NAME, SCHEME, HOST, PORT)
                .unboundFormItem(ping)
                .unsorted()
                .onCancel((form) -> switchTo(SELECT))
                .onSave((form, changedValues) -> {
                    Endpoint endpoint = form.getModel();
                    if (!endpoint.hasDefined(PORT)) {
                        endpoint.get(PORT).set(EndpointManager.DEFAULT_PORT);
                    }
                    storage.add(endpoint);
                    switchTo(SELECT);
                    select(endpoint);
                })
                .build();

        addPage = new Elements.Builder()
                .div()
                .p().textContent(CONSTANTS.endpointAddDescription()).end()
                .add(alert)
                .add(form.asElement())
                .end().build();

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
        endpoint.setName("__transientEndpoint__"); //NON-NLS
        FormItem<String> scheme = form.getFormItem(SCHEME);
        endpoint.get(SCHEME).set(scheme.getValue());
        FormItem<String> host = form.getFormItem(HOST);
        endpoint.get(HOST).set(host.getValue());
        FormItem<Number> port = form.getFormItem(PORT);
        if (port.getValue() == null) {
            endpoint.get(PORT).set(EndpointManager.DEFAULT_PORT);
        } else {
            endpoint.get(PORT).set(port.getValue().intValue());
        }
        return endpoint;
    }

    private void switchTo(final Mode mode) {
        ButtonElement primaryButton = dialog.getButton(PRIMARY_POSITION);
        if (mode == SELECT) {
            dialog.setTitle(CONSTANTS.endpointSelectTitle());
            table.api().clear().add(storage.list()).refresh(HOLD);
            primaryButton.setInnerText(CONSTANTS.endpointConnect());
            primaryButton.setDisabled(!table.api().hasSelection());
            Elements.setVisible(addPage, false);
            Elements.setVisible(selectPage, true);

        } else if (mode == ADD) {
            dialog.setTitle(CONSTANTS.endpointAddTitle());
            Elements.setVisible(alert.asElement(), false);
            form.add(new Endpoint());
            primaryButton.setInnerText(CONSTANTS.add());
            primaryButton.setDisabled(false);
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

        FormItem<String> host = form.getFormItem(HOST);
        host.asElement(EDITING).setOnkeyup(event -> ping.setEnabled(emptyToNull(host.getValue()) != null));
        host.addValueChangeHandler(event -> ping.setEnabled(emptyToNull(host.getValue()) != null));

        table.api().onSelectionChange(api -> dialog.getButton(PRIMARY_POSITION).setDisabled(!api.hasSelection()));
        table.api().add(storage.list()).refresh(RESET);

        switchTo(SELECT);
        storage.list().stream()
                .filter(Endpoint::isSelected)
                .findAny()
                .ifPresent(endpoint -> select(endpoint));
    }
}
