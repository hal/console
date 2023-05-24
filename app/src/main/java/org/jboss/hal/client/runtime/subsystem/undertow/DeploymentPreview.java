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
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.chart.Donut;
import org.jboss.hal.ballroom.chart.GroupedBar;
import org.jboss.hal.ballroom.chart.Utilization;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerUrl;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.asHtmlElement;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.htmlElements;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.stream;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACTIVE_SESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTEXT_ROOT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPIRED_SESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LINK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAX_ACTIVE_SESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REJECTED_SESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOLVE_EXPRESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SESSIONS_CREATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SESSION_AVG_ALIVE_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SESSION_MAX_ALIVE_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDERTOW;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VIRTUAL_HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.resources.CSS.fontAwesome;

class DeploymentPreview extends PreviewContent<DeploymentResource> {

    private final DeploymentResource deploymentResource;
    private final Environment environment;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final ServerActions serverActions;
    private final PreviewAttributes<DeploymentResource> previewAttributes;
    private final EmptyState noStatistics;
    private final Donut sessions;
    private final HTMLElement sessionsElement;
    private final GroupedBar sessionTime;
    private final Utilization maxSessions;
    private final HTMLElement maxTimeElement;

    DeploymentPreview(DeploymentResource deploymentResource,
            FinderPathFactory finderPathFactory,
            Places places,
            Resources resources,
            Environment environment,
            Dispatcher dispatcher,
            StatementContext statementContext,
            ServerActions serverActions) {
        super(deploymentResource.getPath());
        this.deploymentResource = deploymentResource;
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.serverActions = serverActions;

        getHeaderContainer().appendChild(refreshLink(() -> update(deploymentResource)));

        previewAttributes = new PreviewAttributes<>(deploymentResource)
                .append(model -> new PreviewAttributes.PreviewAttribute(new LabelBuilder().label(CONTEXT_ROOT),
                        span().textContent(model.get(CONTEXT_ROOT).asString())
                                .data(LINK, "").element()))
                .append(model -> {
                    FinderPath path = finderPathFactory.deployment(deploymentResource.getDeployment());
                    PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
                    return new PreviewAttributes.PreviewAttribute(Names.DEPLOYMENT, deploymentResource.getPath(),
                            places.historyToken(placeRequest));
                })
                .append(model -> {
                    String server = model.get(SERVER).asString();
                    FinderPath path = finderPathFactory.runtimeServerPath()
                            .append(Ids.RUNTIME_SUBSYSTEM, UNDERTOW)
                            .append(Ids.UNDERTOW_RUNTIME, Ids.asId(Names.SERVER))
                            .append(Ids.UNDERTOW_RUNTIME_SERVER, Ids.webServer(server));
                    PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
                    return new PreviewAttributes.PreviewAttribute(Names.SERVER, server,
                            places.historyToken(placeRequest));
                })
                .append(VIRTUAL_HOST);

        noStatistics = new EmptyState.Builder(Ids.UNDERTOW_DEPLOYMENT_STATISTICS_DISABLED,
                resources.constants().statisticsDisabledHeader())
                .description(resources.messages().statisticsDisabled(Names.UNDERTOW))
                .icon(fontAwesome("line-chart"))
                .primaryAction(resources.constants().enableStatistics(), this::enableStatistics,
                        Constraint.writable(WEB_SUBSYSTEM_TEMPLATE, STATISTICS_ENABLED))
                .build();

        sessions = new Donut.Builder(Names.SESSIONS)
                .add(ACTIVE_SESSIONS, resources.constants().activeSessions(), PatternFly.colors.green)
                .add(EXPIRED_SESSIONS, resources.constants().expiredSessions(), PatternFly.colors.orange)
                .add(REJECTED_SESSIONS, resources.constants().rejectedSessions(), PatternFly.colors.red)
                .legend(Donut.Legend.RIGHT)
                .responsive(true)
                .build();
        registerAttachable(sessions);
        sessionsElement = section()
                .add(h(2, Names.SESSIONS))
                .add(sessions).element();

        // the order of rows is determined at update time.
        sessionTime = new GroupedBar.Builder(resources.constants().seconds())
                .add(SESSION_MAX_ALIVE_TIME, resources.constants().sessionsMaxAliveTime(), PatternFly.colors.orange)
                .add(SESSION_AVG_ALIVE_TIME, resources.constants().sessionsAvgAliveTime(), PatternFly.colors.green)
                .responsive(true)
                .horizontal()
                .build();
        registerAttachable(sessionTime);
        maxSessions = new Utilization(resources.constants().activeSessions(),
                resources.constants().maxActiveSessions(), false, false);
        maxTimeElement = section()
                .add(h(2, resources.constants().sessionTime()))
                .add(maxSessions)
                .add(sessionTime).element();

        previewBuilder().addAll(previewAttributes);
        previewBuilder()
                .add(noStatistics)
                .add(sessionsElement)
                .add(maxTimeElement);

        setVisible(noStatistics.element(), false);
        setVisible(sessionsElement, false);
        setVisible(maxTimeElement, false);
    }

    @Override
    public void attach() {
        super.attach();
        injectUrls();
    }

    @Override
    public void update(DeploymentResource item) {
        Operation opDeployment = new Operation.Builder(item.getAddress(), READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        ResourceAddress webRuntimeAddress = WEB_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation opStatistics = new Operation.Builder(webRuntimeAddress, READ_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(RESOLVE_EXPRESSIONS, true)
                .build();
        dispatcher.execute(new Composite(opDeployment, opStatistics), (CompositeResult compositeResult) -> {

            ModelNode deploymentResult = compositeResult.step(0).get(RESULT);
            ModelNode statisticsResult = compositeResult.step(1).get(RESULT);
            DeploymentResource deploymentStats = new DeploymentResource(item.getAddress(), deploymentResult);
            previewAttributes.refresh(deploymentStats);

            boolean statsAvailable = deploymentStats.get(SESSIONS_CREATED).asLong() > 0;
            boolean statsEnabled = statisticsResult.asBoolean(statsAvailable);
            if (statsEnabled) {
                Map<String, Long> updatedSession = new HashMap<>();
                updatedSession.put(ACTIVE_SESSIONS, deploymentStats.get(ACTIVE_SESSIONS).asLong());
                updatedSession.put(EXPIRED_SESSIONS, deploymentStats.get(EXPIRED_SESSIONS).asLong());
                updatedSession.put(REJECTED_SESSIONS, deploymentStats.get(REJECTED_SESSIONS).asLong());
                sessions.update(updatedSession);

                // only shows this chart if the max_active_session is set, otherwise the max has no limits
                if (deploymentStats.get(MAX_ACTIVE_SESSIONS).asInt() > -1) {
                    maxSessions.update(deploymentStats.get(ACTIVE_SESSIONS).asLong(),
                            deploymentStats.get(MAX_ACTIVE_SESSIONS).asLong());
                    setVisible(maxSessions.element(), true);
                } else {
                    setVisible(maxSessions.element(), false);
                }

                Map<String, Long> updatedTime = new HashMap<>();
                updatedTime.put(SESSION_MAX_ALIVE_TIME, deploymentStats.get(SESSION_MAX_ALIVE_TIME).asLong());
                updatedTime.put(SESSION_AVG_ALIVE_TIME, deploymentStats.get(SESSION_AVG_ALIVE_TIME).asLong());
                sessionTime.update(updatedTime);
            }
            setVisible(noStatistics.element(), !statsEnabled);
            setVisible(sessionsElement, statsEnabled);
            setVisible(maxTimeElement, statsEnabled);
            injectUrls();
        });
    }

    private void enableStatistics() {
        ResourceAddress address = AddressTemplate.of("{selected.profile}/subsystem=undertow").resolve(statementContext);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> update(deploymentResource));
    }

    private void injectUrls() {
        List<HTMLElement> linkContainers = new ArrayList<>();
        forEach(e -> {
            List<HTMLElement> elements = stream(e.querySelectorAll("[data-" + LINK + "]")) // NON-NLS
                    .filter(htmlElements())
                    .map(asHtmlElement())
                    .collect(toList());
            linkContainers.addAll(elements);
        });
        if (!linkContainers.isEmpty()) {
            String host = environment.isStandalone() ? Server.STANDALONE.getHost() : statementContext.selectedHost();
            String serverGroup = statementContext.selectedServerGroup();
            String server = environment.isStandalone() ? Server.STANDALONE.getName() : statementContext.selectedServer();
            // noinspection Duplicates
            serverActions.readUrl(environment.isStandalone(), host, serverGroup, server,
                    new AsyncCallback<ServerUrl>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            // noop
                        }

                        @Override
                        public void onSuccess(ServerUrl url) {
                            for (HTMLElement linkContainer : linkContainers) {
                                String link = linkContainer.textContent;
                                Elements.removeChildrenFrom(linkContainer);
                                linkContainer.appendChild(a(url.getUrl() + link)
                                        .apply(a -> a.target = Ids.hostServer(host, server))
                                        .textContent(link).element());
                            }
                        }
                    });
        }
    }
}
