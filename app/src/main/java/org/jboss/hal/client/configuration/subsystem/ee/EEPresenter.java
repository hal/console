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
package org.jboss.hal.client.configuration.subsystem.ee;

import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.mvp.SubsystemPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Claudio Miranda
 */
public class EEPresenter extends SubsystemPresenter<EEPresenter.MyView, EEPresenter.MyProxy> {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.EE)
    @Requires(AddressTemplates.EE_ADDRESS)
    public interface MyProxy extends ProxyPlace<EEPresenter> {}

    public interface MyView extends PatternFlyView, HasVerticalNavigation, HasPresenter<EEPresenter> {
        void reset();
        void update(ModelNode eeData);
    }
    // @formatter:on


    static Metadata globalModulesMetadata(final SecurityFramework securityFramework,
            final ResourceDescriptions descriptions, final Capabilities capabilities) {
        SecurityContext securityContext = securityFramework.lookup(AddressTemplates.EE_SUBSYSTEM_TEMPLATE);
        ResourceDescription eeDescription = descriptions.lookup(AddressTemplates.EE_SUBSYSTEM_TEMPLATE);

        ResourceDescription globalModulesDescription;
        Property globalModules = eeDescription.findAttribute(ATTRIBUTES, GLOBAL_MODULES);
        if (globalModules != null && globalModules.getValue().hasDefined(VALUE_TYPE)) {
            ModelNode repackaged = new ModelNode();
            repackaged.get(ATTRIBUTES).set(globalModules.getValue().get(VALUE_TYPE));
            globalModulesDescription = new ResourceDescription(repackaged);
        } else {
            globalModulesDescription = new ResourceDescription(new ModelNode());
        }
        return new Metadata(securityContext, globalModulesDescription, capabilities);
    }

    private final Resources resources;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final OperationFactory operationFactory;
    private final EventBus eventBus;
    private final Metadata globalModulesMetadata;

    @Inject
    public EEPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final Resources resources,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final SecurityFramework securityFramework,
            final ResourceDescriptions descriptions,
            final Capabilities capabilities) {
        super(eventBus, view, proxy, finder);

        this.resources = resources;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.operationFactory = new OperationFactory();
        this.eventBus = eventBus;
        this.globalModulesMetadata = globalModulesMetadata(securityFramework, descriptions, capabilities);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().reset();
        loadEESubsystem();
    }

    @Override
    protected FinderPath finderPath() {
        return FinderPath
                .subsystemPath(statementContext.selectedProfile(), AddressTemplates.EE_SUBSYSTEM_TEMPLATE.lastValue());
    }

    private void loadEESubsystem() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                AddressTemplates.EE_SUBSYSTEM_TEMPLATE.resolve(statementContext))
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> getView().update(result));
    }

    void saveAttributes(final Map<String, Object> changedValues) {
        ResourceAddress resourceAddress = AddressTemplates.EE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Composite composite = operationFactory.fromChangeSet(resourceAddress, changedValues);

        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages()
                            .modifyResourceSuccess(Names.EE, resources.constants().deploymentAttributes())));
            loadEESubsystem();
        });
    }

    void saveDefaultBindings(Map<String, Object> changedValues) {
        ResourceAddress resourceAddress = AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE.resolve(statementContext);
        Composite composite = operationFactory.fromChangeSet(resourceAddress, changedValues);

        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages()
                            .modifyResourceSuccess(Names.EE, resources.constants().defaultBindings())));
            loadEESubsystem();
        });

    }

    void launchAddDialogGlobalModule() {
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.EE_GLOBAL_MODULES_FORM, globalModulesMetadata)
                .addOnly()
                .include("name", "slot", "annotations", "services", "meta-inf")
                .unsorted()
                .build();

        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.GLOBAL_MODULES),
                form, (name, globalModule) -> {
            ResourceAddress address = AddressTemplates.EE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(LIST_ADD, address)
                    .param(NAME, GLOBAL_MODULES)
                    .param(VALUE, globalModule)
                    .build();
            dispatcher.execute(operation, result -> {
                MessageEvent.fire(eventBus, Message.success(
                        resources.messages().addResourceSuccess(Names.GLOBAL_MODULES, name)));
                loadEESubsystem();
            });
        });

        dialog.show();
    }

    void removeGlobalModule(ModelNode globalModule) {
        String name = globalModule.get(NAME).asString();
        Dialog dialog = DialogFactory.confirmation(
                resources.messages().removeResourceConfirmationTitle(Names.GLOBAL_MODULES),
                resources.messages().removeResourceConfirmationQuestion(name),
                () -> {
                    ResourceAddress address = AddressTemplates.EE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
                    Operation operation = new Operation.Builder(LIST_REMOVE, address)
                            .param(NAME, GLOBAL_MODULES)
                            .param(VALUE, globalModule)
                            .build();
                    dispatcher.execute(operation, result -> {
                        MessageEvent.fire(eventBus, Message.success(
                                resources.messages().removeResourceSuccess(Names.GLOBAL_MODULES, name)));
                        loadEESubsystem();
                    });
                    return true;
                });
        dialog.show();
    }
}
