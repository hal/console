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
package org.jboss.hal.client.runtime;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Provider;

import org.jboss.elemento.Elements;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.group.ServerGroupActionEvent;
import org.jboss.hal.core.runtime.group.ServerGroupActions;
import org.jboss.hal.core.runtime.group.ServerGroupResultEvent;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActionEvent;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.host.HostResultEvent;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActionEvent;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerResultEvent;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.NodeList;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.asHtmlElement;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.htmlElements;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.td;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.core.runtime.TopologyTasks.serverConfigsOfHost;
import static org.jboss.hal.core.runtime.TopologyTasks.startedServerOperations;
import static org.jboss.hal.core.runtime.TopologyTasks.topology;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.empty;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.height;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.CSS.pullRight;
import static org.jboss.hal.resources.CSS.px;
import static org.jboss.hal.resources.CSS.topology;

/**
 * Acts like a kind of controller or presenter for the topology. The model and view parts can be found in
 * {@link TopologyStatus}, {@link TopologyElements} or {@link TopologyAttributes}.
 */
class TopologyPreview extends PreviewContent<StaticItem>
        implements HostActionEvent.HostActionHandler, HostResultEvent.HostResultHandler,
        ServerGroupActionEvent.ServerGroupActionHandler, ServerGroupResultEvent.ServerGroupResultHandler,
        ServerActionEvent.ServerActionHandler, ServerResultEvent.ServerResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(TopologyPreview.class);
    private static final long TOPOLOGY_TIMEOUT = 5_000; // milli seconds

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final EventBus eventBus;
    private final Resources resources;
    private final TopologyStatus topologyStatus;
    private final TopologyElements topologyElements;
    private final TopologyAttributes topologyAttributes;

    public TopologyPreview(
            SecurityContextRegistry securityContextRegistry,
            Environment environment,
            Dispatcher dispatcher,
            Provider<Progress> progress,
            EventBus eventBus,
            Places places,
            FinderPathFactory finderPathFactory,
            HostActions hostActions,
            ServerGroupActions serverGroupActions,
            ServerActions serverActions,
            Resources resources) {
        super(Names.TOPOLOGY, resources.previews().runtimeTopology());

        this.environment = environment;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.eventBus = eventBus;
        this.resources = resources;
        this.topologyStatus = new TopologyStatus();
        this.topologyElements = new TopologyElements(
                this::hostDetails, this::serverGroupDetails, this::serverDetails, this::updateServer,
                securityContextRegistry, hostActions, serverGroupActions, serverActions,
                environment, resources);
        this.topologyAttributes = new TopologyAttributes(places, finderPathFactory, hostActions, serverActions, resources);

        eventBus.addHandler(HostActionEvent.getType(), this);
        eventBus.addHandler(HostResultEvent.getType(), this);
        eventBus.addHandler(ServerGroupActionEvent.getType(), this);
        eventBus.addHandler(ServerGroupResultEvent.getType(), this);
        eventBus.addHandler(ServerActionEvent.getType(), this);
        eventBus.addHandler(ServerResultEvent.getType(), this);

        // keep the order (1-3)!
        previewBuilder()
                .add(p() // 1
                        .add(a().css(clickable, pullRight).on(click, event -> update(null))
                                .add(span().css(fontAwesome("refresh"), marginRight5))
                                .add(span().textContent(resources.constants().refresh()))));
        topologyElements.addTo(previewBuilder()); // 2
        topologyAttributes.addTo(previewBuilder()); // 3
    }

    // ------------------------------------------------------ update

    @Override
    public void update(StaticItem item) {
        startUpdate();
        sequential(new FlowContext(progress.get()), topology(environment, dispatcher))
                .timeout(TOPOLOGY_TIMEOUT)
                .subscribe(context -> {
                    if (context.successful()) {
                        finishUpdate();
                        List<Host> hosts = context.get(TopologyTasks.HOSTS);
                        List<ServerGroup> serverGroups = context.get(TopologyTasks.SERVER_GROUPS);
                        topologyElements.update(hosts, serverGroups);
                        updateServers(hosts, serverGroups);

                    } else if (context.timeout()) {
                        finishUpdate();
                        logger.warn("Timeout in topology()");
                        MessageEvent.fire(eventBus, Message.warning(resources.messages().topologyTimeout()));

                    } else if (context.failure()) {
                        finishUpdate();
                        String reason = context.failureReason();
                        logger.error("Error in topology(): {}", reason);
                        MessageEvent.fire(eventBus, Message.error(resources.messages().topologyError(), reason));
                    }
                });
    }

    private void startUpdate() {
        topologyStatus.reset();
        topologyAttributes.hideAll();
        topologyElements.startUpdate();
    }

    private void finishUpdate() {
        topologyElements.finishUpdate();
    }

    // ------------------------------------------------------ host

    private void hostDetails(Host host) {
        topologyElements.selectHost(host);
        topologyAttributes.refreshHost(host);
    }

    @Override
    public void onHostAction(final HostActionEvent event) {
        if (topologyElements.isVisible()) {
            Host host = event.getHost();

            disableDropdown(Ids.host(host.getAddressName()), host.getName());
            topologyElements.startProgress(host);

            event.getServers().forEach(server -> {
                disableDropdown(server.getId(), server.getName());
                topologyElements.startProgress(server);
            });
        }
    }

    @Override
    public void onHostResult(final HostResultEvent event) {
        if (topologyElements.isVisible()) {
            Host host = event.getHost();

            topologyElements.stopProgress(host);
            event.getServers().forEach(topologyElements::stopProgress);
            update(null);
        }
    }

    // ------------------------------------------------------ server group

    private void serverGroupDetails(ServerGroup serverGroup) {
        topologyElements.selectServerGroup(serverGroup);
        topologyAttributes.refreshServerGroup(serverGroup);
    }

    @Override
    public void onServerGroupAction(final ServerGroupActionEvent event) {
        if (topologyElements.isVisible()) {
            ServerGroup serverGroup = event.getServerGroup();
            disableDropdown(Ids.serverGroup(serverGroup.getName()), serverGroup.getName());
            event.getServers().forEach(server -> {
                disableDropdown(server.getId(), server.getName());
                topologyElements.startProgress(server);
            });
        }
    }

    @Override
    public void onServerGroupResult(final ServerGroupResultEvent event) {
        if (topologyElements.isVisible()) {
            event.getServers().forEach(topologyElements::stopProgress);
            update(null);
        }
    }

    // ------------------------------------------------------ server

    @SuppressWarnings("Convert2MethodRef")
    private void updateServers(List<Host> hosts, List<ServerGroup> serverGroups) {
        for (Host host : hosts) {
            if (host.isConnected()) {
                // 1. Read server configs
                sequential(new FlowContext(progress.get()), serverConfigsOfHost(environment, dispatcher, host.getName()))
                        .timeout(TOPOLOGY_TIMEOUT)
                        .subscribe(context -> {
                            if (context.successful()) {
                                List<Server> servers = context.get(TopologyTasks.SERVERS);
                                for (ServerGroup serverGroup : serverGroups) {
                                    List<HTMLElement> serverElements = servers.stream()
                                            .filter(sc -> host.getName().equals(sc.getHost()) &&
                                                    serverGroup.getName().equals(sc.getServerGroup()))
                                            .sorted(comparing(Server::getName))
                                            .map(server -> topologyElements.serverElement(server))
                                            .collect(toList());
                                    if (!serverElements.isEmpty()) {
                                        HTMLElement td = topologyElements.lookupServersElement(host, serverGroup);
                                        if (td != null) {
                                            td.classList.remove(empty);
                                            td.classList.remove(CSS.progress);
                                            td(td).add(div().css(CSS.servers)
                                                    .addAll(serverElements));
                                            adjustTdHeight();
                                        }
                                    }
                                }

                                // 2. Read runtime attributes for started servers one at a time
                                // to prevent timeouts for blocked servers (HAL-1795)
                                servers.stream()
                                        .filter(Server::isStarted)
                                        .forEach(topologyElements::startProgress);
                                Map<String, Server> serverLookup = servers.stream()
                                        .collect(toMap(Server::getId, Function.identity()));
                                startedServerOperations(servers).forEach((serverId, composite) -> dispatcher.execute(composite)
                                        .then(result -> {
                                            Server server = serverLookup.get(serverId);
                                            ModelNode attributes = result.step(0).get(RESULT);
                                            server.addServerAttributes(attributes);
                                            List<ModelNode> bootErrors = result.step(1).get(RESULT).asList();
                                            server.setBootErrors(!bootErrors.isEmpty());
                                            topologyElements.replaceServer(server,
                                                    () -> topologyElements.serverElement(server),
                                                    __ -> serverDetails(server));
                                            return null;
                                        })
                                        .catch_(failure -> {
                                            String reason = String.valueOf(failure);
                                            Server server = serverLookup.get(serverId);
                                            server.setOperationFailure(reason);
                                            logger.error("Error in serverConfigsOfHost({}): Unable to update server {}: {}",
                                                    host.getAddress(), server.getServerConfigAddress(), reason);
                                            MessageEvent.fire(eventBus,
                                                    Message.error(resources.messages().topologyError(), reason));
                                            topologyElements.replaceServer(server,
                                                    () -> topologyElements.serverElement(server),
                                                    __ -> serverDetails(server));
                                            return null;
                                        })
                                        .finally_(() -> {
                                            Server server = serverLookup.get(serverId);
                                            topologyElements.stopProgress(server);
                                        }));

                            } else if (context.timeout()) {
                                logger.warn("Timeout in serverConfigsOfHost({})", host.getAddress());
                                MessageEvent.fire(eventBus, Message.warning(resources.messages().topologyTimeout()));

                            } else if (context.failure()) {
                                String reason = context.failureReason();
                                logger.error("Error in serverConfigsOfHost({}): {}", host.getAddress(), reason);
                                MessageEvent.fire(eventBus, Message.warning(resources.messages().topologyError(),
                                        reason));
                            }
                        });
            }
        }
    }

    private void updateServer(Server server) {
        // It's not enough to read just the server. We also need to update
        // its host and server group. So we use topology() here.
        sequential(new FlowContext(progress.get()), topology(environment, dispatcher))
                .timeout(TOPOLOGY_TIMEOUT)
                .subscribe(context -> {
                    if (context.successful()) {
                        Host host = null;
                        List<Host> hosts = context.get(TopologyTasks.HOSTS);
                        for (Host h : hosts) {
                            if (h.getName().equals(server.getHost())) {
                                host = h;
                                break;
                            }
                        }
                        ServerGroup serverGroup = null;
                        List<ServerGroup> serverGroups = context.get(TopologyTasks.SERVER_GROUPS);
                        for (ServerGroup sg : serverGroups) {
                            if (sg.getName().equals(server.getServerGroup())) {
                                serverGroup = sg;
                                break;
                            }
                        }
                        if (host == null || serverGroup == null) {
                            return;
                        }

                        // 1. Update the server config
                        host.getServers().stream()
                                .filter(srv -> srv.getId().equals(server.getId()))
                                .findAny()
                                .ifPresent(updatedServer -> topologyElements.replaceServer(server,
                                        () -> topologyElements.serverElement(updatedServer),
                                        __ -> serverDetails(updatedServer)));

                        // 2. Update not only the server config, but also the host and server group.
                        // Since the server's state has changed the host and server group
                        // dropdown links need to be updated as well.
                        Host finalHost = host;
                        ServerGroup finalServerGroup = serverGroup;
                        topologyElements.replaceHost(host,
                                () -> topologyElements.hostElement(finalHost),
                                __ -> hostDetails(finalHost));
                        topologyElements.replaceServerGroup(serverGroup,
                                () -> topologyElements.serverGroupElement(finalServerGroup),
                                updatedElement -> serverGroupDetails(finalServerGroup));

                        // 3. Try to update the server attribute if the server has been started
                        if (server.isStarted()) {
                            startedServerOperations(singletonList(server))
                                    .forEach((__, composite) -> dispatcher.execute(composite)
                                            .then(result -> {
                                                ModelNode attributes = result.step(0).get(RESULT);
                                                server.addServerAttributes(attributes);
                                                List<ModelNode> bootErrors = result.step(1).get(RESULT).asList();
                                                server.setBootErrors(!bootErrors.isEmpty());
                                                topologyElements.replaceServer(server,
                                                        () -> topologyElements.serverElement(server),
                                                        ___ -> serverDetails(server));
                                                return null;
                                            })
                                            .catch_(failure -> {
                                                String reason = String.valueOf(failure);
                                                server.setOperationFailure(reason);
                                                logger.error("Error in serverConfigsOfHost({}): Unable to update server {}: {}",
                                                        finalHost.getAddress(), server.getServerConfigAddress(), reason);
                                                MessageEvent.fire(eventBus,
                                                        Message.error(resources.messages().topologyError(), reason));
                                                topologyElements.replaceServer(server,
                                                        () -> topologyElements.serverElement(server),
                                                        ___ -> serverDetails(server));
                                                return null;
                                            }));
                        }

                    } else if (context.timeout()) {
                        logger.warn("Timeout in updateServer({})", server.getServerConfigAddress());
                        MessageEvent.fire(eventBus, Message.warning(resources.messages().topologyTimeout()));

                    } else if (context.failure()) {
                        String reason = context.failureReason();
                        logger.error("Error in updateServer({}): {}", server.getServerConfigAddress(), reason);
                        MessageEvent.fire(eventBus, Message.error(resources.messages().topologyError(), reason));
                    }
                });
    }

    private void serverDetails(Server server) {
        topologyElements.selectServer(server);
        topologyAttributes.refreshServer(server);
    }

    @Override
    public void onServerAction(final ServerActionEvent event) {
        if (topologyElements.isVisible()) {
            Server server = event.getServer();
            disableDropdown(server.getId(), server.getName());
            topologyElements.startProgress(server);
        }
    }

    @Override
    public void onServerResult(final ServerResultEvent event) {
        if (topologyElements.isVisible()) {
            topologyElements.stopProgress(event.getServer());
            updateServer(event.getServer());
        }
    }

    // ------------------------------------------------------ helpers

    private void adjustTdHeight() {
        NodeList<Element> servers = document.querySelectorAll("." + topology + " ." + CSS.servers);
        Elements.stream(servers)
                .filter(htmlElements())
                .map(asHtmlElement())
                .forEach(element -> {
                    HTMLElement parent = (HTMLElement) element.parentNode;
                    element.style.height = height(px(parent.offsetHeight - 1));
                });
    }

    private void disableDropdown(String id, String name) {
        Element link = document.getElementById(id);
        if (link != null) {
            Element parent = (Element) link.parentNode;
            Element ul = link.nextElementSibling;
            if (parent != null && ul != null) {
                HTMLElement noLink = span().css(CSS.name).title(name).textContent(name).element();
                parent.classList.remove("opened");
                parent.replaceChild(noLink, link);
                parent.removeChild(ul);
            }
        }
    }
}
