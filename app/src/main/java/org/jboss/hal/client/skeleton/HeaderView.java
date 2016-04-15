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
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.Window;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.resources.IdBuilder;
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
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.skeleton.HeaderPresenter.MESSAGE_TIMEOUT;
import static org.jboss.hal.config.InstanceInfo.WILDFLY;
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
    public static HeaderView create(final TokenFormatter tokenFormatter, final Resources resources, final User user) {
        return new Templated_HeaderView(tokenFormatter, resources, user);
    }

    public abstract TokenFormatter tokenFormatter();
    public abstract Resources resources();
    public abstract User user();
    // @formatter:on


    private static final Logger logger = LoggerFactory.getLogger(HeaderView.class);

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

        tlc = new HashMap<>();
        tlc.put(NameTokens.HOMEPAGE, root.querySelector("#" + Ids.TLC_HOMEPAGE));
        tlc.put(NameTokens.DEPLOYMENTS, root.querySelector("#" + Ids.TLC_DEPLOYMENTS));
        tlc.put(NameTokens.CONFIGURATION, root.querySelector("#" + Ids.TLC_CONFIGURATION));
        tlc.put(NameTokens.RUNTIME, root.querySelector("#" + Ids.TLC_RUNTIME));
        tlc.put(NameTokens.ACCESS_CONTROL, root.querySelector("#" + Ids.TLC_ACCESS_CONTROL));
        tlc.put(NameTokens.PATCHING, root.querySelector("#" + Ids.TLC_PATCHING));

        boolean su = user().isSuperuser() || user().isAdministrator();
        Elements.setVisible(accessControl, su);
        Elements.setVisible(patching, su);
        Elements.setVisible(breadcrumbs, false);
    }

    String historyToken(String token) {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token).build();
        return "#" + tokenFormatter().toHistoryToken(singletonList(placeRequest));
    }

    String historyToken(String token, FinderPath path) {
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(token);
        if (!path.isEmpty()) {
            builder.with("path", path.toString());
        }
        PlaceRequest placeRequest = builder.build();
        return "#" + tokenFormatter().toHistoryToken(singletonList(placeRequest));
    }

    @Override
    public void setPresenter(final HeaderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(Environment environment, Endpoints endpoints, User user) {
        if (environment.getInstanceInfo() == WILDFLY) {
            setLogo(new String[]{
                    environment.getInstanceInfo().description().substring(0, 4),
                    environment.getInstanceInfo().description().substring(4),
            });
        } else if (environment.getInstanceInfo() == InstanceInfo.EAP) {
            setLogo(new String[]{
                    environment.getInstanceInfo().description().substring(0, 13),
                    environment.getInstanceInfo().description().substring(13).trim(),
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

    @Override
    public void showMessage(final Message message) {
        // TODO Prevent showing two messages at once -> queue multiple messages
        switch (message.getLevel()) {
            case ERROR:
                logger.error(message.getMessage());
                break;
            case WARNING:
                logger.warn(message.getMessage());
                break;
            case INFO:
                logger.info(message.getMessage());
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

    @Override
    public void updateBack(final FinderContext finderContext) {
        String token = finderContext.getToken();
        if (token != null) {
            String historyToken = historyToken(token, finderContext.getPath());
            backLink.setAttribute("href", historyToken); //NON-NLS
            Element link = tlc.get(token);
            if (link != null) {
                link.setAttribute("href", historyToken); //NON-NLS
            }
        }
    }

    private void clearBreadcrumb() {
        while (breadcrumbs.getLastChild() != null && breadcrumbs.getChildren().getLength() > 1) {
            breadcrumbs.removeChild(breadcrumbs.getLastChild());
        }
    }

    @Override
    public void updateBreadcrumb(final FinderContext context) {
        clearBreadcrumb();
        FinderPath currentPath = new FinderPath();

        for (Iterator<FinderSegment> iterator = context.getPath().iterator(); iterator.hasNext(); ) {
            //noinspection unchecked
            FinderSegment<Object> segment = iterator.next();
            boolean last = !iterator.hasNext();
            currentPath.append(segment.getKey(), segment.getValue());

            Elements.Builder builder = new Elements.Builder().li();
            if (last) {
                builder.css(active);
            }
            builder.span().css(key);
            if (context.getToken() != null) {
                builder.a(historyToken(context.getToken(), currentPath))
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
                        })
                        .span().textContent(segment.getBreadcrumbValue() + " ").end()
                        .span().css(caret).end()
                        .end();
                builder.ul()
                        .css(dropdownMenu, valueDropdown)
                        .aria(UIConstants.LABELLED_BY, id)
                        .end();
            } else {
                builder.css(value);
                builder.textContent(segment.getBreadcrumbValue());
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
