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

import java.util.List;
import javax.inject.Inject;

import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.dom.Element;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.subsystem.jpa.AddressTemplates.JPA_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.jpa.AddressTemplates.JPA_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.JPA_RUNTIME)
@Requires(value = JPA_ADDRESS)
public class JpaColumn extends FinderColumn<JpaStatistic> {

    @Inject
    public JpaColumn(final Finder finder,
            final ItemActionFactory itemActionFactory,
            final Environment environment,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final FinderPathFactory finderPathFactory,
            final Places places,
            final TokenFormatter tokenFormatter,
            final Resources resources) {

        super(new Builder<JpaStatistic>(finder, Ids.JPA_RUNTIME, Names.JPA)

                .itemsProvider((context, callback) -> {
                    ResourceAddress address = JPA_TEMPLATE.resolve(statementContext);
                    Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE, true)
                            .build();
                    dispatcher.execute(operation, result -> callback.onSuccess(result.asList().stream()
                            .filter(node -> !node.isFailure())
                            .map(node -> new JpaStatistic(new ResourceAddress(node.get(ADDRESS)), node.get(RESULT)))
                            .collect(toList())));
                })

                .itemRenderer(item -> new ItemDisplay<JpaStatistic>() {
                    @Override
                    public String getId() {
                        return Ids.jpaStatistic(item.getDeployment(), item.getName());
                    }

                    @Override
                    public Element asElement() {
                        return ItemDisplay.withSubtitle(item.getName(), item.getDeployment());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    @Override
                    public Element getIcon() {
                        return item.isStatisticsEnabled() ? Icons.ok() : Icons.disabled();
                    }

                    @Override
                    public List<ItemAction<JpaStatistic>> actions() {
                        //noinspection HardCodedStringLiteral
                        return singletonList(itemActionFactory
                                .view(NameTokens.JPA_RUNTIME, NAME, item.get("scoped-unit-name").asString()));
                    }
                })

                .withFilter()
                .useFirstActionAsBreadcrumbHandler()
                .onPreview(
                        item -> new JpaPreview(item, environment, dispatcher, finderPathFactory, places, tokenFormatter,
                                resources))
        );
    }
}
