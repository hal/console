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
package org.jboss.hal.client.configuration.subsystem.transactions;

import java.util.Map;
import javax.annotation.PostConstruct;

import com.google.common.base.Strings;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.client.configuration.subsystem.transactions.TransactionPresenter.PROCESS_ID_SOCKET_BINDING;
import static org.jboss.hal.client.configuration.subsystem.transactions.TransactionPresenter.PROCESS_ID_SOCKET_MAX_PORTS;
import static org.jboss.hal.client.configuration.subsystem.transactions.TransactionPresenter.PROCESS_ID_UUID;

/**
 * @author Claudio Miranda
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class TransactionView extends MbuiViewImpl<TransactionPresenter> implements TransactionPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static TransactionView create(final MbuiContext mbuiContext) {
        return new Mbui_TransactionView(mbuiContext);
    }

    @MbuiElement("transaction-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("attributes-form") Form<ModelNode> attributesForm;
    @MbuiElement("process-form") Form<ModelNode> processForm;
    @MbuiElement("recovery-form") Form<ModelNode> recoveryForm;
    @MbuiElement("path-form") Form<ModelNode> pathForm;
    @MbuiElement("jdbc-form") Form<ModelNode> jdbcForm;

    TransactionView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }
    
    @PostConstruct
    void init() {

        ValidationResult invalid = ValidationResult.invalid("Validation error, see error messages below.");
        
        // --------------- form validation for the general attributes 
        attributesForm.addFormValidation(formItems -> {

            final FormItem<Boolean> journalStoreEnableAsyncIoItem = formItem(formItems, "journal-store-enable-async-io");
            final FormItem<Boolean> useJournalStoreItem = formItem(formItems, "use-journal-store");

            ValidationResult validationResult = ValidationResult.OK;

            if (journalStoreEnableAsyncIoItem != null) {
                final boolean journalStoreEnableAsyncIo = journalStoreEnableAsyncIoItem.getValue() != null && journalStoreEnableAsyncIoItem.getValue();
                final boolean useJournalStore = useJournalStoreItem != null && useJournalStoreItem.getValue() != null && useJournalStoreItem.getValue();

                if (journalStoreEnableAsyncIo && !useJournalStore) {
                    useJournalStoreItem.showError("Journal store needs to be enabled before enabling asynchronous IO.");
                    validationResult = invalid;
                }
            }
            return validationResult;
        });
        
        // --------------- form validation for the process attributes
        processForm.addFormValidation(formItems -> {

            ValidationResult validationResult = ValidationResult.OK;
            FormItem<Boolean> uuidItem = formItem(formItems, PROCESS_ID_UUID);
            FormItem<String> socketBindingItem = formItem(formItems, PROCESS_ID_SOCKET_BINDING);
            FormItem<Number> socketMaxPortsItem = formItem(formItems, PROCESS_ID_SOCKET_MAX_PORTS);
            if (uuidItem != null && socketBindingItem != null) {
                boolean uuidGiven = uuidItem.getValue() != null && uuidItem.getValue();
                String socketBinding = Strings.emptyToNull(socketBindingItem.getValue());

                if ((uuidGiven && socketBinding != null) || (!uuidGiven && socketBinding == null)) {
                    socketBindingItem.showError("Please set either UUID or socket binding");
                    validationResult = ValidationResult.invalid("Validation error, see error messages below.");
                }
            }
            if (socketBindingItem != null && socketMaxPortsItem != null) {
                String socketBinding = Strings.emptyToNull(socketBindingItem.getValue());
                Number socketMaxPorts = socketMaxPortsItem.getValue();

                if (socketBinding == null && socketMaxPorts != null && socketMaxPortsItem.isModified()) {
                    socketMaxPortsItem.showError("Can't be set if socket binding is not set");
                    validationResult = invalid;
                }
            }
            return validationResult;
        });
        
        // set the process fields as not required, because uuid and socket-binding are mutually exclusive.
        processForm.getFormItems().forEach(formItem -> formItem.setRequired(false));
        
        processForm.setSaveCallback(new Form.SaveCallback<ModelNode>() {
            @Override
            public void onSave(final Form<ModelNode> f, final Map<String, Object> changeSet) {
                if (!changeSet.isEmpty()) {
                    Boolean uuid;
                    String socketBinding;
                    Integer maxPorts;

                    if (changeSet.containsKey(PROCESS_ID_UUID)) {
                        uuid = (Boolean) changeSet.get(PROCESS_ID_UUID);
                    } else {
                        // if not in changeSet, get current value from edited entity
                        uuid = getCurrentValue(PROCESS_ID_UUID).asBoolean();
                    }

                    if (changeSet.containsKey(PROCESS_ID_SOCKET_BINDING)) {
                        socketBinding = (String) changeSet.get(PROCESS_ID_SOCKET_BINDING);
                    } else {
                        socketBinding = getCurrentValue(PROCESS_ID_SOCKET_BINDING).isDefined() ?
                                getCurrentValue(PROCESS_ID_SOCKET_BINDING).asString() : null;
                    }

                    if (changeSet.containsKey(PROCESS_ID_SOCKET_MAX_PORTS)) {
                        maxPorts = (Integer) changeSet.get(PROCESS_ID_SOCKET_MAX_PORTS);
                    } else {
                        maxPorts = getCurrentValue(PROCESS_ID_SOCKET_MAX_PORTS).isDefined() ?
                                getCurrentValue(PROCESS_ID_SOCKET_MAX_PORTS).asInt() : null;
                    }

                    presenter.saveProcessSettings(uuid, socketBinding, maxPorts);
                }
            }

            private ModelNode getCurrentValue(String field) {
                return processForm.getModel().get(field);
            }

        });
        
        // --------------- form validation for the jdbc attributes
        jdbcForm.addFormValidation(formItems -> {

            ValidationResult validationResult = ValidationResult.OK;

            final FormItem<Boolean> useJdbc = formItem(formItems, "use-jdbc-store");
            final FormItem<String> datasource = formItem(formItems, "jdbc-store-datasource");

            if (useJdbc != null && useJdbc.getValue() == true) {
                if (datasource == null || datasource.getValue() == null || datasource.getValue().isEmpty()) {
                    datasource.showError("Please provide datasource JNDI name if using jdbc store.");
                    validationResult = invalid;
                }
            }
            return validationResult;
        });
        
    }

    private <T> FormItem<T> formItem(Iterable<FormItem> formItems, String name) {
        for (FormItem formItem : formItems) {
            if (name.equals(formItem.getName())) {
                return formItem;
            }
        }
        return null;
    }
    

    @Override
    public void updateConfiguration(final ModelNode configuration) {
        attributesForm.view(configuration);
        processForm.view(configuration);
        recoveryForm.view(configuration);
        pathForm.view(configuration);
        jdbcForm.view(configuration);
    }

    // ------------------------------------------------------ view / mbui contract

    @Override
    public VerticalNavigation getVerticalNavigation() {
        return navigation;
    }
}
