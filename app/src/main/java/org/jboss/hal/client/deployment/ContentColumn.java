/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.deployment;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.client.deployment.DeploymentTasks.LoadContent;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.deployment.ServerGroupDeployment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.Strings;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.client.deployment.AbstractDeploymentColumn.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.deployment.ContentColumn.ROOT_ADDRESS;
import static org.jboss.hal.client.deployment.ContentColumn.SERVER_GROUP_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.resources.CSS.fontAwesome;

/** Column used in domain mode to manage content in the content repository. */
@Column(Ids.CONTENT)
@Requires(value = { ROOT_ADDRESS, DEPLOYMENT_ADDRESS, SERVER_GROUP_DEPLOYMENT_ADDRESS }, recursive = false)
public class ContentColumn extends AbstractDeploymentColumn<Content> {

    static final String ROOT_ADDRESS = "/";
    private static final String SPACE = " ";

    @Inject
    public ContentColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Environment environment,
            Dispatcher dispatcher,
            CrudOperations crud,
            EventBus eventBus,
            Places places,
            @Footer Provider<Progress> progress,
            MetadataRegistry metadataRegistry,
            SecurityContextRegistry securityContextRegistry,
            StatementContext statementContext,
            Resources resources) {

        super(new FinderColumn.Builder<Content>(finder, Ids.CONTENT, resources.constants().content())
                .itemsProvider(
                        finderContext -> sequential(new FlowContext(progress.get()), singletonList(new LoadContent(dispatcher)))
                                .then(flowContext -> {
                                    List<Content> content = flowContext.pop();
                                    return Promise.resolve(content);
                                }))

                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .showCount()
                .withFilter()
                .filterDescription(resources.messages().contentFilterDescription()),
                columnActionFactory, crud, dispatcher, environment, eventBus, itemActionFactory,
                metadataRegistry, securityContextRegistry, progress, resources, statementContext);

        setPreviewCallback(item -> new ContentPreview(this, item, environment, places,
                metadataRegistry.lookup(SERVER_GROUP_DEPLOYMENT_TEMPLATE), resources));

        setItemRenderer(item -> new ItemDisplay<Content>() {
            @Override
            public String getId() {
                return Strings.sanitize(Ids.content(item.getName()));
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement element() {
                if (!item.getServerGroupDeployments().isEmpty()) {
                    return ItemDisplay.withSubtitle(item.getName(), item.getServerGroupDeployments().stream()
                            .map(ServerGroupDeployment::getServerGroup)
                            .collect(joining(", ")));
                }
                return null;
            }

            @Override
            public String getTooltip() {
                return String.join(", ",
                        item.isExploded() ? resources.constants().exploded() : resources.constants().archived(),
                        item.isManaged() ? resources.constants().managed() : resources.constants().unmanaged());
            }

            @Override
            public HTMLElement getIcon() {
                String icon = item.isExploded() ? fontAwesome("folder-open") : fontAwesome("archive");
                return span().css(icon).element();
            }

            @Override
            public String getFilterData() {
                String status = String.join(SPACE,
                        item.isExploded() ? resources.constants().exploded() : resources.constants().archived(),
                        item.isManaged() ? resources.constants().managed() : resources.constants().unmanaged());
                String deployments = item.getServerGroupDeployments().isEmpty()
                        ? resources.constants().undeployed()
                        : item.getServerGroupDeployments().stream().map(ServerGroupDeployment::getServerGroup)
                                .collect(joining(SPACE));
                return getTitle() + SPACE + status + SPACE + deployments;
            }

            @Override
            public List<ItemAction<Content>> actions() {
                return getItemActions(item);
            }
        });
    }

    @Override
    ColumnProps getColumnProps() {
        return CONTENT_COLUMN;
    }
}
