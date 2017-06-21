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
import org.jboss.hal.dmr.ModelNodeHelper;
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
public class RealmsPresenter extends MbuiPresenter<RealmsPresenter.MyView, RealmsPresenter.MyProxy>
        implements SupportsExpertMode, ElytronPresenter {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(value ={
        PROPERTIES_REALM, FILESYSTEM_REALM, CACHING_REALM, JDBC_REALM, LDAP_REALM, KEYSTORE_REALM, AGGREGATE_REALM,
        CUSTOM_MODIFIABLE_REALM, CUSTOM_REALM, IDENTITY_REALM, TOKEN_REALM, MAPPED_REGEX_REALM_MAPPER,
        SIMPLE_REGEX_REALM_MAPPER, CUSTOM_REALM_MAPPER, CONSTANT_REALM_MAPPER
    })
    @NameToken(NameTokens.ELYTRON_SECURITY_REALMS)
    public interface MyProxy extends ProxyPlace<RealmsPresenter> {}

    public interface MyView extends MbuiView<RealmsPresenter> {
        void updateAggregateRealm(List<NamedNode> model);
        void updateCachingRealm(List<NamedNode> model);
        void updateCustomModifiableRealm(List<NamedNode> model);
        void updateCustomRealm(List<NamedNode> model);
        void updateFilesystemRealm(List<NamedNode> model);
        void updateIdentityRealm(List<NamedNode> model);
        void updateJdbcRealm(List<NamedNode> model);
        void updateKeyStoreRealm(List<NamedNode> model);
        void updateLdapRealm(List<NamedNode> model);
        void updatePropertiesRealm(List<NamedNode> model);
        void updateTokenRealm(List<NamedNode> model);
        void updateConstantRealmMapper(List<NamedNode> model);
        void updateCustomRealmMapper(List<NamedNode> model);
        void updateMappedRegexRealmMapper(List<NamedNode> model);
        void updateSimpleRegexRealmMapper(List<NamedNode> model);

    }
    // @formatter:on

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private MetadataRegistry metadataRegistry;
    private final Resources resources;

    @Inject
    public RealmsPresenter(final EventBus eventBus,
            final RealmsPresenter.MyView view,
            final RealmsPresenter.MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
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
                .append(Ids.ELYTRON, Ids.asId(Names.SECURITY_REALMS),
                        resources.constants().settings(), Names.SECURITY_REALMS);
    }

    public void addLDAPRealm() {

        Metadata metadata = metadataRegistry.lookup(LDAP_REALM_ADDRESS);

        // repackage "identity-mapping" as it is a required attribute to be displayed in the form of the ADD dialog.
        String complexAttributeName = "identity-mapping";
        // the repackaged attribute must be prefixed so, the user knowns where it comes from.
        Metadata nestedMetadata = metadata.repackageComplexAttribute(complexAttributeName, true, true, true);

        new AddResourceDialog(Ids.build(Ids.ELYTRON_LDAP_REALM, Ids.ADD_SUFFIX), resources.messages().addResourceTitle(Names.ELYTRON_LDAP_REALM), nestedMetadata,
                (name, model) -> {
                    // once the model is posted, it must be correctly assembled as the attributes are not correct,
                    // related to the r-r-d
                    ModelNodeHelper.reassembleComplexAttribute(complexAttributeName, model, true);
                    ResourceAddress address = LDAP_REALM_ADDRESS.resolve(statementContext, name);
                    crud.add(Names.ELYTRON_LDAP_REALM, name, address, model, (name1, address1) -> reload());
                }).show();

    }

    public void addPropertiesRealm() {

        Metadata metadata = metadataRegistry.lookup(PROPERTIES_REALM_ADDRESS);

        // repackage "users-properties" as it is a required attribute to be displayed in the form of the ADD dialog.
        String complexAttributeName = "users-properties";
        // the repackaged attribute must be prefixed so, the user knowns where it comes from.
        Metadata nestedMetadata = metadata.repackageComplexAttribute(complexAttributeName, true, true, true);

        new AddResourceDialog(Ids.build(Ids.ELYTRON_PROPERTIES_REALM, Ids.ADD_SUFFIX), resources.messages().addResourceTitle(Names.ELYTRON_PROPERTIES_REALM), nestedMetadata,
                (name, model) -> {
                    ModelNodeHelper.reassembleComplexAttribute(complexAttributeName, model, true);
                    ResourceAddress address = PROPERTIES_REALM_ADDRESS.resolve(statementContext, name);
                    crud.add(Names.ELYTRON_PROPERTIES_REALM, name, address, model, (name1, address1) -> reload());
                }).show();

    }

    @Override
    public void reload() {

        ResourceAddress address = ELYTRON_SUBSYSTEM_ADDRESS.resolve(statementContext);
        crud.readChildren(address, asList(
                "aggregate-realm",
                "caching-realm",
                "custom-modifiable-realm",
                "custom-realm",
                "filesystem-realm",
                "identity-realm",
                "jdbc-realm",
                "key-store-realm",
                "ldap-realm",
                "properties-realm",
                "token-realm",
                "constant-realm-mapper",
                "custom-realm-mapper",
                "mapped-regex-realm-mapper",
                "simple-regex-realm-mapper"
                ),
                result -> {
                    // @formatter:off
                    getView().updateAggregateRealm(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateCachingRealm(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateCustomModifiableRealm(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateCustomRealm(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateFilesystemRealm(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateIdentityRealm(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                    getView().updateJdbcRealm(asNamedNodes(result.step(6).get(RESULT).asPropertyList()));
                    getView().updateKeyStoreRealm(asNamedNodes(result.step(7).get(RESULT).asPropertyList()));
                    getView().updateLdapRealm(asNamedNodes(result.step(8).get(RESULT).asPropertyList()));
                    getView().updatePropertiesRealm(asNamedNodes(result.step(9).get(RESULT).asPropertyList()));
                    getView().updateTokenRealm(asNamedNodes(result.step(10).get(RESULT).asPropertyList()));
                    getView().updateConstantRealmMapper(asNamedNodes(result.step(11).get(RESULT).asPropertyList()));
                    getView().updateCustomRealmMapper(asNamedNodes(result.step(12).get(RESULT).asPropertyList()));
                    getView().updateMappedRegexRealmMapper(asNamedNodes(result.step(13).get(RESULT).asPropertyList()));
                    getView().updateSimpleRegexRealmMapper(asNamedNodes(result.step(14).get(RESULT).asPropertyList()));
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
    public void listAdd(final String title, final String name, String complexAttributeName, final AddressTemplate template,
            final Map<String, Object> changedValues, final Metadata metadata) {

        ResourceAddress address = template.resolve(statementContext, name);
        crud.listAdd(title, name, complexAttributeName, address, changedValues, metadata, () -> reload());
    }

    @Override
    public void resetComplexAttribute(final String type, final String name, final AddressTemplate template,
            final Set<String> attributes,
            final Metadata metadata, final Callback callback) {

        ResourceAddress address = template.resolve(statementContext, name);
        crud.reset(type, name, address, attributes, metadata, callback);
    }

    public void launchOnAddJDBCRealm() {

    }

    @Override
    public void launchAddDialog(AddressTemplate template, Function<String,String> resourceNameFunction, String complexAttributeName,
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

    @Override
    public void listRemove(String title, String resourceName, String complexAttributeName, int index,
            AddressTemplate template) {

        ResourceAddress address = template.resolve(statementContext, resourceName);
        crud.listRemove(title, resourceName, complexAttributeName, index, address, () -> reload());
    }

}
