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

import com.google.common.base.Joiner;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.Window;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.InstanceInfo;
import org.jboss.hal.config.User;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.FinderSegment.DropdownItem;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath.Segment;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.skeleton.HeaderPresenter.MAX_BREADCRUMB_VALUE_LENGTH;
import static org.jboss.hal.client.skeleton.HeaderPresenter.MESSAGE_TIMEOUT;
import static org.jboss.hal.config.InstanceInfo.WILDFLY;
import static org.jboss.hal.core.Strings.abbreviateMiddle;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.HAL;
import static org.jboss.hal.resources.Names.MANAGEMENT_CONSOLE;
import static org.jboss.hal.resources.Names.NYI;

/**
 * @author Harald Pehl
 */
@Templated("MainLayout.html#header")
public abstract class HeaderView extends ViewImpl implements HeaderPresenter.MyView, IsElement {

    // @formatter:off
    public static HeaderView create(final Resources resources, final User user) {
        return new Templated_HeaderView(resources, user);
    }

    public abstract Resources resources();
    public abstract User user();
    // @formatter:on


    private static final Logger logger = LoggerFactory.getLogger(HeaderView.class);
    private static final PlaceRequest HOMEPAGE = new PlaceRequest.Builder().nameToken(NameTokens.HOMEPAGE).build();

    private PlaceRequest backPlaceRequest;
    private Map<String, PlaceRequest> tlcPlaceRequests;
    private Map<String, Element> tlc;
    private int messageTimeoutHandle;
    private HeaderPresenter presenter;

    @DataElement Element logoFirst;
    @DataElement Element logoLast;
    @DataElement Element messagesIcon;
    @DataElement Element messagesLabel;
    @DataElement Element userName;
    @DataElement Element roles;
    @DataElement Element connectedTo;
    @DataElement Element accessControl;
    @DataElement Element patching;
    @DataElement Element topLevelTabs;
    @DataElement Element breadcrumbs;
    @DataElement Element backLink;


    @PostConstruct
    void init() {
        Element root = asElement();

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

        boolean su = user().isSuperuser() || user().isAdministrator();
        Elements.setVisible(accessControl, su);
        Elements.setVisible(patching, su);
        Elements.setVisible(breadcrumbs, false);
    }

    @Override
    public void setPresenter(final HeaderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(Environment environment, Endpoints endpoints, User user) {
        if (environment.getInstanceInfo() == WILDFLY) {
            setLogo(new String[]{
                    environment.getInstanceInfo().platform().substring(0, 4),
                    environment.getInstanceInfo().platform().substring(4),
            });
        } else if (environment.getInstanceInfo() == InstanceInfo.EAP) {
            setLogo(new String[]{
                    environment.getInstanceInfo().platform().substring(0, 13),
                    environment.getInstanceInfo().platform().substring(13).trim(),
            });
        } else {
            setLogo(new String[]{HAL, MANAGEMENT_CONSOLE});
        }

        if (endpoints.isSameOrigin()) {
            connectedTo.setInnerText(resources().constants().sameOrigin());
        } else {
            connectedTo.setInnerText(resources().messages().connectedTo(endpoints.dmr()));
        }

        userName.setInnerHTML(user.getName());
        // Keep this in sync with the template!
        Elements.setVisible(roles, !user.getRoles().isEmpty());
        Elements.setVisible(roles.getNextElementSibling(), !user.getRoles().isEmpty());
        roles.setInnerText(resources().messages().activeRoles(Joiner.on(", ").join(user.getRoles())));
    }

    private void setLogo(String[] parts) {
        logoFirst.setInnerText(parts[0]);
        logoLast.setInnerText(parts[1]);
    }


    // ------------------------------------------------------ links and tokens

    @Override
    public void updateLinks(final FinderContext finderContext) {
        PlaceRequest placeRequest = finderContext.getToken() != null ? finderContext.toPlaceRequest() : HOMEPAGE;
        backPlaceRequest = placeRequest;
        if (tlcPlaceRequests.containsKey(finderContext.getToken())) {
            tlcPlaceRequests.put(finderContext.getToken(), placeRequest);
        }
    }

    @Override
    public void selectTlc(final String nameToken) {
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


    // ------------------------------------------------------ messages

    @Override
    public void showMessage(final Message message) {
        // TODO Prevent showing two messages at once -> queue multiple messages
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
        Element body = Browser.getDocument().getBody();
        Element messageElement = new MessageElement(message).asElement();
        body.insertBefore(messageElement, body.getFirstChild());
        if (!message.isSticky()) {
            startMessageTimeout(messageElement);
            messageElement.setOnmouseover(event -> stopMessageTimeout());
            messageElement.setOnmouseout(event -> startMessageTimeout(messageElement));
        }
    }

    private void startMessageTimeout(Element element) {
        Window window = Browser.getWindow();
        Element body = Browser.getDocument().getBody();
        messageTimeoutHandle = window.setTimeout(() -> body.removeChild(element), MESSAGE_TIMEOUT);
    }

    private void stopMessageTimeout() {
        Browser.getWindow().clearTimeout(messageTimeoutHandle);
    }


    // ------------------------------------------------------ modes

    @Override
    public void tlcMode() {
        Elements.setVisible(topLevelTabs, true);
        Elements.setVisible(breadcrumbs, false);
    }

    @Override
    public void fullscreenMode(final String title) {
        applicationMode();
        clearBreadcrumb();
        Element li = Browser.getDocument().createLIElement();
        li.setTextContent(title);
        breadcrumbs.appendChild(li);
    }

    @Override
    public void applicationMode() {
        Elements.setVisible(topLevelTabs, false);
        Elements.setVisible(breadcrumbs, true);
    }


    // ------------------------------------------------------ breadcrumb

    private void clearBreadcrumb() {
        while (breadcrumbs.getLastChild() != null && breadcrumbs.getChildren().getLength() > 1) {
            breadcrumbs.removeChild(breadcrumbs.getLastChild());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateBreadcrumb(final FinderContext context) {
        clearBreadcrumb();
        FinderPath currentPath = new FinderPath();

        for (Iterator<FinderSegment> iterator = context.getPath().iterator(); iterator.hasNext(); ) {
            FinderSegment<Object> segment = iterator.next();
            if (segment.getKey() == null || segment.getValue() == null) {
                // we need to ignore half filled segments which occur when removing items from a column
                break;
            }

            boolean last = !iterator.hasNext();
            currentPath.append(segment.getKey(), segment.getValue());

            Elements.Builder builder = new Elements.Builder().li();
            if (last) {
                builder.css(active);
            }
            builder.span().css(key);
            if (context.getToken() != null) {
                PlaceRequest keyRequest = new PlaceRequest.Builder()
                        .nameToken(context.getToken())
                        .with("path", currentPath.toString())
                        .build();
                builder.a().css(clickable).on(click, event -> presenter.goTo(keyRequest))
                        .textContent(segment.getBreadcrumbKey())
                        .end();
            } else {
                builder.textContent(segment.getBreadcrumbKey());
            }
            builder.end().span().css(arrow).innerHtml(SafeHtmlUtils.fromSafeConstant("&#8658;")).end();

            builder.span();
            if (segment.supportsDropdown()) {
                builder.css(value, dropdown);
                String id = IdBuilder.build(segment.getKey(), VALUE);
                builder.a().id(id)
                        .css(clickable)
                        .data(UIConstants.TARGET, "#")
                        .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                        .aria(UIConstants.HAS_POPUP, String.valueOf(true))
                        .aria(UIConstants.EXPANDED, String.valueOf(false))
                        .attr(UIConstants.ROLE, UIConstants.BUTTON)
                        .on(click, event -> {
                            Element ul = ((Element) event.getCurrentTarget()).getNextElementSibling();
                            segment.dropdown(context, items -> {
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
                                                .on(click, e -> dropdownItem.onSelect(context))
                                                .textContent(dropdownItem.getTitle()).end().end().build();
                                        ul.appendChild(element);
                                    }
                                }
                            });
                        });

                String breadcrumbValue = segment.getBreadcrumbValue();
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
                String breadcrumbValue = segment.getBreadcrumbValue();
                if (breadcrumbValue.length() > MAX_BREADCRUMB_VALUE_LENGTH) {
                    builder.textContent(abbreviateMiddle(breadcrumbValue, MAX_BREADCRUMB_VALUE_LENGTH))
                            .title(breadcrumbValue);
                } else {
                    builder.textContent(segment.getBreadcrumbValue());
                }
            }
            builder.end(); // </span>
            builder.end(); // </li>
            breadcrumbs.appendChild(builder.build());
        }
    }

    @Override
    public void updateBreadcrumb(final ModelBrowserPath path) {
        clearBreadcrumb();
        if (path == null) {
            // deselection
            breadcrumbs.appendChild(
                    new Elements.Builder().li().textContent(resources().constants().nothingSelected()).build());

        } else {
            if (path.isEmpty()) {
                breadcrumbs.appendChild(new Elements.Builder().li().textContent("").build());

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
                    breadcrumbs.appendChild(builder.build());
                }
            }
        }
    }


    // ------------------------------------------------------ event handler

    @EventHandler(element = "logoLink", on = click)
    void onLogo() {
        presenter.goTo(NameTokens.HOMEPAGE);
    }

    @EventHandler(element = "messages", on = click)
    void onMessages() {
    }

    @EventHandler(element = "logout", on = click)
    void onLogout() {
        Browser.getWindow().alert(NYI);
    }

    @EventHandler(element = "reconnect", on = click)
    void onReconnect() {
        Browser.getWindow().alert(NYI);
    }
}
