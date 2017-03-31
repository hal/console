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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.PostConstruct;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.LIElement;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.core.accesscontrol.AccessControl;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.Roles;
import org.jboss.hal.config.Settings;
import org.jboss.hal.config.User;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.FinderSegment.DropdownItem;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath.Segment;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.skeleton.HeaderPresenter.MAX_BREADCRUMB_VALUE_LENGTH;
import static org.jboss.hal.config.AccessControlProvider.RBAC;
import static org.jboss.hal.config.Settings.Key.RUN_AS;
import static org.jboss.hal.core.Strings.abbreviateMiddle;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.FontAwesomeSize.large;

/**
 * @author Harald Pehl
 */
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
    private Map<String, Element> tlc;
    private HeaderPresenter presenter;
    private MessagePanel messagePanel;
    private MessageSink messageSink;

    @DataElement Element logoFirst;
    @DataElement Element logoLast;
    @DataElement Element reloadContainer;
    @DataElement Element reloadLink;
    @DataElement Element reloadLabel;
    @DataElement Element messagesIcon;
    @DataElement Element userName;
    @DataElement Element userDropdown;
    @DataElement Element logoutItem;
    @DataElement Element connectedToContainer;
    @DataElement Element connectedTo;
    @DataElement Element patching;
    @DataElement Element accessControl;
    @DataElement Element topLevelCategories;
    @DataElement Element breadcrumb;
    @DataElement Element backItem;
    @DataElement Element backLink;
    @DataElement Element breadcrumbToolsItem;
    @DataElement Element switchModeLink;
    @DataElement Element switchModeIcon;
    @DataElement Element externalLink;


    // ------------------------------------------------------ initialization

    @PostConstruct
    void init() {
        Element root = asElement();
        Elements.setVisible(reloadContainer, false);
        messagePanel = new MessagePanel(resources()); // message panel adds itself to the body
        messageSink = new MessageSink(resources());
        topLevelCategories.getParentElement().insertBefore(messageSink.asElement(), topLevelCategories);

        backPlaceRequest = HOMEPAGE;
        backLink.setOnclick(event -> presenter.goTo(backPlaceRequest));

        // @formatter:off
        tlcPlaceRequests = new HashMap<>();
        tlcPlaceRequests.put(NameTokens.HOMEPAGE,       new PlaceRequest.Builder().nameToken(NameTokens.HOMEPAGE).build());
        tlcPlaceRequests.put(NameTokens.DEPLOYMENTS,    new PlaceRequest.Builder().nameToken(NameTokens.DEPLOYMENTS).build());
        tlcPlaceRequests.put(NameTokens.CONFIGURATION,  new PlaceRequest.Builder().nameToken(NameTokens.CONFIGURATION).build());
        tlcPlaceRequests.put(NameTokens.RUNTIME,        new PlaceRequest.Builder().nameToken(NameTokens.RUNTIME).build());
        tlcPlaceRequests.put(NameTokens.ACCESS_CONTROL, new PlaceRequest.Builder().nameToken(NameTokens.ACCESS_CONTROL).build());
        tlcPlaceRequests.put(NameTokens.PATCHING,       new PlaceRequest.Builder().nameToken(NameTokens.PATCHING).build());
        // @formatter:on

        tlc = new HashMap<>();
        tlc.put(NameTokens.HOMEPAGE, root.querySelector("#" + Ids.TLC_HOMEPAGE));
        tlc.put(NameTokens.DEPLOYMENTS, root.querySelector("#" + Ids.TLC_DEPLOYMENTS));
        tlc.put(NameTokens.CONFIGURATION, root.querySelector("#" + Ids.TLC_CONFIGURATION));
        tlc.put(NameTokens.RUNTIME, root.querySelector("#" + Ids.TLC_RUNTIME));
        tlc.put(NameTokens.ACCESS_CONTROL, root.querySelector("#" + Ids.TLC_ACCESS_CONTROL));
        tlc.put(NameTokens.PATCHING, root.querySelector("#" + Ids.TLC_PATCHING));
        for (Map.Entry<String, Element> entry : tlc.entrySet()) {
            entry.getValue().setOnclick(event -> {
                if (tlcPlaceRequests.containsKey(entry.getKey())) {
                    presenter.goTo(tlcPlaceRequests.get(entry.getKey()));
                }
            });
        }

        boolean su = ac().isSuperUserOrAdministrator();
        if (!su) {
            topLevelCategories.removeChild(patching);
            topLevelCategories.removeChild(accessControl);
        }
        Elements.setVisible(breadcrumb, false);
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
            connectedTo.setInnerText(resources().constants().sameOrigin());
        } else {
            connectedTo.setInnerText(resources().messages().connectedTo(endpoints.dmr()));
        }

        userName.setInnerHTML(user.getName());
        updateRoles(environment, settings, user);
    }

    @Override
    public void updateRoles(Environment environment, Settings settings, User user) {
        for (Iterator<Element> iterator = Elements.iterator(userDropdown); iterator.hasNext(); ) {
            Element element = iterator.next();
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
            Element activeRoles = new Elements.Builder().li().css(static_, CSS.activeRoles)
                    .textContent(resources().messages().activeRoles(csr))
                    .title(resources().messages().activeRoles(csr))
                    .end().build();
            userDropdown.insertBefore(activeRoles, logoutItem);
            userDropdown.insertBefore(divider(), logoutItem);

            if (user.isSuperuser() && environment.getAccessControlProvider() == RBAC) {
                String runAsRoleSetting = settings.get(RUN_AS).value();
                Element runAs = new Elements.Builder().li().css(static_)
                        .textContent(resources().constants().runAs())
                        .end().build();
                userDropdown.insertBefore(runAs, logoutItem);

                stream(environment.getRoles().spliterator(), false)
                        .sorted(Roles.STANDARD_FIRST.thenComparing(Roles.BY_NAME))
                        .forEach(role -> {
                            // @formatter:off
                            Elements.Builder builder = new Elements.Builder()
                                .li()
                                    .a().css(clickable).on(click, event -> presenter.runAs(role.getName()))
                                        .span()
                                            .css(fontAwesome("check"), marginRight5);
                                            if (!role.getName().equals(runAsRoleSetting)) {
                                                builder.style("visibility: hidden");
                                            }
                                        builder.end()
                                        .span()
                                            .textContent(role.getName());
                                            if (role.isScoped()) {
                                                String scopedInfo = role.getBaseRole().getName()  +
                                                        " / " + String.join(", ", role.getScope());
                                                builder.title(scopedInfo);
                                            }
                                        builder.end()
                                    .end()
                                .end();
                            // @formatter:on
                            Element runAsRole = builder.build();
                            userDropdown.insertBefore(runAsRole, logoutItem);
                        });

                if (runAsRoleSetting != null) {
                    // @formatter:off
                    Element clearRunAs = new Elements.Builder()
                        .li()
                            .a().css(clickable).on(click, event -> presenter.clearRunAs())
                                .textContent(resources().constants().clearRunAs())
                            .end()
                        .end()
                    .build();
                    // @formatter:on
                    userDropdown.insertBefore(clearRunAs, logoutItem);
                }
                userDropdown.insertBefore(divider(), logoutItem);
            }
        }
    }

    private Element divider() {
        LIElement divider = Browser.getDocument().createLIElement();
        divider.getClassList().add(CSS.divider);
        return divider;
    }

    private void setLogo(String first, String last) {
        logoFirst.setInnerText(first);
        logoLast.setInnerText(Strings.nullToEmpty(last));
    }


    // ------------------------------------------------------ logo, reload, messages & global state

    @EventHandler(element = "logoLink", on = click)
    void onLogo() {
        presenter.goTo(NameTokens.HOMEPAGE);
    }

    @Override
    public void showReload(final String text, final String tooltip) {
        reloadLabel.setTextContent(text);
        Tooltip.element(reloadLink).setTitle(tooltip);
        Elements.setVisible(reloadContainer, true);
    }

    @Override
    public void hideReload() {
        Elements.setVisible(reloadContainer, false);
    }

    @EventHandler(element = "reloadLink", on = click)
    void onReload() {
        presenter.reload();
    }

    @EventHandler(element = "messages", on = click)
    void onMessages() {
        messageSink.asElement().getClassList().toggle(hide);
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
        messagesIcon.getClassList().remove("fa-bell-o"); //NON-NLS
        messagesIcon.getClassList().add("fa-bell"); //NON-NLS
    }

    @Override
    public void clearMessages() {
        messageSink.clear();
        messagesIcon.getClassList().remove("fa-bell"); //NON-NLS
        messagesIcon.getClassList().add("fa-bell-o"); //NON-NLS
    }

    @EventHandler(element = "logout", on = click)
    void onLogout() {
        presenter.logout();
    }

    @EventHandler(element = "reconnect", on = click)
    void onReconnect() {
        presenter.reconnect();
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
                tlc.get(token).getClassList().add(active);
                tlc.get(token).getParentElement().getClassList().add(active);
            } else {
                tlc.get(token).getClassList().remove(active);
                tlc.get(token).getParentElement().getClassList().remove(active);
            }
        }
    }


    // ------------------------------------------------------ breadcrumb

    @Override
    public void updateBreadcrumb(final String title) {
        clearBreadcrumb();
        Element li = Browser.getDocument().createLIElement();
        li.setTextContent(title);
        breadcrumb.insertBefore(li, breadcrumbToolsItem);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateBreadcrumb(final FinderContext finderContext) {
        clearBreadcrumb();
        FinderPath currentPath = new FinderPath();

        for (Iterator<FinderSegment> iterator = finderContext.getPath().iterator(); iterator.hasNext(); ) {
            FinderSegment<Object> segment = iterator.next();
            if (segment.getColumnId() == null || segment.getItemId() == null) {
                // we need to ignore half filled segments which occur when removing items from a column
                break;
            }

            boolean last = !iterator.hasNext();
            currentPath.append(segment.getColumnId(), segment.getItemId());

            Elements.Builder builder = new Elements.Builder().li();
            if (last) {
                builder.css(active);
            }
            builder.span().css(key);
            if (finderContext.getToken() != null) {
                PlaceRequest keyRequest = new PlaceRequest.Builder()
                        .nameToken(finderContext.getToken())
                        .with("path", currentPath.toString())
                        .build();
                builder.a().css(clickable).on(click, event -> presenter.goTo(keyRequest))
                        .textContent(segment.getColumnTitle())
                        .end();
            } else {
                builder.textContent(segment.getColumnTitle());
            }
            builder.end().span().css(arrow).innerHtml(SafeHtmlUtils.fromSafeConstant("&#8658;")).end();

            builder.span();
            if (segment.supportsDropdown()) {
                builder.css(value, dropdown);
                String id = Ids.build(segment.getColumnId(), VALUE);
                builder.a().id(id)
                        .css(clickable)
                        .data(UIConstants.TARGET, "#")
                        .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                        .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                        .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                        .attr(UIConstants.ROLE, UIConstants.BUTTON)
                        .on(click, event -> {
                            Element ul = ((Element) event.getCurrentTarget()).getNextElementSibling();
                            segment.dropdown(finderContext, items -> {
                                Elements.removeChildrenFrom(ul);
                                if (items.isEmpty()) {
                                    Element empty = new Elements.Builder().li().css(CSS.empty)
                                            .textContent(HeaderView.this.resources().constants().noItems()).end()
                                            .build();
                                    ul.appendChild(empty);
                                } else {
                                    for (DropdownItem<Object> dropdownItem : items) {
                                        Element element = new Elements.Builder().li().a()
                                                .css(clickable)
                                                .on(click, e -> dropdownItem.onSelect(finderContext))
                                                .textContent(dropdownItem.getTitle()).end().end().build();
                                        ul.appendChild(element);
                                    }
                                }
                            });
                        });

                String breadcrumbValue = segment.getItemTitle();
                if (breadcrumbValue.length() > MAX_BREADCRUMB_VALUE_LENGTH) {
                    builder.span()
                            .textContent(abbreviateMiddle(breadcrumbValue, MAX_BREADCRUMB_VALUE_LENGTH) + " ")
                            .title(breadcrumbValue).end();
                } else {
                    builder.span().textContent(breadcrumbValue + " ").end();
                }
                builder.span().css(caret).end()
                        .end();
                builder.ul()
                        .css(dropdownMenu, valueDropdown)
                        .aria(UIConstants.LABELLED_BY, id)
                        .end();
            } else {
                builder.css(value);
                String breadcrumbValue = segment.getItemTitle();
                if (breadcrumbValue.length() > MAX_BREADCRUMB_VALUE_LENGTH) {
                    builder.textContent(abbreviateMiddle(breadcrumbValue, MAX_BREADCRUMB_VALUE_LENGTH))
                            .title(breadcrumbValue);
                } else {
                    builder.textContent(segment.getItemTitle());
                }
            }
            builder.end(); // </span>
            builder.end(); // </li>
            breadcrumb.insertBefore(builder.build(), breadcrumbToolsItem);
        }
    }

    @Override
    public void updateBreadcrumb(final ModelBrowserPath path) {
        clearBreadcrumb();
        if (path == null || path.isEmpty()) {
            // deselection
            breadcrumb.insertBefore(
                    new Elements.Builder().li().textContent(resources().constants().nothingSelected()).build(),
                    breadcrumbToolsItem);

        } else {
            ModelBrowser modelBrowser = path.getModelBrowser();
            for (Iterator<Segment[]> iterator = path.iterator(); iterator.hasNext(); ) {
                Segment[] segments = iterator.next();
                Segment key = segments[0];
                Segment value = segments[1];
                boolean link = value != ModelBrowserPath.WILDCARD && iterator.hasNext();

                // @formatter:off
                Elements.Builder builder = new Elements.Builder()
                    .li()
                        .span().css(CSS.key)
                            .a().css(clickable).on(click, event -> modelBrowser.select(key.id, true))
                                .textContent(key.text)
                            .end()
                        .end()
                        .span().css(arrow).innerHtml(SafeHtmlUtils.fromSafeConstant("&#8658;")).end()
                        .span().css(CSS.value);
                // @formatter:on
                if (link) {
                    builder.a().css(clickable).on(click, event -> modelBrowser.select(value.id, true));
                }
                builder.textContent(value.text);
                if (link) {
                    builder.end(); // </a>
                }
                builder.end().end(); // </span> </li>
                breadcrumb.insertBefore(builder.build(), breadcrumbToolsItem);
            }
        }
    }

    private void clearBreadcrumb() {
        for (Iterator<Element> iterator = Elements.iterator(breadcrumb); iterator.hasNext(); ) {
            Element element = iterator.next();
            if (element == backItem || element == breadcrumbToolsItem) {
                continue;
            }
            iterator.remove();
        }
    }


    // ------------------------------------------------------ breadcrumb tools

    @Override
    public void showExpertMode(final ResourceAddress address) {
        switchModeLink.setOnclick(event -> presenter.switchToExpertMode(address));
        switchModeLink.setTitle(resources().constants().expertMode());
        switchModeIcon.setClassName(fontAwesome("sitemap", large));
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
        switchModeLink.setOnclick(event -> presenter.backToNormalMode());
        switchModeLink.setTitle(resources().constants().backToNormalMode());
        switchModeIcon.setClassName(fontAwesome("th-list", large));
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
