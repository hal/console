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

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.mvp.SubsystemPresenter;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.*;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.Map;

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

    public interface MyView extends PatternFlyView, HasPresenter<EEPresenter> {
        void update(ModelNode eeData);
    }
    // @formatter:on

    private final Resources resources;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final OperationFactory operationFactory;
    private final EventBus eventBus;
    private final Capabilities capabilities;
    private ResourceDescription globalModulesResourceDescription;

    @Inject
    public EEPresenter(final EventBus eventBus,
                       final MyView view,
                       final MyProxy proxy,
                       final Finder finder,
                       final Resources resources,
                       final Dispatcher dispatcher,
                       final StatementContext statementContext,
                       final ResourceDescriptions descriptions,
                       final Capabilities capabilities) {
        super(eventBus, view, proxy, finder);

        this.resources = resources;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.operationFactory = new OperationFactory();
        this.eventBus = eventBus;
        this.capabilities = capabilities;

        ResourceDescription descriptionEESbsystem = descriptions.lookup(AddressTemplates.EE_SUBSYSTEM_TEMPLATE);

        globalModulesResourceDescription = new ResourceDescription();
        for (Property e: descriptionEESbsystem.getAttributes()) {
            if (GLOBAL_MODULES.equals(e.getName())) {
                ModelNode modelNode = e.getValue().get(VALUE_TYPE);
                globalModulesResourceDescription.set(modelNode);
                break;
            }
        }
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadEESubsystem();
    }

    @Override
    protected FinderPath finderPath() {
        return FinderPath.subsystemPath(statementContext.selectedProfile(), AddressTemplates.EE_SUBSYSTEM_TEMPLATE.lastValue());
    }

    void loadEESubsystem() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                AddressTemplates.EE_SUBSYSTEM_TEMPLATE.resolve(statementContext))
                .build();
        operation.get(RECURSIVE).set(true);
        dispatcher.execute(operation, result -> getView().update(result));
    }

    void saveAttributes(final Map<String, Object> changedValues) {
        ResourceAddress resourceAddress = AddressTemplates.EE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Composite composite = operationFactory.fromChangeSet(resourceAddress, changedValues);

        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifyResourceSuccess(Names.EE, resources.constants().deploymentAttributes())));
            loadEESubsystem();
        });
    }

    public void saveDefaultBindings(Map<String, Object> changedValues) {
        ResourceAddress resourceAddress = AddressTemplates.SERVICE_DEFAULT_BINDINGS_TEMPLATE.resolve(statementContext);
        Composite composite = operationFactory.fromChangeSet(resourceAddress, changedValues);

        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifyResourceSuccess(Names.EE, resources.constants().defaultBindings())));
            loadEESubsystem();
        });

    }

    void launchAddDialogGlobalModule() {
        
        Metadata metadata = new Metadata(SecurityContext.RWX, globalModulesResourceDescription, capabilities);

        Form<GlobalModule> form = new ModelNodeForm.Builder<GlobalModule>(Ids.EE_GLOBAL_MODULES_FORM, metadata)
                .addOnly()
                .createResource()
                .attributeTypeList()
                .include("name", "slot", "annotations", "services", "meta-inf")
                .unsorted()
                .onSave((form2, changedValues) -> {
                    GlobalModule globalModule = form2.getModel();

                    ModelNode opListAdd = new ModelNode();
                    opListAdd.get(ModelDescriptionConstants.ADDRESS).set(AddressTemplates.EE_SUBSYSTEM_TEMPLATE.resolve(statementContext));
                    opListAdd.get(OP).set(LIST_ADD);
                    opListAdd.get(NAME).set(GLOBAL_MODULES);
                    opListAdd.get(VALUE).set(globalModule);
                    Operation op = new Operation(opListAdd);

                    dispatcher.execute(op, result -> loadEESubsystem());
                    
                })
                .build();

        
        Element addPage = new Elements.Builder()
                .div()
                .p().textContent(resources.constants().globalModuleAdd()).end()
                .add(form.asElement())
                .end().build();

        form.add(new GlobalModule());
        Dialog dialog = new Dialog.Builder(resources.constants().globalModuleAdd())
                .add(addPage)
                .primary(() -> {
                    form.save();
                    return true;
                })
                .secondary(() -> {
                    form.cancel();
                    return true;
                })
                .closeIcon(false)
                .closeOnEsc(false)
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        
    }

    public void removeGlobalModule(Api<ModelNode> tableApi) {
        Dialog dialog = DialogFactory.confirmation(resources.messages().removeResourceConfirmationTitle(resources.constants().globalModules()),
                resources.messages().removeResourceConfirmationQuestion(tableApi.selectedRow().get(NAME).asString()),
                () -> {

                    ModelNode opListRemove = new ModelNode();
                    opListRemove.get(ModelDescriptionConstants.ADDRESS).set(AddressTemplates.EE_SUBSYSTEM_TEMPLATE.resolve(statementContext));
                    opListRemove.get(OP).set(LIST_REMOVE);
                    opListRemove.get(NAME).set(GLOBAL_MODULES);
                    opListRemove.get(VALUE).set(tableApi.selectedRow());
                    Operation op = new Operation(opListRemove);
                    
                    dispatcher.execute(op, result -> {
                        MessageEvent.fire(eventBus, Message.success(
                                resources.messages().removeResourceSuccess("Global Module", tableApi.selectedRow().get(NAME).asString())));
                        loadEESubsystem();
                    });
                    return true;
                });
        dialog.show();
    }



}
