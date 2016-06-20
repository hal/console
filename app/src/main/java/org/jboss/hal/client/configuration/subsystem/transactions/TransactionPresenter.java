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

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.client.configuration.PathsTypeahead;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
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

import static org.jboss.hal.client.configuration.subsystem.transactions.AddressTemplates.TRANSACTIONS_SUBSYSTEM_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.transactions.AddressTemplates.TRANSACTIONS_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * @author Claudio Miranda
 */
public class TransactionPresenter extends MbuiPresenter<TransactionPresenter.MyView, TransactionPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.TRANSACTIONS)
    @Requires({TRANSACTIONS_SUBSYSTEM_ADDRESS})
    public interface MyProxy extends ProxyPlace<TransactionPresenter> {}

    public interface MyView extends MbuiView<TransactionPresenter>, HasVerticalNavigation {
        void updateConfiguration(ModelNode conf);
    }
    // @formatter:on

    static final String PROCESS_ID_UUID = "process-id-uuid";
    static final String PROCESS_ID_SOCKET_BINDING = "process-id-socket-binding";
    static final String PROCESS_ID_SOCKET_MAX_PORTS = "process-id-socket-max-ports";
    
    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final Resources resources;

    @Inject
    public TransactionPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final Environment environment,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.environment = environment;
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
    protected FinderPath finderPath() {
        return FinderPath
            .subsystemPath(statementContext.selectedProfile(), ModelDescriptionConstants.TRANSACTIONS);
    }

    @Override
    protected void reload() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                TRANSACTIONS_SUBSYSTEM_TEMPLATE.resolve(statementContext))
                .param(RECURSIVE_DEPTH, 1)
                .build();
        dispatcher.execute(operation, result -> {
            // @formatter:off
            getView().updateConfiguration(result);
            // @formatter:on
        });

        PathsTypeahead.updateOperation(environment, dispatcher, statementContext);
    }

    void saveProcessSettings(Boolean uuid, String socketBinding, Integer maxPorts) {
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
        
        Operation undefineUuid = new Operation.Builder("undefine-attribute", address)
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
                    reload();
                }
            }
        });
    }

}
