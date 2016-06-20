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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Provider;

import com.google.common.base.Joiner;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.client.runtime.group.ServerGroup;
import org.jboss.hal.client.runtime.group.ServerGroupActions;
import org.jboss.hal.client.runtime.host.Host;
import org.jboss.hal.client.runtime.host.HostActions;
import org.jboss.hal.client.runtime.server.Server;
import org.jboss.hal.client.runtime.server.ServerActions;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GROUP;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.*;

/**
 * @author Harald Pehl
 */
class TopologyPreview extends PreviewContent<StaticItem> {

    private static final String TOPOLOGY_SECTION = "topology-section";
    private static final String SERVER_GROUP_ATTRIBUTES_SECTION = "server-group-attributes-section";
    private static final String SERVER_GROUP_DATA = "server-group-data";
    private static final String HOST_ATTRIBUTES_SECTION = "host-attributes-section";
    private static final String HOST_DATA = "host-data";
    private static final String SERVER_ATTRIBUTES_SECTION = "server-attributes-section";
    private static final String SERVER_DATA = "server-data";

    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final EventBus eventBus;
    private final HostActions hostActions;
    private final ServerGroupActions serverGroupActions;
    private final ServerActions serverActions;
    private final Resources resources;
    private final Element topologySection;
    private final Element serverGroupAttributesSection;
    private final Element hostAttributesSection;
    private final Element serverAttributesSection;
    private final PreviewAttributes<ServerGroup> serverGroupAttributes;
    private final PreviewAttributes<Host> hostAttributes;
    private final PreviewAttributes<Server> serverAttributes;

    TopologyPreview(final Dispatcher dispatcher,
            final Provider<Progress> progress,
            final EventBus eventBus,
            final HostActions hostActions,
            final ServerGroupActions serverGroupActions,
            final ServerActions serverActions,
            final Resources resources) {
        super(Names.TOPOLOGY, resources.previews().topology());
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.eventBus = eventBus;
        this.hostActions = hostActions;
        this.serverGroupActions = serverGroupActions;
        this.serverActions = serverActions;
        this.resources = resources;

        // @formatter:off
        previewBuilder().p()
            .a().css(clickable, pullRight).on(click, event-> update(null))
                .span().css(fontAwesome("refresh"), marginRight4).end()
                .span().textContent(resources.constants().refresh()).end()
            .end()
        .end();
        // @formatter:on

        previewBuilder().section().rememberAs(TOPOLOGY_SECTION).end();
        topologySection = previewBuilder().referenceFor(TOPOLOGY_SECTION);

        serverGroupAttributes = new PreviewAttributes<>(new ServerGroup(new ModelNode()), Names.SERVER_GROUP,
                Arrays.asList(NAME, PROFILE, SOCKET_BINDING_GROUP, SOCKET_BINDING_PORT_OFFSET,
                        SOCKET_BINDING_DEFAULT_INTERFACE))
                .end();
        hostAttributes = new PreviewAttributes<>(new Host(new ModelNode()), Names.HOST,
                asList(NAME, RELEASE_CODENAME, RELEASE_VERSION, PRODUCT_NAME, PRODUCT_VERSION,
                        HOST_STATE, RUNNING_MODE))
                .append(model -> {
                    return new String[]{
                            "Management Version", //NON-NLS
                            Joiner.on('.').join(
                                    model.get(MANAGEMENT_MAJOR_VERSION),
                                    model.get(MANAGEMENT_MINOR_VERSION),
                                    model.get(MANAGEMENT_MICRO_VERSION))
                    };
                })
                .end();
        serverAttributes = new PreviewAttributes<>(new Server("", new ModelNode()), Names.SERVER,
                asList(NAME, HOST, GROUP, PROFILE_NAME, AUTO_START, SOCKET_BINDING_PORT_OFFSET, STATUS, RUNNING_MODE,
                        SERVER_STATE, SUSPEND_STATE))
                .end();

        // @formatter:off
        previewBuilder()
            .section().rememberAs(SERVER_GROUP_ATTRIBUTES_SECTION)
                .addAll(serverGroupAttributes)
            .end()
            .section().rememberAs(HOST_ATTRIBUTES_SECTION)
                .addAll(hostAttributes)
            .end()
            .section().rememberAs(SERVER_ATTRIBUTES_SECTION)
                .addAll(serverAttributes)
            .end();
        // @formatter:on
        serverGroupAttributesSection = previewBuilder().referenceFor(SERVER_GROUP_ATTRIBUTES_SECTION);
        hostAttributesSection = previewBuilder().referenceFor(HOST_ATTRIBUTES_SECTION);
        serverAttributesSection = previewBuilder().referenceFor(SERVER_ATTRIBUTES_SECTION);
        Elements.setVisible(serverGroupAttributesSection, false);
        Elements.setVisible(hostAttributesSection, false);
        Elements.setVisible(serverAttributesSection, false);
    }


    // ------------------------------------------------------ dmr functions

    @Override
    public void update(final StaticItem item) {
        Function<FunctionContext> topologyFn = control -> {
            Operation hostOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                    .param(CHILD_TYPE, HOST)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation serverGroupOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                    .param(CHILD_TYPE, SERVER_GROUP)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation serverConfigOp = new Operation.Builder(QUERY,
                    new ResourceAddress().add(HOST, "*").add(SERVER_CONFIG, "*"))
                    .param(SELECT, new ModelNode().add(NAME).add(AUTO_START).add(GROUP).add(SOCKET_BINDING_PORT_OFFSET)
                            .add(STATUS))
                    .build();
            dispatcher.executeInFunction(control, new Composite(hostOp, serverGroupOp, serverConfigOp),
                    (CompositeResult result) -> {
                        // first collect all hosts, sort them by name and finally
                        // remove the host controller to add it as first element
                        List<Host> allHosts = result.step(0).get(RESULT).asPropertyList().stream()
                                .map(Host::new)
                                .sorted(comparing(Host::getName))
                                .collect(toList());
                        Host domainController = null;
                        List<Host> hosts = new ArrayList<>(allHosts);
                        for (Iterator<Host> iterator = hosts.iterator();
                                iterator.hasNext() && domainController == null; ) {
                            Host host = iterator.next();
                            if (host.isDomainController()) {
                                domainController = host;
                                iterator.remove();
                            }
                        }
                        if (domainController != null) {
                            hosts.add(0, domainController);
                        }
                        control.getContext().set(HOST_DATA, hosts);

                        List<ServerGroup> serverGroups = result.step(1).get(RESULT).asPropertyList().stream()
                                .map(ServerGroup::new)
                                .sorted(comparing(ServerGroup::getName))
                                .collect(toList());
                        control.getContext().set(SERVER_GROUP_DATA, serverGroups);

                        List<Server> serverConfigs = result.step(2).get(RESULT).asList().stream()
                                .filter(modelNode -> !modelNode.isFailure())
                                .map(modelNode -> {
                                    ResourceAddress address = new ResourceAddress(modelNode.get(ADDRESS));
                                    String host = address.getParent().lastValue();
                                    return new Server(host, modelNode.get(RESULT));
                                })
                                .collect(toList());
                        control.getContext().set(SERVER_DATA, serverConfigs);
                        control.proceed();
                    });
        };

        Function<FunctionContext> runningServerDetailsFn = control -> {
            List<Server> servers1 = control.getContext().get(SERVER_DATA);
            Map<String, Server> serversByName = servers1.stream().collect(toMap(Server::getName, identity()));
            Composite composite = new Composite(servers1.stream()
                    .filter(Server::isStarted)
                    .map((server1 ->
                            new Operation.Builder(READ_RESOURCE_OPERATION,
                                    new ResourceAddress().add(HOST, server1.getHost()).add(SERVER, server1.getName()))
                                    .param(INCLUDE_RUNTIME, true)
                                    .build()))
                    .collect(toList()));
            dispatcher.executeInFunction(control, composite, (CompositeResult result) -> {
                result.stream().filter((modelNode) -> !modelNode.isFailure())
                        .forEach(modelNode -> {
                            ModelNode payload = modelNode.get(RESULT);
                            Server server1 = serversByName.get(payload.get(NAME).asString());
                            if (server1 != null) {
                                server1.addServerAttributes(payload);
                            }
                        });
                control.proceed();
            });
        };

        new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                MessageEvent.fire(eventBus, Message.error(resources.messages().topologyError(),
                        context.getErrorMessage()));
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                Elements.removeChildrenFrom(topologySection);

                List<Host> hosts = context.get(HOST_DATA);
                List<ServerGroup> serverGroups = context.get(SERVER_GROUP_DATA);
                List<Server> servers = context.get(SERVER_DATA);
                topologySection.appendChild(createTable(hosts, serverGroups, servers));

                // Browser.getWindow().setOnresize(event -> adjustTdHeight());
                adjustTdHeight();
            }
        }, topologyFn, runningServerDetailsFn);
    }


    // ------------------------------------------------------ UI update

    @SuppressWarnings("HardCodedStringLiteral")
    private Element createTable(List<Host> hosts, List<ServerGroup> serverGroups, List<Server> servers) {
        Elements.Builder builder = new Elements.Builder().table().css(topology);

        // <colgroup>
        double width = 100.0 / (serverGroups.size() + 1);
        builder.start("colgroup").start("col").attr("width", String.valueOf(width) + "%").end();
        for (int i = 0; i < serverGroups.size(); i++) {
            builder.start("col").attr("width", String.valueOf(width) + "%").end();
        }
        builder.end();
        // </colgroup>

        // <thead> @formatter:off
        builder.thead()
            .tr()
                .th().css(empty)
                    .innerHtml(new SafeHtmlBuilder()
                            .appendEscaped(Names.SERVER_GROUPS + " ")
                            .appendHtmlConstant("&rarr;").appendHtmlConstant("<br/>")
                            .appendEscaped(Names.HOSTS + " ")
                            .appendHtmlConstant("&darr;").toSafeHtml())
                .end();
                for (ServerGroup serverGroup : serverGroups) {
                    String serverGroupDropDownId = IdBuilder.build(serverGroup.getName(), "server-group", "dropdown");
                    builder.th()
                            .on(click, event -> serverGroupDetails(serverGroup))
                            .data("serverGroup", serverGroup.getName())
                        .div().css(serverGroupContainer)
                            .div().css(dropdown)
                                .a().id(serverGroupDropDownId)
                                    .css(clickable, dropdownToggle, name)
                                    .data(TOGGLE, DROPDOWN)
                                    .aria(HAS_POPUP, "true")
                                    .title(serverGroup.getName())
                                    .textContent(serverGroup.getName())
                                .end()
                                .ul().css(dropdownMenu).attr(ROLE, "menu").aria(LABELLED_BY, serverGroupDropDownId);
                                    serverGroupActions(builder, serverGroup);
                                builder.end()
                            .end()
                        .end()
                    .end();
                }
            builder.end()
        .end();
        // </thead> @formatter:on

        // <tbody> @formatter:off
        builder.tbody();
        for (Host host : hosts) {
            String hostDropDownId = IdBuilder.build(host.getName(), "host", "dropdown");
            builder.tr()
                .th().css(rowHeader)
                        .on(click, event -> hostDetails(host))
                        .data("host", host.getName())
                    .div().css(hostContainer)
                        .div().css(dropdown)
                            .a()
                                .id(hostDropDownId)
                                .css(clickable, dropdownToggle, name)
                                .data(TOGGLE, DROPDOWN)
                                .aria(HAS_POPUP, "true")
                                .title(host.getName());
                                if (host.isDomainController()) {
                                    builder.span().css(fontAwesome("star"), marginRight4).title(Names.DOMAIN_CONTROLLER).end();
                                }
                                builder.span().textContent(host.getName()).end()
                            .end()
                            .ul().css(dropdownMenu).attr(ROLE, "menu").aria(LABELLED_BY, hostDropDownId);
                                hostActions(builder, host);
                            builder.end()
                        .end()
                    .end()
                .end();
                for (ServerGroup serverGroup : serverGroups) {
                    List<Server> matchingServers =  servers.stream()
                            .filter(sc -> host.getName().equals(sc.getHost()) && serverGroup.getName().equals(sc.getServerGroup()))
                            .sorted(comparing(Server::getName))
                            .collect(toList());
                    if (matchingServers.isEmpty()) {
                        builder.td().css(empty).end();
                    } else {
                        builder.td()
                            .div().css(CSS.servers);
                                for (Server srv : matchingServers) {
                                    String serverDropDownId = IdBuilder.build(serverGroup.getName(), "server", "dropdown");
                                    builder.div()
                                            .css(server, statusCss(srv))
                                            .on(click, event -> serverDetails(srv))
                                            .data("serverGroup", serverGroup.getName())
                                            .data("host", host.getName())
                                        .div().css(dropdown)
                                            .a()
                                                .id(serverDropDownId)
                                                .css(clickable, dropdownToggle, name)
                                                .data(TOGGLE, DROPDOWN)
                                                .aria(HAS_POPUP, "true")
                                                .title(srv.getName())
                                                .textContent(srv.getName())
                                            .end()
                                            .ul().css(dropdownMenu).attr(ROLE, "menu").aria(LABELLED_BY, serverDropDownId);
                                                serverActions(builder, srv);
                                            builder.end()
                                        .end()
                                    .end();
                                }
                            builder.end()
                        .end();
                    }
                }
            builder.end();
        }
        builder.end();
        // </tbody> @formatter:on

        return builder.end().build();
    }

    private void adjustTdHeight() {
        NodeList servers = Browser.getDocument().querySelectorAll("." + CSS.topology + " ." + CSS.servers);
        Elements.elements(servers).forEach(element ->
                element.getStyle().setHeight(element.getParentElement().getOffsetHeight(), PX));
    }


    // ------------------------------------------------------ server group

    private void serverGroupDetails(final ServerGroup serverGroup) {
        serverGroupAttributes.refresh(serverGroup);
        Elements.setVisible(serverGroupAttributesSection, true);
        Elements.setVisible(hostAttributesSection, false);
        Elements.setVisible(serverAttributesSection, false);
    }

    private void serverGroupActions(final Elements.Builder builder, final ServerGroup serverGroup) {
        builder.li().a().css(clickable).textContent(resources.constants().reload()).end().end()
                .li().a().css(clickable).textContent(resources.constants().restart()).end().end()
                .li().a().css(clickable).textContent(resources.constants().suspend()).end().end()
                .li().a().css(clickable).textContent(resources.constants().resume()).end().end()
                .li().a().css(clickable).textContent(resources.constants().stop()).end().end()
                .li().a().css(clickable).textContent(resources.constants().start()).end().end();
    }


    // ------------------------------------------------------ host

    private void hostDetails(final Host host) {
        hostAttributes.refresh(host);
        Elements.setVisible(serverGroupAttributesSection, false);
        Elements.setVisible(hostAttributesSection, true);
        Elements.setVisible(serverAttributesSection, false);
    }

    private void hostActions(final Elements.Builder builder, final Host host) {
        builder.li().a().css(clickable).textContent(resources.constants().reload()).end().end()
                .li().a().css(clickable).textContent(resources.constants().restart()).end().end();
    }


    // ------------------------------------------------------ server

    private void serverDetails(final Server server) {
        serverAttributes.refresh(server);
        Elements.setVisible(serverGroupAttributesSection, false);
        Elements.setVisible(hostAttributesSection, false);
        Elements.setVisible(serverAttributesSection, true);

        serverAttributes.setVisible(PROFILE_NAME, server.isStarted());
        serverAttributes.setVisible(RUNNING_MODE, server.isStarted());
        serverAttributes.setVisible(SERVER_STATE, server.isStarted());
        serverAttributes.setVisible(SUSPEND_STATE, server.isStarted());
    }

    private void serverActions(final Elements.Builder builder, final Server server) {
        if (!server.isStarted()) {
            builder.li().a().css(clickable).textContent(resources.constants().start()).end().end();
        } else {
            builder.li().a().css(clickable).textContent(resources.constants().reload()).end().end()
                    .li().a().css(clickable).textContent(resources.constants().restart()).end().end();
            if (server.isSuspending()) {
                builder.li().a().css(clickable).textContent(resources.constants().resume()).end().end();
            } else {
                builder.li().a().css(clickable).textContent(resources.constants().suspend()).end().end();
            }
            builder.li().a().css(clickable).textContent(resources.constants().stop()).end().end();
        }
    }

    private String[] statusCss(final Server server) {
        List<String> status = new ArrayList<>();
        if (server.isAdminMode() || server.isStopped()) {
            status.add(inactive);
        } else if (server.isSuspending() || server.needsReload() || server.needsRestart()) {
            status.add(warning);
        } else if (server.isStarted() || server.isRunning()) {
            status.add(ok);
        } else if (server.isTimeout()) {
            status.add(error);
        }
        if (server.isStarting()) {
            status.add(withProgress);
        }
        return status.toArray(new String[status.size()]);
    }
}
