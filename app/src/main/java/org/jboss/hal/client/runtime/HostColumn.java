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
package org.jboss.hal.client.runtime;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.HostSelectionEvent;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.RunningMode.ADMIN_ONLY;
import static org.jboss.hal.client.runtime.RunningState.RELOAD_REQUIRED;
import static org.jboss.hal.client.runtime.RunningState.RESTART_REQUIRED;
import static org.jboss.hal.client.runtime.SuspendState.PRE_SUSPEND;
import static org.jboss.hal.client.runtime.SuspendState.SUSPENDED;
import static org.jboss.hal.client.runtime.SuspendState.SUSPENDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.subtitle;

/**
 * @author Harald Pehl
 */
@Column(HOST)
@Requires(value = "/host=*", recursive = false)
public class HostColumn extends FinderColumn<Host> {

    @Inject
    public HostColumn(final Finder finder,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final ItemActionFactory itemActionFactory,
            final Places places) {
        super(new Builder<Host>(finder, HOST, Names.HOSTS)

                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                            ResourceAddress.ROOT)
                            .param(CHILD_TYPE, HOST)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    dispatcher.execute(operation,
                            result -> callback.onSuccess(result.asPropertyList().stream()
                                    .map(Host::new).collect(toList())));
                })

                .onItemSelect(host -> eventBus.fireEvent(new HostSelectionEvent(host.getName())))
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<Host>() {
            @Override
            public String getId() {
                return Host.id(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public Element asElement() {
                if (item.isDomainController()) {
                    return new Elements.Builder()
                            .span().css(itemText)
                            .span().textContent(item.getName()).end()
                            .start("small").css(subtitle).textContent(Names.DOMAIN_CONTROLLER).end()
                            .end().build();
                }
                return null;
            }

            @Override
            public String getFilterData() {
                return Joiner.on(' ').join(item.getName(), item.isDomainController() ? "dc" : "hc");
            }

            @Override
            public String getTooltip() {
                return item.getStatusText();
            }

            @Override
            public Element getIcon() {
                if (item.getRunningMode() == ADMIN_ONLY) {
                    return Icons.disabled();
                } else if (EnumSet.of(PRE_SUSPEND, SUSPENDING, SUSPENDED).contains(item.getSuspendState()) ||
                        EnumSet.of(RESTART_REQUIRED, RELOAD_REQUIRED).contains(item.getHostState())) {
                    return Icons.warning();
                } else if (item.isRunning()) {
                    return Icons.ok();
                }
                return Icons.error();
            }

            @Override
            public String nextColumn() {
                return SERVER;
            }

            @Override
            public List<ItemAction<Host>> actions() {
                PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.HOST_CONFIGURATION)
                        .with(HOST, item.getName()).build();
                return Collections.singletonList(itemActionFactory.view(placeRequest));
            }
        });
    }
}
