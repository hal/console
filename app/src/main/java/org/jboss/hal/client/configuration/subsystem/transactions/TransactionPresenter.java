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
import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.transactions.AddressTemplates.TRANSACTIONS_SUBSYSTEM_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.transactions.AddressTemplates.TRANSACTIONS_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * TODO I18n for error / validation messages
 *
 * @author Claudio Miranda
 */
public class TransactionPresenter
        extends MbuiPresenter<TransactionPresenter.MyView, TransactionPresenter.MyProxy>
        implements SupportsExpertMode {

    // datasource address is required as there is a typeahead declared in TransactionView.mbui.xml
    // to lookup datasource subsystem
    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.TRANSACTIONS)
    @Requires({TRANSACTIONS_SUBSYSTEM_ADDRESS, DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS})
    public interface MyProxy extends ProxyPlace<TransactionPresenter> {}

    public interface MyView extends MbuiView<TransactionPresenter> {
        void updateConfiguration(ModelNode conf);
    }
    // @formatter:on

    private static final String PROCESS_ID_UUID = "process-id-uuid";
    private static final String PROCESS_ID_SOCKET_BINDING = "process-id-socket-binding";
    private static final String PROCESS_ID_SOCKET_MAX_PORTS = "process-id-socket-max-ports";
    private final static ValidationResult invalid = ValidationResult
            .invalid("Validation error, see error messages below.");

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final Resources resources;

    @Inject
    public TransactionPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return TRANSACTIONS_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(ModelDescriptionConstants.TRANSACTIONS);
    }

    @Override
    protected void reload() {
        crud.read(TRANSACTIONS_SUBSYSTEM_TEMPLATE, 1, result -> getView().updateConfiguration(result));
    }

    // The process form, contains attributes that must have some special treatment before save operation
    // the process-uuid and process-id-socket-binding are mutually exclusive
    // this is called from process-form in TransactionView.mbui.xml
    void saveProcessForm(Form<ModelNode> form, Map<String, Object> changeSet) {
        if (!changeSet.isEmpty()) {
            Boolean uuid;
            String socketBinding;
            Integer maxPorts;

            if (changeSet.containsKey(PROCESS_ID_UUID)) {
                uuid = (Boolean) changeSet.get(PROCESS_ID_UUID);
            } else {
                // if not in changeSet, get current value from edited entity
                uuid = form.getModel().get(PROCESS_ID_UUID).asBoolean();
            }

            if (changeSet.containsKey(PROCESS_ID_SOCKET_BINDING)) {
                socketBinding = (String) changeSet.get(PROCESS_ID_SOCKET_BINDING);
            } else {
                socketBinding = form.getModel().get(PROCESS_ID_SOCKET_BINDING).isDefined() ?
                        form.getModel().get(PROCESS_ID_SOCKET_BINDING).asString() : null;
            }

            if (changeSet.containsKey(PROCESS_ID_SOCKET_MAX_PORTS)) {
                maxPorts = (Integer) changeSet.get(PROCESS_ID_SOCKET_MAX_PORTS);
            } else {
                maxPorts = form.getModel().get(PROCESS_ID_SOCKET_MAX_PORTS).isDefined() ?
                        form.getModel().get(PROCESS_ID_SOCKET_MAX_PORTS).asInt() : null;
            }

            boolean socketBindingEmpty = socketBinding == null || socketBinding.trim().length() == 0;

            if (uuid != null && socketBindingEmpty) {
                switchToUuid();
            } else if (!socketBindingEmpty && (uuid == null || !uuid)) {
                switchToSocketBinding(socketBinding, maxPorts);
            } else {
                MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().transactionSetUuidOrSocket()));
            }
        }
    }

    private void switchToUuid() {
        ResourceAddress address = TRANSACTIONS_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation op = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                .param(NAME, PROCESS_ID_UUID)
                .param(VALUE, true)
                .build();
        dispatcher.execute(op, result -> {
            if (result.isFailure()) {
                MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().transactionUnableSetProcessId(),
                                result.getFailureDescription()));
            } else {
                MessageEvent.fire(getEventBus(),
                        Message.success(resources.messages().modifySingleResourceSuccess("Process")));
                reload();
            }
        });
    }

    private void switchToSocketBinding(String socketBinding, Integer maxPorts) {
        Composite composite;
        ResourceAddress address = TRANSACTIONS_SUBSYSTEM_TEMPLATE.resolve(statementContext);

        Operation writeSocketBinding = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                .param(NAME, PROCESS_ID_SOCKET_BINDING)
                .param(VALUE, socketBinding)
                .build();

        Operation undefineUuid = new Operation.Builder(UNDEFINE_ATTRIBUTE_OPERATION, address)
                .param(NAME, PROCESS_ID_UUID)
                .build();

        if (maxPorts != null) {
            Operation writeMaxPorts = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                    .param(NAME, PROCESS_ID_SOCKET_MAX_PORTS)
                    .param(VALUE, maxPorts)
                    .build();
            composite = new Composite(undefineUuid, writeSocketBinding, writeMaxPorts);
        } else {
            composite = new Composite(undefineUuid, writeSocketBinding);
        }

        dispatcher.execute(composite, new Dispatcher.CompositeCallback() {
            @Override
            public void onSuccess(final CompositeResult result) {

                ModelNode writeSocketResult = result.step(0);
                ModelNode undefineUuidResult = result.step(1);

                boolean failed = writeSocketResult.isFailure() || undefineUuidResult.isFailure();
                if (failed) {
                    String failMessage = writeSocketBinding.isFailure() ? writeSocketBinding.getFailureDescription()
                            : undefineUuidResult.getFailureDescription();
                    MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().transactionUnableSetProcessId(), failMessage));
                } else {
                    MessageEvent.fire(getEventBus(),
                            Message.success(resources.messages().modifySingleResourceSuccess("Process")));
                    reload();
                }
            }
        });
    }

    private FormValidation<ModelNode> attributesFormValidation = form -> {

        final FormItem<Boolean> journalStoreEnableAsyncIoItem = form
                .getFormItem("journal-store-enable-async-io");
        final FormItem<Boolean> useJournalStoreItem = form.getFormItem("use-journal-store");

        ValidationResult validationResult = ValidationResult.OK;

        if (journalStoreEnableAsyncIoItem != null) {
            final boolean journalStoreEnableAsyncIo = journalStoreEnableAsyncIoItem
                    .getValue() != null && journalStoreEnableAsyncIoItem.getValue();
            final boolean useJournalStore = useJournalStoreItem != null && useJournalStoreItem
                    .getValue() != null && useJournalStoreItem.getValue();

            if (journalStoreEnableAsyncIo && !useJournalStore) {
                useJournalStoreItem
                        .showError("Journal store needs to be enabled before enabling asynchronous IO.");
                validationResult = invalid;
            }
        }
        return validationResult;
    };

    private FormValidation<ModelNode> processFormValidation = form -> {

        ValidationResult validationResult = ValidationResult.OK;
        FormItem<Boolean> uuidItem = form.getFormItem(PROCESS_ID_UUID);
        FormItem<String> socketBindingItem = form.getFormItem(PROCESS_ID_SOCKET_BINDING);
        FormItem<Number> socketMaxPortsItem = form.getFormItem(PROCESS_ID_SOCKET_MAX_PORTS);
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
    };

    private FormValidation<ModelNode> jdbcFormValidation = form -> {

        ValidationResult validationResult = ValidationResult.OK;

        final FormItem<Boolean> useJdbc = form.getFormItem("use-jdbc-store");
        final FormItem<String> datasource = form.getFormItem("jdbc-store-datasource");

        if (useJdbc != null && useJdbc.getValue()) {
            if (datasource == null || datasource.getValue() == null || datasource.getValue().isEmpty()) {
                datasource.showError("Please provide datasource JNDI name if using jdbc store.");
                validationResult = invalid;
            }
        }
        return validationResult;
    };

    FormValidation<ModelNode> getAttributesFormValidation() {
        return attributesFormValidation;
    }

    FormValidation<ModelNode> getProcessFormValidation() {
        return processFormValidation;
    }

    FormValidation<ModelNode> getJdbcFormValidation() {
        return jdbcFormValidation;
    }
}
