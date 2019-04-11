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
package org.jboss.hal.client.configuration.subsystem.security;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.ResourceCheck;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;
import rx.Completable;

import static org.jboss.hal.client.configuration.subsystem.security.AddressTemplates.SECURITY_DOMAIN_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.security.AddressTemplates.SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.security.AddressTemplates.SELECTED_SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.security.AddressTemplates.TRUST_MODULE_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.meta.token.NameTokens.SECURITY_DOMAIN;

public class SecurityDomainPresenter
        extends MbuiPresenter<SecurityDomainPresenter.MyView, SecurityDomainPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String EQ_WILDCARD = "=*";
    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final Provider<Progress> progress;
    private final MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String securityDomain;

    @Inject
    public SecurityDomainPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            Dispatcher dispatcher,
            CrudOperations crud,
            @Footer Provider<Progress> progress,
            MetadataRegistry metadataRegistry,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.progress = progress;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> securityDomain);
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        securityDomain = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_SECURITY_DOMAIN_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(SECURITY)
                .append(Ids.SECURITY_DOMAIN, Ids.securityDomain(securityDomain), Names.SECURITY_DOMAIN, securityDomain);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_SECURITY_DOMAIN_TEMPLATE.resolve(statementContext);
        crud.readRecursive(address, result -> getView().update(new SecurityDomain(securityDomain, result)));
    }

    void saveSecurityDomain(Map<String, Object> changedValues) {
        crud.save(Names.SECURITY_DOMAIN, securityDomain, SECURITY_DOMAIN_TEMPLATE.replaceWildcards(securityDomain),
                changedValues, this::reload);
    }

    void resetSecurityDomain(Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(SECURITY_DOMAIN_TEMPLATE);
        crud.reset(Names.SECURITY_DOMAIN, securityDomain, SECURITY_DOMAIN_TEMPLATE.replaceWildcards(securityDomain),
                form, metadata, new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    void addClassicAuthenticationModule() {
        // Check if there's already a 'authentication=jaspi' singleton node.
        // Either 'authentication=classic' or 'authentication=jaspi' is allowed not both!
        Operation operation = new Operation.Builder(
                SELECTED_SECURITY_DOMAIN_TEMPLATE.append("authentication=jaspi").resolve(statementContext),
                READ_RESOURCE_OPERATION
        )
                .build();
        dispatcher.execute(operation,
                result -> {
                    // error: there's already a 'authentication=jaspi' singleton
                    MessageEvent.fire(getEventBus(), Message.error(resources.messages().duplicateAuthenticationModule(),
                            resources.messages().duplicateAuthenticationModuleReason()));
                },
                (op, failure) -> {
                    // everything ok: no 'authentication=jaspi' found
                    addModule(Module.AUTHENTICATION);
                });
    }

    void addModule(Module module) {
        // first check for (and add if necessary) the intermediate singleton
        // then add the final resource
        AddressTemplate singletonTemplate = SELECTED_SECURITY_DOMAIN_TEMPLATE.append(module.singleton);
        series(new FlowContext(progress.get()),
                new ResourceCheck(dispatcher, singletonTemplate.resolve(statementContext)),
                context -> {
                    int status = context.pop();
                    if (status == 200) {
                        return Completable.complete();
                    } else {
                        Operation operation = new Operation.Builder(singletonTemplate.resolve(statementContext), ADD)
                                .build();
                        return dispatcher.execute(operation).toCompletable();
                    }
                })
                .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                    @Override
                    public void onSuccess(FlowContext context) {
                        AddressTemplate metadataTemplate = SECURITY_DOMAIN_TEMPLATE
                                .append(module.singleton)
                                .append(module.resource + EQ_WILDCARD);
                        AddressTemplate selectionTemplate = SELECTED_SECURITY_DOMAIN_TEMPLATE
                                .append(module.singleton)
                                .append(module.resource + EQ_WILDCARD);
                        Metadata metadata = metadataRegistry.lookup(metadataTemplate);
                        AddResourceDialog dialog = new AddResourceDialog(module.id,
                                resources.messages().addResourceTitle(module.type),
                                metadata,
                                (name, modelNode) -> {
                                    ResourceAddress address = selectionTemplate.resolve(statementContext, name);
                                    crud.add(module.type, name, address, modelNode, (n, a) -> reload());
                                });
                        dialog.show();
                    }
                });
    }

    void saveModule(Form<NamedNode> form, Map<String, Object> changedValues, Module module) {
        Metadata metadata = metadataRegistry.lookup(SECURITY_DOMAIN_TEMPLATE
                .append(module.singleton)
                .append(module.resource + EQ_WILDCARD));
        String name = form.getModel().getName();
        crud.save(module.type, name,
                SELECTED_SECURITY_DOMAIN_TEMPLATE
                        .append(module.singleton)
                        .append(module.resource + EQ_WILDCARD)
                        .resolve(statementContext, name),
                changedValues, metadata, this::reload);
    }

    void resetModule(Form<NamedNode> form, Module module) {
        Metadata metadata = metadataRegistry.lookup(SECURITY_DOMAIN_TEMPLATE
                .append(module.singleton)
                .append(module.resource + EQ_WILDCARD));
        String name = form.getModel().getName();
        crud.reset(module.type, name,
                SELECTED_SECURITY_DOMAIN_TEMPLATE
                        .append(module.singleton)
                        .append(module.resource + EQ_WILDCARD)
                        .resolve(statementContext, name),
                form, metadata, new FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(Form<NamedNode> form) {
                        reload();
                    }
                });
    }

    void removeModule(Table<NamedNode> table, Module module) {
        //noinspection ConstantConditions
        String name = table.selectedRow().getName();
        crud.remove(module.type, name,
                SELECTED_SECURITY_DOMAIN_TEMPLATE
                        .append(module.singleton)
                        .append(module.resource + EQ_WILDCARD)
                        .resolve(statementContext, name),
                this::reload);
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(SECURITY_DOMAIN)
    @Requires({SECURITY_DOMAIN_ADDRESS, TRUST_MODULE_ADDRESS})
    public interface MyProxy extends ProxyPlace<SecurityDomainPresenter> {
    }

    public interface MyView extends MbuiView<SecurityDomainPresenter> {
        void update(SecurityDomain securityDomain);
    }
    // @formatter:on
}
