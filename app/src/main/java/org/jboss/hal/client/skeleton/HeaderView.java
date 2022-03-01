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
package org.jboss.hal.client.skeleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
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
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.b;
import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.i;
import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.Elements.nav;
import static org.jboss.gwt.elemento.core.Elements.ol;
import static org.jboss.gwt.elemento.core.Elements.setVisible;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.Elements.ul;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.skeleton.HeaderPresenter.MAX_BREADCRUMB_VALUE_LENGTH;
import static org.jboss.hal.config.AccessControlProvider.RBAC;
import static org.jboss.hal.config.Settings.Key.RUN_AS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.active;
import static org.jboss.hal.resources.CSS.arrow;
import static org.jboss.hal.resources.CSS.back;
import static org.jboss.hal.resources.CSS.badge;
import static org.jboss.hal.resources.CSS.breadcrumbTools;
import static org.jboss.hal.resources.CSS.caret;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.collapse;
import static org.jboss.hal.resources.CSS.divider;
import static org.jboss.hal.resources.CSS.drawerPfTrigger;
import static org.jboss.hal.resources.CSS.drawerPfTriggerIcon;
import static org.jboss.hal.resources.CSS.dropdown;
import static org.jboss.hal.resources.CSS.dropdownMenu;
import static org.jboss.hal.resources.CSS.dropdownToggle;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.halBreadcrumb;
import static org.jboss.hal.resources.CSS.halHeaderCollapse;
import static org.jboss.hal.resources.CSS.hidden;
import static org.jboss.hal.resources.CSS.iconBar;
import static org.jboss.hal.resources.CSS.logo;
import static org.jboss.hal.resources.CSS.logoText;
import static org.jboss.hal.resources.CSS.logoTextFirst;
import static org.jboss.hal.resources.CSS.logoTextLast;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.CSS.nav;
import static org.jboss.hal.resources.CSS.navItemIconic;
import static org.jboss.hal.resources.CSS.navbar;
import static org.jboss.hal.resources.CSS.navbarBrand;
import static org.jboss.hal.resources.CSS.navbarCollapse;
import static org.jboss.hal.resources.CSS.navbarDefault;
import static org.jboss.hal.resources.CSS.navbarFixedTop;
import static org.jboss.hal.resources.CSS.navbarHeader;
import static org.jboss.hal.resources.CSS.navbarNav;
import static org.jboss.hal.resources.CSS.navbarPf;
import static org.jboss.hal.resources.CSS.navbarPrimary;
import static org.jboss.hal.resources.CSS.navbarToggle;
import static org.jboss.hal.resources.CSS.navbarUtility;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.srOnly;
import static org.jboss.hal.resources.CSS.static_;
import static org.jboss.hal.resources.CSS.valueDropdown;
import static org.jboss.hal.resources.CSS.warningTriangleO;
import static org.jboss.hal.resources.FontAwesomeSize.large;
import static org.jboss.hal.resources.Strings.abbreviateMiddle;
import static org.jboss.hal.resources.UIConstants.BODY;
import static org.jboss.hal.resources.UIConstants.COLLAPSE;
import static org.jboss.hal.resources.UIConstants.CONTAINER;
import static org.jboss.hal.resources.UIConstants.DROPDOWN;
import static org.jboss.hal.resources.UIConstants.HASH;
import static org.jboss.hal.resources.UIConstants.PLACEMENT;
import static org.jboss.hal.resources.UIConstants.TARGET;
import static org.jboss.hal.resources.UIConstants.TOGGLE;
import static org.jboss.hal.resources.UIConstants.TOOLTIP;

public class HeaderView extends HalViewImpl implements HeaderPresenter.MyView {

    private static final Logger logger = LoggerFactory.getLogger(HeaderView.class);
    private static final PlaceRequest HOMEPAGE = new PlaceRequest.Builder().nameToken(NameTokens.HOMEPAGE).build();

    private final Places places;
    private final Resources resources;

    private final HTMLElement logoFirst;
    private final HTMLElement logoLast;
    private final HTMLElement reloadContainer;
    private final HTMLElement reloadLink;
    private final HTMLElement reloadLabel;
    private final HTMLElement nonProgressingOperationContainer;
    private final HTMLElement messages;
    private final HTMLElement badgeIcon;
    private final HTMLElement userName;
    private final HTMLElement userDropdown;
    private final HTMLElement logoutItem;
    private final HTMLElement connectedToContainer;
    private final HTMLElement connectedTo;
    private final HTMLElement topLevelCategories;
    private final HTMLElement breadcrumb;
    private final HTMLElement backItem;
    private final HTMLElement breadcrumbToolsItem;
    private final HTMLElement switchModeLink;
    private final HTMLElement switchModeIcon;
    private final HTMLElement externalLink;
    private final HTMLElement refreshLink;

    private final Map<String, PlaceRequest> tlcPlaceRequests;
    private final Map<String, HTMLElement> tlc;
    private final ToastNotifications toastNotifications;
    private final NotificationDrawer notificationDrawer;
    private final List<HandlerRegistration> handlers;
    private final List<HandlerRegistration> breadcrumbHandlers;
    private HeaderPresenter presenter;
    private PlaceRequest backPlaceRequest;
    private HandlerRegistration switchModeHandler;
    private HandlerRegistration refreshHandler;

    @Inject
    public HeaderView(Environment environment, Places places, AccessControl ac, Resources resources) {
        this.places = places;
        this.resources = resources;

        HTMLElement logoLink;
        HTMLElement nonProgressingOperationLink;
        HTMLElement logout;
        HTMLElement reconnect;
        HTMLElement patching;
        HTMLElement accessControl;
        HTMLElement backLink;
        HTMLElement root = nav().css(navbar, navbarDefault, navbarFixedTop, navbarPf)
                .add(div().css(navbarHeader)
                        .add(button().css(navbarToggle)
                                .data(TOGGLE, COLLAPSE)
                                .data(TARGET, "." + halHeaderCollapse)
                                .add(span().css(srOnly).textContent(resources.constants().toggleNavigation()))
                                .add(span().css(iconBar))
                                .add(span().css(iconBar))
                                .add(span().css(iconBar)))
                        .add(logoLink = a().css(navbarBrand, logo, clickable)
                                .add(logoFirst = span().css(logoText, logoTextFirst)
                                        .textContent("HAL")
                                        .element())
                                .add(logoLast = span().css(logoText, logoTextLast)
                                        .textContent("Management Console")
                                        .element())
                                .element()))
                .add(div().css(collapse, navbarCollapse, halHeaderCollapse)
                        .add(ul().css(nav, navbarNav, navbarUtility)
                                .add(nonProgressingOperationContainer = li()
                                        .add(nonProgressingOperationLink = a().css(clickable)
                                                .id(Ids.NONE_PROGRESSING_LINK)
                                                .data(TOGGLE, TOOLTIP)
                                                .data(PLACEMENT, "bottom")
                                                .data(CONTAINER, BODY)
                                                .title("There is an operation in progress, longer than 15s, it may be a non progressing operation. Click to navigate to the Management Operations view to check in more detail.")
                                                .add(span().css(pfIcon(warningTriangleO)))
                                                .add(span().textContent("Non Progressing Operation"))
                                                .element())
                                        .element())
                                .add(reloadContainer = li()
                                        .add(reloadLink = a().css(clickable)
                                                .id(Ids.RELOAD_LINK)
                                                .data(TOGGLE, TOOLTIP)
                                                .data(PLACEMENT, "bottom")
                                                .data(CONTAINER, BODY)
                                                .title(Names.NOT_AVAILABLE)
                                                .add(span().css(pfIcon("restart")))
                                                .add(reloadLabel = span().element())
                                                .element())
                                        .element())
                                .add(li().css(drawerPfTrigger, dropdown)
                                        .add(messages = a().css(navItemIconic, drawerPfTriggerIcon)
                                                .id(Ids.MESSAGES_LINK)
                                                .title(resources.messages().notifications(0))
                                                .add(span()
                                                        .css(fontAwesome("bell"))
                                                        .style("padding-right: 0"))
                                                .add(badgeIcon = span().css(badge).id(Ids.BADEGE_ICON).element())
                                                .element()))
                                .add(li().css(dropdown, hidden).id(Ids.HEADER_EXTENSIONS_DROPDOWN)
                                        .add(a().css(clickable, dropdownToggle)
                                                .data(TOGGLE, DROPDOWN)
                                                .title(Names.EXTENSIONS)
                                                .add(span().css(fontAwesome("th-large")))
                                                .add(b().css(caret)))
                                        .add(ul().css(dropdownMenu, CSS.userDropdown).id(Ids.HEADER_EXTENSIONS)))
                                .add(li().css(dropdown)
                                        .add(a().css(clickable, dropdownToggle).data(TOGGLE, DROPDOWN)
                                                .add(span().css(pfIcon("user")))
                                                .add(userName = span().id(Ids.HEADER_USERNAME).element())
                                                .add(b().css(caret)))
                                        .add(userDropdown = ul().css(dropdownMenu, CSS.userDropdown)
                                                .add(logoutItem = li()
                                                        .add(logout = a().css(clickable)
                                                                .id(Ids.LOGOUT_LINK)
                                                                .textContent(resources.constants().logout())
                                                                .element())
                                                        .element())
                                                .element()))
                                .add(connectedToContainer = li().css(dropdown)
                                        .add(a().css(clickable, dropdownToggle).data(TOGGLE, DROPDOWN)
                                                .add(span().css(pfIcon("plugged")))
                                                .add(b().css(caret)))
                                        .add(ul().css(dropdownMenu)
                                                .add(connectedTo = li().css(static_).id(Ids.HEADER_CONNECTED_TO)
                                                        .element())
                                                .add(li().css(divider))
                                                .add(li()
                                                        .add(reconnect = a().css(clickable)
                                                                .textContent(resources.constants().connectToServer())
                                                                .element())))
                                        .element()))
                        .add(topLevelCategories = ul().css(nav, navbarNav, navbarPrimary)
                                .add(li().css(active)
                                        .add(a().css(clickable, active)
                                                .id(Ids.TLC_HOMEPAGE)
                                                .textContent(Names.HOMEPAGE)))
                                .add(li()
                                        .add(a().css(clickable)
                                                .id(Ids.TLC_DEPLOYMENTS)
                                                .textContent(Names.DEPLOYMENTS)))
                                .add(li()
                                        .add(a().css(clickable)
                                                .id(Ids.TLC_CONFIGURATION)
                                                .textContent(Names.CONFIGURATION)))
                                .add(li()
                                        .add(a().css(clickable)
                                                .id(Ids.TLC_RUNTIME)
                                                .textContent(Names.RUNTIME)))
                                .add(patching = li()
                                        .add(a().css(clickable)
                                                .id(Ids.TLC_PATCHING)
                                                .textContent(Names.PATCHING))
                                        .element())
                                .add(accessControl = li()
                                        .add(a().css(clickable)
                                                .id(Ids.TLC_ACCESS_CONTROL)
                                                .textContent(Names.ACCESS_CONTROL))
                                        .element())
                                .element())
                        .add(breadcrumb = ol().css(CSS.breadcrumb, halBreadcrumb)
                                .add(backItem = li()
                                        .add(backLink = a().css(clickable, back)
                                                .add(i().css(fontAwesome("angle-double-left")))
                                                .add(" " + resources.constants().back())
                                                .element())
                                        .element())
                                .add(breadcrumbToolsItem = li().css(breadcrumbTools)
                                        .add(refreshLink = a().css(clickable)
                                                .title(resources.constants().refresh())
                                                .add(span().css(fontAwesome("refresh", large)))
                                                .element())
                                        .add(switchModeLink = a().css(clickable)
                                                .title(resources.constants().openInModelBrowser())
                                                .add(switchModeIcon = span().css(fontAwesome("sitemap", large))
                                                        .element())
                                                .element())
                                        .add(externalLink = a().css(clickable)
                                                .title(resources.constants().openInExternalWindow())
                                                .add(span().css(fontAwesome("external-link", large)))
                                                .element())
                                        .element())
                                .element()))
                .element();
        initElement(root);

        backPlaceRequest = HOMEPAGE;
        setVisible(reloadContainer, false);
        setVisible(nonProgressingOperationContainer, false);
        setVisible(breadcrumb, false);

        toastNotifications = new ToastNotifications(resources); // adds itself to the body
        notificationDrawer = new NotificationDrawer(resources);
        topLevelCategories.parentNode.insertBefore(notificationDrawer.element(), topLevelCategories);

        boolean su = ac.isSuperUserOrAdministrator();
        if (!su) {
            topLevelCategories.removeChild(patching);
            topLevelCategories.removeChild(accessControl);
        }

        if (!environment.isPatchingEnabled() && topLevelCategories.contains(patching)) {
            topLevelCategories.removeChild(patching);
        }

        String accessControlNameToken = ac.isSingleSignOn() ? NameTokens.ACCESS_CONTROL_SSO : NameTokens.ACCESS_CONTROL;

        // @formatter:off
        tlcPlaceRequests = new HashMap<>();
        tlcPlaceRequests.put(NameTokens.HOMEPAGE, new PlaceRequest.Builder().nameToken(NameTokens.HOMEPAGE).build());
        tlcPlaceRequests.put(NameTokens.DEPLOYMENTS, new PlaceRequest.Builder().nameToken(NameTokens.DEPLOYMENTS).build());
        tlcPlaceRequests.put(NameTokens.CONFIGURATION, new PlaceRequest.Builder().nameToken(NameTokens.CONFIGURATION).build());
        tlcPlaceRequests.put(NameTokens.RUNTIME, new PlaceRequest.Builder().nameToken(NameTokens.RUNTIME).build());
        tlcPlaceRequests.put(NameTokens.PATCHING, new PlaceRequest.Builder().nameToken(NameTokens.PATCHING).build());
        tlcPlaceRequests.put(accessControlNameToken, new PlaceRequest.Builder().nameToken(accessControlNameToken).build());
        // @formatter:on

        tlc = new HashMap<>();
        initTlc(root,
                new String[] {
                        NameTokens.HOMEPAGE,
                        NameTokens.DEPLOYMENTS,
                        NameTokens.CONFIGURATION,
                        NameTokens.RUNTIME,
                        NameTokens.PATCHING,
                        accessControlNameToken,
                },
                new String[] {
                        Ids.TLC_HOMEPAGE,
                        Ids.TLC_DEPLOYMENTS,
                        Ids.TLC_CONFIGURATION,
                        Ids.TLC_RUNTIME,
                        Ids.TLC_PATCHING,
                        Ids.TLC_ACCESS_CONTROL,
                });

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
                bind(nonProgressingOperationLink, click, event -> presenter.goTo(NameTokens.MANAGEMENT_OPERATIONS)),
                bind(reloadLink, click, event -> presenter.reload()),
                bind(messages, click, event -> notificationDrawer.toggle()),
                bind(logout, click, event -> presenter.logout()),
                bind(reconnect, click, event -> presenter.reconnect())));
    }

    private void initTlc(HTMLElement root, String[] tokens, String[] ids) {
        for (int i = 0; i < tokens.length; i++) {
            HTMLElement element = (HTMLElement) root.querySelector(HASH + ids[i]);
            if (element != null) {
                tlc.put(tokens[i], element);
            }
        }
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
    public void setPresenter(HeaderPresenter presenter) {
        this.presenter = presenter;
        notificationDrawer.setPresenter(presenter);
    }

    @Override
    public void init(Environment environment, Endpoints endpoints, Settings settings, User user) {
        setLogo(resources.theme().getFirstName(), resources.theme().getLastName());

        if (endpoints.isSameOrigin()) {
            connectedTo.textContent = resources.constants().sameOrigin();
        } else {
            connectedTo.textContent = resources.messages().connectedTo(endpoints.dmr());
        }

        userName.textContent = user.getName();
        updateRoles(environment, settings, user);
        updateMessageElements();
    }

    @Override
    public void updateRoles(Environment environment, Settings settings, User user) {
        for (Iterator<HTMLElement> iterator = Elements.iterator(userDropdown); iterator.hasNext();) {
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
                    .textContent(resources.messages().activeRoles(csr))
                    .title(resources.messages().activeRoles(csr))
                    .element();
            userDropdown.insertBefore(activeRoles, logoutItem);
            userDropdown.insertBefore(divider(), logoutItem);

            if (user.isSuperuser() && environment.getAccessControlProvider() == RBAC) {
                Set<String> runAsRoleSetting = settings.get(RUN_AS).asSet();
                HTMLElement runAs = li().css(static_)
                        .textContent(resources.constants().runAs())
                        .element();
                userDropdown.insertBefore(runAs, logoutItem);

                stream(environment.getRoles().spliterator(), false)
                        .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME))
                        .forEach(role -> {
                            HTMLElement check, name;
                            HTMLElement runAsRole = li()
                                    .add(a().css(clickable).on(click, event -> presenter.runAs(role.getName()))
                                            .add(check = span().css(fontAwesome("check"), marginRight5)
                                                    .element())
                                            .add(name = span().textContent(role.getName())
                                                    .element()))
                                    .element();
                            if (!runAsRoleSetting.contains(role.getName())) {
                                check.style.visibility = "hidden"; // NON-NLS
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
                                    .textContent(resources.constants().clearRunAs()))
                            .element();
                    userDropdown.insertBefore(clearRunAs, logoutItem);
                }
                userDropdown.insertBefore(divider(), logoutItem);
            }
        }
    }

    private HTMLElement divider() {
        return li().css(CSS.divider).element();
    }

    private void setLogo(String first, String last) {
        logoFirst.textContent = first;
        logoLast.textContent = Strings.nullToEmpty(last);
    }

    // ------------------------------------------------------ logo, reload, messages & global state

    @Override
    public void showReload(String text, String tooltip) {
        reloadLabel.textContent = text;
        Tooltip.element(reloadLink).setTitle(tooltip);
        setVisible(reloadContainer, true);
    }

    @Override
    public void hideReload() {
        setVisible(reloadContainer, false);
    }

    @Override
    public void hideReconnect() {
        setVisible(connectedToContainer, false);
    }

    @Override
    public void onMessage(Message message) {
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
            default:
                break;
        }
        toastNotifications.add(message);
        notificationDrawer.add(message);
        updateMessageElements();
    }

    @Override
    public void onMarkAllAsRead() {
        updateMessageElements();
    }

    @Override
    public void onClearMessage() {
        updateMessageElements();
    }

    private void updateMessageElements() {
        int unreadCount = notificationDrawer.getUnreadCount();
        setVisible(badgeIcon, unreadCount != 0);
        messages.title = resources.messages().notifications(unreadCount);
    }

    public void onNonProgressingOperation(boolean display) {
        setVisible(nonProgressingOperationContainer, display);
    }

    // ------------------------------------------------------ modes

    @Override
    public void topLevelCategoryMode() {
        setVisible(topLevelCategories, true);
        setVisible(breadcrumb, false);
    }

    @Override
    public void applicationMode() {
        setVisible(topLevelCategories, false);
        setVisible(breadcrumb, true);
    }

    // ------------------------------------------------------ links & tlc

    @Override
    public void updateLinks(FinderContext finderContext) {
        PlaceRequest placeRequest = finderContext.getToken() != null ? finderContext.toPlaceRequest() : HOMEPAGE;
        backPlaceRequest = placeRequest;
        if (tlcPlaceRequests.containsKey(finderContext.getToken())) {
            tlcPlaceRequests.put(finderContext.getToken(), placeRequest);
        }
    }

    @Override
    public void selectTopLevelCategory(String nameToken) {
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
    public void updateBreadcrumb(String title) {
        clearBreadcrumb();
        HTMLElement li = li().textContent(title).element();
        breadcrumb.insertBefore(li, breadcrumbToolsItem);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void updateBreadcrumb(FinderContext finderContext) {
        clearBreadcrumb();
        FinderPath currentPath = new FinderPath();

        for (HandlerRegistration handler : breadcrumbHandlers) {
            handler.removeHandler();
        }
        breadcrumbHandlers.clear();

        for (Iterator<FinderSegment> iterator = finderContext.getPath().iterator(); iterator.hasNext();) {
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

            HTMLElement key = span().css(CSS.key).element();
            if (finderContext.getToken() != null) {
                PlaceRequest keyRequest = new PlaceRequest.Builder()
                        .nameToken(finderContext.getToken())
                        .with("path", currentPath.toString())
                        .build();
                key.appendChild(a().css(clickable).on(click, event -> presenter.goTo(keyRequest))
                        .textContent(segment.getColumnTitle())
                        .element());
            } else {
                key.textContent = segment.getColumnTitle();
            }
            builder.add(key)
                    .add(span().css(arrow).innerHtml(SafeHtmlUtils.fromSafeConstant("&#8658;")));

            HTMLElement value = span().css(CSS.value).element();
            if (segment.supportsDropdown()) {
                value.classList.add(dropdown);

                HTMLElement a;
                String id = Ids.build(segment.getColumnId(), VALUE);
                value.appendChild(a = a().css(clickable)
                        .id(id)
                        .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                        .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                        .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                        .attr(UIConstants.ROLE, UIConstants.BUTTON)
                        .element());
                breadcrumbHandlers.add(bind(a, click, event -> {
                    Element ul = a.nextElementSibling;
                    segment.dropdown(finderContext, items -> {
                        Elements.removeChildrenFrom(ul);
                        if (items.isEmpty()) {
                            HTMLElement empty = li().css(CSS.empty)
                                    .textContent(HeaderView.this.resources.constants().noItems())
                                    .element();
                            ul.appendChild(empty);
                        } else {
                            for (DropdownItem<Object> dropdownItem : items) {
                                HTMLElement element = li()
                                        .add(a().css(clickable)
                                                .on(click, e -> dropdownItem.onSelect(finderContext))
                                                .textContent(dropdownItem.getTitle()))
                                        .element();
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
                            .element());
                } else {
                    a.appendChild(span().textContent(breadcrumbValue + " ").element());
                }
                a.appendChild(span().css(caret).element());
                value.appendChild(ul()
                        .css(dropdownMenu, valueDropdown)
                        .aria(UIConstants.LABELLED_BY, id)
                        .element());
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
            breadcrumb.insertBefore(builder.element(), breadcrumbToolsItem);
        }
    }

    @Override
    public void updateBreadcrumb(ModelBrowserPath path) {
        clearBreadcrumb();
        if (path == null || path.isEmpty()) {
            // deselection
            breadcrumb.insertBefore(li().textContent(resources.constants().nothingSelected()).element(),
                    breadcrumbToolsItem);

        } else {
            ModelBrowser modelBrowser = path.getModelBrowser();
            for (Iterator<Segment[]> iterator = path.iterator(); iterator.hasNext();) {
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
                        .add(valueContainer = span().css(CSS.value).element())
                        .element();
                if (link) {
                    valueContainer.appendChild(valueContainer = a().css(clickable)
                            .on(click, event -> modelBrowser.select(value.id, true))
                            .element());
                }
                valueContainer.textContent = value.text;
                breadcrumb.insertBefore(li, breadcrumbToolsItem);
            }
        }
    }

    private void clearBreadcrumb() {
        for (Iterator<HTMLElement> iterator = Elements.iterator(breadcrumb); iterator.hasNext();) {
            HTMLElement element = iterator.next();
            if (element == backItem || element == breadcrumbToolsItem) {
                continue;
            }
            iterator.remove();
        }
    }

    // ------------------------------------------------------ normal / expert mode

    @Override
    public void showExpertMode(ResourceAddress address) {
        if (switchModeHandler != null) {
            switchModeHandler.removeHandler();
        }
        switchModeHandler = bind(switchModeLink, click, event -> presenter.switchToExpertMode(address));
        switchModeLink.title = resources.constants().expertMode();
        switchModeIcon.className = fontAwesome("sitemap", large);
        setVisible(switchModeLink, true);
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
        switchModeLink.title = resources.constants().backToNormalMode();
        switchModeIcon.className = fontAwesome("th-list", large);
        setVisible(switchModeLink, true);
    }

    @Override
    public void hideSwitchMode() {
        setVisible(switchModeLink, false);
    }

    // ------------------------------------------------------ external / refresh

    @Override
    public void showExternal(PlaceRequest placeRequest) {
        setVisible(externalLink, true);
        externalLink.setAttribute(UIConstants.TARGET, placeRequest.getNameToken());
        externalLink.setAttribute(UIConstants.HREF, places.historyToken(placeRequest));
    }

    @Override
    public void hideExternal() {
        setVisible(externalLink, false);
    }

    @Override
    public void showRefresh() {
        setVisible(refreshLink, true);
        if (refreshHandler != null) {
            refreshHandler.removeHandler();
        }
        refreshHandler = bind(refreshLink, click, event -> presenter.refresh());
    }

    @Override
    public void hideRefresh() {
        setVisible(refreshLink, false);
    }
}
