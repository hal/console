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
import java.util.List;
import java.util.Map;
import javax.inject.Provider;

import com.google.common.base.Joiner;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.group.ServerGroupActions;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerConfigStatus;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.DISABLED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STARTED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STOPPED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GROUP;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.*;

/**
 * @author Harald Pehl
 */
class TopologyPreview extends PreviewContent<StaticItem> {

    private static final String LOADING_SECTION = "loading-section";
    private static final String TOPOLOGY_SECTION = "topology-section";
    private static final String SERVER_GROUP_ATTRIBUTES_SECTION = "server-group-attributes-section";
    private static final String HOST_ATTRIBUTES_SECTION = "host-attributes-section";
    private static final String SERVER_ATTRIBUTES_SECTION = "server-attributes-section";

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final EventBus eventBus;
    private final HostActions hostActions;
    private final ServerGroupActions serverGroupActions;
    private final ServerActions serverActions;
    private final Resources resources;
    private final Element loadingSection;
    private final Element topologySection;
    private final Element serverGroupAttributesSection;
    private final Element hostAttributesSection;
    private final Element serverAttributesSection;
    private final PreviewAttributes<ServerGroup> serverGroupAttributes;
    private final PreviewAttributes<Host> hostAttributes;
    private final PreviewAttributes<Server> serverAttributes;

    TopologyPreview(final Environment environment,
            final Dispatcher dispatcher,
            final Provider<Progress> progress,
            final EventBus eventBus,
            final HostActions hostActions,
            final ServerGroupActions serverGroupActions,
            final ServerActions serverActions,
            final Resources resources) {
        super(Names.TOPOLOGY, resources.previews().topology());
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.eventBus = eventBus;
        this.hostActions = hostActions;
        this.serverGroupActions = serverGroupActions;
        this.serverActions = serverActions;
        this.resources = resources;

        // @formatter:off
        previewBuilder()
            .p()
                .a().css(clickable, pullRight).on(click, event-> update(null))
                    .span().css(fontAwesome("refresh"), marginRight4).end()
                    .span().textContent(resources.constants().refresh()).end()
                .end()
            .end()
            .section().css(centerBlock).rememberAs(LOADING_SECTION)
                .p().textContent(resources.constants().loading()).end()
                .div().css(spinner, spinnerLg).end()
            .end()
            .section().rememberAs(TOPOLOGY_SECTION)
            .end();
        // @formatter:on

        loadingSection = previewBuilder().referenceFor(LOADING_SECTION);
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
    }


    // ------------------------------------------------------ dmr functions

    @Override
    public void update(final StaticItem item) {
        clearSelected();
        Elements.setVisible(loadingSection, false);
        Elements.setVisible(topologySection, false);
        Elements.setVisible(serverGroupAttributesSection, false);
        Elements.setVisible(hostAttributesSection, false);
        Elements.setVisible(serverAttributesSection, false);

        // show the loading indicator if the dmr operation take too long
        int timeoutHandle = Browser.getWindow()
                .setTimeout(() -> Elements.setVisible(loadingSection, true), PROGRESS_TIMEOUT);
        new Async<FunctionContext>(progress.get()).single(
                new FunctionContext(),
                new Outcome<FunctionContext>() {
                    @Override
                    public void onFailure(final FunctionContext context) {
                        Browser.getWindow().clearTimeout(timeoutHandle);
                        Elements.setVisible(loadingSection, false);
                        MessageEvent.fire(eventBus, Message.error(resources.messages().topologyError(),
                                context.getErrorMessage()));
                    }

                    @Override
                    public void onSuccess(final FunctionContext context) {
                        Browser.getWindow().clearTimeout(timeoutHandle);
                        Elements.setVisible(loadingSection, false);
                        Elements.removeChildrenFrom(topologySection);

                        List<Host> hosts = context.get(TopologyFunctions.HOSTS);
                        List<ServerGroup> serverGroups = context.get(TopologyFunctions.SERVER_GROUPS);
                        List<Server> servers = context.get(TopologyFunctions.SERVERS);

                        topologySection.appendChild(createTable(hosts, serverGroups, servers));
                        Elements.setVisible(topologySection, true);
                        adjustTdHeight();
                    }
                },
                new TopologyFunctions.Topology(environment, dispatcher));
    }


    // ------------------------------------------------------ UI methods

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
                    long serversInGroup = servers.stream()
                            .filter(sc -> serverGroup.getName().equals(sc.getServerGroup()))
                            .count();
                    Map<ServerConfigStatus, List<Server>> byServerConfigStatus = servers.stream()
                            .filter(sc -> serverGroup.getName().equals(sc.getServerGroup()))
                            .collect(groupingBy(Server::getServerConfigStatus));
                    boolean allStarted = byServerConfigStatus.getOrDefault(STARTED, emptyList()).size() == serversInGroup;
                    boolean allStopped = byServerConfigStatus.getOrDefault(STOPPED, emptyList()).size() == serversInGroup;
                    boolean allDisabled = byServerConfigStatus.getOrDefault(DISABLED, emptyList()).size() == serversInGroup;
                    String serverGroupDropDownId = IdBuilder.build(serverGroup.getName(), "server-group", "dropdown");
                    builder.th()
                            .on(click, event -> serverGroupDetails(serverGroup))
                            .data("serverGroup", serverGroup.getName())
                        .div().css(serverGroupContainer)
                            // The dropdown is also added in case there are no servers. Otherwise the heights of
                            // the cells w/ and w/o servers would be different.
                            .div().css(dropdown);
                                if (serversInGroup > 0) {
                                    builder.a().id(serverGroupDropDownId)
                                        .css(clickable, dropdownToggle, name)
                                        .data(TOGGLE, DROPDOWN)
                                        .aria(HAS_POPUP, "true")
                                        .title(serverGroup.getName())
                                        .textContent(serverGroup.getName())
                                    .end()
                                    .ul().css(dropdownMenu).attr(ROLE, MENU).aria(LABELLED_BY, serverGroupDropDownId);
                                        serverGroupActions(builder, serverGroup, allStarted, allStopped, allDisabled);
                                    builder.end();
                                } else {
                                    builder.span().css(name).title(serverGroup.getName())
                                        .textContent(serverGroup.getName())
                                    .end();
                                }
                            builder.end()
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
                            .ul().css(dropdownMenu).attr(ROLE, MENU).aria(LABELLED_BY, hostDropDownId);
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
                                            .data("server", srv.getName())
                                        .div().css(dropdown)
                                            .a()
                                                .id(serverDropDownId)
                                                .css(clickable, dropdownToggle, name)
                                                .data(TOGGLE, DROPDOWN)
                                                .aria(HAS_POPUP, "true")
                                                .title(srv.getName())
                                                .textContent(srv.getName())
                                            .end()
                                            .ul().css(dropdownMenu).attr(ROLE, MENU).aria(LABELLED_BY, serverDropDownId);
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
                element.getStyle().setHeight(element.getParentElement().getOffsetHeight() - 1, PX));
    }

    private void clearSelected() {
        NodeList selected = Browser.getDocument().querySelectorAll("." + CSS.topology + " ." + CSS.selected);
        Elements.elements(selected).forEach(element -> element.getClassList().remove(CSS.selected));
    }

    private void startProgress(String selector) {
        Document document = Browser.getDocument();
        Elements.stream(document.querySelector(selector)).forEach(element -> element.getClassList().add(withProgress));
        Element menu = document.querySelector(selector + " " + dropdownMenu);
        if (menu != null) {
            // Elements.setVisible() will use "display: none" which will mess up the drop down menu
            menu.getStyle().setVisibility(HIDDEN);
        }
    }

    private void stopProgress(String selector) {
        Document document = Browser.getDocument();
        Elements.stream(document.querySelector(selector))
                .forEach(element -> element.getClassList().remove(withProgress));
        Element menu = document.querySelector(selector + " " + dropdownMenu);
        if (menu != null) {
            menu.getStyle().clearVisibility();
        }
    }

    private void timeout(String selector) {
        Document document = Browser.getDocument();
        Elements.stream(document.querySelector(selector)).forEach(element -> {
            element.getClassList().remove(withProgress);
            element.getClassList().add(error);
        });
        Element menu = document.querySelector(selector + " " + dropdownMenu);
        if (menu != null) {
            menu.getStyle().clearVisibility();
        }
    }


    // ------------------------------------------------------ server group

    private void serverGroupDetails(final ServerGroup serverGroup) {
        clearSelected();
        //noinspection HardCodedStringLiteral
        Element element = Browser.getDocument().querySelector("th[data-server-group=" + serverGroup.getName() + "]");
        if (element != null) {
            element.getClassList().add(selected);
        }

        serverGroupAttributes.refresh(serverGroup);
        Elements.setVisible(serverGroupAttributesSection, true);
        Elements.setVisible(hostAttributesSection, false);
        Elements.setVisible(serverAttributesSection, false);
    }

    private void serverGroupActions(final Elements.Builder builder, final ServerGroup serverGroup,
            final boolean allStarted, final boolean allStopped, final boolean allDisabled) {
        // @formatter:off
        if (!(allStopped || allDisabled)) {
            builder.li().attr(ROLE, PRESENTATION)
                .a().css(clickable)
                    .on(click, event -> serverGroupActions.reload(serverGroup, () -> {/* noop */}))
                    .textContent(resources.constants().reload())
                .end()
            .end()
            .li().attr(ROLE, PRESENTATION)
                .a().css(clickable)
                    .textContent(resources.constants().restart())
                .end()
            .end()
            .li().attr(ROLE, PRESENTATION)
                .a().css(clickable)
                    .textContent(resources.constants().suspend())
                .end()
            .end()
            .li().attr(ROLE, PRESENTATION)
                .a().css(clickable)
                    .textContent(resources.constants().resume())
                .end()
            .end()
            .li().attr(ROLE, PRESENTATION)
                .a().css(clickable)
                    .textContent(resources.constants().stop())
                .end()
            .end();
        }
        if (!allStarted) {
            builder.li().attr(ROLE, PRESENTATION)
                .a().css(clickable)
                    .textContent(resources.constants().start())
                .end()
            .end();
        }
        // @formatter:on
    }


    // ------------------------------------------------------ host

    private void hostDetails(final Host host) {
        clearSelected();
        //noinspection HardCodedStringLiteral
        Element element = Browser.getDocument().querySelector("th[data-host=" + host.getName() + "]");
        if (element != null) {
            element.getClassList().add(selected);
        }

        hostAttributes.refresh(host);
        Elements.setVisible(serverGroupAttributesSection, false);
        Elements.setVisible(hostAttributesSection, true);
        Elements.setVisible(serverAttributesSection, false);
    }

    private void hostActions(final Elements.Builder builder, final Host host) {
        builder.li().attr(ROLE, PRESENTATION).a().css(clickable).textContent(resources.constants().reload()).end()
                .end()
                .li().attr(ROLE, PRESENTATION).a().css(clickable).textContent(resources.constants().restart()).end()
                .end();
    }


    // ------------------------------------------------------ server

    private void serverDetails(final Server server) {
        clearSelected();
        //noinspection HardCodedStringLiteral
        Element element = Browser.getDocument().querySelector("div[data-server=" + server.getName() + "]");
        if (element != null) {
            element.getClassList().add(selected);
        }

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
            builder.li().attr(ROLE, PRESENTATION).a().css(clickable).textContent(resources.constants().start()).end()
                    .end();
        } else {
            builder.li().attr(ROLE, PRESENTATION).a().css(clickable).textContent(resources.constants().reload()).end()
                    .end()
                    .li().attr(ROLE, PRESENTATION).a().css(clickable).textContent(resources.constants().restart())
                    .end().end();
            if (server.isSuspending()) {
                builder.li().attr(ROLE, PRESENTATION).a().css(clickable).textContent(resources.constants().resume())
                        .end().end();
            } else {
                builder.li().attr(ROLE, PRESENTATION).a().css(clickable).textContent(resources.constants().suspend())
                        .end().end();
            }
            builder.li().attr(ROLE, PRESENTATION).a().css(clickable).textContent(resources.constants().stop()).end()
                    .end();
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
        } else if (server.isFailed()) {
            status.add(error);
        }
        if (server.isStarting()) {
            status.add(withProgress);
        }
        return status.toArray(new String[status.size()]);
    }
}
