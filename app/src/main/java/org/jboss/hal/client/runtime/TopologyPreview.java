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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.inject.Provider;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableColElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableSectionElement;
import elemental2.dom.MouseEvent;
import elemental2.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventCallbackFn;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.client.runtime.server.ServerStatusSwitch;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.group.ServerGroupActionEvent;
import org.jboss.hal.core.runtime.group.ServerGroupActionEvent.ServerGroupActionHandler;
import org.jboss.hal.core.runtime.group.ServerGroupActions;
import org.jboss.hal.core.runtime.group.ServerGroupResultEvent;
import org.jboss.hal.core.runtime.group.ServerGroupResultEvent.ServerGroupResultHandler;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActionEvent;
import org.jboss.hal.core.runtime.host.HostActionEvent.HostActionHandler;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.host.HostResultEvent;
import org.jboss.hal.core.runtime.host.HostResultEvent.HostResultHandler;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActionEvent;
import org.jboss.hal.core.runtime.server.ServerActionEvent.ServerActionHandler;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerResultEvent;
import org.jboss.hal.core.runtime.server.ServerResultEvent.ServerResultHandler;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static com.google.common.collect.Lists.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.Elements.table;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class TopologyPreview extends PreviewContent<StaticItem> implements HostActionHandler, HostResultHandler,
        ServerGroupActionHandler, ServerGroupResultHandler, ServerActionHandler, ServerResultHandler {

    private static final String CONTAINER = "container";

    private final SecurityContextRegistry securityContextRegistry;
    private final Environment environment;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final EventBus eventBus;
    private final Places places;
    private final HostActions hostActions;
    private final ServerGroupActions serverGroupActions;
    private final ServerActions serverActions;
    private final Resources resources;
    private final HTMLElement loadingSection;
    private final HTMLElement topologySection;
    private final HTMLElement hostAttributesSection;
    private final HTMLElement serverGroupAttributesSection;
    private final HTMLElement serverAttributesSection;
    private final PreviewAttributes<ServerGroup> serverGroupAttributes;
    private final PreviewAttributes<Host> hostAttributes;
    private final PreviewAttributes<Server> serverAttributes;

    TopologyPreview(final SecurityContextRegistry securityContextRegistry,
            final Environment environment,
            final Dispatcher dispatcher,
            final Provider<Progress> progress,
            final EventBus eventBus,
            final Places places,
            final FinderPathFactory finderPathFactory,
            final HostActions hostActions,
            final ServerGroupActions serverGroupActions,
            final ServerActions serverActions,
            final Resources resources) {
        super(Names.TOPOLOGY, resources.previews().runtimeTopology());
        this.securityContextRegistry = securityContextRegistry;
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.eventBus = eventBus;
        this.places = places;
        this.hostActions = hostActions;
        this.serverGroupActions = serverGroupActions;
        this.serverActions = serverActions;
        this.resources = resources;

        eventBus.addHandler(HostActionEvent.getType(), this);
        eventBus.addHandler(HostResultEvent.getType(), this);
        eventBus.addHandler(ServerGroupActionEvent.getType(), this);
        eventBus.addHandler(ServerGroupResultEvent.getType(), this);
        eventBus.addHandler(ServerActionEvent.getType(), this);
        eventBus.addHandler(ServerResultEvent.getType(), this);

        previewBuilder()
                .add(p()
                        .add(a().css(clickable, pullRight).on(click, event -> update(null))
                                .add(span().css(fontAwesome("refresh"), marginRight5))
                                .add(span().textContent(resources.constants().refresh()))))
                .add(loadingSection = section().css(centerBlock)
                        .add(p().textContent(resources.constants().loading()))
                        .add(div().css(spinner, spinnerLg))
                        .asElement())
                .add(topologySection = section().asElement());

        hostAttributes = new PreviewAttributes<>(new Host(new ModelNode()), Names.HOST)
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, ModelNode::isDefined,
                            m -> finderPathFactory.runtimeHostPath(m.getAddressName()));
                    return new PreviewAttribute(Names.NAME, model.getName(), token);
                })
                .append(RELEASE_CODENAME)
                .append(RELEASE_VERSION)
                .append(PRODUCT_NAME)
                .append(PRODUCT_VERSION)
                .append(HOST_STATE)
                .append(RUNNING_MODE)
                .append(model -> new PreviewAttribute(
                        "Management Version", //NON-NLS
                        String.join(".",
                                model.get(MANAGEMENT_MAJOR_VERSION).asString(),
                                model.get(MANAGEMENT_MINOR_VERSION).asString(),
                                model.get(MANAGEMENT_MICRO_VERSION).asString())
                ));

        serverGroupAttributes = new PreviewAttributes<>(new ServerGroup("", new ModelNode()), Names.SERVER_GROUP)
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, ModelNode::isDefined,
                            m -> finderPathFactory.runtimeServerGroupPath(m.getName()));
                    return new PreviewAttribute(Names.NAME, model.getName(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.CONFIGURATION, model, ModelNode::isDefined,
                            m -> new FinderPath()
                                    .append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES))
                                    .append(Ids.PROFILE, m.getProfile()));
                    return new PreviewAttribute(Names.PROFILE, model.getProfile(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.CONFIGURATION, model, ModelNode::isDefined,
                            m -> new FinderPath()
                                    .append(Ids.CONFIGURATION, Ids.asId(Names.SOCKET_BINDINGS))
                                    .append(Ids.SOCKET_BINDING_GROUP, model.get(SOCKET_BINDING_GROUP).asString()));
                    return new PreviewAttribute(Names.SOCKET_BINDING_GROUP, model.get(SOCKET_BINDING_GROUP).asString(),
                            token);
                })
                .append(SOCKET_BINDING_PORT_OFFSET)
                .append(SOCKET_BINDING_DEFAULT_INTERFACE);

        serverAttributes = new PreviewAttributes<>(new Server("", new ModelNode()), Names.SERVER)
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, m -> !Strings.isNullOrEmpty(m.getHost()),
                            m -> finderPathFactory.runtimeServerPath(model.getHost(), model.getName()));
                    return new PreviewAttribute(Names.NAME, model.getName(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, m -> !Strings.isNullOrEmpty(m.getHost()),
                            m -> finderPathFactory.runtimeHostPath(model.getHost()));
                    return new PreviewAttribute(Names.HOST, model.getHost(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, m -> !Strings.isNullOrEmpty(m.getHost()),
                            m -> finderPathFactory.runtimeServerGroupPath(model.getServerGroup()));
                    return new PreviewAttribute(Names.SERVER_GROUP, model.getServerGroup(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.CONFIGURATION, model, m -> !Strings.isNullOrEmpty(m.getHost()),
                            m -> new FinderPath()
                                    .append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES))
                                    .append(Ids.PROFILE, model.get(PROFILE_NAME).asString()));
                    return new PreviewAttribute(Names.PROFILE, model.get(PROFILE_NAME).asString(), token);
                })
                .append(AUTO_START)
                .append(SOCKET_BINDING_PORT_OFFSET)
                .append(STATUS)
                .append(RUNNING_MODE)
                .append(SERVER_STATE)
                .append(SUSPEND_STATE);

        previewBuilder()
                .add(hostAttributesSection = section()
                        .addAll(hostAttributes)
                        .asElement())
                .add(serverGroupAttributesSection = section()
                        .addAll(serverGroupAttributes)
                        .asElement())
                .add(serverAttributesSection = section()
                        .addAll(serverAttributes)
                        .asElement());
    }

    private <T extends NamedNode> String lazyToken(String tlc, T model,
            Predicate<T> defined, Function<T, FinderPath> path) {
        String token = "";
        if (defined.test(model)) {
            PlaceRequest placeRequest = places.finderPlace(tlc, path.apply(model)).build();
            token = places.historyToken(placeRequest);
        }
        return token;
    }


    // ------------------------------------------------------ dmr functions

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(final StaticItem item) {
        // remember selection
        HTMLElement element = (HTMLElement) DomGlobal.document.querySelector("." + topology + " ." + selected);
        String hostName = element != null ? String.valueOf(element.dataset.get("host")) : null;
        String serverGroupName = element != null ? String.valueOf(element.dataset.get("serverGroup")) : null;
        String serverName = element != null ? String.valueOf(element.dataset.get("server")) : null;

        clearSelected();
        Elements.setVisible(loadingSection, false);
        Elements.setVisible(topologySection, false);
        hideDetails();

        // show the loading indicator if the dmr operation takes too long
        double timeoutHandle = DomGlobal.setTimeout((o) -> Elements.setVisible(loadingSection, true),
                UIConstants.MEDIUM_TIMEOUT);
        new Async<FunctionContext>(progress.get()).waterfall(
                new FunctionContext(),
                new Outcome<FunctionContext>() {
                    @Override
                    public void onFailure(final FunctionContext context) {
                        DomGlobal.clearTimeout(timeoutHandle);
                        Elements.setVisible(loadingSection, false);
                        MessageEvent.fire(eventBus,
                                Message.error(resources.messages().topologyError(), context.getError()));
                    }

                    @Override
                    public void onSuccess(final FunctionContext context) {
                        DomGlobal.clearTimeout(timeoutHandle);
                        Elements.setVisible(loadingSection, false);
                        Elements.removeChildrenFrom(topologySection);

                        List<Host> hosts = context.get(TopologyFunctions.HOSTS);
                        List<ServerGroup> serverGroups = context.get(TopologyFunctions.SERVER_GROUPS);
                        List<Server> servers = context.get(TopologyFunctions.SERVERS);

                        topologySection.appendChild(buildTable(hosts, serverGroups, servers));
                        Elements.setVisible(topologySection, true);
                        adjustTdHeight();

                        // restore selection
                        if (hostName != null) {
                            hosts.stream()
                                    .filter(host -> hostName.equals(host.getName()))
                                    .findAny()
                                    .ifPresent(host -> hostDetails(host));
                        }
                        if (serverGroupName != null) {
                            serverGroups.stream()
                                    .filter(serverGroup -> serverGroupName.equals(serverGroup.getName()))
                                    .findAny()
                                    .ifPresent(serverGroup -> serverGroupDetails(serverGroup));
                        }
                        if (serverName != null) {
                            servers.stream()
                                    .filter(server -> serverName.equals(server.getName()))
                                    .findAny()
                                    .ifPresent(server -> serverDetails(server));
                        }
                    }
                },
                new TopologyFunctions.Topology(environment, dispatcher),
                new TopologyFunctions.TopologyStartedServers(environment, dispatcher));
    }

    private void updateServer(Server server) {
        new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), new Outcome<FunctionContext>() {
                    @Override
                    public void onFailure(final FunctionContext context) {
                        MessageEvent.fire(eventBus,
                                Message.error(resources.messages().updateServerError(server.getName()), context.getError()));
                    }

                    @Override
                    public void onSuccess(final FunctionContext context) {
                        elemental2.dom.Document document = DomGlobal.document;

                        Host host = context.get(TopologyFunctions.HOST);
                        ServerGroup serverGroup = context.get(TopologyFunctions.SERVER_GROUP);
                        if (host != null && serverGroup != null) {
                            // Does not matter where we take the updated server from (must be included in both
                            // host and server group)
                            host.getServers().stream()
                                    .filter(srv -> srv.getId().equals(server.getId()))
                                    .findAny()
                                    .ifPresent(updatedServer -> {
                                        String updatedContainerId = Ids.build(updatedServer.getId(), CONTAINER);
                                        replaceElement(document.getElementById(updatedContainerId),
                                                () -> serverElement(updatedServer),
                                                whatever -> serverDetails(updatedServer));
                                    });

                            // Update not only the server, but also the host and server group elements. Since the
                            // server's state has changed the host and server group dropdown links need to be updated
                            // as well.
                            replaceElement(document.querySelector(hostSelector(host)),
                                    () -> hostElement(host),
                                    whatever -> hostDetails(host));
                            replaceElement(document.querySelector(serverGroupSelector(serverGroup)),
                                    () -> serverGroupElement(serverGroup),
                                    updatedElement -> serverGroupDetails(serverGroup));
                        }
                    }
                },
                // TODO Include function to read server boot errors
                new TopologyFunctions.HostWithServerConfigs(server.getHost(), dispatcher),
                new TopologyFunctions.HostStartedServers(dispatcher),
                new TopologyFunctions.ServerGroupWithServerConfigs(server.getServerGroup(), dispatcher),
                new TopologyFunctions.ServerGroupStartedServers(dispatcher)
        );
    }


    // ------------------------------------------------------ UI methods

    @SuppressWarnings("HardCodedStringLiteral")
    private HTMLElement buildTable(List<Host> hosts, List<ServerGroup> serverGroups, List<Server> servers) {
        HTMLTableElement table = table().css(topology).asElement();

        // <colgroup>
        double width = 100.0 / (serverGroups.size() + 1);
        HtmlContentBuilder<HTMLTableColElement> colgroup = colgroup()
                .add(col().attr("width", String.valueOf(width) + "%"));
        for (int i = 0; i < serverGroups.size(); i++) {
            colgroup.add(col().attr("width", String.valueOf(width) + "%"));
        }
        table.appendChild(colgroup.asElement());
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
        table.appendChild(thead.asElement());
        // </thead>

        HTMLElement tbody = tbody().asElement();
        for (Host host : hosts) {
            HTMLElement tr;
            tbody.appendChild(tr = tr().asElement());
            tr.appendChild(hostElement(host));
            for (ServerGroup serverGroup : serverGroups) {
                List<HTMLElement> matchingServers = servers.stream()
                        .filter(sc -> host.getName().equals(sc.getHost()) &&
                                serverGroup.getName().equals(sc.getServerGroup()))
                        .sorted(comparing(Server::getName))
                        .map(this::serverElement)
                        .collect(toList());
                if (matchingServers.isEmpty()) {
                    tr.appendChild(td().css(empty).asElement());
                } else {
                    tr.appendChild(td()
                            .add(div().css(CSS.servers)
                                    .addAll(matchingServers))
                            .asElement());
                }
            }
        }
        table.appendChild(tbody);
        // </tbody> @formatter:on

        return table;
    }

    private HTMLElement hostElement(final Host host) {
        HTMLElement dropdown;
        HTMLTableCellElement th = th()
                .css(asList(rowHeader, statusCss(host)).toArray(new String[]{}))
                .on(click, event -> hostDetails(host))
                .data("host", host.getName())
                .add(div().css(hostContainer)
                        .add(dropdown = div().css(CSS.dropdown).asElement()))
                .asElement();

        if (host.hasServers() && !hostActions.isPending(host) && isAllowed(host)) {
            HTMLElement a;
            String hostDropDownId = Ids.host(host.getAddressName());
            dropdown.appendChild(a = a()
                    .id(hostDropDownId)
                    .css(clickable, dropdownToggle, name)
                    .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                    .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                    .title(host.getName())
                    .asElement());
            if (host.isDomainController()) {
                a.appendChild(span().css(fontAwesome("star"), marginRight5).title(Names.DOMAIN_CONTROLLER).asElement());
            }
            a.appendChild(span().textContent(host.getName()).asElement());
            dropdown.appendChild(ul()
                    .css(dropdownMenu)
                    .attr(UIConstants.ROLE, UIConstants.MENU)
                    .aria(UIConstants.LABELLED_BY, hostDropDownId)
                    .addAll(hostActions(host))
                    .asElement());
        } else {
            dropdown.appendChild(span().css(name).title(host.getName())
                    .textContent(host.getName())
                    .asElement());
        }
        return th;
    }

    private HTMLElement serverGroupElement(final ServerGroup serverGroup) {
        HTMLElement dropdown;
        HTMLTableCellElement element = th()
                .on(click, event -> serverGroupDetails(serverGroup))
                .data("serverGroup", serverGroup.getName())
                .add(div().css(serverGroupContainer)
                        .add(dropdown = div().css(CSS.dropdown).asElement()))
                .asElement();

        if (serverGroup.hasServers() && !serverGroupActions.isPending(serverGroup) && isAllowed(serverGroup)) {
            String serverGroupDropDownId = Ids.serverGroup(serverGroup.getName());
            dropdown.appendChild(a()
                    .id(serverGroupDropDownId)
                    .css(clickable, dropdownToggle, name)
                    .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                    .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                    .title(serverGroup.getName())
                    .textContent(serverGroup.getName())
                    .asElement());
            dropdown.appendChild(ul()
                    .css(dropdownMenu)
                    .attr(UIConstants.ROLE, UIConstants.MENU)
                    .aria(UIConstants.LABELLED_BY, serverGroupDropDownId)
                    .addAll(serverGroupActions(serverGroup))
                    .asElement());
        } else {
            dropdown.appendChild(span()
                    .css(name)
                    .title(serverGroup.getName())
                    .textContent(serverGroup.getName())
                    .asElement());
        }
        return element;
    }

    private HTMLElement serverElement(Server srv) {
        HTMLElement dropdown;
        HTMLDivElement element = div()
                .id(Ids.build(srv.getId(), CONTAINER))
                .css(asList(server, statusCss(srv)).toArray(new String[]{}))
                .data(SERVER, srv.getId())
                .on(click, event -> serverDetails(srv))
                .add(dropdown = div().css(CSS.dropdown).asElement())
                .asElement();

        if (!serverActions.isPending(srv) && isAllowed(srv)) {
            dropdown.appendChild(a()
                    .id(srv.getId())
                    .css(clickable, dropdownToggle, name)
                    .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                    .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                    .title(srv.getName())
                    .textContent(srv.getName())
                    .asElement());
            dropdown.appendChild(ul()
                    .css(dropdownMenu)
                    .attr(UIConstants.ROLE, UIConstants.MENU)
                    .aria(UIConstants.LABELLED_BY, srv.getId())
                    .addAll(serverActions(srv))
                    .asElement());
        } else {
            dropdown.appendChild(span()
                    .css(name)
                    .title(srv.getName())
                    .textContent(srv.getName())
                    .asElement());
        }
        return element;
    }

    private void replaceElement(Element existingElement, Supplier<Element> updateElement, Consumer<Element> select) {
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

    private void adjustTdHeight() {
        NodeList<Element> servers = DomGlobal.document.querySelectorAll("." + topology + " ." + CSS.servers);
        Elements.stream(servers)
                .filter(htmlElements())
                .map(asHtmlElement())
                .forEach(element -> {
                    HTMLElement parent = (HTMLElement) element.parentNode;
                    element.style.height = height(px(parent.offsetHeight - 1));
                });
    }

    private void hideDetails() {
        Elements.setVisible(serverGroupAttributesSection, false);
        Elements.setVisible(hostAttributesSection, false);
        Elements.setVisible(serverAttributesSection, false);
    }

    private void clearSelected() {
        NodeList<Element> selectedNodes = DomGlobal.document.querySelectorAll("." + topology + " ." + selected);
        Elements.elements(selectedNodes).forEach(element -> element.classList.remove(selected));
    }

    private HTMLElement actionLink(EventCallbackFn<MouseEvent> listener, String text) {
        return li().attr(UIConstants.ROLE, UIConstants.PRESENTATION)
                .add(a().css(clickable).on(click, listener).textContent(text))
                .asElement();
    }

    private void startProgress(String selector) {
        Elements.stream(DomGlobal.document.querySelectorAll(selector))
                .forEach(element -> element.classList.add(withProgress));
    }

    private void stopProgress(String selector) {
        Elements.stream(DomGlobal.document.querySelectorAll(selector))
                .forEach(element -> element.classList.remove(withProgress));
    }

    private void disableDropdown(String id, String name) {
        Element link = DomGlobal.document.getElementById(id);
        if (link != null) {
            Element parent = (Element) link.parentNode;
            Element ul = link.nextElementSibling;
            if (parent != null && ul != null) {
                HTMLElement noLink = span().css(CSS.name).title(name).textContent(name).asElement();
                parent.classList.remove("opened"); //NON-NLS
                parent.replaceChild(noLink, link);
                parent.removeChild(ul);
            }
        }
    }

    private boolean isVisible() {
        return Elements.isVisible(topologySection) && topologySection.parentNode != null;
    }


    // ------------------------------------------------------ host

    private void hostDetails(final Host host) {
        clearSelected();
        HTMLElement element = (HTMLElement) DomGlobal.document.querySelector(hostSelector(host));
        if (element != null) {
            element.classList.add(selected);
        }

        hostAttributes.refresh(host);
        hostAttributes.setVisible(HOST_STATE, hostActions.isPending(host));
        hostAttributes.setVisible(RUNNING_MODE, hostActions.isPending(host));
        Elements.setVisible(serverGroupAttributesSection, false);
        Elements.setVisible(hostAttributesSection, true);
        Elements.setVisible(serverAttributesSection, false);
    }

    private boolean isAllowed(Host host) {
        // To keep it simple, we take a all or nothing approach:
        // We check *one* action and assume that the other actions have the same constraints
        return AuthorisationDecision.from(environment, securityContextRegistry)
                .isAllowed(Constraint.executable(AddressTemplate.of("/host=" + host.getAddressName()), RELOAD));
    }

    private List<HTMLElement> hostActions(final Host host) {
        List<HTMLElement> actions = new ArrayList<>();
        actions.add(actionLink(event -> hostActions.reload(host), resources.constants().reload()));
        actions.add(actionLink(event -> hostActions.restart(host), resources.constants().restart()));
        return actions;
    }

    private String[] statusCss(final Host host) {
        return hostActions.isPending(host) ? new String[]{withProgress} : new String[]{};
    }

    @Override
    public void onHostAction(final HostActionEvent event) {
        if (isVisible()) {
            Host host = event.getHost();

            disableDropdown(Ids.host(host.getAddressName()), host.getName());
            startProgress(hostSelector(host));

            event.getServers().forEach(server -> {
                disableDropdown(server.getId(), server.getName());
                startProgress(serverSelector(server));
            });
        }
    }

    @Override
    public void onHostResult(final HostResultEvent event) {
        if (isVisible()) {
            Host host = event.getHost();

            stopProgress(hostSelector(host));
            event.getServers().forEach(server -> stopProgress(serverSelector(server)));
            update(null);
        }
    }

    private String hostSelector(final Host host) {
        return "[data-host='" + host.getName() + "']"; //NON-NLS
    }


    // ------------------------------------------------------ server group

    private void serverGroupDetails(final ServerGroup serverGroup) {
        clearSelected();
        HTMLElement element = (HTMLElement) DomGlobal.document.querySelector(serverGroupSelector(serverGroup));
        if (element != null) {
            element.classList.add(selected);
        }

        serverGroupAttributes.refresh(serverGroup);
        Elements.setVisible(serverGroupAttributesSection, true);
        Elements.setVisible(hostAttributesSection, false);
        Elements.setVisible(serverAttributesSection, false);
    }

    @SuppressWarnings("unused")
    private boolean isAllowed(ServerGroup serverGroup) {
        // To keep it simple, we take a all or nothing approach:
        // We check *one* action and assume that the other actions have the same constraints
        Constraints constraints = Constraints.or(
                Constraint.executable(AddressTemplate.of("/server-group=*"), RELOAD_SERVERS),
                Constraint.executable(AddressTemplate.of("/server-group=" + serverGroup.getName()), RELOAD_SERVERS));
        return AuthorisationDecision.from(environment, securityContextRegistry)
                .isAllowed(constraints);
    }

    private List<HTMLElement> serverGroupActions(final ServerGroup serverGroup) {
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
        }

        return actions;
    }

    @Override
    public void onServerGroupAction(final ServerGroupActionEvent event) {
        if (isVisible()) {
            ServerGroup serverGroup = event.getServerGroup();
            disableDropdown(Ids.serverGroup(serverGroup.getName()), serverGroup.getName());
            event.getServers().forEach(server -> {
                disableDropdown(server.getId(), server.getName());
                startProgress(serverSelector(server));
            });
        }
    }

    @Override
    public void onServerGroupResult(final ServerGroupResultEvent event) {
        if (isVisible()) {
            event.getServers().forEach(server -> stopProgress(serverSelector(server)));
            update(null);
        }
    }

    private String serverGroupSelector(final ServerGroup serverGroup) {
        return "[data-server-group='" + serverGroup.getName() + "']"; //NON-NLS
    }


    // ------------------------------------------------------ server

    private void serverDetails(final Server server) {
        clearSelected();
        HTMLElement element = (HTMLElement) DomGlobal.document.querySelector(serverSelector(server));
        if (element != null) {
            element.classList.add(selected);
        }

        if (server.hasBootErrors()) {
            PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.SERVER_BOOT_ERRORS)
                    .with(HOST, server.getHost())
                    .with(SERVER, server.getName())
                    .build();
            String token = places.historyToken(placeRequest);
            serverAttributes.setDescription(resources.messages().serverBootErrorsAndLink(server.getName(), token));
        } else {
            serverAttributes.hideDescription();
        }
        serverAttributes.refresh(server);
        serverAttributes.setVisible(PROFILE, server.isStarted());
        serverAttributes.setVisible(RUNNING_MODE, server.isStarted());
        serverAttributes.setVisible(SERVER_STATE, server.isStarted());
        serverAttributes.setVisible(SUSPEND_STATE, server.isStarted());
        Elements.setVisible(serverGroupAttributesSection, false);
        Elements.setVisible(hostAttributesSection, false);
        Elements.setVisible(serverAttributesSection, true);
    }

    private boolean isAllowed(Server server) {
        // To keep it simple, we take a all or nothing approach:
        // We check *one* action and assume that the other actions have the same constraints
        return AuthorisationDecision.from(environment, securityContextRegistry)
                .isAllowed(Constraint.executable(AddressTemplate.of("/host=" + server.getHost() + "/server-config=*"),
                        RELOAD));
    }

    private List<HTMLElement> serverActions(final Server server) {
        List<HTMLElement> actions = new ArrayList<>();

        if (!server.isStarted()) {
            actions.add(actionLink(event -> serverActions.start(server), resources.constants().start()));
        } else {
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
        // add kill link regardless of server state to kill servers which might show a wrong state
        actions.add(actionLink(event -> serverActions.kill(server), resources.constants().kill()));

        return actions;
    }

    private String[] statusCss(final Server server) {
        Set<String> status = new HashSet<>();
        ServerStatusSwitch sss = new ServerStatusSwitch(serverActions) {
            @Override
            protected void onPending(final Server server) {}

            @Override
            protected void onBootErrors(final Server server) {
                status.add(error);
            }

            @Override
            protected void onFailed(final Server server) {
                status.add(error);
            }

            @Override
            protected void onAdminMode(final Server server) {
                status.add(inactive);
            }

            @Override
            protected void onStarting(final Server server) {}

            @Override
            protected void onSuspended(final Server server) {
                status.add(suspended);
            }

            @Override
            protected void onNeedsReload(final Server server) {
                status.add(warning);
            }

            @Override
            protected void onNeedsRestart(final Server server) {
                status.add(warning);
            }

            @Override
            protected void onRunning(final Server server) {
                status.add(ok);
            }

            @Override
            protected void onStopped(final Server server) {
                status.add(inactive);
            }

            @Override
            protected void onUnknown(final Server server) {
            }
        };
        sss.accept(server);
        if (serverActions.isPending(server) || server.isStandalone()) {
            status.add(withProgress);
        }
        return status.toArray(new String[status.size()]);
    }

    @Override
    public void onServerAction(final ServerActionEvent event) {
        if (isVisible()) {
            Server server = event.getServer();
            disableDropdown(server.getId(), server.getName());
            startProgress(serverSelector(server));
        }
    }

    @Override
    public void onServerResult(final ServerResultEvent event) {
        if (isVisible()) {
            stopProgress(serverSelector(event.getServer()));
            updateServer(event.getServer());
        }
    }

    private String serverSelector(final Server server) {
        return "[data-server='" + server.getId() + "']"; //NON-NLS
    }
}
