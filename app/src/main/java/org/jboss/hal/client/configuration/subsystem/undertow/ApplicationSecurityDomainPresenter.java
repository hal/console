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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.FilteringStatementContext.Filter;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.APPLICATION_SECURITY_DOMAIN_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.APPLICATION_SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SELECTED_APPLICATION_SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SELECTED_SINGLE_SIGN_ON_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.move;
import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_KEY;

/**
 * @author Claudio Miranda
 */
public class ApplicationSecurityDomainPresenter extends
        ApplicationFinderPresenter<ApplicationSecurityDomainPresenter.MyView, ApplicationSecurityDomainPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(APPLICATION_SECURITY_DOMAIN_ADDRESS)
    @NameToken(NameTokens.UNDERTOW_APPLICATION_SECURITY_DOMAIN)
    public interface MyProxy extends ProxyPlace<ApplicationSecurityDomainPresenter> {}

    public interface MyView extends HalView, HasPresenter<ApplicationSecurityDomainPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on

    private final CrudOperations crud;
    private final ComplexAttributeOperations ca;
    private final MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String appSecurityDomain;

    @Inject
    public ApplicationSecurityDomainPresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final ComplexAttributeOperations ca,
            final MetadataRegistry metadataRegistry,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.ca = ca;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new FilteringStatementContext(statementContext,
                new Filter() {
                    @Override
                    public String filter(final String placeholder) {
                        if (SELECTION_KEY.equals(placeholder)) {
                            return appSecurityDomain;
                        }
                        return null;
                    }

                    @Override
                    public String[] filterTuple(final String placeholder) {
                        return null;
                    }
                });
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        appSecurityDomain = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_APPLICATION_SECURITY_DOMAIN_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(UNDERTOW)
                .append(Ids.UNDERTOW_SETTINGS, Ids.asId(Names.APPLICATION_SECURITY_DOMAIN),
                        resources.constants().settings(), Names.APPLICATION_SECURITY_DOMAIN)
                .append(Ids.UNDERTOW_APP_SECURITY_DOMAIN, Ids.undertowApplicationSecurityDomain(appSecurityDomain),
                        Names.APPLICATION_SECURITY_DOMAIN, appSecurityDomain);
    }

    @Override
    protected void reload() {
        reload(result -> getView().update(result));
    }

    private void reload(Consumer<ModelNode> payload) {
        crud.readRecursive(SELECTED_APPLICATION_SECURITY_DOMAIN_TEMPLATE.resolve(statementContext), payload::accept);
    }

    void save(final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(APPLICATION_SECURITY_DOMAIN_TEMPLATE);
        crud.save(Names.APPLICATION_SECURITY_DOMAIN, appSecurityDomain,
                SELECTED_APPLICATION_SECURITY_DOMAIN_TEMPLATE.resolve(statementContext), changedValues,
                metadata, this::reload);
    }

    void reset(final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(APPLICATION_SECURITY_DOMAIN_TEMPLATE);
        crud.reset(Names.APPLICATION_SECURITY_DOMAIN, appSecurityDomain,
                SELECTED_APPLICATION_SECURITY_DOMAIN_TEMPLATE.resolve(statementContext), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    // ------------------------------------------------------ single sign on

    Operation checkSingleSignOn() {
        ResourceAddress address = SELECTED_SINGLE_SIGN_ON_TEMPLATE.resolve(statementContext);
        return new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, SETTING)
                .build();
    }

    void addSingleSignOn() {
        Metadata metadata = metadataRegistry.lookup(SELECTED_SINGLE_SIGN_ON_TEMPLATE);
        Metadata crMetadata = metadata.forComplexAttribute(CREDENTIAL_REFERENCE, true);
        crMetadata.copyComplexAttributeAtrributes(asList(STORE, ALIAS, TYPE, CLEAR_TEXT), metadata);

        ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(Ids.UNDERTOW_SINGLE_SIGN_ON_ADD, metadata)
                .addOnly()
                .requiredOnly()
                .include(KEY_ALIAS, KEY_STORE, STORE, ALIAS, TYPE, CLEAR_TEXT)
                .unsorted()
                .build();

        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.SINGLE_SIGN_ON),
                form, (name, model) -> {

            if (model != null) {
                move(model, STORE, CREDENTIAL_REFERENCE + "/" + STORE);
                move(model, ALIAS, CREDENTIAL_REFERENCE + "/" + ALIAS);
                move(model, TYPE, CREDENTIAL_REFERENCE + "/" + TYPE);
                move(model, CLEAR_TEXT, CREDENTIAL_REFERENCE + "/" + CLEAR_TEXT);
            }

            ResourceAddress address = SELECTED_SINGLE_SIGN_ON_TEMPLATE.resolve(statementContext);
            crud.addSingleton(Names.SINGLE_SIGN_ON, address, model, a -> reload());
        });
        dialog.show();
    }

    void saveSingleSignOn(final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SINGLE_SIGN_ON_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SELECTED_SINGLE_SIGN_ON_TEMPLATE);
        crud.saveSingleton(Names.SINGLE_SIGN_ON, address, changedValues, metadata, this::reload);
    }

    void resetSingleSignOn(final Form<ModelNode> form) {
        ResourceAddress address = SELECTED_SINGLE_SIGN_ON_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SELECTED_SINGLE_SIGN_ON_TEMPLATE);
        crud.resetSingleton(Names.SINGLE_SIGN_ON, address, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    void removeSingleSignOn(final Form<ModelNode> form) {
        ResourceAddress address = SELECTED_SINGLE_SIGN_ON_TEMPLATE.resolve(statementContext);
        crud.removeSingleton(Names.SINGLE_SIGN_ON, address, new Form.FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    // ------------------------------------------------------ single sign-on credential-reference


    void saveCredentialReference(final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SINGLE_SIGN_ON_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SELECTED_SINGLE_SIGN_ON_TEMPLATE);
        ca.save(CREDENTIAL_REFERENCE, Names.CREDENTIAL_REFERENCE, address, changedValues, metadata, this::reload);
    }

    // ------------------------------------------------------ getter

    StatementContext getStatementContext() {
        return statementContext;
    }

}
