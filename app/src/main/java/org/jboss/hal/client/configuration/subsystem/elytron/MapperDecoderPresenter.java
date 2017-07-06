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
import java.util.function.Consumer;
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
import static org.jboss.hal.client.configuration.subsystem.elytron.ElytronResource.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;


/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class MapperDecoderPresenter extends MbuiPresenter<MapperDecoderPresenter.MyView, MapperDecoderPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(value = {
            ADD_PREFIX_ROLE_MAPPER_ADDRESS,
            ADD_SUFFIX_ROLE_MAPPER_ADDRESS,
            AGGREGATE_PRINCIPAL_DECODER_ADDRESS,
            AGGREGATE_ROLE_MAPPER_ADDRESS,
            CONCATENATING_PRINCIPAL_DECODER_ADDRESS,
            CONSTANT_PERMISSION_MAPPER_ADDRESS,
            CONSTANT_PRINCIPAL_DECODER_ADDRESS,
            CONSTANT_ROLE_MAPPER_ADDRESS,
            CUSTOM_PERMISSION_MAPPER_ADDRESS,
            CUSTOM_PRINCIPAL_DECODER_ADDRESS,
            CUSTOM_ROLE_DECODER_ADDRESS,
            CUSTOM_ROLE_MAPPER_ADDRESS,
            LOGICAL_PERMISSION_MAPPER_ADDRESS,
            LOGICAL_ROLE_MAPPER_ADDRESS,
            SIMPLE_PERMISSION_MAPPER_ADDRESS,
            SIMPLE_ROLE_DECODER_ADDRESS,
            X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS})
    @NameToken(NameTokens.ELYTRON_MAPPERS_DECODERS)
    public interface MyProxy extends ProxyPlace<MapperDecoderPresenter> {}

    public interface MyView extends MbuiView<MapperDecoderPresenter> {
        void updateAddPrefixRoleMapper(List<NamedNode> model);
        void updateAddSuffixRoleMapper(List<NamedNode> model);
        void updateAggregatePrincipalDecoder(List<NamedNode> model);
        void updateAggregateRoleMapper(List<NamedNode> model);
        void updateConcatenatingPrincipalDecoder(List<NamedNode> model);
        void updateConstantPermissionMapper(List<NamedNode> model);
        void updateConstantPrincipalDecoder(List<NamedNode> model);
        void updateConstantRoleMapper(List<NamedNode> model);
        void updateCustomPermissionMapper(List<NamedNode> model);
        void updateCustomPrincipalDecoder(List<NamedNode> model);
        void updateCustomRoleDecoder(List<NamedNode> model);
        void updateCustomRoleMapper(List<NamedNode> model);
        void updateLogicalPermissionMapper(List<NamedNode> model);
        void updateLogicalRoleMapper(List<NamedNode> model);
        void updateSimplePermissionMapper(List<NamedNode> model);
        void updateSimpleRoleDecoder(List<NamedNode> model);
        void updateX500AttributePrincipalDecoder(List<NamedNode> model);
    }
    // @formatter:on

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public MapperDecoderPresenter(final EventBus eventBus,
            final MapperDecoderPresenter.MyView view,
            final MapperDecoderPresenter.MyProxy proxy,
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
        return ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(Ids.ELYTRON)
                .append(Ids.ELYTRON, Ids.asId(Names.MAPPERS_DECODERS),
                        resources.constants().settings(), Names.MAPPERS_DECODERS);
    }

    @Override
    protected void reload() {
        ResourceAddress address = ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        crud.readChildren(address, asList(
                ADD_PREFIX_ROLE_MAPPER.resource,
                ADD_SUFFIX_ROLE_MAPPER.resource,
                AGGREGATE_PRINCIPAL_DECODER.resource,
                AGGREGATE_ROLE_MAPPER.resource,
                CONCATENATING_PRINCIPAL_DECODER.resource,
                CONSTANT_PERMISSION_MAPPER.resource,
                CONSTANT_PRINCIPAL_DECODER.resource,
                CONSTANT_ROLE_MAPPER.resource,
                CUSTOM_PERMISSION_MAPPER.resource,
                CUSTOM_PRINCIPAL_DECODER.resource,
                CUSTOM_ROLE_DECODER.resource,
                CUSTOM_ROLE_MAPPER.resource,
                LOGICAL_PERMISSION_MAPPER.resource,
                LOGICAL_ROLE_MAPPER.resource,
                SIMPLE_PERMISSION_MAPPER.resource,
                SIMPLE_ROLE_DECODER.resource,
                X500_ATTRIBUTE_PRINCIPAL_DECODER.resource),
                result -> {
                    // @formatter:off
                    getView().updateAddPrefixRoleMapper(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateAddSuffixRoleMapper(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateAggregatePrincipalDecoder(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateAggregateRoleMapper(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateConcatenatingPrincipalDecoder(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateConstantPermissionMapper(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                    getView().updateConstantPrincipalDecoder(asNamedNodes(result.step(6).get(RESULT).asPropertyList()));
                    getView().updateConstantRoleMapper(asNamedNodes(result.step(7).get(RESULT).asPropertyList()));
                    getView().updateCustomPermissionMapper(asNamedNodes(result.step(8).get(RESULT).asPropertyList()));
                    getView().updateCustomPrincipalDecoder(asNamedNodes(result.step(9).get(RESULT).asPropertyList()));
                    getView().updateCustomRoleDecoder(asNamedNodes(result.step(10).get(RESULT).asPropertyList()));
                    getView().updateCustomRoleMapper(asNamedNodes(result.step(11).get(RESULT).asPropertyList()));
                    getView().updateLogicalPermissionMapper(asNamedNodes(result.step(12).get(RESULT).asPropertyList()));
                    getView().updateLogicalRoleMapper(asNamedNodes(result.step(13).get(RESULT).asPropertyList()));
                    getView().updateSimplePermissionMapper(asNamedNodes(result.step(14).get(RESULT).asPropertyList()));
                    getView().updateSimpleRoleDecoder(asNamedNodes(result.step(15).get(RESULT).asPropertyList()));
                    getView().updateX500AttributePrincipalDecoder(asNamedNodes(result.step(16).get(RESULT).asPropertyList()));
                    // @formatter:on
                });
    }

    void reload(String resource, Consumer<List<NamedNode>> callback) {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, resource,
                children -> callback.accept(asNamedNodes(children)));
    }
}
