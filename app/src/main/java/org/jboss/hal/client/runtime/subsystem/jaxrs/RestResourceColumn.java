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
package org.jboss.hal.client.runtime.subsystem.jaxrs;

import java.util.Comparator;

import javax.inject.Inject;

import com.google.common.collect.Sets;
import elemental2.dom.HTMLElement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.DeploymentResources;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static org.jboss.hal.dmr.ModelDescriptionConstants.JAX_RS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REST_RESOURCE;
import static org.jboss.hal.resources.CSS.breakTooltip;
import static org.jboss.hal.resources.Strings.abbreviateFqClassName;

@AsyncColumn(Ids.REST_RESOURCE)
public class RestResourceColumn extends FinderColumn<RestResource> {

    @Inject
    public RestResourceColumn(Finder finder,
            FinderPathFactory finderPathFactory,
            ColumnActionFactory columnActionFactory,
            Environment environment,
            ServerActions serverActions,
            DeploymentResources deploymentResources,
            Places places,
            StatementContext statementContext,
            Resources resources) {

        super(new FinderColumn.Builder<RestResource>(finder, Ids.REST_RESOURCE, Names.REST_RESOURCE)

                .columnAction(columnActionFactory.refresh(Ids.REST_RESOURCE_REFRESH))

                .itemsProvider((context, callback) -> deploymentResources.readChildren(JAX_RS, REST_RESOURCE,
                        RestResource::new, restResources -> {
                            restResources.sort(Comparator.comparing(RestResource::getName));
                            callback.onSuccess(restResources);
                        }))

                .itemRenderer(item -> new ItemDisplay<RestResource>() {
                    @Override
                    public String getId() {
                        return Ids.restResource(item.getDeployment(), item.getSubdeployment(), item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return abbreviateFqClassName(item.getName());
                    }

                    @Override
                    public HTMLElement element() {
                        HTMLElement element = ItemDisplay.withSubtitle(abbreviateFqClassName(item.getName()),
                                item.getPath());
                        element.classList.add(breakTooltip);
                        return element;
                    }

                    @Override
                    public String getFilterData() {
                        String resourceMethods = String.join(" ", item.getResourceMethods());
                        String mediaTypes = String.join(" ",
                                Sets.union(item.getConsumes(), item.getProduces()).immutableCopy());
                        return String.join(" ", item.getName(), resourceMethods, mediaTypes);
                    }

                    @Override
                    public String getTooltip() {
                        return item.getName();
                    }
                })

                .onPreview(item -> new RestResourcePreview(item, environment, finderPathFactory, places, serverActions,
                        statementContext, resources))
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .filterDescription(resources.messages().restColumnFilterDescription())
                .showCount()
                .pinnable());
    }
}
