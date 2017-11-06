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
package org.jboss.hal.client.runtime.managementoperations;

import java.util.List;

import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.meta.token.NameTokens.MANAGEMENT_OPERATIONS;

public class ManagementOperationsPresenter extends
        ApplicationFinderPresenter<ManagementOperationsPresenter.MyView, ManagementOperationsPresenter.MyProxy> {

    public static final String MANAGEMENT_OPERATIONS_ADDRESS = "{selected.host}/core-service=management/service=management-operations";
    public static final String ACTIVE_OPERATIONS_ADDRESS = "{selected.host}/core-service=management/service=management-operations/active-operation=*";
    public static final AddressTemplate MANAGEMENT_OPERATIONS_TEMPLATE = AddressTemplate.of(
            MANAGEMENT_OPERATIONS_ADDRESS);
    public static final AddressTemplate ACTIVE_OPERATIONS_TEMPLATE = AddressTemplate.of(ACTIVE_OPERATIONS_ADDRESS);

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private EventBus eventBus;
    private Environment environment;

    @Inject
    public ManagementOperationsPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            Environment environment,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.eventBus = eventBus;
        this.environment = environment;
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public FinderPath finderPath() {
        return environment.isStandalone() ? finderPathFactory.runtimeServerPath() : finderPathFactory.runtimeHostPath();
    }

    public StatementContext getStatementContext() {
        return statementContext;
    }

    @Override
    protected void reload() {
        ResourceAddress addressFindNP = MANAGEMENT_OPERATIONS_TEMPLATE.resolve(statementContext);
        Operation operationFindNP = new Operation.Builder(addressFindNP, FIND_NON_PROGRESSING_OPERATION)
                .build();

        ResourceAddress addressMO = MANAGEMENT_OPERATIONS_TEMPLATE.resolve(statementContext);
        Operation operationMO = new Operation.Builder(addressMO, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, ACTIVE_OPERATION)
                .build();

        dispatcher.execute(new Composite(operationFindNP, operationMO), (CompositeResult result) -> {

            ModelNode resultNP = result.step(0).get(RESULT);
            ModelNode resultOperations = result.step(1).get(RESULT);
            final String nonProgressingId = resultNP.isDefined() ? resultNP.asString() : null;
            getView().update(asNamedNodes(resultOperations.asPropertyList()), nonProgressingId);

        });
    }

    void cancelNonProgressingOperation() {
        ResourceAddress address = MANAGEMENT_OPERATIONS_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, CANCEL_NON_PROGRESSING_OPERATION)
                .build();
        dispatcher.execute(operation,
                result -> {
                    MessageEvent.fire(eventBus,
                            Message.info(resources.messages().cancelledOperation(result.asString())));
                    reload();
                },
                (operation1, failure) -> {
                    MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromString(failure)));
                    reload();
                },
                (operation1, exception) -> {
                    MessageEvent.fire(eventBus,
                            Message.error(SafeHtmlUtils.fromString(exception.getMessage())));
                    reload();
                });
    }

    public void cancel(ManagementOperations item) {
        DialogFactory.showConfirmation(resources.constants().cancelActiveOperation(),
                resources.messages().cancelActiveOperation(item.getName()),
                () -> {
                    ResourceAddress address = ACTIVE_OPERATIONS_TEMPLATE.resolve(statementContext, item.getName());
                    Operation operation = new Operation.Builder(address, CANCEL_OPERATION)
                            .build();
                    dispatcher.execute(operation, result -> reload());
                });
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(MANAGEMENT_OPERATIONS)
    @Requires({MANAGEMENT_OPERATIONS_ADDRESS, ACTIVE_OPERATIONS_ADDRESS})
    public interface MyProxy extends ProxyPlace<ManagementOperationsPresenter> {

    }

    public interface MyView extends HalView, HasPresenter<ManagementOperationsPresenter> {
        void update(List<NamedNode> nodes, String nonProgressingOperation);
    }
    // @formatter:on
}
