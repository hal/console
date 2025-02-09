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
package org.jboss.hal.client.runtime.subsystem.resourceadapter;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.chart.Utilization;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_SERVER;
import static org.jboss.hal.resources.CSS.hidden;
import static org.jboss.hal.resources.CSS.underline;

class ChildResourcePreview extends PreviewContent<StatisticsResource> {
    private final Server server;
    private final StatisticsResource raChild;
    private final Environment environment;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final ResourceAddress resourceAddress;

    private Alert needsReloadWarning;
    private Alert needsRestartWarning;
    private HTMLElement refresh;
    private HTMLElement poolHeader;
    private Utilization activeConnections;
    private Utilization maxUsedConnections;

    ChildResourcePreview(Server server,
            StatisticsResource raChild,
            Environment environment,
            Dispatcher dispatcher,
            StatementContext statementContext,
            ServerActions serverActions,
            Resources resources) {

        super(raChild.getName(), raChild.getResourceType().name());
        this.server = server;
        this.raChild = raChild;
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resourceAddress = raChild.getResourceType().getTemplate().resolve(statementContext, raChild.getParentName(),
                raChild.getName());

        raChild.get(StatisticsResource.EXT_STATS_AVAILABLE).set(raChild.getExtendedStats().isDefined());
        previewBuilder()
                .addAll(new PreviewAttributes<>(raChild, List.of(StatisticsResource.EXT_STATS_AVAILABLE)));

        if (raChild.getResourceType() == StatisticsResource.ResourceType.ADMIN_OBJECT) {
            return;
        }

        Constraint reloadConstraint = environment.isStandalone()
                ? Constraint.executable(AddressTemplate.of("/"), RELOAD)
                : Constraint.executable(AddressTemplate.of("/{selected.host}/server-config=*"), RELOAD);
        Constraint restartConstraint = environment.isStandalone()
                ? Constraint.executable(AddressTemplate.of("/"), RELOAD)
                : Constraint.executable(AddressTemplate.of("/{selected.host}/server-config=*"), RESTART);

        needsReloadWarning = new Alert(Icons.WARNING,
                new SafeHtmlBuilder()
                        .append(resources.messages().serverNeedsReload(server.getName()))
                        .appendEscaped(" ")
                        .append(resources.messages().staleStatistics())
                        .toSafeHtml(),
                resources.constants().reload(), event -> serverActions.reload(server),
                reloadConstraint);

        needsRestartWarning = new Alert(Icons.WARNING,
                new SafeHtmlBuilder()
                        .append(resources.messages().serverNeedsRestart(server.getName()))
                        .appendEscaped(" ")
                        .append(resources.messages().staleStatistics())
                        .toSafeHtml(),
                resources.constants().restart(), event -> serverActions.restart(server),
                restartConstraint);

        activeConnections = new Utilization(resources.constants().active(), Names.CONNECTIONS,
                environment.isStandalone(), true);
        maxUsedConnections = new Utilization(resources.constants().maxUsed(), Names.CONNECTIONS,
                environment.isStandalone(), true);
        getHeaderContainer().appendChild(refresh = refreshLink(() -> update(null)));
        previewBuilder()
                .add(needsReloadWarning)
                .add(needsRestartWarning)
                .add(poolHeader = h(2).css(underline).textContent(Names.CONNECTION_POOL).element())
                .add(activeConnections)
                .add(maxUsedConnections);

        // to prevent flickering we initially hide everything
        needsReloadWarning.element().classList.add(hidden);
        needsRestartWarning.element().classList.add(hidden);
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(StatisticsResource sr) {
        if (raChild.getResourceType() == StatisticsResource.ResourceType.ADMIN_OBJECT) {
            return;
        }
        List<Operation> operations = new ArrayList<>();
        if (environment.isStandalone()) {
            operations.add(new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(ATTRIBUTES_ONLY, true)
                    .build());
        } else {
            ResourceAddress address = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                    .resolve(statementContext);
            operations.add(new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(ATTRIBUTES_ONLY, true)
                    .build());
        }
        if (sr == null) {
            operations.add(new Operation.Builder(resourceAddress, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());
        }

        dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
            server.addServerAttributes(result.step(0).get(RESULT));
            if (sr == null) {
                raChild.update(result.step(1).get(RESULT));
            }

            boolean statisticsEnabled = raChild.isStatisticsEnabled();
            setVisible(refresh, statisticsEnabled);
            setVisible(poolHeader, statisticsEnabled);
            setVisible(activeConnections.element(), statisticsEnabled);
            setVisible(maxUsedConnections.element(), statisticsEnabled);

            // Do not simply hide the links, but add the hidden CSS class.
            // Important when constraints for the links are processed later.
            needsReloadWarning.element().classList.add(hidden);
            needsRestartWarning.element().classList.add(hidden);
            if (statisticsEnabled) {
                Elements.toggle(needsReloadWarning.element(), hidden, !server.needsReload());
                Elements.toggle(needsRestartWarning.element(), hidden, !server.needsRestart());

                // pool statistics
                ModelNode pool = raChild.getPoolStats();
                if (pool.isDefined()) {
                    int available = pool.get("AvailableCount").asInt(0);
                    int active = pool.get("ActiveCount").asInt(0);
                    int maxUsed = pool.get("MaxUsedCount").asInt(0);
                    activeConnections.update(active, available);
                    maxUsedConnections.update(maxUsed, available);
                } else {
                    activeConnections.update(0, 0);
                    maxUsedConnections.update(0, 0);
                }
            }
        });
    }
}
