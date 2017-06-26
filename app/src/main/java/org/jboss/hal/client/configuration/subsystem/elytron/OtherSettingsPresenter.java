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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;


/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class OtherSettingsPresenter extends MbuiPresenter<OtherSettingsPresenter.MyView, OtherSettingsPresenter.MyProxy>
        implements SupportsExpertMode, ElytronPresenter {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(value ={
        KEY_STORE, KEY_MANAGER, SERVER_SSL_CONTEXT, CLIENT_SSL_CONTEXT, TRUST_MANAGER, CREDENTIAL_STORE,
        FILTERING_KEY_STORE, LDAP_KEY_STORE, PROVIDER_LOADER, AGGREGATE_PROVIDERS, SECURITY_DOMAIN, SECURITY_PROPERTY,
        DIR_CONTEXT, AUTHENTICATION_CONTEXT, AUTHENTICATION_CONF
    })
    @NameToken(NameTokens.ELYTRON_OTHER)
    public interface MyProxy extends ProxyPlace<OtherSettingsPresenter> {}

    public interface MyView extends MbuiView<OtherSettingsPresenter> {
        void updateKeyStore(List<NamedNode> model);
        void updateKeyManagers(List<NamedNode> model);
        void updateServerSslContext(List<NamedNode> model);
        void updateClientSslContext(List<NamedNode> model);
        void updateTrustManagers(List<NamedNode> model);
        void updateCredentialStore(List<NamedNode> model);
        void updateFilteringKeyStore(List<NamedNode> model);
        void updateLdapKeyStore(List<NamedNode> model);
        void updateProviderLoader(List<NamedNode> model);
        void updateAggregateProviders(List<NamedNode> model);
        void updateSecurityDomain(List<NamedNode> model);
        void updateSecurityProperty(List<NamedNode> model);
        void updateDirContext(List<NamedNode> model);
        void updateAuthenticationContext(List<NamedNode> model);
        void updateAuthenticationConfiguration(List<NamedNode> model);
    }
    // @formatter:on

    private EventBus eventBus;
    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private MetadataRegistry metadataRegistry;
    private final Resources resources;

    @Inject
    public OtherSettingsPresenter(final EventBus eventBus,
            final OtherSettingsPresenter.MyView view,
            final OtherSettingsPresenter.MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.eventBus = eventBus;
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ELYTRON_SUBSYSTEM_ADDRESS.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(Ids.ELYTRON)
                .append(Ids.ELYTRON, Ids.asId(Names.OTHER_SETTINGS),
                        resources.constants().settings(), Names.OTHER_SETTINGS);
    }

    @Override
    public void reload() {

        ResourceAddress address = ELYTRON_SUBSYSTEM_ADDRESS.resolve(statementContext);
        crud.readChildren(address, asList(
                "key-store",
                "key-manager",
                "server-ssl-context",
                "client-ssl-context",
                "trust-manager",
                "credential-store",
                "filtering-key-store",
                "ldap-key-store",
                "provider-loader",
                "aggregate-providers",
                "security-domain",
                "security-property",
                "dir-context",
                "authentication-context",
                "authentication-configuration"
                ),
                result -> {
                    // @formatter:off
                    getView().updateKeyStore(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateKeyManagers(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateServerSslContext(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateClientSslContext(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateTrustManagers(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateCredentialStore(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                    getView().updateFilteringKeyStore(asNamedNodes(result.step(6).get(RESULT).asPropertyList()));
                    getView().updateLdapKeyStore(asNamedNodes(result.step(7).get(RESULT).asPropertyList()));
                    getView().updateProviderLoader(asNamedNodes(result.step(8).get(RESULT).asPropertyList()));
                    getView().updateAggregateProviders(asNamedNodes(result.step(9).get(RESULT).asPropertyList()));
                    getView().updateSecurityDomain(asNamedNodes(result.step(10).get(RESULT).asPropertyList()));
                    getView().updateSecurityProperty(asNamedNodes(result.step(11).get(RESULT).asPropertyList()));
                    getView().updateDirContext(asNamedNodes(result.step(12).get(RESULT).asPropertyList()));
                    getView().updateAuthenticationContext(asNamedNodes(result.step(13).get(RESULT).asPropertyList()));
                    getView().updateAuthenticationConfiguration(asNamedNodes(result.step(14).get(RESULT).asPropertyList()));
                    // @formatter:on
                });
    }

    @Override
    public void saveForm(final String title, final String name, final AddressTemplate template,
            final Map<String, Object> changedValues, final Metadata metadata) {

        ResourceAddress address = template.resolve(statementContext, name);
        crud.save(title, name, address, changedValues, metadata, () -> reload());
    }

    @Override
    public void saveComplexForm(final String title, final String name, String complexAttributeName, final AddressTemplate template,
            final Map<String, Object> changedValues, final Metadata metadata) {

        ResourceAddress address = template.resolve(statementContext, name);
        crud.save(title, name, complexAttributeName, address, changedValues, metadata, () -> reload());
    }

    @Override
    public void listAdd(final String title, final String name, String complexAttributeName,
            final AddressTemplate template,
            final Map<String, Object> changedValues, final Metadata metadata) {

        ResourceAddress address = template.resolve(statementContext, name);
        crud.listAdd(title, name, complexAttributeName, address, changedValues, metadata, () -> reload());
    }

    @Override
    public void listRemove(String title, String resourceName, String complexAttributeName, int index,
            AddressTemplate template) {

        ResourceAddress address = template.resolve(statementContext, resourceName);
        crud.listRemove(title, resourceName, complexAttributeName, index, address, () -> reload());
    }


    @Override
    public void resetComplexAttribute(final String type, final String name, final AddressTemplate template,
            final Set<String> attributes, final Metadata metadata, final Callback callback) {
        ResourceAddress address = template.resolve(statementContext, name);
        crud.reset(type, type + " from " + name, address, attributes, metadata, callback);
    }

    @Override
    public void launchAddDialog(AddressTemplate template, Function<String, String> resourceNameFunction, String complexAttributeName,
            Metadata metadata, String title) {

        String id = Ids.build(complexAttributeName, Ids.FORM_SUFFIX, Ids.ADD_SUFFIX);
        ResourceAddress address = template.resolve(statementContext, resourceNameFunction.apply(null));

        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .fromRequestProperties()
                .build();

        AddResourceDialog dialog = new AddResourceDialog(title, form,
                (name, model) -> crud.listAdd(title, name, complexAttributeName, address, model, () -> reload()));
        dialog.show();
    }

}
