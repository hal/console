/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.messaging;

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.Dialog.Size;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLOSE_CONNECTIONS_FOR_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLOSE_CONNECTIONS_FOR_USER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLOSE_CONSUMER_CONNECTIONS_FOR_ADDRESS;

class CloseConnectionsDialog implements HasPresenter<ServerPresenter> {

    private final Metadata metadata;
    private final Resources resources;
    private Map<String, Form<ModelNode>> operationToForm;
    private Map<String, String> tabToOperation;
    private Tabs tabs;
    private ServerPresenter presenter;

    CloseConnectionsDialog(Metadata metadata, Resources resources) {
        this.metadata = metadata;
        this.resources = resources;
    }

    void show() {
        Form<ModelNode> cfaForm = new OperationFormBuilder<>(
                Ids.MESSAGING_SERVER_CONNECTION_CLOSE_FOR_ADDRESS_FORM, metadata, CLOSE_CONNECTIONS_FOR_ADDRESS)
                .build();
        Form<ModelNode> ccForm = new OperationFormBuilder<>(
                Ids.MESSAGING_SERVER_CONNECTION_CLOSE_CONSUMER_FORM, metadata, CLOSE_CONSUMER_CONNECTIONS_FOR_ADDRESS)
                .build();
        Form<ModelNode> cfuForm = new OperationFormBuilder<>(
                Ids.MESSAGING_SERVER_CONNECTION_CLOSE_FOR_USER_FORM, metadata, CLOSE_CONNECTIONS_FOR_USER)
                .build();
        operationToForm = new HashMap<>();
        operationToForm.put(CLOSE_CONNECTIONS_FOR_ADDRESS, cfaForm);
        operationToForm.put(CLOSE_CONSUMER_CONNECTIONS_FOR_ADDRESS, ccForm);
        operationToForm.put(CLOSE_CONNECTIONS_FOR_USER, cfuForm);

        tabs = new Tabs(Ids.MESSAGING_SERVER_CONNECTION_CLOSE_TABS);
        tabs.add(Ids.MESSAGING_SERVER_CONNECTION_CLOSE_FOR_ADDRESS_TAB, resources.constants().forAddress(),
                cfaForm.element());
        tabs.add(Ids.MESSAGING_SERVER_CONNECTION_CLOSE_CONSUMER_TAB, resources.constants().consumersForAddress(),
                ccForm.element());
        tabs.add(Ids.MESSAGING_SERVER_CONNECTION_CLOSE_FOR_USER_TAB, resources.constants().forUser(),
                cfuForm.element());
        tabToOperation = new HashMap<>();
        tabToOperation.put(Ids.MESSAGING_SERVER_CONNECTION_CLOSE_FOR_ADDRESS_TAB, CLOSE_CONNECTIONS_FOR_ADDRESS);
        tabToOperation.put(Ids.MESSAGING_SERVER_CONNECTION_CLOSE_CONSUMER_TAB, CLOSE_CONSUMER_CONNECTIONS_FOR_ADDRESS);
        tabToOperation.put(Ids.MESSAGING_SERVER_CONNECTION_CLOSE_FOR_USER_TAB, CLOSE_CONNECTIONS_FOR_USER);

        Dialog dialog = new Dialog.Builder(resources.constants().close())
                .add(p().textContent(resources.constants().chooseConnectionsToClose()).element())
                .add(tabs.element())
                .primary(resources.constants().close(), this::onClose)
                .size(Size.MEDIUM)
                .closeIcon(true)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(cfaForm, ccForm, cfuForm);
        cfaForm.edit(new ModelNode());
        ccForm.edit(new ModelNode());
        cfuForm.edit(new ModelNode());
        dialog.show();
    }

    private boolean onClose() {
        Form<ModelNode> form;
        String operation;

        String selectedTab = tabs.getSelectedId();
        if (selectedTab != null) {
            operation = tabToOperation.get(selectedTab);
            form = operationToForm.get(operation);
            if (operation != null && form != null) {
                if (form.save()) {
                    presenter.closeConnections(operation, form.getModel());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setPresenter(ServerPresenter presenter) {
        this.presenter = presenter;
    }
}
