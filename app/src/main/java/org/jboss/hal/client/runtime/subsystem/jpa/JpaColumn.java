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
package org.jboss.hal.client.runtime.subsystem.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.DeploymentResources;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.subsystem.jpa.AddressTemplates.JPA_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

@AsyncColumn(Ids.JPA_RUNTIME)
@Requires(JPA_DEPLOYMENT_ADDRESS)
public class JpaColumn extends FinderColumn<JpaStatistic> {

    @Inject
    public JpaColumn(final Finder finder,
            final ItemActionFactory itemActionFactory,
            final Environment environment,
            final Dispatcher dispatcher,
            final DeploymentResources deploymentResources,
            final FinderPathFactory finderPathFactory,
            final PlaceManager placeManager,
            final Places places,
            final Resources resources) {

        super(new Builder<JpaStatistic>(finder, Ids.JPA_RUNTIME, Names.JPA)

                .itemRenderer(item -> new ItemDisplay<JpaStatistic>() {
                    @Override
                    public String getId() {
                        return Ids.jpaStatistic(item.getDeployment(), item.getSubdeployment(), item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getPersistenceUnit();
                    }

                    @Override
                    public HTMLElement asElement() {
                        return ItemDisplay.withSubtitle(item.getPersistenceUnit(), item.getPath());
                    }

                    @Override
                    public String getFilterData() {
                        Set<String> data = new HashSet<>(asList(item.getName(), item.getDeployment()));
                        if (item.getSubdeployment() != null) {
                            data.add(item.getSubdeployment());
                        }
                        return String.join(" ", data);
                    }

                    @Override
                    public String getTooltip() {
                        if (!item.isStatisticsEnabled()) {
                            return resources.constants().statisticsDisabled();
                        }
                        return null;
                    }

                    @Override
                    public List<ItemAction<JpaStatistic>> actions() {
                        if (item.isStatisticsEnabled()) {
                            PlaceRequest.Builder builder = places.selectedServer(NameTokens.JPA_RUNTIME)
                                    .with(DEPLOYMENT, item.getDeployment());
                            if (item.getSubdeployment() != null) {
                                builder.with(SUBDEPLOYMENT, item.getSubdeployment());
                            }
                            PlaceRequest placeRequest = builder.with(NAME, item.getName()).build();
                            return singletonList(itemActionFactory.view(placeRequest));
                        }
                        return ItemDisplay.super.actions();
                    }
                })

                .withFilter()
                .useFirstActionAsBreadcrumbHandler()
                .onPreview(item -> new JpaPreview(item, environment, dispatcher, finderPathFactory, placeManager, places,
                                resources))
        );

        ItemsProvider<JpaStatistic> itemsProvider = (context, callback) ->
                deploymentResources.readChildren(JPA, HIBERNATE_PERSISTENCE_UNIT, JpaStatistic::new,
                        callback::onSuccess);

        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider((context, callback) ->
                itemsProvider.get(context, new AsyncCallback<List<JpaStatistic>>() {
                    @Override
                    public void onFailure(final Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(final List<JpaStatistic> result) {
                        // only persistence units w/ enabled statistics will show up in the breadcrumb dropdown
                        List<JpaStatistic> puWithStatistics = result.stream()
                                .filter(JpaStatistic::isStatisticsEnabled)
                                .collect(toList());
                        callback.onSuccess(puWithStatistics);
                    }
                }));
    }
}
