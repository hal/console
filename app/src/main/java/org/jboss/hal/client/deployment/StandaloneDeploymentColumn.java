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
package org.jboss.hal.client.deployment;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.deployment.Deployment;
import org.jboss.hal.core.deployment.Deployment.Status;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.Strings;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.client.deployment.AbstractDeploymentColumn.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.hal.resources.CSS.fontAwesome;

/** Column used in standalone mode to manage deployments. */
@Column(Ids.DEPLOYMENT)
@Requires(value = DEPLOYMENT_ADDRESS, recursive = false)
public class StandaloneDeploymentColumn extends AbstractDeploymentColumn<Deployment> {

    @Inject
    public StandaloneDeploymentColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Environment environment,
            ServerActions serverActions,
            Dispatcher dispatcher,
            CrudOperations crud,
            StatementContext statementContext,
            EventBus eventBus,
            MetadataRegistry metadataRegistry,
            SecurityContextRegistry securityContextRegistry,
            @Footer Provider<Progress> progress,
            Resources resources) {

        super(new Builder<Deployment>(finder, Ids.DEPLOYMENT, Names.DEPLOYMENT)

                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(ResourceAddress.root(),
                            READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, DEPLOYMENT)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE_DEPTH, 2)
                            .build();
                    dispatcher.execute(operation, result -> {
                        List<Deployment> deployments = result.asPropertyList().stream()
                                .map(property -> new Deployment(Server.STANDALONE, property.getValue()))
                                .collect(toList());
                        callback.onSuccess(deployments);
                    });
                })

                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .showCount()
                .withFilter()
                .filterDescription(resources.messages().deploymentStandaloneColumnFilterDescription()),
                columnActionFactory, crud, dispatcher, environment, eventBus, itemActionFactory,
                metadataRegistry, securityContextRegistry, progress, resources, statementContext);

        setItemRenderer(item -> new ItemDisplay<Deployment>() {
            @Override
            public String getId() {
                return Strings.sanitize(Ids.deployment(item.getName()));
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String getTooltip() {
                if (item.getStatus() == Status.FAILED) {
                    return resources.constants().failed();
                } else if (item.getStatus() == Status.STOPPED) {
                    return resources.constants().stopped();
                } else if (item.getStatus() == Status.OK) {
                    return resources.constants().activeLower();
                } else {
                    return item.isEnabled() ? resources.constants().enabled()
                            : resources.constants()
                                    .disabled();
                }
            }

            @Override
            public HTMLElement getIcon() {
                String icon = item.isExploded() ? fontAwesome("folder-open") : fontAwesome("archive");
                return span().css(icon).element();
            }

            @Override
            public String getFilterData() {
                return item.getName() + " " + (item.isEnabled() ? ENABLED : DISABLED);
            }

            @Override
            public List<ItemAction<Deployment>> actions() {
                return getItemActions(item);
            }
        });

        setPreviewCallback(
                deployment -> new StandaloneDeploymentPreview(StandaloneDeploymentColumn.this, deployment, resources,
                        serverActions, environment));
    }

    @Override
    ColumnProps getColumnProps() {
        return STANDALONE_COLUMN;
    }
}
