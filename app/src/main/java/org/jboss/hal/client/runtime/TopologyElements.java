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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jboss.elemento.Elements;
import org.jboss.elemento.ElementsBag;
import org.jboss.elemento.EventCallbackFn;
import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.hal.client.runtime.server.ServerStatusSwitch;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.group.ServerGroupActions;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableColElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableSectionElement;
import elemental2.dom.MouseEvent;
import elemental2.dom.NodeList;

import static com.google.common.collect.Lists.asList;
import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setTimeout;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.col;
import static org.jboss.elemento.Elements.colgroup;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.table;
import static org.jboss.elemento.Elements.tbody;
import static org.jboss.elemento.Elements.td;
import static org.jboss.elemento.Elements.th;
import static org.jboss.elemento.Elements.thead;
import static org.jboss.elemento.Elements.tr;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.runtime.TopologySelectors.hostDataName;
import static org.jboss.hal.client.runtime.TopologySelectors.hostDataValue;
import static org.jboss.hal.client.runtime.TopologySelectors.hostSelector;
import static org.jboss.hal.client.runtime.TopologySelectors.serverDataName;
import static org.jboss.hal.client.runtime.TopologySelectors.serverDataValue;
import static org.jboss.hal.client.runtime.TopologySelectors.serverGroupDataName;
import static org.jboss.hal.client.runtime.TopologySelectors.serverGroupDataValue;
import static org.jboss.hal.client.runtime.TopologySelectors.serverGroupSelector;
import static org.jboss.hal.client.runtime.TopologySelectors.serverSelector;
import static org.jboss.hal.client.runtime.TopologySelectors.serversDataName;
import static org.jboss.hal.client.runtime.TopologySelectors.serversDataValue;
import static org.jboss.hal.client.runtime.TopologySelectors.serversSelector;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_SERVERS;
import static org.jboss.hal.resources.CSS.centerBlock;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.disconnected;
import static org.jboss.hal.resources.CSS.divider;
import static org.jboss.hal.resources.CSS.dropdownMenu;
import static org.jboss.hal.resources.CSS.dropdownToggle;
import static org.jboss.hal.resources.CSS.empty;
import static org.jboss.hal.resources.CSS.error;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.hostContainer;
import static org.jboss.hal.resources.CSS.inactive;
import static org.jboss.hal.resources.CSS.marginLeft5;
import static org.jboss.hal.resources.CSS.name;
import static org.jboss.hal.resources.CSS.ok;
import static org.jboss.hal.resources.CSS.rowHeader;
import static org.jboss.hal.resources.CSS.selected;
import static org.jboss.hal.resources.CSS.serverGroupContainer;
import static org.jboss.hal.resources.CSS.spinner;
import static org.jboss.hal.resources.CSS.spinnerLg;
import static org.jboss.hal.resources.CSS.suspended;
import static org.jboss.hal.resources.CSS.topology;
import static org.jboss.hal.resources.CSS.warning;
import static org.jboss.hal.resources.CSS.withProgress;
import static org.jboss.hal.resources.UIConstants.MEDIUM_TIMEOUT;

class TopologyElements {

    private final TopologyCallback<Host> hostSelectCallback;
    private final TopologyCallback<ServerGroup> serverGroupSelectCallback;
    private final TopologyCallback<Server> serverSelectCallback;
    private final TopologyCallback<Server> serverUpdateCallback;
    private final SecurityContextRegistry securityContextRegistry;
    private final Environment environment;
    private final HostActions hostActions;
    private final ServerGroupActions serverGroupActions;
    private final ServerActions serverActions;
    private final Resources resources;
    private final HTMLElement loadingSection;
    private final HTMLElement topologySection;
    private double timeoutHandle;

    TopologyElements(
            TopologyCallback<Host> hostSelectCallback,
            TopologyCallback<ServerGroup> serverGroupSelectCallback,
            TopologyCallback<Server> serverSelectCallback,
            TopologyCallback<Server> serverUpdateCallback,
            SecurityContextRegistry securityContextRegistry,
            HostActions hostActions,
            ServerGroupActions serverGroupActions,
            ServerActions serverActions,
            Environment environment,
            Resources resources) {
        this.hostSelectCallback = hostSelectCallback;
        this.serverGroupSelectCallback = serverGroupSelectCallback;
        this.serverSelectCallback = serverSelectCallback;
        this.serverUpdateCallback = serverUpdateCallback;
        this.securityContextRegistry = securityContextRegistry;
        this.environment = environment;
        this.hostActions = hostActions;
        this.serverGroupActions = serverGroupActions;
        this.serverActions = serverActions;
        this.resources = resources;

        this.loadingSection = section().css(centerBlock)
                .add(p().textContent(resources.constants().loading()))
                .add(div().css(spinner, spinnerLg)).element();
        this.topologySection = section().element();
    }

    void addTo(ElementsBag previewBuilder) {
        previewBuilder
                .add(loadingSection)
                .add(topologySection);
    }

    // ------------------------------------------------------ topology

    void startUpdate() {
        setVisible(loadingSection, false);
        setVisible(topologySection, false);

        removeChildrenFrom(topologySection);
        clearSelected();
        timeoutHandle = setTimeout((o) -> setVisible(loadingSection, true), MEDIUM_TIMEOUT);
    }

    void update(List<Host> hosts, List<ServerGroup> serverGroups) {
        topologySection.appendChild(topologyElement(hosts, serverGroups));
        setVisible(topologySection, true);
    }

    void finishUpdate() {
        clearTimeout(timeoutHandle);
        setVisible(loadingSection, false);
    }

    boolean isVisible() {
        return Elements.isVisible(topologySection) && topologySection.parentNode != null;
    }

    private HTMLElement topologyElement(List<Host> hosts, List<ServerGroup> serverGroups) {
        HTMLTableElement table = table().css(topology).element();

        // <colgroup>
        double width = 100.0 / (serverGroups.size() + 1);
        HtmlContentBuilder<HTMLTableColElement> colgroup = colgroup()
                .add(col().attr("width", width + "%"));
        for (int i = 0; i < serverGroups.size(); i++) {
            colgroup.add(col().attr("width", width + "%"));
        }
        table.appendChild(colgroup.element());
        // </colgroup>

        // <thead>
        HtmlContentBuilder<HTMLTableSectionElement> thead = thead()
                .add(tr()
                        .add(th().css(empty)
                                .innerHtml(new SafeHtmlBuilder()
                                        .appendEscaped(Names.SERVER_GROUPS + " ")
                                        .appendHtmlConstant("&rarr;").appendHtmlConstant("<br/>")
                                        .appendEscaped(Names.HOSTS + " ")
                                        .appendHtmlConstant("&darr;").toSafeHtml()))
                        .addAll(serverGroups.stream().map(this::serverGroupElement).collect(toList())));
        table.appendChild(thead.element());
        // </thead>

        // <tbody>
        HTMLElement tbody = tbody().element();
        for (Host host : hosts) {
            HTMLElement tr;
            tbody.appendChild(tr = tr().element());
            tr.appendChild(hostElement(host));
            for (ServerGroup serverGroup : serverGroups) {
                tr.appendChild(td().css(empty, CSS.progress)
                        .data(serversDataName(), serversDataValue(host, serverGroup))
                        .element());
            }
        }
        table.appendChild(tbody);
        // </tbody>

        return table;
    }

    // ------------------------------------------------------ host

    HTMLElement hostElement(Host host) {
        HTMLElement dropdown;
        List<String> css = asList(rowHeader, hostActions.isPending(host) ? new String[] { withProgress } : new String[0]);
        HTMLTableCellElement th = th()
                .css(css.toArray(new String[0]))
                .data(hostDataName(), hostDataValue(host))
                .on(click, event -> hostSelectCallback.execute(host))
                .add(div().css(hostContainer)
                        .add(dropdown = div().css(CSS.dropdown).element()))
                .element();

        HTMLElement hostNameElement;
        if (host.isAlive() && !hostActions.isPending(host) && isAllowed(host)) {
            String hostDropDownId = Ids.host(host.getAddressName());
            dropdown.appendChild(hostNameElement = a()
                    .id(hostDropDownId)
                    .css(clickable, dropdownToggle, name)
                    .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                    .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                    .title(host.getName()).element());
            dropdown.appendChild(ul()
                    .css(dropdownMenu)
                    .attr(UIConstants.ROLE, UIConstants.MENU)
                    .aria(UIConstants.LABELLED_BY, hostDropDownId)
                    .add(actionLink(event -> hostActions.reload(host), resources.constants().reload()))
                    .add(actionLink(event -> hostActions.restart(host), resources.constants().restart()))
                    .element());
        } else {
            dropdown.appendChild(hostNameElement = span().css(name).title(host.getName()).element());
        }
        hostNameElement.appendChild(hostNameElement.ownerDocument.createTextNode(host.getName()));
        if (!host.isConnected()) {
            hostNameElement.classList.add(disconnected);
            hostNameElement.title = hostNameElement.title + " (" + resources.constants().disconnected() + ")";
        }
        if (host.isDomainController()) {
            hostNameElement.appendChild(
                    span().css(fontAwesome("star"), marginLeft5).title(Names.DOMAIN_CONTROLLER).element());
        }

        return th;
    }

    void selectHost(Host host) {
        clearSelected();
        HTMLElement element = lookupHost(host);
        if (element != null) {
            element.classList.add(selected);
        }
    }

    void replaceHost(Host host, Supplier<Element> updateElement, Consumer<Element> select) {
        replace(lookupHost(host), updateElement, select);
    }

    HTMLElement lookupHost(Host host) {
        return (HTMLElement) document.querySelector(hostSelector(host));
    }

    void startProgress(Host host) {
        startProgress(hostSelector(host));
    }

    void stopProgress(Host host) {
        stopProgress(hostSelector(host));
    }

    private boolean isAllowed(Host host) {
        // To keep it simple, we take an all or nothing approach:
        // We check *one* action and assume that the other actions have the same constraints
        return AuthorisationDecision.from(environment, securityContextRegistry)
                .isAllowed(Constraint.executable(AddressTemplate.of("/host=" + host.getAddressName()), RELOAD));
    }

    // ------------------------------------------------------ server group

    HTMLElement serverGroupElement(ServerGroup serverGroup) {
        HTMLElement dropdown;
        HTMLTableCellElement element = th()
                .data(serverGroupDataName(), serverGroupDataValue(serverGroup))
                .on(click, event -> serverGroupSelectCallback.execute(serverGroup))
                .add(div().css(serverGroupContainer)
                        .add(dropdown = div().css(CSS.dropdown).element()))
                .element();

        if (!serverGroupActions.isPending(serverGroup) && isAllowed(serverGroup)) {
            String serverGroupDropDownId = Ids.serverGroup(serverGroup.getName());
            dropdown.appendChild(a()
                    .id(serverGroupDropDownId)
                    .css(clickable, dropdownToggle, name)
                    .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                    .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                    .title(serverGroup.getName())
                    .textContent(serverGroup.getName()).element());
            dropdown.appendChild(ul()
                    .css(dropdownMenu)
                    .attr(UIConstants.ROLE, UIConstants.MENU)
                    .aria(UIConstants.LABELLED_BY, serverGroupDropDownId)
                    .addAll(serverGroupActions(serverGroup)).element());
        } else {
            dropdown.appendChild(span()
                    .css(name)
                    .title(serverGroup.getName())
                    .textContent(serverGroup.getName()).element());
        }
        return element;
    }

    void selectServerGroup(ServerGroup serverGroup) {
        clearSelected();
        HTMLElement element = lookupServerGroup(serverGroup);
        if (element != null) {
            element.classList.add(selected);
        }
    }

    void replaceServerGroup(ServerGroup serverGroup, Supplier<Element> updateElement, Consumer<Element> select) {
        replace(lookupServerGroup(serverGroup), updateElement, select);
    }

    HTMLElement lookupServerGroup(ServerGroup serverGroup) {
        return (HTMLElement) document.querySelector(serverGroupSelector(serverGroup));
    }

    private List<HTMLElement> serverGroupActions(ServerGroup serverGroup) {
        List<HTMLElement> actions = new ArrayList<>();

        // Order is: reload, restart, suspend, resume, stop, start
        if (serverGroup.hasServers(Server::isStarted)) {
            actions.add(actionLink(event -> serverGroupActions.reload(serverGroup), resources.constants().reload()));
            actions.add(actionLink(event -> serverGroupActions.restart(serverGroup), resources.constants().restart()));
        }
        if (serverGroup.getServers(Server::isStarted).size() - serverGroup.getServers(Server::isSuspended).size() > 0) {
            actions.add(actionLink(event -> serverGroupActions.suspend(serverGroup), resources.constants().suspend()));
        }
        if (serverGroup.hasServers(Server::isSuspended)) {
            actions.add(actionLink(event -> serverGroupActions.resume(serverGroup), resources.constants().resume()));
        }
        if (serverGroup.hasServers(Server::isStarted)) {
            actions.add(actionLink(event -> serverGroupActions.stop(serverGroup), resources.constants().stop()));
        }
        if (serverGroup.hasServers(server -> server.isStopped() || server.isFailed())) {
            actions.add(actionLink(event -> serverGroupActions.start(serverGroup), resources.constants().start()));
            actions.add(actionLink(event -> serverGroupActions.startInSuspendedMode(serverGroup),
                    resources.constants().startInSuspendedMode()));
        }
        // add kill link regardless of state to destroy and kill servers which might show a wrong state
        actions.add(actionLink(event -> serverGroupActions.destroy(serverGroup), resources.constants().destroy()));
        actions.add(actionLink(event -> serverGroupActions.kill(serverGroup), resources.constants().kill()));

        // add remove action to groups which have only stopped servers or no servers at all
        if (!serverGroup.hasServers(Server::isStarted)) {
            actions.add(actionLink(event -> serverGroupActions.remove(serverGroup), resources.constants().remove()));
        }
        return actions;
    }

    private boolean isAllowed(ServerGroup serverGroup) {
        // To keep it simple, we take an all or nothing approach:
        // We check *one* action and assume that the other actions have the same constraints
        Constraints constraints = Constraints.or(
                Constraint.executable(AddressTemplate.of("/server-group=*"), RELOAD_SERVERS),
                Constraint.executable(AddressTemplate.of("/server-group=" + serverGroup.getName()), RELOAD_SERVERS));
        return AuthorisationDecision.from(environment, securityContextRegistry)
                .isAllowed(constraints);
    }

    // ------------------------------------------------------ server

    HTMLElement serverElement(Server server) {
        HTMLElement dropdown;
        HTMLDivElement element = div()
                .css(asList(CSS.server, statusCss(server)).toArray(new String[0]))
                .data(serverDataName(), serverDataValue(server))
                .on(click, event -> serverSelectCallback.execute(server))
                .add(dropdown = div().css(CSS.dropdown).element()).element();

        if (!serverActions.isPending(server) && isAllowed(server)) {
            dropdown.appendChild(a()
                    .id(server.getId())
                    .css(clickable, dropdownToggle, name)
                    .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                    .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                    .title(server.getName())
                    .textContent(server.getName()).element());
            dropdown.appendChild(ul()
                    .css(dropdownMenu)
                    .attr(UIConstants.ROLE, UIConstants.MENU)
                    .aria(UIConstants.LABELLED_BY, server.getId())
                    .addAll(serverActions(server)).element());
        } else {
            dropdown.appendChild(span()
                    .css(name)
                    .title(server.getName())
                    .textContent(server.getName()).element());
        }
        return element;
    }

    void selectServer(Server server) {
        clearSelected();
        HTMLElement element = lookupServerElement(server);
        if (element != null) {
            element.classList.add(selected);
        }
    }

    void replaceServer(Server server, Supplier<Element> updateElement, Consumer<Element> select) {
        replace(lookupServerElement(server), updateElement, select);
    }

    HTMLElement lookupServersElement(Host host, ServerGroup serverGroup) {
        return (HTMLElement) document.querySelector(serversSelector(host, serverGroup));
    }

    HTMLElement lookupServerElement(Server server) {
        return (HTMLElement) document.querySelector(serverSelector(server));
    }

    void startProgress(Server server) {
        startProgress(serverSelector(server));
    }

    void stopProgress(Server server) {
        stopProgress(serverSelector(server));
    }

    private List<HTMLElement> serverActions(Server server) {
        List<HTMLElement> actions = new ArrayList<>();

        if (!server.hasOperationFailure()) {
            if (!server.isStarted()) {
                actions.add(actionLink(event -> serverActions.start(server), resources.constants().start()));
                actions.add(actionLink(event -> serverActions.startInSuspendedMode(server),
                        resources.constants().startInSuspendedMode()));
            } else {
                actions.add(actionLink(event -> serverActions.editUrl(server, () -> {
                    if (isVisible()) {
                        serverUpdateCallback.execute(server);
                    }
                }), resources.constants().editURL()));
                actions.add(li().css(divider).attr(UIConstants.ROLE, UIConstants.SEPARATOR).element());
                // Order is: reload, restart, (resume | suspend), stop
                actions.add(actionLink(event -> serverActions.reload(server), resources.constants().reload()));
                actions.add(actionLink(event -> serverActions.restart(server), resources.constants().restart()));
                if (server.isSuspended()) {
                    actions.add(actionLink(event -> serverActions.resume(server), resources.constants().resume()));
                } else {
                    actions.add(actionLink(event -> serverActions.suspend(server), resources.constants().suspend()));
                }
                actions.add(actionLink(event -> serverActions.stop(server), resources.constants().stop()));
            }
        }
        // add kill link regardless of state to destroy and kill servers which might show a wrong state
        actions.add(actionLink(event -> serverActions.destroy(server), resources.constants().destroy()));
        actions.add(actionLink(event -> serverActions.kill(server), resources.constants().kill()));

        return actions;
    }

    private boolean isAllowed(Server server) {
        // To keep it simple, we take an all or nothing approach:
        // We check *one* action and assume that the other actions have the same constraints
        return AuthorisationDecision.from(environment, securityContextRegistry)
                .isAllowed(Constraint.executable(AddressTemplate.of("/host=" + server.getHost() + "/server-config=*"),
                        RELOAD));
    }

    private String[] statusCss(Server server) {
        Set<String> status = new HashSet<>();
        ServerStatusSwitch sss = new ServerStatusSwitch(serverActions) {
            @Override
            protected void onPending(Server server) {
            }

            @Override
            protected void onBootErrors(Server server) {
                status.add(error);
            }

            @Override
            protected void onFailed(Server server) {
                status.add(error);
            }

            @Override
            protected void onAdminMode(Server server) {
                status.add(inactive);
            }

            @Override
            protected void onStarting(Server server) {
            }

            @Override
            protected void onSuspended(Server server) {
                status.add(suspended);
            }

            @Override
            protected void onNeedsReload(Server server) {
                status.add(warning);
            }

            @Override
            protected void onNeedsRestart(Server server) {
                status.add(warning);
            }

            @Override
            protected void onRunning(Server server) {
                status.add(ok);
            }

            @Override
            protected void onStopped(Server server) {
                status.add(inactive);
            }

            @Override
            protected void onUnknown(Server server) {
            }
        };
        sss.accept(server);
        if (serverActions.isPending(server) || server.isStandalone()) {
            status.add(withProgress);
        }
        return status.toArray(new String[0]);
    }

    // ------------------------------------------------------ helpers

    private void clearSelected() {
        NodeList<Element> selectedNodes = document.querySelectorAll("." + topology + " ." + selected);
        Elements.elements(selectedNodes).forEach(element -> element.classList.remove(selected));
    }

    private HTMLElement actionLink(EventCallbackFn<MouseEvent> listener, String text) {
        return li().attr(UIConstants.ROLE, UIConstants.PRESENTATION)
                .add(a().css(clickable).on(click, listener).textContent(text)).element();
    }

    private void startProgress(String selector) {
        Elements.stream(document.querySelectorAll(selector))
                .forEach(element -> element.classList.add(withProgress));
    }

    private void stopProgress(String selector) {
        Elements.stream(document.querySelectorAll(selector))
                .forEach(element -> element.classList.remove(withProgress));
    }

    private void replace(Element existingElement, Supplier<Element> updateElement, Consumer<Element> select) {
        if (existingElement != null) {
            boolean hasSelection = existingElement.classList.contains(selected);
            Element parent = (Element) existingElement.parentNode;
            if (parent != null) {
                Element updatedElement = updateElement.get();
                parent.replaceChild(updatedElement, existingElement);
                if (hasSelection && select != null) {
                    select.accept(updatedElement);
                }
            }
        }
    }
}
