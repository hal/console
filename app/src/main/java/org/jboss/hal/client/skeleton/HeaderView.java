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
package org.jboss.hal.client.skeleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.gwt.elemento.template.DataElement;
import org.jboss.gwt.elemento.template.Templated;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.Roles;
import org.jboss.hal.config.Settings;
import org.jboss.hal.config.User;
import org.jboss.hal.core.accesscontrol.AccessControl;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.FinderSegment.DropdownItem;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath.Segment;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.Elements.ul;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.skeleton.HeaderPresenter.MAX_BREADCRUMB_VALUE_LENGTH;
import static org.jboss.hal.config.AccessControlProvider.RBAC;
import static org.jboss.hal.config.Settings.Key.RUN_AS;
import static org.jboss.hal.core.Strings.abbreviateMiddle;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.FontAwesomeSize.large;

@SuppressWarnings("WeakerAccess")
@Templated("MainLayout.html#header")
public abstract class HeaderView extends HalViewImpl implements HeaderPresenter.MyView {

    // @formatter:off
    public static HeaderView create(final Places places, final AccessControl ac, final User user,
            final Resources resources) {
        return new Templated_HeaderView(places, ac, user, resources);
    }

    public abstract Places places();
    public abstract AccessControl ac();
    public abstract User user();
    public abstract Resources resources();
    // @formatter:on


    @NonNls private static final Logger logger = LoggerFactory.getLogger(HeaderView.class);
    private static final PlaceRequest HOMEPAGE = new PlaceRequest.Builder().nameToken(NameTokens.HOMEPAGE).build();

    private PlaceRequest backPlaceRequest;
    private Map<String, PlaceRequest> tlcPlaceRequests;
    private Map<String, HTMLElement> tlc;
    private HeaderPresenter presenter;
    private MessagePanel messagePanel;
    private MessageSink messageSink;
    private HandlerRegistration switchModeHandler;
    private List<HandlerRegistration> handlers;
    private List<HandlerRegistration> breadcrumbHandlers;

    @DataElement HTMLElement logoFirst;
    @DataElement HTMLElement logoLast;
    @DataElement HTMLElement logoLink;
    @DataElement HTMLElement reloadContainer;
    @DataElement HTMLElement reloadLink;
    @DataElement HTMLElement reloadLabel;
    @DataElement HTMLElement messages;
    @DataElement HTMLElement messagesIcon;
    @DataElement HTMLElement userName;
    @DataElement HTMLElement userDropdown;
    @DataElement HTMLElement logout;
    @DataElement HTMLElement logoutItem;
    @DataElement HTMLElement reconnect;
    @DataElement HTMLElement connectedToContainer;
    @DataElement HTMLElement connectedTo;
    @DataElement HTMLElement patching;
    @DataElement HTMLElement accessControl;
    @DataElement HTMLElement management;
    @DataElement HTMLElement topLevelCategories;
    @DataElement HTMLElement breadcrumb;
    @DataElement HTMLElement backItem;
    @DataElement HTMLElement backLink;
    @DataElement HTMLElement breadcrumbToolsItem;
    @DataElement HTMLElement switchModeLink;
    @DataElement HTMLElement switchModeIcon;
    @DataElement HTMLElement externalLink;


    // ------------------------------------------------------ initialization

    @PostConstruct
    void init() {
        backPlaceRequest = HOMEPAGE;
        HTMLElement root = asElement();
        Elements.setVisible(reloadContainer, false);
        Elements.setVisible(breadcrumb, false);

        messagePanel = new MessagePanel(resources()); // message panel adds itself to the body
        messageSink = new MessageSink(resources());
        topLevelCategories.parentNode.insertBefore(messageSink.asElement(), topLevelCategories);

        boolean su = ac().isSuperUserOrAdministrator();
        if (!su) {
            topLevelCategories.removeChild(patching);
            topLevelCategories.removeChild(accessControl);
            topLevelCategories.removeChild(management);
        }

        // @formatter:off
        tlcPlaceRequests = new HashMap<>();
        tlcPlaceRequests.put(NameTokens.HOMEPAGE,       new PlaceRequest.Builder().nameToken(NameTokens.HOMEPAGE).build());
        tlcPlaceRequests.put(NameTokens.DEPLOYMENTS,    new PlaceRequest.Builder().nameToken(NameTokens.DEPLOYMENTS).build());
        tlcPlaceRequests.put(NameTokens.CONFIGURATION,  new PlaceRequest.Builder().nameToken(NameTokens.CONFIGURATION).build());
        tlcPlaceRequests.put(NameTokens.RUNTIME,        new PlaceRequest.Builder().nameToken(NameTokens.RUNTIME).build());
        tlcPlaceRequests.put(NameTokens.PATCHING,       new PlaceRequest.Builder().nameToken(NameTokens.PATCHING).build());
        tlcPlaceRequests.put(NameTokens.ACCESS_CONTROL, new PlaceRequest.Builder().nameToken(NameTokens.ACCESS_CONTROL).build());
        tlcPlaceRequests.put(NameTokens.MANAGEMENT,     new PlaceRequest.Builder().nameToken(NameTokens.MANAGEMENT).build());

        tlc = new HashMap<>();
        tlc.put(NameTokens.HOMEPAGE,        (HTMLElement) root.querySelector("#" + Ids.TLC_HOMEPAGE));
        tlc.put(NameTokens.DEPLOYMENTS,     (HTMLElement) root.querySelector("#" + Ids.TLC_DEPLOYMENTS));
        tlc.put(NameTokens.CONFIGURATION,   (HTMLElement) root.querySelector("#" + Ids.TLC_CONFIGURATION));
        tlc.put(NameTokens.RUNTIME,         (HTMLElement) root.querySelector("#" + Ids.TLC_RUNTIME));
        tlc.put(NameTokens.PATCHING,        (HTMLElement) root.querySelector("#" + Ids.TLC_PATCHING));
        tlc.put(NameTokens.ACCESS_CONTROL,  (HTMLElement) root.querySelector("#" + Ids.TLC_ACCESS_CONTROL));
        tlc.put(NameTokens.MANAGEMENT,      (HTMLElement) root.querySelector("#" + Ids.TLC_MANAGEMENT));
        // @formatter:on

        handlers = new ArrayList<>();
        breadcrumbHandlers = new ArrayList<>();
        for (Map.Entry<String, HTMLElement> entry : tlc.entrySet()) {
            handlers.add(bind(entry.getValue(), click, event -> {
                if (tlcPlaceRequests.containsKey(entry.getKey())) {
                    presenter.goTo(tlcPlaceRequests.get(entry.getKey()));
                }
            }));
        }
        handlers.addAll(asList(
                bind(logoLink, click, event -> presenter.goTo(NameTokens.HOMEPAGE)),
                bind(backLink, click, event -> presenter.goTo(backPlaceRequest)),
                bind(reloadLink, click, event -> presenter.reload()),
                bind(messages, click, event -> messageSink.asElement().classList.toggle(hide)),
                bind(logout, click, event -> presenter.logout()),
                bind(reconnect, click, event -> presenter.reconnect())));
    }

    @Override
    public void detach() {
        super.detach();
        for (HandlerRegistration handler : breadcrumbHandlers) {
            handler.removeHandler();
        }
        breadcrumbHandlers.clear();
        for (HandlerRegistration handler : handlers) {
            handler.removeHandler();
        }
        handlers.clear();
    }

    @Override
    public void setPresenter(final HeaderPresenter presenter) {
        this.presenter = presenter;
        messageSink.setPresenter(presenter);
    }

    @Override
    public void init(Environment environment, Endpoints endpoints, Settings settings, User user) {
        setLogo(resources().theme().getFirstName(), resources().theme().getLastName());

        if (endpoints.isSameOrigin()) {
            connectedTo.textContent = resources().constants().sameOrigin();
        } else {
            connectedTo.textContent = resources().messages().connectedTo(endpoints.dmr());
        }

        userName.textContent = user.getName();
        updateRoles(environment, settings, user);
    }

    @Override
    public void updateRoles(Environment environment, Settings settings, User user) {
        for (Iterator<HTMLElement> iterator = Elements.iterator(userDropdown); iterator.hasNext(); ) {
            HTMLElement element = iterator.next();
            if (element == logoutItem) {
                continue;
            }
            iterator.remove();
        }

        if (!user.getRoles().isEmpty()) {
            String csr = user.getRoles().stream()
                    .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME))
                    .map(Role::getName)
                    .collect(joining(", "));
            HTMLElement activeRoles = li().css(static_, CSS.activeRoles)
                    .textContent(resources().messages().activeRoles(csr))
                    .title(resources().messages().activeRoles(csr))
                    .asElement();
            userDropdown.insertBefore(activeRoles, logoutItem);
            userDropdown.insertBefore(divider(), logoutItem);

            if (user.isSuperuser() && environment.getAccessControlProvider() == RBAC) {
                Set<String> runAsRoleSetting = settings.get(RUN_AS).asSet();
                HTMLElement runAs = li().css(static_)
                        .textContent(resources().constants().runAs())
                        .asElement();
                userDropdown.insertBefore(runAs, logoutItem);

                stream(environment.getRoles().spliterator(), false)
                        .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME))
                        .forEach(role -> {
                            HTMLElement check, name;
                            HTMLElement runAsRole = li()
                                    .add(a().css(clickable).on(click, event -> presenter.runAs(role.getName()))
                                            .add(check = span().css(fontAwesome("check"), marginRight5)
                                                    .asElement())
                                            .add(name = span().textContent(role.getName())
                                                    .asElement()))
                                    .asElement();
                            if (!runAsRoleSetting.contains(role.getName())) {
                                check.style.visibility = "hidden"; //NON-NLS
                            }
                            if (role.isScoped()) {
                                name.title = role.getBaseRole().getName() + " / " + String.join(", ",
                                        role.getScope());
                            }
                            userDropdown.insertBefore(runAsRole, logoutItem);
                        });

                if (runAsRoleSetting != null) {
                    HTMLElement clearRunAs = li()
                            .add(a().css(clickable)
                                    .on(click, event -> presenter.clearRunAs())
                                    .textContent(resources().constants().clearRunAs()))
                            .asElement();
                    userDropdown.insertBefore(clearRunAs, logoutItem);
                }
                userDropdown.insertBefore(divider(), logoutItem);
            }
        }
    }

    private HTMLElement divider() {
        return li().css(CSS.divider).asElement();
    }

    private void setLogo(String first, String last) {
        logoFirst.textContent = first;
        logoLast.textContent = Strings.nullToEmpty(last);
    }


    // ------------------------------------------------------ logo, reload, messages & global state

    @Override
    public void showReload(final String text, final String tooltip) {
        reloadLabel.textContent = text;
        Tooltip.element(reloadLink).setTitle(tooltip);
        Elements.setVisible(reloadContainer, true);
    }

    @Override
    public void hideReload() {
        Elements.setVisible(reloadContainer, false);
    }

    @Override
    public void showMessage(final Message message) {
        switch (message.getLevel()) {
            case ERROR:
                logger.error(message.getMessage().asString());
                break;
            case WARNING:
                logger.warn(message.getMessage().asString());
                break;
            case INFO:
                logger.info(message.getMessage().asString());
                break;
        }
        messagePanel.add(message);
        messageSink.add(message);
        messagesIcon.classList.remove("fa-bell-o"); //NON-NLS
        messagesIcon.classList.add("fa-bell"); //NON-NLS
    }

    @Override
    public void clearMessages() {
        messageSink.clear();
    }

    @Override
    public void onClearMessage() {
        if (messageSink.getMessageCount() == 0) {
            messagesIcon.classList.remove("fa-bell"); //NON-NLS
            messagesIcon.classList.add("fa-bell-o"); //NON-NLS
        }
    }

    @Override
    public void onClearAllMessages() {
        messagesIcon.classList.remove("fa-bell"); //NON-NLS
        messagesIcon.classList.add("fa-bell-o"); //NON-NLS
    }

    @Override
    public void hideReconnect() {
        Elements.setVisible(connectedToContainer, false);
    }


    // ------------------------------------------------------ modes

    @Override
    public void topLevelCategoryMode() {
        Elements.setVisible(topLevelCategories, true);
        Elements.setVisible(breadcrumb, false);
    }

    @Override
    public void applicationMode() {
        Elements.setVisible(topLevelCategories, false);
        Elements.setVisible(breadcrumb, true);
    }


    // ------------------------------------------------------ links & tlc

    @Override
    public void updateLinks(final FinderContext finderContext) {
        PlaceRequest placeRequest = finderContext.getToken() != null ? finderContext.toPlaceRequest() : HOMEPAGE;
        backPlaceRequest = placeRequest;
        if (tlcPlaceRequests.containsKey(finderContext.getToken())) {
            tlcPlaceRequests.put(finderContext.getToken(), placeRequest);
        }
    }

    @Override
    public void selectTopLevelCategory(final String nameToken) {
        for (String token : tlc.keySet()) {
            if (token.equals(nameToken)) {
                tlc.get(token).classList.add(active);
                ((HTMLElement) tlc.get(token).parentNode).classList.add(active);
            } else {
                tlc.get(token).classList.remove(active);
                ((HTMLElement) tlc.get(token).parentNode).classList.remove(active);
            }
        }
    }


    // ------------------------------------------------------ breadcrumb

    @Override
    public void updateBreadcrumb(final String title) {
        clearBreadcrumb();
        HTMLElement li = li().textContent(title).asElement();
        breadcrumb.insertBefore(li, breadcrumbToolsItem);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateBreadcrumb(final FinderContext finderContext) {
        clearBreadcrumb();
        FinderPath currentPath = new FinderPath();

        for (HandlerRegistration handler : breadcrumbHandlers) {
            handler.removeHandler();
        }
        breadcrumbHandlers.clear();

        for (Iterator<FinderSegment> iterator = finderContext.getPath().iterator(); iterator.hasNext(); ) {
            FinderSegment<Object> segment = iterator.next();
            if (segment.getColumnId() == null || segment.getItemId() == null) {
                // we need to ignore half filled segments which occur when removing items from a column
                break;
            }

            boolean last = !iterator.hasNext();
            currentPath.append(segment.getColumnId(), segment.getItemId());

            HtmlContentBuilder<HTMLLIElement> builder = li();
            if (last) {
                builder.css(active);
            }

            HTMLElement key = span().css(CSS.key).asElement();
            if (finderContext.getToken() != null) {
                PlaceRequest keyRequest = new PlaceRequest.Builder()
                        .nameToken(finderContext.getToken())
                        .with("path", currentPath.toString())
                        .build();
                key.appendChild(a().css(clickable).on(click, event -> presenter.goTo(keyRequest))
                        .textContent(segment.getColumnTitle())
                        .asElement());
            } else {
                key.textContent = segment.getColumnTitle();
            }
            builder.add(key)
                    .add(span().css(arrow).innerHtml(SafeHtmlUtils.fromSafeConstant("&#8658;")));

            HTMLElement value = span().css(CSS.value).asElement();
            if (segment.supportsDropdown()) {
                value.classList.add(dropdown);

                HTMLElement a;
                String id = Ids.build(segment.getColumnId(), VALUE);
                value.appendChild(a = a().css(clickable)
                        .id(id)
                        .data(UIConstants.TARGET, "#")
                        .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                        .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                        .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                        .attr(UIConstants.ROLE, UIConstants.BUTTON)
                        .asElement());
                breadcrumbHandlers.add(bind(a, click, event -> {
                    Element ul = a.nextElementSibling;
                    segment.dropdown(finderContext, items -> {
                        Elements.removeChildrenFrom(ul);
                        if (items.isEmpty()) {
                            HTMLElement empty = li().css(CSS.empty)
                                    .textContent(HeaderView.this.resources().constants().noItems())
                                    .asElement();
                            ul.appendChild(empty);
                        } else {
                            for (DropdownItem<Object> dropdownItem : items) {
                                HTMLElement element = li()
                                        .add(a().css(clickable)
                                                .on(click, e -> dropdownItem.onSelect(finderContext))
                                                .textContent(dropdownItem.getTitle()))
                                        .asElement();
                                ul.appendChild(element);
                            }
                        }
                    });
                }));

                String breadcrumbValue = segment.getItemTitle();
                if (breadcrumbValue.length() > MAX_BREADCRUMB_VALUE_LENGTH) {
                    a.appendChild(span()
                            .textContent(abbreviateMiddle(breadcrumbValue, MAX_BREADCRUMB_VALUE_LENGTH) + " ")
                            .title(breadcrumbValue)
                            .asElement());
                } else {
                    a.appendChild(span().textContent(breadcrumbValue + " ").asElement());
                }
                a.appendChild(span().css(caret).asElement());
                value.appendChild(ul()
                        .css(dropdownMenu, valueDropdown)
                        .aria(UIConstants.LABELLED_BY, id)
                        .asElement());
            } else {
                String breadcrumbValue = segment.getItemTitle();
                if (breadcrumbValue.length() > MAX_BREADCRUMB_VALUE_LENGTH) {
                    value.textContent = abbreviateMiddle(breadcrumbValue, MAX_BREADCRUMB_VALUE_LENGTH);
                    value.title = breadcrumbValue;
                } else {
                    value.textContent = segment.getItemTitle();
                }
            }
            builder.add(value);
            breadcrumb.insertBefore(builder.asElement(), breadcrumbToolsItem);
        }
    }

    @Override
    public void updateBreadcrumb(final ModelBrowserPath path) {
        clearBreadcrumb();
        if (path == null || path.isEmpty()) {
            // deselection
            breadcrumb.insertBefore(li().textContent(resources().constants().nothingSelected()).asElement(),
                    breadcrumbToolsItem);

        } else {
            ModelBrowser modelBrowser = path.getModelBrowser();
            for (Iterator<Segment[]> iterator = path.iterator(); iterator.hasNext(); ) {
                Segment[] segments = iterator.next();
                Segment key = segments[0];
                Segment value = segments[1];
                boolean link = value != ModelBrowserPath.WILDCARD && iterator.hasNext();

                HTMLElement valueContainer;
                HTMLElement li = li()
                        .add(span().css(CSS.key)
                                .add(a().css(clickable)
                                        .on(click, event -> modelBrowser.select(key.id, true))
                                        .textContent(key.text)))
                        .add(span().css(arrow).innerHtml(SafeHtmlUtils.fromSafeConstant("&#8658;")))
                        .add(valueContainer = span().css(CSS.value).asElement())
                        .asElement();
                if (link) {
                    valueContainer.appendChild(valueContainer = a().css(clickable)
                            .on(click, event -> modelBrowser.select(value.id, true))
                            .asElement());
                }
                valueContainer.textContent = value.text;
                breadcrumb.insertBefore(li, breadcrumbToolsItem);
            }
        }
    }

    private void clearBreadcrumb() {
        for (Iterator<HTMLElement> iterator = Elements.iterator(breadcrumb); iterator.hasNext(); ) {
            HTMLElement element = iterator.next();
            if (element == backItem || element == breadcrumbToolsItem) {
                continue;
            }
            iterator.remove();
        }
    }


    // ------------------------------------------------------ breadcrumb tools

    @Override
    public void showExpertMode(final ResourceAddress address) {
        if (switchModeHandler != null) {
            switchModeHandler.removeHandler();
        }
        switchModeHandler = bind(switchModeLink, click, event -> presenter.switchToExpertMode(address));
        switchModeLink.title = resources().constants().expertMode();
        switchModeIcon.className = fontAwesome("sitemap", large);
        Elements.setVisible(switchModeLink, true);
    }

    @Override
    public void showBackToNormalMode() {
        FinderContext finderContext = presenter.lastFinderContext();
        if (finderContext != null) {
            FinderPath disconnected = finderContext.getPath().copy(); // remove column references to disable drop downs
            finderContext.reset(disconnected);
            updateBreadcrumb(finderContext);
        }
        if (switchModeHandler != null) {
            switchModeHandler.removeHandler();
        }
        switchModeHandler = bind(switchModeLink, click, event -> presenter.backToNormalMode());
        switchModeLink.title = resources().constants().backToNormalMode();
        switchModeIcon.className = fontAwesome("th-list", large);
        Elements.setVisible(switchModeLink, true);
    }

    @Override
    public void hideSwitchMode() {
        Elements.setVisible(switchModeLink, false);
    }

    @Override
    public void showExternal(final PlaceRequest placeRequest) {
        Elements.setVisible(externalLink, true);
        externalLink.setAttribute(UIConstants.TARGET, placeRequest.getNameToken());
        externalLink.setAttribute(UIConstants.HREF, places().historyToken(placeRequest));
    }

    @Override
    public void hideExternal() {
        Elements.setVisible(externalLink, false);
    }
}
