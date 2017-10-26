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
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHeadingElement;
import org.jboss.gwt.elemento.core.Elements;
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

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_SERVER;
import static org.jboss.hal.resources.CSS.fontAwesome;

class DeploymentPreview extends PreviewContent<DeploymentResource> {

    private DeploymentResource deploymentResource;
    private final Environment environment;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final ServerActions serverActions;
    private PreviewAttributes<DeploymentResource> previewAttributes;
    private Donut sessions;
    private GroupedBar sessionTime;
    private Utilization maxSessions;
    private EmptyState noStatistics;
    private String profile;
    private HTMLHeadingElement sessionsHeader;
    private HTMLHeadingElement sessionTimeHeader;

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

        previewAttributes = new PreviewAttributes<>(deploymentResource)
                .append(model -> new PreviewAttributes.PreviewAttribute(new LabelBuilder().label(CONTEXT_ROOT),
                        span().textContent(model.get(CONTEXT_ROOT).asString())
                                .data(LINK, "")
                                .asElement()))
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
        getHeaderContainer().appendChild(refreshLink(() -> update(deploymentResource)));

        ResourceAddress address = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                .resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .build();
        dispatcher.execute(operation, result -> {

            profile = result.get(PROFILE_NAME).asString();
            noStatistics = new EmptyState.Builder(resources.constants().statisticsDisabledHeader())
                    .description(resources.messages().statisticsDisabled(Names.UNDERTOW, profile))
                    .icon(fontAwesome("line-chart"))
                    .primaryAction(resources.constants().enableStatistics(), this::enableStatistics,
                            Constraint.writable(WEB_SUBSYSTEM_TEMPLATE, STATISTICS_ENABLED))
                    .build();

            previewBuilder().addAll(previewAttributes);
            previewBuilder()
                    .add(noStatistics);

            sessions = new Donut.Builder(Names.SESSIONS)
                    .add(ACTIVE_SESSIONS, resources.constants().activeSessions(), PatternFly.colors.green)
                    .add(EXPIRED_SESSIONS, resources.constants().expiredSessions(), PatternFly.colors.orange)
                    .add(REJECTED_SESSIONS, resources.constants().rejectedSessions(), PatternFly.colors.red)
                    .legend(Donut.Legend.BOTTOM)
                    .responsive(true)
                    .build();
            registerAttachable(sessions);

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

            sessionsHeader = h(2, resources.constants().sessions()).asElement();
            sessionTimeHeader = h(2, resources.constants().sessionTime()).asElement();

            previewBuilder()
                    .add(sessionsHeader)
                    .add(sessions)
                    .add(sessionTimeHeader)
                    .add(maxSessions)
                    .add(sessionTime);

            Elements.setVisible(noStatistics.asElement(), false);
            Elements.setVisible(maxSessions.asElement(), false);
            Elements.setVisible(sessionsHeader, false);
            Elements.setVisible(sessionTimeHeader, false);
        });
    }

    @Override
    public void attach() {
        super.attach();
        injectUrls();
    }

    @Override
    public void update(final DeploymentResource item) {
        Operation opDeployment = new Operation.Builder(item.getAddress(), READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        ResourceAddress webRuntimeAddress = WEB_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation opSubsystem = new Operation.Builder(webRuntimeAddress, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(new Composite(opDeployment, opSubsystem), (CompositeResult compositeResult) -> {

            ModelNode deploymentResult = compositeResult.step(0).get(RESULT);
            ModelNode subsystemResult = compositeResult.step(1).get(RESULT);
            DeploymentResource deploymentStats = new DeploymentResource(item.getAddress(), deploymentResult);
            previewAttributes.refresh(deploymentStats);

            boolean statsEnabled = subsystemResult.get(STATISTICS_ENABLED).asBoolean();
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
                    Elements.setVisible(maxSessions.asElement(), true);
                } else {
                    Elements.setVisible(maxSessions.asElement(), false);
                }

                Map<String, Long> updatedTime = new HashMap<>();
                updatedTime.put(SESSION_MAX_ALIVE_TIME, deploymentStats.get(SESSION_MAX_ALIVE_TIME).asLong());
                updatedTime.put(SESSION_AVG_ALIVE_TIME, deploymentStats.get(SESSION_AVG_ALIVE_TIME).asLong());
                sessionTime.update(updatedTime);
                Elements.setVisible(noStatistics.asElement(), false);
                Elements.setVisible(sessionsHeader, true);
                Elements.setVisible(sessionTimeHeader, true);
            } else {
                Elements.setVisible(noStatistics.asElement(), true);
                Elements.setVisible(sessionTime.asElement(), false);
                Elements.setVisible(sessions.asElement(), false);
                Elements.setVisible(maxSessions.asElement(), false);
                Elements.setVisible(sessionsHeader, false);
                Elements.setVisible(sessionTimeHeader, false);
            }
            injectUrls();
        });
    }

    private void enableStatistics() {
        ResourceAddress address = new ResourceAddress()
                .add(PROFILE, profile)
                .add(SUBSYSTEM, UNDERTOW);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> {
            Elements.setVisible(noStatistics.asElement(), false);
            Elements.setVisible(sessionsHeader, true);
            Elements.setVisible(sessionTimeHeader, true);
            Elements.setVisible(sessionTime.asElement(), true);
            Elements.setVisible(sessions.asElement(), true);
            Elements.setVisible(maxSessions.asElement(), true);
            update(deploymentResource);
        });
    }

    private void injectUrls() {
        List<HTMLElement> linkContainers = new ArrayList<>();
        asElements().forEach(e -> {
            List<HTMLElement> elements = stream(e.querySelectorAll("[data-" + LINK + "]")) //NON-NLS
                    .filter(htmlElements())
                    .map(asHtmlElement())
                    .collect(toList());
            linkContainers.addAll(elements);
        });
        if (!linkContainers.isEmpty()) {
            String host = environment.isStandalone() ? Server.STANDALONE.getHost() : statementContext.selectedHost();
            String serverGroup = statementContext.selectedServerGroup();
            String server = environment.isStandalone() ? Server.STANDALONE.getName() : statementContext.selectedServer();
            //noinspection Duplicates
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
                                        .textContent(link)
                                        .asElement());
                            }
                        }
                    });
        }
    }
}
