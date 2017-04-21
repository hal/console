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
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;


/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class RealmsPresenter extends MbuiPresenter<RealmsPresenter.MyView, RealmsPresenter.MyProxy>
        implements SupportsExpertMode {

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
    private final Resources resources;

    @Inject
    public RealmsPresenter(final EventBus eventBus,
            final RealmsPresenter.MyView view,
            final RealmsPresenter.MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
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

    @Override
    protected void reload() {

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

}
